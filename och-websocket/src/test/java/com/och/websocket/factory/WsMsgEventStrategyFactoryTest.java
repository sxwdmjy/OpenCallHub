package com.och.websocket.factory;

import com.och.common.annotation.WsEventName;
import com.och.websocket.domain.WsMsgPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * WsMsgEventStrategyFactory 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class WsMsgEventStrategyFactoryTest {

    private WsMsgEventStrategyFactory factory;

    private TestAgentStatusStrategy agentStatusStrategy;
    private TestHeartbeatStrategy heartbeatStrategy;

    @BeforeEach
    void setUp() {
        agentStatusStrategy = new TestAgentStatusStrategy();
        heartbeatStrategy = new TestHeartbeatStrategy();

        Map<String, WsMsgEventStrategy> strategyMap = new HashMap<>();
        strategyMap.put("agentStatusStrategy", agentStatusStrategy);
        strategyMap.put("heartbeatStrategy", heartbeatStrategy);

        factory = new WsMsgEventStrategyFactory(strategyMap);
    }

    @Test
    @DisplayName("已知事件 AGENT_STATUS -- 应返回对应的策略实例")
    void testGetWsMsgEventStrategy_WithKnownAgentStatusEvent_ShouldReturnStrategy() {
        // when
        WsMsgEventStrategy result = factory.getWsMsgEventStrategy("AGENT_STATUS");

        // then
        assertNotNull(result);
        assertEquals(agentStatusStrategy, result);
    }

    @Test
    @DisplayName("已知事件 HEARTBEAT -- 应返回对应的策略实例")
    void testGetWsMsgEventStrategy_WithKnownHeartbeatEvent_ShouldReturnStrategy() {
        // when
        WsMsgEventStrategy result = factory.getWsMsgEventStrategy("HEARTBEAT");

        // then
        assertNotNull(result);
        assertEquals(heartbeatStrategy, result);
    }

    @Test
    @DisplayName("未知事件 UNKNOWN_EVENT -- 应返回 null")
    void testGetWsMsgEventStrategy_WithUnknownEvent_ShouldReturnNull() {
        // when
        WsMsgEventStrategy result = factory.getWsMsgEventStrategy("UNKNOWN_EVENT");

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("事件名大小写不匹配 -- 应返回 null（精确匹配）")
    void testGetWsMsgEventStrategy_WithCaseMismatch_ShouldReturnNull() {
        // when
        WsMsgEventStrategy result = factory.getWsMsgEventStrategy("agent_status");

        // then: 大小写敏感，AGENT_STATUS != agent_status
        assertNull(result);
    }

    @Test
    @DisplayName("空策略 Map -- 任何事件都返回 null")
    void testGetWsMsgEventStrategy_WithEmptyMap_ShouldReturnNull() {
        // given
        WsMsgEventStrategyFactory emptyFactory = new WsMsgEventStrategyFactory(Collections.emptyMap());

        // when
        WsMsgEventStrategy result = emptyFactory.getWsMsgEventStrategy("AGENT_STATUS");

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("null 事件名 -- 应为安全兜底，Objects.equals(null, null) 为 true 但不会匹配")
    void testGetWsMsgEventStrategy_WithNullEventName_ShouldReturnNull() {
        // when
        WsMsgEventStrategy result = factory.getWsMsgEventStrategy(null);

        // then: 不会匹配任何策略，因为注解 name() 不会为 null
        assertNull(result);
    }

    // ========== 测试用的具体策略实现类 ==========

    /**
     * 模拟坐席状态变更策略
     */
    @WsEventName(name = "AGENT_STATUS")
    static class TestAgentStatusStrategy implements WsMsgEventStrategy {
        @Override
        public void handler(Long userId, WsMsgPayload payload) {
            // no-op for testing
        }
    }

    /**
     * 模拟心跳检测策略
     */
    @WsEventName(name = "HEARTBEAT")
    static class TestHeartbeatStrategy implements WsMsgEventStrategy {
        @Override
        public void handler(Long userId, WsMsgPayload payload) {
            // no-op for testing
        }
    }
}
