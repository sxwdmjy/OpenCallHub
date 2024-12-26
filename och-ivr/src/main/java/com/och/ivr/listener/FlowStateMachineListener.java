package com.och.ivr.listener;

import com.alibaba.fastjson.JSON;
import com.och.common.enums.FlowNodeTypeEnum;
import com.och.common.utils.SpringUtils;
import com.och.common.utils.StringUtils;
import com.och.common.constant.FlowDataContext;
import com.och.ivr.domain.entity.FlowInstances;
import com.och.ivr.domain.entity.FlowNodeExecutionHistory;
import com.och.ivr.domain.vo.FlowInfoVo;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.handler.AbstractIFlowNodeHandler;
import com.och.ivr.service.*;
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
    private final IFlowInfoService iFlowInfoService;
    private final IFlowInstancesService iFlowInstancesService;
    private final IFlowNodeExecutionHistoryService iFlowNodeExecutionHistoryService;
    private final RedisStateMachinePersister<Object, Object> persister;
    private StateContext<Object, Object> stateContext;

    public FlowStateMachineListener() {
        this.iFlowNodesService = SpringUtils.getBean(IFlowNodesService.class);
        this.iFlowEdgesService = SpringUtils.getBean(IFlowEdgesService.class);
        this.iFlowInfoService = SpringUtils.getBean(IFlowInfoService.class);
        this.iFlowInstancesService = SpringUtils.getBean(IFlowInstancesService.class);
        this.iFlowNodeExecutionHistoryService = SpringUtils.getBean(IFlowNodeExecutionHistoryService.class);
        this.persister = SpringUtils.getBean("redisStateMachinePersister");

    }


    @Override
    public void stateEntered(State<Object, Object> state) {
        FlowDataContext flowData = stateContext.getExtendedState().get("flowData", FlowDataContext.class);
        log.info("状态机状态进入:{},{}", state.getId(), flowData);
        if (flowData == null) {
            return;
        }
        FlowInfoVo info = iFlowInfoService.getInfo(flowData.getFlowId());
        List<FlowNodeVo> nodes = info.getNodes();
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
        String handler = FlowNodeTypeEnum.getHandler(currentFlowNode.getType());
        if(StringUtils.isNotBlank(handler)){
            AbstractIFlowNodeHandler nodeHandler = SpringUtils.getBean(handler, AbstractIFlowNodeHandler.class);
            nodeHandler.handle(stateContext);
        }
    }

    @Override
    public void stateExited(State<Object, Object> state) {
        FlowDataContext flowData = stateContext.getExtendedState().get("flowData", FlowDataContext.class);
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
