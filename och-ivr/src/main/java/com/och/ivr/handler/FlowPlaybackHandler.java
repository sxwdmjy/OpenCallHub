package com.och.ivr.handler;

import com.och.common.constant.FlowDataContext;
import com.och.esl.service.IFlowNoticeService;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 放音节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Component
public class FlowPlaybackHandler extends AbstractIFlowNodeHandler {


    public FlowPlaybackHandler(RedisStateMachinePersister<Object, Object> persister, IFlowNoticeService iFlowNoticeService) {
        super(persister, iFlowNoticeService);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
