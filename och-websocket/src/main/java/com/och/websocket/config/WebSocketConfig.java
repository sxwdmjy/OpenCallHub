package com.och.websocket.config;

import com.och.common.config.redis.RedisService;
import com.och.websocket.handler.WebSocketHandler;
import com.och.websocket.interceptor.WsHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author danmo
 * @date 2023年09月22日 10:39
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private RedisService redisService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "ws")
                .addInterceptors(new WsHandshakeInterceptor(redisService))
                .setAllowedOrigins("*");
    }
}
