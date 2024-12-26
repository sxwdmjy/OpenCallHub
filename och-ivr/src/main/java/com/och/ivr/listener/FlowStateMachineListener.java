package com.och.ivr.listener;

import com.alibaba.fastjson.JSON;
import com.och.common.constant.CacheConstants;
import com.och.common.utils.SpringUtils;
import com.och.ivr.contants.FlowData;
import com.och.ivr.domain.entity.FlowInstances;
import com.och.ivr.domain.entity.FlowNodeExecutionHistory;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.service.IFlowEdgesService;
import com.och.ivr.service.IFlowInstancesService;
import com.och.ivr.service.IFlowNodeExecutionHistoryService;
import com.och.ivr.service.IFlowNodesService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Slf4j
public class FlowStateMachineListener extends StateMachineListenerAdapter<Object, Object> {


    private final IFlowNodesService iFlowNodesService;
    private final IFlowEdgesService iFlowEdgesService;
    private final IFlowInstancesService iFlowInstancesService;
    private final IFlowNodeExecutionHistoryService iFlowNodeExecutionHistoryService;
    private final RedisStateMachinePersister<Object, Object> persister;
    private StateContext<Object, Object> stateContext;

    public FlowStateMachineListener() {
        this.iFlowNodesService = SpringUtils.getBean(IFlowNodesService.class);
        this.iFlowEdgesService = SpringUtils.getBean(IFlowEdgesService.class);
        this.iFlowInstancesService = SpringUtils.getBean(IFlowInstancesService.class);
        this.iFlowNodeExecutionHistoryService = SpringUtils.getBean(IFlowNodeExecutionHistoryService.class);
        this.persister = SpringUtils.getBean("redisStateMachinePersister");

    }


    @Override
    public void stateEntered(State<Object, Object> state) {
        FlowData flowData = stateContext.getExtendedState().get("flowData", FlowData.class);
        log.info("状态机状态进入:{},{}", state.getId(), flowData);
        if (flowData == null) {
            return;
        }
        List<FlowNodeVo> nodes = flowData.getFlowInfoVo().getNodes();
        FlowNodeVo currentFlowNode = nodes.stream().filter(n -> Objects.equals(state.getId(), n.getId())).findFirst().orElseGet(null);
        if (Objects.isNull(currentFlowNode)) {
            log.info("未找到当前节点flowData:{}, id:{}", JSON.toJSONString(flowData), state.getId());
            return;
        }
        FlowInstances instances = FlowInstances.builder().currentNodeId((Long) state.getId()).variables(currentFlowNode.getProperties()).id(flowData.getInstanceId()).build();
        iFlowInstancesService.updateById(instances);
        //记录节点执行历史
        FlowNodeExecutionHistory history = new FlowNodeExecutionHistory();
        history.setInstanceId(flowData.getInstanceId());
        history.setNodeId((Long)state.getId());
        history.setStatus(1);
        history.setStartTime(new Date());
        iFlowNodeExecutionHistoryService.save(history);
        flowData.setCurrentHistoryId(history.getId());
        stateContext.getExtendedState().getVariables().put("flowData", flowData);
        //todo 执行节点逻辑
        try {
            persister.persist(stateContext.getStateMachine(), String.format(CacheConstants.CALL_IVR_INSTANCES_KEY, flowData.getInstanceId()));
        } catch (Exception e) {
            log.error("持久化状态机异常:event:{},error:{}", stateContext.getEvent(), e.getMessage(), e);
        }
    }

    @Override
    public void stateExited(State<Object, Object> state) {
        FlowData flowData = stateContext.getExtendedState().get("flowData", FlowData.class);
        log.info("状态机状态退出:{},{}", state.getId(), flowData);
        if (flowData == null) {
            return;
        }
        FlowNodeExecutionHistory history = iFlowNodeExecutionHistoryService.getById(flowData.getCurrentHistoryId());
        history.setStatus(2);
        history.setEndTime(new Date());
        history.setDuration((int) (history.getEndTime().getTime() - history.getStartTime().getTime()) / 1000);
        iFlowNodeExecutionHistoryService.updateById(history);
    }

    @Override
    public void stateMachineError(StateMachine<Object, Object> stateMachine, Exception exception) {
        log.info("状态机异常 id:{}, error:{}", stateMachine.getId(), exception.getMessage());
    }

    @Override
    public void stateContext(StateContext<Object, Object> stateContext) {
        this.stateContext = stateContext;
    }
}
