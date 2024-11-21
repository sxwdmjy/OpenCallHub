package com.och.esl.factory;

import com.och.common.config.redis.RedisService;
import com.och.common.enums.AgentStateEnum;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.service.ICallDisplayService;
import com.och.system.service.ICallRecordService;
import com.och.system.service.ISipAgentService;
import lombok.extern.slf4j.Slf4j;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2023年09月28日 14:21
 */
@Slf4j
@Component
public abstract class AbstractFsEslEventHandler implements FsEslEventHandler {

    public final String SIP_USER_AGENT = "FreeSWITCH";

    @Autowired
    protected IFsCallCacheService ifsCallCacheService;

    @Autowired
    protected FsEslProcessFactory fsEslProcessFactory;

    @Autowired
    protected ISipAgentService iSipAgentService;

    @Autowired
    protected ICallDisplayService iCallDisplayService;

    @Autowired
    protected RedisService redisService;

    @Lazy
    @Autowired
    protected FsClient fsClient;

    @Autowired
    protected ICallRecordService iCallRecordService;


    @Override
    public abstract void handleEslEvent(String address, EslEvent event);


    protected void sendAgentStatus(Long callId, String caller, String callee, Integer direction, AgentStateEnum status) {
        /*LfsWsMsg wsMsg = new LfsWsMsg();
        wsMsg.setCallId(callId);
        wsMsg.setCallee(caller);
        wsMsg.setCaller(callee);
        wsMsg.setDirection(direction);
        wsMsg.setStatus(status.getCode());
        wsMsg.setContent(status.getDes());
        LfsMqMsg mqMsg = LfsMqMsg.builder().msg(JSONObject.toJSONString(wsMsg)).userId(1L).build();
        LfsBaseMqMsg baseMqMsg = LfsBaseMqMsg.builder().msg(mqMsg).topic("callEventStatus-out").build();
        Boolean send = lfsRocketMqMsgProducer.send(baseMqMsg);
        if (send) {
            log.info("[发送MQ消息-坐席websocket消息通知]成功 wsMsg:{}", wsMsg);
        } else {
            log.info("[发送MQ消息-坐席websocket消息通知]失败 wsMsg:{}", wsMsg);
        }*/
    }
}
