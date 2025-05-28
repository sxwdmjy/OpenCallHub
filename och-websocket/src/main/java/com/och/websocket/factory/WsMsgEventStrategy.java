package com.och.websocket.factory;


import com.och.websocket.domain.WsMsgPayload;

/**
 * @author danmo
 * @date 2025/05/27 22:12
 */
public interface WsMsgEventStrategy {

    void handle(Long userId, WsMsgPayload payload);
}
