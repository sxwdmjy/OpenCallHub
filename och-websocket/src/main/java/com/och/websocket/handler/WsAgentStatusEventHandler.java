package com.och.websocket.handler;


import com.alibaba.fastjson2.JSONObject;
import com.och.common.annotation.WsEventName;
import com.och.system.service.ISipAgentService;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.factory.WsMsgEventStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2025/05/27 22:20
 */
@WsEventName(name = "AGENT_STATUS")
@Slf4j
@AllArgsConstructor
@Component
public class WsAgentStatusEventHandler implements WsMsgEventStrategy {

    private final ISipAgentService iSipAgentService;

    @Override
    public void handle(Long userId, WsMsgPayload payload) {
        log.info("WsAgentStatusEventHandler handle");
        WsAgentStatusData data = JSONObject.parseObject(payload.getData(), WsAgentStatusData.class);
        iSipAgentService.updateOnlineStatus(data.getAgentId(), data.getOnlineStatus(), payload.getTimestamp());
    }


    @Data
    public static class WsAgentStatusData {
        private Long agentId;
        private Integer onlineStatus;
    }
}
