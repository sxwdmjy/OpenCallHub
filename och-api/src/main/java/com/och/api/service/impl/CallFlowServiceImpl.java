package com.och.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.acd.service.impl.QueueDispatchServiceImpl;
import com.och.acd.domain.AcdAgentStatus;
import com.och.acd.domain.AcdQueueSession;
import com.och.api.service.ICallFlowService;
import com.och.call.domain.CallSession;
import com.och.call.domain.CallState;
import com.och.call.mapper.CallSessionMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.ivr.service.impl.IvrRuntimeEngineImpl;
import com.och.ivr.service.impl.IvrRuntimeEngineImpl.IvrFlowContext;
import com.och.ivr.application.node.IvrNodeHandlerResult;
import com.och.ivr.domain.*;
import com.och.ivr.mapper.IvrEdgeMapper;
import com.och.ivr.mapper.IvrFlowMapper;
import com.och.ivr.mapper.IvrNodeMapper;
import com.och.ivr.service.IIvrFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 呼叫流程编排服务实现
 * <p>
 * 负责呼入全链路编排，串联各模块：
 * <ol>
 *   <li>查找呼入号码关联的已发布 IVR 流程</li>
 *   <li>加载开始节点并注册到 {@link IvrRuntimeEngineImpl}</li>
 *   <li>IVR 路由到 QUEUE 节点时委托给 {@link QueueDispatchService} 入队分配</li>
 *   <li>分配完成后通过 {@link IWebSocketService} 通知座席</li>
 * </ol>
 * </p>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li><b>门面模式</b>：聚合 och-call/och-ivr/och-acd/och-websocket 四大模块</li>
 *   <li><b>模板方法</b>：呼入流程的骨架步骤固定，局部由各策略组件决策</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallFlowServiceImpl implements ICallFlowService {

    private final CallSessionMapper sessionMapper;
    private final IvrFlowMapper ivrFlowMapper;
    private final IvrNodeMapper ivrNodeMapper;
    private final IvrEdgeMapper ivrEdgeMapper;
    private final IvrRuntimeEngineImpl ivrRuntimeEngine;
    private final IIvrFlowService ivrFlowService;
    private final QueueDispatchServiceImpl dispatchService;

    @Override
    @Transactional
    public CallSession startInboundFlow(String callId, String calledNumber, String callerNumber) {
        CallSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<CallSession>().eq(CallSession::getCallId, callId));
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "呼叫会话不存在: " + callId);
        }

        // 1. 查找关联的已发布 IVR 流程
        IvrFlow ivrFlow = findPublishedIvrFlow(calledNumber);
        if (ivrFlow == null) {
            log.info("[编排] 无匹配IVR流程，直接接听: callId={}, calledNumber={}", callId, calledNumber);
            session.setState(CallState.BRIDGED.name());
            session.setAnswerTime(LocalDateTime.now());
            sessionMapper.updateById(session);
            return session;
        }

        Long versionId = ivrFlow.getPublishedVersionId();

        // 2. 加载开始节点并注册到IVR引擎
        IvrNode startNode = findStartNode(versionId);
        if (startNode == null) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                    "IVR流程无开始节点: flowId=" + ivrFlow.getId());
        }

        List<IvrEdge> startEdges = ivrEdgeMapper.findByVersionAndSourceNode(versionId, startNode.getNodeId());
        Map<String, Object> variables = new ConcurrentHashMap<>();
        variables.put("callerNumber", callerNumber);
        variables.put("calledNumber", calledNumber);

        ivrRuntimeEngine.registerFlow(callId, ivrFlow.getId(), versionId, startNode, startEdges, variables);

        // 3. 更新会话状态
        session.setState(CallState.IVR.name());
        session.setIvrFlowId(ivrFlow.getId());
        sessionMapper.updateById(session);

        log.info("[编排] 呼入流程启动: callId={}, flowName={}, versionId={}",
                callId, ivrFlow.getFlowName(), versionId);

        // 4. 同步执行开始节点路由
        IvrNodeHandlerResult result = ivrRuntimeEngine.executeNode(startNode, null, startEdges, variables);
        processIvrResult(callId, session, result);

        return session;
    }

    /**
     * 处理 DTMF 按键事件
     * <p>由 IvrRuntimeEngineImpl 的 @EventListener 自动处理，
     * 此处可在引擎处理完后做补充逻辑（如排队通知）。</p>
     */
    @Override
    public void handleDtmf(String callId, String digit) {
        // DTMF 事件由 IvrRuntimeEngineImpl.onDtmfEvent 自动捕获处理
        // 此方法供外部（如测试或手动触发）调用
        log.info("[编排] 手动触发 DTMF: callId={}, digit={}", callId, digit);
    }

    @Override
    public FlowStatus getFlowStatus(String callId) {
        CallSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<CallSession>().eq(CallSession::getCallId, callId));
        if (session == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "呼叫会话不存在: " + callId);
        }

        IvrFlow ivrFlow = session.getIvrFlowId() != null ? ivrFlowService.getById(session.getIvrFlowId()) : null;

        return new FlowStatus(
                callId,
                session.getState(),
                ivrFlow != null ? ivrFlow.getFlowName() : "无IVR流程",
                parseQueueState(session),
                session.getAgentId());
    }

    // ==================== 私有方法 ====================

    /** 处理 IVR 执行结果 */
    private void processIvrResult(String callId, CallSession session, IvrNodeHandlerResult result) {
        switch (result.getAction()) {
            case "ENQUEUE" -> {
                Long skillGroupId = resolveSkillGroupId(result);
                AcdQueueSession queueSession = dispatchService.enqueue(
                        skillGroupId, callId, session.getId(), session.getCallerNumber());
                session.setState(CallState.QUEUEING.name());
                session.setQueueId(queueSession.getSkillGroupId());
                sessionMapper.updateById(session);
                log.info("[编排] 入队完成: callId={}, skillGroupId={}, position={}",
                        callId, skillGroupId, queueSession.getPosition());
            }
            case "TRANSFER" -> {
                String target = (String) result.getVariables().get("transferTarget");
                session.setState(CallState.BRIDGED.name());
                sessionMapper.updateById(session);
                log.info("[编排] 转接: callId={}, target={}", callId, target);
            }
            case "HANGUP" -> {
                session.setState(CallState.ENDED.name());
                session.setEndTime(LocalDateTime.now());
                sessionMapper.updateById(session);
                log.info("[编排] 挂断: callId={}", callId);
            }
            case "VOICEMAIL" -> {
                session.setState(CallState.VOICEMAIL.name());
                sessionMapper.updateById(session);
                log.info("[编排] 语音信箱: callId={}", callId);
            }
            default -> log.debug("[编排] IVR继续: callId={}, action={}", callId, result.getAction());
        }
    }

    private Long resolveSkillGroupId(IvrNodeHandlerResult result) {
        Object value = result.getVariables() == null
                ? null
                : result.getVariables().get(IvrNodeHandlerResult.VARIABLE_SKILL_GROUP_ID);
        Long skillGroupId = null;
        if (value instanceof Number) {
            skillGroupId = ((Number) value).longValue();
        } else if (value instanceof String) {
            String text = (String) value;
            if (!text.isBlank()) {
                skillGroupId = Long.valueOf(text);
            }
        }
        if (skillGroupId == null) {
            Object queueNodeId = result.getVariables() == null
                    ? null
                    : result.getVariables().get(IvrNodeHandlerResult.VARIABLE_QUEUE_NODE_ID);
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                    "IVR排队节点缺少skillGroupId配置: nodeId=" + queueNodeId);
        }
        return skillGroupId;
    }

    /** 按被叫号码匹配已发布的 IVR 流程 */
    private IvrFlow findPublishedIvrFlow(String calledNumber) {
        List<IvrFlow> flows = ivrFlowMapper.selectList(
                new LambdaQueryWrapper<IvrFlow>()
                        .eq(IvrFlow::getEnabled, true)
                        .isNotNull(IvrFlow::getPublishedVersionId));
        // 按 flowCode 匹配（简化：直接匹配号码）
        for (IvrFlow flow : flows) {
            if (calledNumber.equals(flow.getFlowCode()) || calledNumber.contains(flow.getFlowCode())) {
                return flow;
            }
        }
        return null;
    }

    /** 查找开始节点 */
    private IvrNode findStartNode(Long versionId) {
        return ivrNodeMapper.selectOne(
                new LambdaQueryWrapper<IvrNode>()
                        .eq(IvrNode::getVersionId, versionId)
                        .eq(IvrNode::getNodeType, IvrNodeType.START.name()));
    }

    /** 解析队列状态 */
    private String parseQueueState(CallSession session) {
        if (session.getState() == null) return "未排队";
        return switch (session.getState()) {
            case "QUEUEING" -> "排队中";
            case "ASSIGNED" -> "已分配坐席";
            case "BRIDGED", "HOLD" -> "通话中";
            default -> session.getState();
        };
    }
}