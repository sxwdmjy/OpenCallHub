package com.och.ivr.handler;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.FlowDataContext;
import com.och.esl.service.IFlowNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * 结束节点处理
 *
 * @author danmo
 * @date 2024-12-26
 */
@Slf4j
@Component
public class IFlowEndHandler extends AbstractIFlowNodeHandler {


    public IFlowEndHandler(RedisStateMachinePersister<Object, Object> persister, IFlowNoticeService iFlowNoticeService) {
        super(persister, iFlowNoticeService);
    }

    @Override
    public void execute(FlowDataContext flowData) {

    }
}
