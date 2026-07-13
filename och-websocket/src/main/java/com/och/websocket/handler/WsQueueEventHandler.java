package com.och.websocket.handler;

import com.och.common.annotation.WsEventName;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.factory.AbstractWsMsgEventStrategy;
import com.och.websocket.service.IWebSocketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 队列事件 WebSocket 处理器（策略模式）
 * <p>
 * 处理 ACD 队列事件（排队更新/坐席分配），通过 WebSocket 广播推送给所有班长监控端。
 * 使用 sendBroadcastMsg 将队列状态变更通知所有在线座席的监控页面。
 * </p>
 *
 * @author danmo
 */
@WsEventName(name = "QUEUE")
@Slf4j
@AllArgsConstructor
@Component
public class WsQueueEventHandler extends AbstractWsMsgEventStrategy {

    private final IWebSocketService webSocketService;

    @Override
    protected void doHandler(Long userId, WsMsgPayload payload) {
        log.debug("[WS-QUEUE] 推送队列事件: userId={}, event={}", userId, payload.getEvent());

        try {
            // 队列事件广播给所有在线班长
            webSocketService.sendBroadcastMsg(payload.toString());
        } catch (Exception e) {
            log.error("[WS-QUEUE] 推送失败: event={}", payload.getEvent(), e);
        }
    }
}