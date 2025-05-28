package com.och.websocket.factory;


import com.och.common.annotation.WsEventName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
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
