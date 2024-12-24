package com.och.ivr.handler;

import com.alibaba.fastjson.JSON;
import com.och.ivr.domain.entity.FlowInstances;
import com.och.ivr.domain.vo.FlowEdgeVo;
import com.och.ivr.domain.vo.FlowInfoVo;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.event.FlowEvent;
import com.och.ivr.listener.FlowStateMachineListener;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class FlowEventHandler implements ApplicationListener<FlowEvent> {

    private final RedisStateMachinePersister<Object, Object> persister;
    private final IFlowInfoService iFlowInfoService;
    private final IFlowInstancesService iFlowInstancesService;

    @Override
    public void onApplicationEvent(FlowEvent event) {
        Integer eventType = event.getType();
        switch (eventType) {
            case 1:
                // 1-创建
                startStateMachine(event);
                break;
            case 2:
                // 2-流转
                transferStateMachine(event);
                break;
            case 3:
                // 3-结束
                endStateMachine(event);
                break;
            default:
                break;
        }
    }


    /**
     * 结束
     *
     * @param event
     */
    private void endStateMachine(FlowEvent event) {

    }

    /**
     * 转发
     *
     * @param event
     */
    private void transferStateMachine(FlowEvent event) {
        FlowInfoVo info = iFlowInfoService.getInfo(event.getFlowId());
        if (Objects.isNull(info)) {
            log.info("未找到流程信息 event:{}", event);
            return;
        }
        StateMachine<Object, Object> stateMachine = buildStateMachine(info,false);
        try {
            StateMachine<Object, Object> restore = persister.restore(stateMachine, String.valueOf(event.getInstanceId()));
            Message<String> message = MessageBuilder.withPayload("play").build();
            restore.sendEvent(Mono.just(message).subscribe());
        } catch (Exception e) {
            log.error("恢复状态机异常:event:{},error:{}", event, e.getMessage(), e);
        }
    }

    /**
     * 开始
     *
     * @param event
     */
    private void startStateMachine(FlowEvent event) {
        FlowInfoVo info = iFlowInfoService.getInfo(event.getFlowId());
        if (Objects.isNull(info)) {
            log.info("未找到流程信息 event:{}", event);
            return;
        }
        StateMachine<Object, Object> stateMachine = buildStateMachine(info,true);
        if (Objects.isNull(stateMachine)) {
            log.info("创建状态机失败 event:{}", event);
            return;
        }
        //创建流程实例
        FlowInstances instance = FlowInstances.builder()
                .flowId(event.getFlowId())
                .status(1)
                .startTime(new Date())
                .currentNodeId(info.getNodes().stream().filter(n -> Objects.equals("0", n.getType())).findFirst().orElseGet(null).getId())
                .build();
        iFlowInstancesService.save(instance);
        try {
            persister.persist(stateMachine, String.valueOf(instance.getId()));
        } catch (Exception e) {
            log.error("持久化状态机异常:event:{},error:{}", event, e.getMessage(), e);
        }
    }


    private StateMachine<Object, Object> buildStateMachine(FlowInfoVo info, boolean isStart) {
        log.info("创建状态机：{}", JSON.toJSONString(info));
        try {
            List<FlowNodeVo> nodes = info.getNodes();
            FlowNodeVo startNode = nodes.stream().filter(n -> Objects.equals("0", n.getType())).findFirst().orElseGet(null);
            if (Objects.isNull(startNode)) {
                log.info("未找到流程开始节点 id:{}", info.getId());
                return null;
            }
            FlowNodeVo endNode = nodes.stream().filter(n -> Objects.equals("1", n.getType())).findFirst().orElseGet(null);
            if (Objects.isNull(endNode)) {
                log.info("未找到流程结束节点 id:{}", info.getId());
                return null;
            }
            //配置状态机节点
            Set<Long> nodeIds = nodes.stream().map(FlowNodeVo::getId).collect(Collectors.toSet());
            StateMachineBuilder.Builder<Object, Object> stateMachineBuilder = StateMachineBuilder.builder();
            stateMachineBuilder.configureConfiguration()
                    .withConfiguration()
                    .autoStartup(isStart)
                    .listener(new FlowStateMachineListener());
            stateMachineBuilder.configureStates().withStates().initial(startNode.getId()).states(Collections.singleton(nodeIds)).end(endNode.getId());

            //配置状态机边
            List<FlowEdgeVo> edges = info.getEdges();
            StateMachineTransitionConfigurer<Object, Object> transitionConfigurer = stateMachineBuilder.configureTransitions();
            for (FlowEdgeVo edge : edges) {
                transitionConfigurer.withExternal().source(edge.getSourceNodeId()).target(edge.getTargetNodeId()).event(edge.getEvent());
            }
            return stateMachineBuilder.build();
        } catch (Exception e) {
            log.error("创建状态机异常 id:{},error:{}", info.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 是否支持异步
     *
     * @return
     */
    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }
}
