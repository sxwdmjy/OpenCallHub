package com.och.websocket.factory;


import com.och.common.annotation.WsEventName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * WebSocket 消息事件策略工厂
 * <p>根据 {@code @WsEventName} 注解的值，自动发现并匹配对应的消息处理策略。
 * 支持在子类和父类上查找注解，兼容模板方法模式。</p>
 *
 * @author danmo
 * @date 2025/05/27 22:12
 */
@Slf4j
@Component
public class WsMsgEventStrategyFactory {

    private final Map<String, WsMsgEventStrategy> wsMsgEventStrategyMap;

    public WsMsgEventStrategyFactory(Map<String, WsMsgEventStrategy> wsMsgEventStrategyMap) {
        this.wsMsgEventStrategyMap = wsMsgEventStrategyMap;
    }

    public WsMsgEventStrategy getWsMsgEventStrategy(String eventName) {
        for (WsMsgEventStrategy handler : wsMsgEventStrategyMap.values()) {
            WsEventName wsEventName = handler.getClass().getAnnotation(WsEventName.class);
            if (wsEventName == null) {
                wsEventName = handler.getClass().getSuperclass().getAnnotation(WsEventName.class);
            }
            if (wsEventName == null) {
                continue;
            }
            if (Objects.equals(eventName, wsEventName.name())) {
                return handler;
            }
        }
        return null;
    }
}
