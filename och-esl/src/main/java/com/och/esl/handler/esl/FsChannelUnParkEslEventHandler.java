
package com.och.esl.handler.esl;

import com.alibaba.fastjson.JSONObject;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslConstant;
import com.och.common.constant.EslEventNames;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.utils.EslEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 结束振铃
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.CHANNEL_UNPARK)
@Component
public class FsChannelUnParkEslEventHandler extends AbstractFsEslEventHandler {

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("ChannelUnParkEslEventHandler EslEvent:{}.", JSONObject.toJSONString(event));
        if (EslConstant.OK.equals(EslEventUtil.getSipHangupPhrase(event))) {
            return;
        }
        String uniqueId = EslEventUtil.getUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);
        if (Objects.isNull(callInfo)) {
            return;
        }
        ChannelInfo channelInfo = callInfo.getChannelMap().get(uniqueId);
        if (Objects.isNull(channelInfo)) {
            return;
        }
        channelInfo.setRingEndTime(event.getEventDateTimestamp()/1000);
        if(Objects.equals(1,channelInfo.getDirectionType())){
            callInfo.setCallerRingEndTime(channelInfo.getRingEndTime());
        }
        if(Objects.equals(2,channelInfo.getDirectionType())){
            callInfo.setCalleeRingEndTime(channelInfo.getRingEndTime());
        }
        callInfo.setChannelInfoMap(uniqueId,channelInfo);
        ifsCallCacheService.saveCallInfo(callInfo);
    }
}
