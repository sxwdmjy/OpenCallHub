package com.och.ivr.handler;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.esl.service.IFlowNoticeService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 子IVR节点处理
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowSubIvrHandler extends AbstractIFlowNodeHandler{


    public FlowSubIvrHandler(RedisStateMachinePersister<Object, Object> persister, IFlowNoticeService iFlowNoticeService) {
        super(persister, iFlowNoticeService);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
