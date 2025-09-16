package com.och.esl.handler.call;

import cn.hutool.core.date.DateUtil;
import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import com.och.esl.handler.route.FsAbstractRouteHandler;
import com.och.system.domain.entity.CallSchedule;
import com.och.system.domain.vo.route.CallRouteVo;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 外呼任务
 *
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALL_TASK)
@Component
@Slf4j
public class FsCallITaskProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        log.info("进入callTask电话: callId:{} caller:{} called:{} uniqueId:{}, otherUniqueId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());
        lfsCallCacheService.saveCallInfo(callInfo);
        lfsCallCacheService.saveCallRel(lfsChannelInfo.getUniqueId(),callInfo.getCallId());
        Long agentId = callInfo.getAgentId();
        FsAbstractRouteHandler routeHandler = routeFactory.factory(RouteTypeEnum.AGENT.getType());
        if(Objects.nonNull(routeHandler)){
            routeHandler.handler(address, callInfo,lfsChannelInfo.getUniqueId(), String.valueOf(agentId));
        }
    }


}
