
package com.och.esl.handler.esl;

import com.alibaba.fastjson.JSONObject;
import com.och.common.annotation.EslEventName;
import com.och.common.constant.EslEventNames;
import com.och.esl.factory.AbstractFsEslEventHandler;
import com.och.esl.service.IFlowNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.stereotype.Component;

/**
 * 放音结束
 * @author danmo
 * @date 2023年09月18日 19:03
 */
@Slf4j
@EslEventName(EslEventNames.PLAYBACK_STOP)
@Component
public class FsChannelPlaybackEndEslEventHandler extends AbstractFsEslEventHandler {

    private IFlowNoticeService flowNoticeService;

    @Override
    public void handleEslEvent(String address, EslEvent event) {
        log.info("放音结束 EslEvent:{}.", JSONObject.toJSONString(event));

    }
}
