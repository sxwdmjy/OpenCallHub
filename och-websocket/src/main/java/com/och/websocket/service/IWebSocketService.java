package com.och.websocket.service;

import com.och.websocket.domain.WsMsgPayload;

import java.io.IOException;

/**
 * @author danmo
 * @date 2023年09月22日 11:10
 */
public interface IWebSocketService {

    /**
     * 获取sessionId
     * @param userId 员工ID
     * @return
     */
    public String getSessionId(String userId);

    /**
     * 发送消息
     * @param key
     * @param msg
     * @throws IOException
     */
    public void sendMsg(String key, String msg) throws IOException;


    /**
     * 发送Pong消息
     * @param key
     * @throws IOException
     */
    public void sendPongMsg(String key) throws IOException;

    /**
     * 发送Ping消息
     * @param key
     * @throws IOException
     */
    public void sendPingMsg(String key) throws IOException;

    /**
     * 发送广播消息
     * @param msg
     */
    public void sendBroadcastMsg(String msg) throws IOException;
}
