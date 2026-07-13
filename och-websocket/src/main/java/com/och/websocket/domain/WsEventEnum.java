package com.och.websocket.domain;


import lombok.Getter;

/**
 * WebSocket 事件类型枚举
 * <p>定义客户端与服务端之间通信的消息事件类型，包含连接成功、心跳检测和坐席状态变更。</p>
 *
 * @author danmo
 * @date 2025/05/27 18:00
 */
@Getter
public enum WsEventEnum {

    /** 连接建立成功 */
    SUCCEED,
    /** 心跳检测 */
    HEARTBEAT,
    /** 坐席状态变更 */
    AGENT_STATUS,
    ;

}
