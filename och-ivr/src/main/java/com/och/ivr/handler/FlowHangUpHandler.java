package com.och.ivr.handler;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 挂机节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowHangUpHandler extends AbstractIFlowNodeHandler{


    public FlowHangUpHandler(RedisStateMachinePersister<Object, Object> persister, RedisService redisService) {
        super(persister, redisService);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
