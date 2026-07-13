package com.och.ivr.service.impl;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import com.och.ivr.application.node.IvrNodeHandler;
import com.och.ivr.application.node.IvrNodeHandlerFactory;
import com.och.ivr.application.node.IvrNodeHandlerResult;
import com.och.ivr.domain.*;
import com.och.ivr.mapper.IvrEdgeMapper;
import com.och.ivr.mapper.IvrNodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IVR运行时引擎
 * <p>
 * 负责 IVR 流程的实时路由决策：监听 DTMF 事件，通过节点处理器工厂获取对应处理器，
 * 执行路由逻辑，自动加载下一节点和出边并更新流程上下文。
 * </p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>工厂模式</b>：IvrNodeHandlerFactory 根据节点类型返回对应处理器</li>
 *   <li><b>策略模式</b>：每种节点类型封装独立路由策略</li>
 *   <li><b>观察者模式</b>：@EventListener 监听 ESL DTMF 事件</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IvrRuntimeEngineImpl {

    private final ConcurrentHashMap<String, IvrFlowContext> activeFlows = new ConcurrentHashMap<>();
    private final IvrNodeHandlerFactory handlerFactory;
    private final IvrNodeMapper ivrNodeMapper;
    private final IvrEdgeMapper ivrEdgeMapper;
    private final IvrExecutionServiceImpl executionService;

    /**
     * IVR 流程运行时上下文
     */
    public record IvrFlowContext(Long flowId, Long versionId, IvrNode currentNode,
                                 List<IvrEdge> edges, Map<String, Object> variables) {}

    /**
     * 注册呼叫进入IVR流程，同时创建执行追踪记录
     */
    public void registerFlow(String callId, Long flowId, Long versionId, IvrNode currentNode,
                             List<IvrEdge> edges, Map<String, Object> variables) {
        activeFlows.put(callId, new IvrFlowContext(flowId, versionId, currentNode, edges, variables));
        executionService.start(callId, flowId, versionId);
        log.info("[IVR引擎] 注册流程: callId={}, flowId={}, versionId={}, startNode={}",
                callId, flowId, versionId, currentNode.getNodeId());
    }

    /**
     * 注销呼叫的IVR流程，同时更新执行追踪记录
     */
    public void unregisterFlow(String callId, String exitType) {
        IvrFlowContext removed = activeFlows.remove(callId);
        if (removed != null) {
            executionService.complete(callId, exitType);
            log.info("[IVR引擎] 注销流程: callId={}, flowId={}, exitType={}", callId, removed.flowId(), exitType);
        }
    }

    /** 注销流程（默认退出类型为COMPLETED） */
    public void unregisterFlow(String callId) {
        unregisterFlow(callId, "COMPLETED");
    }

    /**
     * 监听 ESL DTMF 按键事件，触发 IVR 路由决策
     */
    @EventListener
    public void onDtmfEvent(NormalizedEslEvent eslEvent) {
        if (EslEventType.DTMF != eslEvent.getEventType()) return;
        String callId = eslEvent.getCallId();
        if (callId == null) return;

        IvrFlowContext ctx = activeFlows.get(callId);
        if (ctx == null) {
            log.debug("[IVR引擎] DTMF 无对应流程: callId={}, digit={}", callId, eslEvent.getDtmfDigit());
            return;
        }

        String dtmfDigit = eslEvent.getDtmfDigit();
        log.info("[IVR引擎] DTMF: callId={}, digit={}, node={}", callId, dtmfDigit, ctx.currentNode().getNodeId());

        IvrNodeHandlerResult result = executeNode(ctx.currentNode(), dtmfDigit, ctx.edges(), ctx.variables());
        handleExecutionResult(callId, ctx, result);
    }

    /**
     * 执行当前节点路由决策
     */
    public IvrNodeHandlerResult executeNode(IvrNode currentNode, String dtmfInput,
                                            List<IvrEdge> edges, Map<String, Object> variables) {
        if (currentNode == null) {
            return IvrNodeHandlerResult.end(variables);
        }

        IvrNodeType nodeType;
        try {
            nodeType = IvrNodeType.valueOf(currentNode.getNodeType());
        } catch (IllegalArgumentException e) {
            log.warn("[IVR引擎] 未知节点类型: nodeType={}, nodeId={}", currentNode.getNodeType(), currentNode.getNodeId());
            String next = (edges != null && !edges.isEmpty()) ? edges.get(0).getTargetNodeId() : null;
            return next != null ? IvrNodeHandlerResult.continueTo(next, variables) : IvrNodeHandlerResult.end(variables);
        }

        IvrNodeHandler handler = handlerFactory.getHandler(nodeType);
        return handler.handle(currentNode, dtmfInput, edges, variables);
    }

    /**
     * 处理节点执行结果，加载下一节点并更新上下文或终止流程
     */
    private void handleExecutionResult(String callId, IvrFlowContext ctx, IvrNodeHandlerResult result) {
        switch (result.getAction()) {
            case "CONTINUE" -> {
                String nextNodeId = result.getNextNodeId();
                log.info("[IVR引擎] 继续: callId={}, currentNode={}, nextNodeId={}",
                        callId, ctx.currentNode().getNodeId(), nextNodeId);

                // 从数据库加载下一节点及其出边，更新上下文
                IvrNode nextNode = ivrNodeMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IvrNode>()
                                .eq(IvrNode::getVersionId, ctx.versionId())
                                .eq(IvrNode::getNodeId, nextNodeId));
                if (nextNode == null) {
                    log.warn("[IVR引擎] 下一节点不存在: callId={}, nextNodeId={}", callId, nextNodeId);
                    unregisterFlow(callId);
                    return;
                }

                List<IvrEdge> nextEdges = ivrEdgeMapper.findByVersionAndSourceNode(ctx.versionId(), nextNodeId);
                activeFlows.put(callId, new IvrFlowContext(ctx.flowId(), ctx.versionId(), nextNode, nextEdges, result.getVariables()));
                log.debug("[IVR引擎] 上下文已更新: callId={}, nextNodeType={}", callId, nextNode.getNodeType());
            }
            case "ENQUEUE" -> {
                log.info("[IVR引擎] 转排队: callId={}", callId);
                unregisterFlow(callId, "TRANSFERRED");
            }
            case "TRANSFER" -> {
                log.info("[IVR引擎] 转接: callId={}, target={}", callId, result.getVariables().get("transferTarget"));
                unregisterFlow(callId, "TRANSFERRED");
            }
            case "VOICEMAIL" -> {
                log.info("[IVR引擎] 转语音信箱: callId={}", callId);
                unregisterFlow(callId, "VOICEMAIL");
            }
            case "END", "HANGUP" -> {
                unregisterFlow(callId, result.getAction());
                log.info("[IVR引擎] 流程终结: callId={}, action={}", callId, result.getAction());
            }
        }
    }
}