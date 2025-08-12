
package com.och.esl.handler.esl;

import com.alibaba.fastjson.JSONObject;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.EslEventNames;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.AgentStateEnum;
import com.och.common.enums.FsHangupCauseEnum;
import com.och.common.enums.SipAgentStatusEnum;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.utils.EslEventUtil;
import com.och.system.domain.entity.CallRecord;
import com.och.system.domain.vo.agent.SipAgentStatusVo;
import com.och.system.service.ICallRecordService;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * 渠道挂机处理类
 *
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.CHANNEL_HANGUP_COMPLETE)
@Component
public class FsChannelHangUpCompleteEslEventHandler extends AbstractFsEslEventHandler {

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("ChannelHangUpCompleteEslEventHandler EslEvent:{}.", JSONObject.toJSONString(event));
        String uniqueId = EslEventUtil.getUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);
        if (Objects.isNull(callInfo)) {
            return;
        }
        log.info("挂机 callId:{} uniqueId:{}", callInfo.getCallId(), uniqueId);
        int count = callInfo.getUniqueIdList().size();
        callInfo.removeUniqueIdList(uniqueId);
        ChannelInfo channelInfo = callInfo.getChannelMap().get(uniqueId);
        if (Objects.isNull(channelInfo)) {
            return;
        }
        channelInfo.setHangupCause(EslEventUtil.getHangupCause(event));
        channelInfo.setSipProtocol(EslEventUtil.getSipViaProtocol(event));
        channelInfo.setSipStatus(EslEventUtil.getSipTermStatus(event));
        channelInfo.setChannelName(EslEventUtil.getChannelName(event));
        channelInfo.setEndTime(event.getEventDateTimestamp() / 1000);
        // 设置挂机方向
        if(Objects.isNull(callInfo.getHangupDir())){
            if(Objects.equals(1,channelInfo.getDirectionType())){
                callInfo.setHangupDir(1);
            }
            if(Objects.equals(2,channelInfo.getDirectionType())){
                callInfo.setHangupDir(2);

            }
            String hangupCause = EslEventUtil.getHangupCause(event);
            FsHangupCauseEnum causeEnum = FsHangupCauseEnum.getByValue(hangupCause);
            if(Objects.nonNull(causeEnum)){
                callInfo.setHangupCause(causeEnum.getCode());
            }
        }
        callInfo.getChannelMap().forEach((key, value) -> fsClient.hangupCall(address, callInfo.getCallId(), key));

        //最后一个挂机
        if (count == 1) {
            //坐席状态变更
            changeAgentStatus(callInfo);
            sendAgentStatus(callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), callInfo.getDirection(), AgentStateEnum.CALL_END);


            CallRecord callRecord = new CallRecord();
            callRecord.transfer(callInfo);
            iCallRecordService.save(callRecord);

            ifsCallCacheService.removeCallInfo(callInfo.getCallId());
        }
        callInfo.setChannelInfoMap(uniqueId, channelInfo);
        ifsCallCacheService.saveCallInfo(callInfo);

    }


    private void changeAgentStatus(CallInfo callInfo) {
        if (Objects.isNull(callInfo)) {
            return;
        }
        Boolean isAgent = redisService.getCacheMapHasKey(CacheConstants.AGENT_CURRENT_STATUS_KEY, String.valueOf(callInfo.getAgentId()));
        if (isAgent) {
            SipAgentStatusVo agentStatusVo = redisService.getCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY, String.valueOf(callInfo.getAgentId()));
            agentStatusVo.setStatusTime(callInfo.getEndTime());
            Integer skillAfterTime = callInfo.gainSkillAfterTime();
            if (Objects.isNull(skillAfterTime)) {
                skillAfterTime = 1;
            }
            agentStatusVo.setCallEndTime(skillAfterTime.longValue());
            agentStatusVo.setStatus(SipAgentStatusEnum.NOT_READY.getCode());
            redisService.setCacheMapValue(CacheConstants.AGENT_CURRENT_STATUS_KEY, String.valueOf(agentStatusVo.getId()), agentStatusVo);
        }
    }
}
