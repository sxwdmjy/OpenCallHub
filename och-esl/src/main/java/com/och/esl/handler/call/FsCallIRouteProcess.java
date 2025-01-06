package com.och.esl.handler.call;

import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.CallInfoDetail;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.esl.handler.route.FsAbstractRouteHandler;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.vo.route.CallRouteVo;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 路由
 *
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALL_ROUTE)
@Component
@Slf4j
public class FsCallIRouteProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        log.info("进入callRoute电话: callId:{} caller:{} called:{} uniqueId:{}, otherUniqueId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());
        lfsCallCacheService.saveCallInfo(callInfo);
        lfsCallCacheService.saveCallRel(lfsChannelInfo.getUniqueId(),callInfo.getCallId());
        CallRouteVo callRoute = lfsCallCacheService.getCallRoute(callInfo.getCallee(), callInfo.getDirection());
        if(Objects.isNull(callRoute)){
            log.info("进入callRoute电话但呼入号码查询为空 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }

        //todo 当日程为空或者无日程安排时直接挂断（具体需求待研究）
        if(Objects.isNull(callRoute.getScheduleId())){
            log.info("进入callRoute电话但日程安排为空 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }
        CallSchedule schedule = callScheduleService.getDetail(callRoute.getScheduleId());
        String filePath = sysSettingConfig.getFsProfile() + "/" + DateUtil.today() + "/" + callInfo.getCallId() + "_" + DateUtil.current() + sysSettingConfig.getFsFileSuffix();
        //设置振铃录音
        fsClient.record(address,callInfo.getCallId(),lfsChannelInfo.getUniqueId(),filePath);
        callInfo.setRecord(filePath);
        callInfo.setRecordStartTime(lfsChannelInfo.getAnswerTime());
        if(!checkSchedule(schedule)){
            log.info("进入callIn电话但无合适日程安排 callId：{}，caller：{}，callee：{}",callInfo.getCallId(),callInfo.getCaller(),callInfo.getCallee());
            fsClient.hangupCall(address, callInfo.getCallId(),lfsChannelInfo.getUniqueId());
            return;
        }
        log.info("scheduleName:{} routeType:{} routeValue:{}", schedule.getName(), callRoute.getRouteType(), callRoute.getRouteValue());

        lfsCallCacheService.saveCallInfo(callInfo);
        FsAbstractRouteHandler routeHandler = routeFactory.factory(callRoute.getRouteType());
        if(Objects.nonNull(routeHandler)){
            routeHandler.handler(address, callInfo,lfsChannelInfo.getUniqueId(),callRoute.getRouteValue());
        }

    }


}
