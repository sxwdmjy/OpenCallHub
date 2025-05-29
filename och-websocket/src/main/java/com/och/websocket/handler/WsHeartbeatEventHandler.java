package com.och.websocket.handler;


import com.och.common.annotation.WsEventName;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.factory.AbstractWsMsgEventStrategy;
import com.och.websocket.factory.WsMsgEventStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @date 2025/05/27 22:20
 */
@WsEventName(name = "HEARTBEAT")
@Slf4j
@AllArgsConstructor
@Component
public class WsHeartbeatEventHandler extends AbstractWsMsgEventStrategy {


    @Override
    protected void doHandler(Long userId, WsMsgPayload payload) {

    }
}
