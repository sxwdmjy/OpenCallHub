package com.och.esl.handler.call;

import com.och.common.annotation.EslProcessName;
import com.och.common.domain.CallInfo;
import com.och.common.domain.ChannelInfo;
import com.och.common.enums.ProcessEnum;
import com.och.common.enums.RouteTypeEnum;
import com.och.esl.handler.route.FsAbstractRouteHandler;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 机器人外呼
 *
 * @author danmo
 * @date 2023-10-23 17:05
 **/
@EslProcessName(ProcessEnum.CALL_TASK)
@Component
@Slf4j
public class FsCallIRobotProcess extends FsAbstractCallProcess {

    @Override
    public void handler(String address, EslEvent event, CallInfo callInfo, ChannelInfo lfsChannelInfo) {
        log.info("进入callRobot电话: callId:{} caller:{} called:{} uniqueId:{}, otherUniqueId:{}", callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), lfsChannelInfo.getOtherUniqueId(), lfsChannelInfo.getUniqueId());

    }


}
