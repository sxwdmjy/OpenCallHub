package com.och.esl.handler.route;

import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.EslRouteName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
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
    public void handler(String address, CallInfo callInfo, String uniqueId, String flowId) {
        log.info("转ivr callId:{} transfer to {}", callInfo.getCallId(), flowId);

        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(2);
        detail.setTransferId(Long.parseLong(flowId));
        iFlowNoticeService.notice(address, callInfo.getCallId(),uniqueId, Long.parseLong(flowId));
        callInfo.addDetailList(detail);
        fsCallCacheService.saveCallInfo(callInfo);
    }
}
