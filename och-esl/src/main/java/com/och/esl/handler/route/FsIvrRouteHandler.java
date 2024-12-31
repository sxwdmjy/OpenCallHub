package com.och.esl.handler.route;

import com.och.common.annotation.EslRouteName;
import com.och.common.domain.CallInfo;
import com.och.common.enums.RouteTypeEnum;
import com.och.esl.service.IFlowNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final IFlowNoticeService iFlowNoticeService;

    @Override
    public void handler(String address, CallInfo callInfo, String uniqueId, String routeValue) {
        log.info("转ivr callId:{} transfer to {}", callInfo.getCallId(), routeValue);
        iFlowNoticeService.notice(address, callInfo.getCallId(), Long.parseLong(routeValue));
    }
}
