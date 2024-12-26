package com.och.ivr.handler;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.utils.StringUtils;
import com.och.common.constant.FlowDataContext;
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
    protected final RedisService redisService;

    @Override
    public void handle(StateContext<Object, Object> stateContext) {
        FlowDataContext flowData = stateContext.getExtendedState().get("flowData", FlowDataContext.class);
        execute(flowData);
        StateMachine<Object, Object> stateMachine = stateContext.getStateMachine();
        if(stateMachine.isComplete()){
            redisService.deleteObject(StringUtils.format(CacheConstants.CALL_IVR_INSTANCES_KEY, flowData.getInstanceId()));
        }else {
            try {
                persister.persist(stateMachine, StringUtils.format(CacheConstants.CALL_IVR_INSTANCES_KEY, flowData.getInstanceId()));
            } catch (Exception e) {
                log.error("持久化状态机异常:event:{},error:{}", stateContext.getEvent(), e.getMessage(), e);
            }
        }
    }

    public abstract void execute(FlowDataContext flowData);
}
