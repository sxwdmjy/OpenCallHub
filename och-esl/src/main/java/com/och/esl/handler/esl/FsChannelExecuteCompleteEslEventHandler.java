
package com.och.esl.handler.esl;


import cn.hutool.core.collection.CollectionUtil;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslEventNames;
import com.och.common.constant.FlowDataContext;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.enums.RouteTypeEnum;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.service.IFlowNoticeService;
import com.och.esl.utils.EslEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;
import com.och.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;

/**
 * 渠道对呼叫完成执行某些操作处理类
 *
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.CHANNEL_EXECUTE_COMPLETE)
@Component
public class FsChannelExecuteCompleteEslEventHandler extends AbstractFsEslEventHandler {

    private final IFlowNoticeService iFlowNoticeService;

    public FsChannelExecuteCompleteEslEventHandler(IFlowNoticeService iFlowNoticeService) {
        this.iFlowNoticeService = iFlowNoticeService;
    }

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("ChannelExecuteCompleteEslEventHandler EslEvent:{}.", JSONObject.toJSONString(event));
        if (StringUtils.isBlank(EslEventUtil.getApplication(event))) {
            return;
        }
        String uniqueId = EslEventUtil.getUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);
        if (callInfo == null) {
            return;
        }
        switch (EslEventUtil.getApplication(event)) {
            case "playback":
                if ("FILE PLAYED".equals(EslEventUtil.getApplicationResponse(event))) {
                    log.info("uniqueId:{}, playback:{} success", uniqueId, EslEventUtil.getApplicationData(event));
                    if (CollectionUtil.isNotEmpty(callInfo.getDetailList())){
                        CallInfoDetail callInfoDetail = callInfo.getDetailList().get(0);
                        if (callInfoDetail.getTransferType() == 2) {
                            iFlowNoticeService.notice(2,"next", callInfo.getFlowDataContext());
                        }
                    }else {

                    }
/*                    if (callInfo.getQueueStartTime() != null && callInfo.getQueueEndTime() == null) {
                        fsClient.playFile(address, uniqueId, "queue.wav");
                        return;
                    }*/

                } else if ("FILE NOT FOUND".equals(EslEventUtil.getApplicationResponse(event))) {
                    log.error("uniqueId:{}  file:{} not found", uniqueId, EslEventUtil.getApplicationData(event));
                    fsClient.hangupCall(address, callInfo.getCallId(), uniqueId);
                    return;
                }
                break;

            case "play_and_get_digits":
                String menuDtmfReturn = EslEventUtil.getMenuDtmfReturn(event);
                iFlowNoticeService.notice(4,menuDtmfReturn, callInfo.getFlowDataContext());
                break;
            case "break":
                break;

            default:
                break;
        }
        log.debug("execute:{}, data:{}, response:{}", EslEventUtil.getApplication(event), EslEventUtil.getApplicationData(event), EslEventUtil.getApplicationResponse(event));
    }
}
