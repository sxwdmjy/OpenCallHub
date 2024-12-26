package com.och.esl.handler.route;

import com.och.common.annotation.EslRouteName;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.enums.RouteTypeEnum;
import com.och.esl.event.FlowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2023-11-10 17:20
 **/
@RequiredArgsConstructor
@EslRouteName(RouteTypeEnum.IVR)
@Component
@Slf4j
public class FsIvrRouteHandler extends FsAbstractRouteHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handler(String address, CallInfo callInfo, String uniqueId, String routeValue) {
        log.info("转ivr callId:{} transfer to {}", callInfo.getCallId(), routeValue);
        FlowDataContext data = new FlowDataContext();
        data.setAddress(address);
        data.setCallId(callInfo.getCallId());
        data.setUniqueId(uniqueId);
        applicationEventPublisher.publishEvent(new FlowEvent(Long.parseLong(routeValue), 1, null, data));
    }
}
