package com.och.websocket.domain;


import lombok.Getter;

/**
 * @author danmo
 * @date 2025/05/27 18:00
 */
@Getter
public enum WsEventEnum {

    //登录
    SUCCEED,
    //心跳
    HEARTBEAT,
    //坐席状态
    AGENT_STATUS,
    ;

}
