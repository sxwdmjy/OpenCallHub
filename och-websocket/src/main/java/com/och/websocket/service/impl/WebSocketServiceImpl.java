package com.och.websocket.service.impl;

import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.websocket.domain.WsEventEnum;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.service.IWebSocketService;
import com.och.websocket.utils.WsSessionUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author danmo
 * @date 2023年09月22日 11:10
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements IWebSocketService, InitializingBean {

    @Autowired
    private RedisService redisService;

    private final ScheduledExecutorService threadPoolExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("ws-heartbeat-pool", true));

    @Override
    public String getSessionId(String userId) {
        if (redisService.getCacheMapHasKey(CacheConstants.CLIENT_USER_POOL_KEY, String.valueOf(userId))) {
            return (String) redisService.getCacheMapValue(CacheConstants.CLIENT_USER_POOL_KEY, String.valueOf(userId));
        }
        return null;
    }

    @Override
    public void sendMsg(String key, String msg) throws IOException {
        WebSocketSession webSocketSession = WsSessionUtil.get(key);
        if (Objects.nonNull(webSocketSession)) {
            webSocketSession.sendMessage(new TextMessage(msg));
        }
    }

    @Override
    public void sendPongMsg(String key) throws IOException {
        WebSocketSession webSocketSession = WsSessionUtil.get(key);
        if (Objects.nonNull(webSocketSession)) {
            webSocketSession.sendMessage(new PongMessage(ByteBuffer.wrap("heartbeat".getBytes())));
        }
    }

    @Override
    public void sendPingMsg(String key) throws IOException {
        WebSocketSession webSocketSession = WsSessionUtil.get(key);
        if (Objects.nonNull(webSocketSession)) {
            webSocketSession.sendMessage(new PingMessage(ByteBuffer.wrap("heartbeat".getBytes())));
        }
    }

    @Override
    public void sendBroadcastMsg(String msg) throws IOException {
        for (WebSocketSession socketSession : WsSessionUtil.sessionPool.values()) {
            if (Objects.nonNull(socketSession)) {
                socketSession.sendMessage(new TextMessage(msg));
            }
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        threadPoolExecutor.scheduleAtFixedRate(() -> {
            if (!WsSessionUtil.sessionPool.isEmpty()) {
                WsSessionUtil.sessionPool.keySet().forEach(key -> {
                    WsMsgPayload heartbeat = WsMsgPayload.builder().event(WsEventEnum.HEARTBEAT)
                            .sessionId(key)
                            .timestamp(System.currentTimeMillis())
                            .data("heartbeat")
                            .build();
                    try {
                        sendMsg(key, heartbeat.toString());
                    } catch (Exception e) {
                        log.error("send heartbeat to sessionId:{} error", key, e);
                        WsSessionUtil.removeAndClose(key);
                    }
                });
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        threadPoolExecutor.shutdown();
    }
}
