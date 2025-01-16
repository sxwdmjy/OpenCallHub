
package com.och.esl.handler.esl;

import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslEventNames;
import com.och.common.domain.CallInfo;
import com.och.common.enums.AgentStateEnum;
import com.och.common.enums.DirectionEnum;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.utils.EslEventUtil;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 对于呼出呼叫，另一方处于响铃状态；对于呼入电话，该方正在进行提醒。
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.CHANNEL_PROGRESS_MEDIA)
@Component
public class FsChannelProgressMediaEslEventHandler extends AbstractFsEslEventHandler {

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("ChannelProgressMediaEslEventHandler  EslEvent:{}.", event);
        String uniqueId = EslEventUtil.getUniqueId(event);
        String otherUniqueId = EslEventUtil.getOtherUniqueId(event);
        CallInfo callInfo = ifsCallCacheService.getCallInfoByUniqueId(uniqueId);
        if(Objects.isNull(callInfo)){
            return;
        }
        Integer direction = callInfo.getDirection();
        if (Objects.requireNonNull(DirectionEnum.getByType(direction)) == DirectionEnum.OUTBOUND) {
            sendAgentStatus(callInfo.getCallId(), callInfo.getCaller(), callInfo.getCallee(), callInfo.getDirection(), AgentStateEnum.CALL_START);
        }

    }
}
