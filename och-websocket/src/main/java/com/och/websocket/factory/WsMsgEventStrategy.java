package com.och.websocket.factory;


import com.och.websocket.domain.WsMsgPayload;

/**
 * WebSocket 消息事件处理策略接口
 * <p>定义不同类型 WebSocket 消息的处理契约，所有具体事件处理器（心跳、坐席状态等）均实现此接口。</p>
 *
 * @author danmo
 * @date 2025/05/27 22:12
 */
public interface WsMsgEventStrategy {

    /**
     * 处理 WebSocket 消息事件
     * @param userId  用户 ID
     * @param payload 消息负载
     */
    void handler(Long userId, WsMsgPayload payload);
}
