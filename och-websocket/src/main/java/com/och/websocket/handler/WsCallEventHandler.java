package com.och.websocket.handler;

import com.och.common.annotation.WsEventName;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.factory.AbstractWsMsgEventStrategy;
import com.och.websocket.service.IWebSocketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通话事件 WebSocket 处理器（策略模式）
 * <p>
 * 处理呼叫事件（振铃/接听/挂断/转接），通过 WebSocket 推送给前端座席。
 * 根据 payload 中的 sessionId 定位目标 WebSocket 连接并发送 JSON 消息。
 * </p>
 *
 * @author danmo
 */
@WsEventName(name = "CALL")
@Slf4j
@AllArgsConstructor
@Component
public class WsCallEventHandler extends AbstractWsMsgEventStrategy {

    private final IWebSocketService webSocketService;

    @Override
    protected void doHandler(Long userId, WsMsgPayload payload) {
        log.debug("[WS-CALL] 推送通话事件: userId={}, event={}", userId, payload.getEvent());

        try {
            String sessionId = webSocketService.getSessionId(String.valueOf(userId));
            if (sessionId != null) {
                webSocketService.sendMsg(sessionId, payload.toString());
            } else {
                log.warn("[WS-CALL] 用户未在线，消息未发送: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("[WS-CALL] 推送失败: userId={}, event={}", userId, payload.getEvent(), e);
        }
    }
}