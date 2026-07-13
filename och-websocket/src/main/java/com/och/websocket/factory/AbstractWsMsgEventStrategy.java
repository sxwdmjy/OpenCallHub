package com.och.websocket.factory;


import com.och.common.utils.StringUtils;
import com.och.security.authority.LoginUserInfo;
import com.och.security.utils.SecurityUtils;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.service.IWebSocketService;
import com.och.websocket.utils.WsSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抽象 WebSocket 消息事件处理策略
 * <p>采用模板方法模式，在处理事件前自动设置当前用户的安全上下文（ThreadLocal），
 * 处理完成后自动清理，确保子类能正确获取当前登录用户信息。</p>
 *
 * @author danmo
 * @date 2025/05/29 10:29
 */
@Slf4j
@Component
public abstract class AbstractWsMsgEventStrategy implements WsMsgEventStrategy{

    @Autowired
    private IWebSocketService webSocketService;


    @Override
    public void handler(Long userId, WsMsgPayload payload) {
        beforeHandler(userId);
        try {
            //todo 可以加线程执行
            doHandler(userId, payload);
        } catch (Exception e) {
            log.error("handler error userId:{},payload:{}", userId, payload, e);
        }
        afterHandler(userId);
    }

    protected abstract void doHandler(Long userId, WsMsgPayload payload);

    protected void beforeHandler(Long userId) {
        String sessionId = webSocketService.getSessionId(String.valueOf(userId));
        if(StringUtils.isNotBlank(sessionId)){
            WebSocketSession webSocketSession = WsSessionUtil.get(sessionId);
            if(Objects.nonNull(webSocketSession)){
                Object user = webSocketSession.getAttributes().get("user");
                if(Objects.nonNull(user) && user instanceof LoginUserInfo userInfo){
                    SecurityUtils.setThreadLocalCurrentUserInfo(userInfo);
                }
            }
        }
    }

    protected void afterHandler(Long userId) {
        SecurityUtils.removeThreadLoCurrentUserInfo();
    }
}
