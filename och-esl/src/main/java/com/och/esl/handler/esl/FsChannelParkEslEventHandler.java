
package com.och.esl.handler.esl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslConstant;
import com.och.common.constant.EslEventNames;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.AgentStateEnum;
import com.och.common.enums.DirectionEnum;
import com.och.common.enums.ProcessEnum;
import com.och.common.utils.StringUtils;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.utils.EslEventUtil;
import com.och.system.domain.entity.CallDisplay;
import com.och.system.domain.entity.CorpInfo;
import com.och.system.domain.query.display.CallDisplayQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.domain.vo.display.CallDisplayVo;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 话机振铃
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.CHANNEL_PARK)
@Component
public class FsChannelParkEslEventHandler extends AbstractFsEslEventHandler {

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("ChannelParkEslEventHandler EslEvent:{}.", JSONObject.toJSONString(event));
        if (EslConstant.OK.equals(EslEventUtil.getSipHangupPhrase(event))) {
            return;
        }
        String uniqueId = EslEventUtil.getUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);

        if (callInfo == null && DirectionEnum.INBOUND.name().equals(EslEventUtil.getCallDirection(event).toUpperCase())) {
            if(StringUtils.containsAny(EslEventUtil.getVariableSipUserAgent(event), "FreeSWITCH")){
                inboundCall(address,event);
                return;
            }else {
                outboundCall(address,event);
            }
//            if (EslEventUtil.getSipReqPort(event) == null) {
//                //呼入
//                inboundCall(address,event);
//                return;
//            }
//            if (!EslEventUtil.getSipContactPort(event).equals(EslEventUtil.getSipViaPort(event))) {
//                //硬话机外呼
//                sipOutboundCall(address,event);
//                return;
//            }
        }

        if (Objects.isNull(callInfo) || callInfo.getHangupDir() != null) {
            return;
        }

        ChannelInfo channelInfo = callInfo.getChannelMap().get(uniqueId);
        if (Objects.isNull(channelInfo)) {
            return;
        }
        if (StringUtils.isNotEmpty(EslEventUtil.getSipHangupPhrase(event))) {
            return;
        }
        if (channelInfo.getAnswerTime() != null && channelInfo.getState() != null) {
            return;
        }
        channelInfo.setRingStartTime(event.getEventDateTimestamp() / 1000);

        Integer direction = callInfo.getDirection();
        switch (DirectionEnum.getByType(direction)){
            case INBOUND -> {
                sendAgentStatus(callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee(),direction, AgentStateEnum.CALL_OUT_RING);
                return;
            }
            case OUTBOUND -> {
                sendAgentStatus(callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee(),direction, AgentStateEnum.CALL_INT_RING);
                return;
            }
            default -> {
            }
        }

        callInfo.setChannelInfoMap(uniqueId,channelInfo);
        ifsCallCacheService.saveCallInfo(callInfo);

        //todo 通知坐席呼叫状态

    }

    /**
     * 呼入
     * @param event
     */
    private void inboundCall(String address, EslEvent event) {
        Long callId = IdUtil.getSnowflakeNextId();
        String uniqueId = EslEventUtil.getUniqueId(event);
        String callerNumber = EslEventUtil.getCallerCallerIdNumber(event);
        String calleeNumber = EslEventUtil.getCallerDestinationNumber(event);
        String sipContactUri = EslEventUtil.getSipContactUri(event);
        log.info("park >>>>>>>inbound callId:{}, caller:{}, called:{}, uniqueId:{}, sipContactUri:{}", callId,callerNumber, calleeNumber, uniqueId, sipContactUri);
  /*      AjaxResult<CallInPhoneVo> ajaxResult = lfsCallInPhoneClient.getDetailByPhone(calleeNumber);
        if(ajaxResult == null || ajaxResult.getData() == null){
            log.info("呼入号码查询为空 callId：{}，caller：{}，callee：{}",callId,callerNumber,calleeNumber);
            fsClient.hangupCall(address, callId, uniqueId);
            return;
        }
        CallInPhoneVo callInPhone = ajaxResult.getData();*/
        //构建呼叫总线
        CallInfo callInfo = CallInfo.builder().callId(callId)
                .caller(callerNumber).calleeDisplay(calleeNumber).direction(DirectionEnum.INBOUND.getType())
                .callTime(DateUtil.current()).callerDisplay(callerNumber).routeType(2).build();
        callInfo.addUniqueIdList(uniqueId);
        //构建主叫通道
        ChannelInfo channelInfo = ChannelInfo.builder().callId(callId).uniqueId(uniqueId).cdrType(1).type(2).callTime(DateUtil.current())
                .caller(callerNumber).called(calleeNumber).display(calleeNumber).build();
        callInfo.setChannelInfoMap(uniqueId,channelInfo);

        //CorpInfo corpInfo = CorpClient.getDetail(callInfo.getTenantId()).getData();
        //callInfo.setCdrNotifyUrl(corpInfo.getCallBackPath());

        callInfo.setProcess(ProcessEnum.CALLIN);

        ifsCallCacheService.saveCallInfo(callInfo);
        ifsCallCacheService.saveCallRel(uniqueId,callId);

        fsClient.answer(address, uniqueId);
    }

    /**
     * 呼出
     * @param address
     * @param event
     */
    private void outboundCall(String address,EslEvent event) {
        String callerNumber = EslEventUtil.getCallerCallerIdNumber(event);
        String calleeNumber = EslEventUtil.getCallerDestinationNumber(event);
        String uniqueId = EslEventUtil.getUniqueId(event);
        Long callId = IdUtil.getSnowflakeNextId();
        SipAgentVo sipAgent = iSipAgentService.getInfoByAgent(callerNumber);
        if(Objects.isNull(sipAgent)){
            log.error("软电话外呼未查询到坐席信息 callId:{}  callerNumber:{} calleeNumber:{}", callId, callerNumber, calleeNumber);
            fsClient.hangupCall(address,callId,uniqueId);
            return;
        }
        //构建呼叫总线
        CallInfo callInfo = CallInfo.builder().callId(callId)
                .agentId(sipAgent.getId()).agentNumber(sipAgent.getAgentNumber()).agentName(sipAgent.getName())
                .caller(callerNumber).callee(calleeNumber).direction(DirectionEnum.OUTBOUND.getType())
                .callTime(DateUtil.current())
                .routeType(2).build();
        callInfo.addUniqueIdList(uniqueId);
        callInfo.setProcess(ProcessEnum.CALL_OTHER);

        //获取被叫显号
        CallDisplayQuery displayQuery = new CallDisplayQuery();
        displayQuery.setType(2);
        List<CallDisplayVo> displayList = iCallDisplayService.getList(displayQuery);
        if(CollectionUtil.isEmpty(displayList)){
            log.error("电话外呼未查询到显号信息 callId:{}  callerNumber:{} calleeNumber:{}", callId, callerNumber, calleeNumber);
            fsClient.hangupCall(address,callId,uniqueId);
            return;
        }

        CallDisplayVo displaySimple = RandomUtil.randomEle(displayList);
        callInfo.setCallerDisplay(sipAgent.getAgentNumber());
        callInfo.setCalleeDisplay(displaySimple.getPhone());

        //CorpInfo corpInfo = lfsCorpClient.getDetail(callInfo.getTenantId()).getData();
        //callInfo.setCdrNotifyUrl(corpInfo.getCallBackPath());

        //构建主叫通道
        ChannelInfo channelInfo = ChannelInfo.builder().callId(callId).uniqueId(uniqueId).cdrType(2).type(1)
                .agentId(sipAgent.getId()).agentNumber(sipAgent.getAgentNumber()).agentName(sipAgent.getName())
                .callTime(DateUtil.current())
                .caller(callInfo.getCallee()).called(callInfo.getCaller()).display(callInfo.getCalleeDisplay()).build();
        callInfo.setChannelInfoMap(uniqueId,channelInfo);

        ifsCallCacheService.saveCallInfo(callInfo);
        ifsCallCacheService.saveCallRel(uniqueId,callId);
        fsClient.answer(address, uniqueId);
    }


}
