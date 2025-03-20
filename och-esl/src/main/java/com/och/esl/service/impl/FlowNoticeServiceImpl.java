package com.och.esl.service.impl;

import com.och.common.constant.FlowDataContext;
import com.och.esl.event.FlowEvent;
import com.och.esl.service.IFlowNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class FlowNoticeServiceImpl implements IFlowNoticeService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void notice(String address, Long callId, String uniqueId, Long flowId) {
        log.info("flowNotice address:{},callId:{}, flowId:{}", address, callId, flowId);
        FlowDataContext data = new FlowDataContext();
        data.setAddress(address);
        data.setCallId(callId);
        data.setFlowId(flowId);
        data.setUniqueId(uniqueId);
        applicationEventPublisher.publishEvent(new FlowEvent(1,  data));
    }

    @Override
    public void notice(Integer type, String event, FlowDataContext data) {
        applicationEventPublisher.publishEvent(new FlowEvent(type, event, data));
    }
}
