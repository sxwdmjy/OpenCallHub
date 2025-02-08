package com.och.ivr.handler.node;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.FlowDataContext;
import com.och.common.exception.FlowNodeException;
import com.och.common.utils.StringUtils;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.service.IFsCallCacheService;
import com.och.ivr.domain.vo.FlowNodeVo;
import com.och.ivr.service.IFlowInfoService;
import com.och.ivr.service.IFlowInstancesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;

/**
 * 抽象节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractIFlowNodeHandler implements IFlowNodeHandler {

    protected final RedisStateMachinePersister<Object, Object> persister;
    protected final IFsCallCacheService fsCallCacheService;
    protected final IFlowNoticeService iFlowNoticeService;
    protected final IFlowInfoService iFlowInfoService;
    protected final IFlowInstancesService iFlowInstancesService;
    protected final FsClient fsClient;
    protected final RedisService redisService;


    @Override
    public void handle(StateContext<Object, Object> stateContext) {
        StateMachine<Object, Object> stateMachine = stateContext.getStateMachine();
        FlowDataContext flowData = stateContext.getExtendedState().get("flowData", FlowDataContext.class);
        try {
            execute(flowData);
        } catch (FlowNodeException e) {
            if (!stateMachine.isComplete()) {
                flowData.setHangUpCause(e.getMessage());
                iFlowNoticeService.notice(2, "end", flowData);
            }
        }
        try {
            persister.persist(stateMachine, StringUtils.format(CacheConstants.CALL_IVR_INSTANCES_KEY, flowData.getInstanceId()));
        } catch (Exception e) {
            log.error("持久化状态机异常:event:{},error:{}", stateContext.getEvent(), e.getMessage(), e);
        }
        if (stateMachine.isComplete()) {
            iFlowNoticeService.notice(3, "", flowData);
        }

    }

    public abstract void execute(FlowDataContext flowData) throws FlowNodeException;


    protected FlowNodeVo getFlowNode(Long flowId, String nodeId) {
        return redisService.getCacheMapValue(StringUtils.format(CacheConstants.CALL_IVR_FLOW_INFO_NODE_KEY, flowId), nodeId);
    }
}
