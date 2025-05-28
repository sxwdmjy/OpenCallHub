package com.och.websocket.handler;

import com.alibaba.fastjson2.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.utils.StringUtils;
import com.och.security.authority.LoginUserInfo;
import com.och.websocket.domain.WsEventEnum;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.factory.WsMsgEventStrategy;
import com.och.websocket.factory.WsMsgEventStrategyFactory;
import com.och.websocket.utils.WsSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;


/**
 * @author danmo
 * @date 2023年09月22日 10:40
 */
@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Autowired
    private RedisService redisService;
    @Autowired
    private WsMsgEventStrategyFactory eventStrategyFactory;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("建立ws 连接 sessionId:{}", session.getId());
        LoginUserInfo user = (LoginUserInfo)session.getAttributes().get("user");
        Long userId = user.getUserId();
        String sessionId = session.getId();
        redisService.setCacheMapValue(CacheConstants.CLIENT_USER_POOL_KEY, String.valueOf(userId), sessionId);
        //可以用group存储，这里不做分组区分
        WsSessionUtil.add(sessionId, session);
        session.sendMessage(new TextMessage(WsMsgPayload.builder().sessionId(sessionId).event(WsEventEnum.SUCCEED).timestamp(System.currentTimeMillis()).build().toString()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("接收到ws文本消息 sessionId:{}, msg:{}", session.getId(), message.getPayload());
        LoginUserInfo user = (LoginUserInfo)session.getAttributes().get("user");
        Long userId = user.getUserId();
        String payload = message.getPayload();
        if(StringUtils.isNotBlank(payload)){
            WsMsgPayload wsMsgPayload = JSONObject.parseObject(payload, WsMsgPayload.class);
            WsMsgEventStrategy eventStrategy = eventStrategyFactory.getWsMsgEventStrategy(wsMsgPayload.getEvent().name());
            if(eventStrategy == null){
                log.error("未知的ws事件:{}", wsMsgPayload.getEvent());
            }else {
                eventStrategy.handle(userId, wsMsgPayload);
            }
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        log.info("接收到ws二进制消息 sessionId:{}, msg:{}", session.getId(), message.toString());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("ws 连接异常 sessionId:{},msg:{}", session.getId(), exception.getMessage(), exception);
        LoginUserInfo user = (LoginUserInfo)session.getAttributes().get("user");
        Long userId = user.getUserId();
        String sessionId = session.getId();
        redisService.delCacheMapValue(CacheConstants.CLIENT_USER_POOL_KEY, String.valueOf(userId));
        WsSessionUtil.removeAndClose(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("关闭ws连接 sessionId:{},status:{},{}", session.getId(), status.getCode(), status.getReason());
        LoginUserInfo user = (LoginUserInfo)session.getAttributes().get("user");
        String sessionId = session.getId();
        Long userId = user.getUserId();
        redisService.delCacheMapValue(CacheConstants.CLIENT_USER_POOL_KEY, String.valueOf(userId));
        WsSessionUtil.removeAndClose(sessionId);

    }
}
