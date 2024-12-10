package com.och.esl.handler.call;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.esl.handler.route.FsAbstractRouteHandler;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.query.callin.CallInPhoneQuery;
import com.och.system.domain.vo.callin.CallInPhoneRelVo;
import com.och.system.domain.vo.callin.CallInPhoneVo;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 呼入
 *
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALLIN)
@Component
@Slf4j
public class FsCallInProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        log.info("进入callIn电话: callId:{} caller:{} called:{} uniqueId:{}, otherUniqueId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());
        CallInPhoneQuery callInPhoneQuery = new CallInPhoneQuery();
        callInPhoneQuery.setPhone(callInfo.getCallee());
        List<CallInPhoneVo> callInPhoneList = callInPhoneService.getList(callInPhoneQuery);
        if(CollectionUtil.isEmpty(callInPhoneList)){
            log.info("进入callIn电话但呼入号码查询为空 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }
        CallInPhoneVo callInPhone = callInPhoneList.get(0);
        List<CallInPhoneRelVo> routeList = callInPhone.getRouteList();
        if(CollectionUtil.isEmpty(routeList)){
            log.info("进入callIn电话但日程安排为空 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }

        String filePath = sysSettingConfig.getFsProfile() + "/" + DateUtil.today() + "/" + callInfo.getCallId() + "_" + DateUtil.current() + sysSettingConfig.getFsFileSuffix();
        //设置振铃录音
        fsClient.record(address,callInfo.getCallId(),lfsChannelInfo.getUniqueId(),filePath);
        callInfo.setRecord(filePath);
        callInfo.setRecordStartTime(lfsChannelInfo.getAnswerTime());
        CallInPhoneRelVo relVo = checkSchedule(routeList);
        if(Objects.isNull(relVo)){
            log.info("进入callIn电话但无合适日程安排 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }
        log.info("scheduleName:{} routeType:{} routeValue:{}", relVo.getScheduleName(), relVo.getRouteType(), relVo.getRouteValue());
        //电话流转详情
        CallInfoDetail detail = new CallInfoDetail();
        detail.setCallId(callInfo.getCallId());
        detail.setStartTime(DateUtil.current());
        detail.setOrderNum(callInfo.getDetailList() == null ? 0 : callInfo.getDetailList().size() + 1);
        detail.setTransferType(1);
        detail.setTransferId(callInPhone.getId());
        callInfo.addDetailList(detail);

        FsAbstractRouteHandler routeHandler = routeFactory.factory(relVo.getRouteType());
        if(Objects.nonNull(routeHandler)){
            routeHandler.handler(address, callInfo,lfsChannelInfo.getUniqueId(),relVo.getRouteValue());
        }
    }

    private CallInPhoneRelVo checkSchedule(List<CallInPhoneRelVo> routeList) {
        routeList = routeList.stream().sorted(Comparator.comparing(item -> item.getScheduleDetail().getLevel())).collect(Collectors.toList());
        //日程选择
        for (CallInPhoneRelVo callInPhoneRel : routeList) {
            boolean dayFlag = false;
            boolean timeFlag = false;
            boolean weekFlag = false;
            CallSchedule scheduleDetail = callInPhoneRel.getScheduleDetail();
            if(scheduleDetail.getType() == 0){
                DateTime startDay = DateUtil.parseDate(scheduleDetail.getStartDay());
                DateTime endDay = DateUtil.parseDate(scheduleDetail.getEndDay());
                DateTime dateTime = DateUtil.parseDate(DateUtil.today());
                dayFlag = dateTime.isAfterOrEquals(startDay) && dateTime.isBeforeOrEquals(endDay);
            }else if(scheduleDetail.getType() == 1){
                int dayOfMonth = DateUtil.thisDayOfMonth();
                dayFlag =  Integer.parseInt(scheduleDetail.getStartDay()) <= dayOfMonth  &&  dayOfMonth <= Integer.parseInt(scheduleDetail.getEndDay());
            }
            DateTime startTime = DateUtil.parse(scheduleDetail.getStartTime(), "HH:mm");
            DateTime endTime = DateUtil.parse(scheduleDetail.getEndTime(), "HH:mm");
            DateTime dateTime = DateUtil.parse(DateUtil.format(new Date(),"HH:mm"),"HH:mm");
            if(startTime.isBeforeOrEquals(dateTime) && endTime.isAfterOrEquals(dateTime)){
                timeFlag = true;
            }
            String workCycle = scheduleDetail.getWorkCycle();
            int dayOfWeek = DateUtil.date().dayOfWeek() == 1 ? 7 :  DateUtil.date().dayOfWeek() -1;
            if(workCycle.contains(String.valueOf(dayOfWeek))){
                weekFlag = true;
            }
            if(dayFlag && timeFlag && weekFlag){
               return callInPhoneRel;
            }
        }
        return null;
    }


}
