package com.och.esl.handler.call;

import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

/**
 * 桥接
 *
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALL_BRIDGE)
@Component
@Slf4j
public class FsCallBridgeProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        log.info("开始桥接电话: callId:{} caller:{} called:{} uniqueId:{}, otherUniqueId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());
        ChannelInfo channel = callInfo.getChannelMap().get(lfsChannelInfo.getOtherUniqueId());
        ChannelInfo otherChannel = callInfo.getChannelMap().get(lfsChannelInfo.getUniqueId());
        if (channel.getBridgeTime() == null) {
            channel.setBridgeTime(event.getEventDateTimestamp() / 1000);
        }
        if (otherChannel.getBridgeTime() == null) {
            otherChannel.setBridgeTime(event.getEventDateTimestamp() / 1000);
        }

        //fsClient.record(address, callInfo.getCallId(), lfsChannelInfo.getUniqueId(), callInfo.getRecord());

        callInfo.setChannelInfoMap(lfsChannelInfo.getOtherUniqueId(),channel);
        callInfo.setChannelInfoMap(lfsChannelInfo.getUniqueId(),otherChannel);
        lfsCallCacheService.saveCallInfo(callInfo);
        fsClient.bridgeCall(address, callInfo.getCallId(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());
    }




}
