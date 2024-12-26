package com.och.ivr.handler;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 开始节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowStartHandler extends AbstractIFlowNodeHandler{

    public FlowStartHandler(RedisStateMachinePersister<Object, Object> persister, RedisService redisService) {
        super(persister, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
