package com.och.api.controller;

import com.och.common.api.ApiResponse;
import com.och.esl.application.EslConnectionManager;
import com.och.infra.file.FileStorage;
import com.och.infra.mq.DomainEvent;
import com.och.infra.mq.EventPublisher;
import com.och.infra.mq.FoundationEventListener;
import com.och.infra.redis.RedisCache;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Foundation 基础验收接口测试
 * <p>
 * FoundationController 是 Phase 1 公共能力的门面入口，测试只验证聚合逻辑和响应结构，
 * 不连接真实 Redis、RabbitMQ、FreeSWITCH，避免单元测试依赖外部环境。
 * </p>
 *
 * @author danmo
 */
class FoundationControllerTest {

    private final RedisCache redisCache = mock(RedisCache.class);
    private final EventPublisher eventPublisher = mock(EventPublisher.class);
    private final FoundationEventListener eventListener = mock(FoundationEventListener.class);
    private final FileStorage fileStorage = mock(FileStorage.class);
    private final EslConnectionManager eslConnectionManager = mock(EslConnectionManager.class);
    private final FoundationController controller = new FoundationController(
            redisCache, eventPublisher, eventListener, fileStorage, eslConnectionManager);

    @Test
    void healthReportsConnectedEslNodes() {
        when(eslConnectionManager.getAllConnectionStates()).thenReturn(
                Map.of("fs-1", EslConnectionManager.ConnectionState.CONNECTED));

        ApiResponse<Map<String, Object>> response = controller.health();

        assertEquals(200, response.getCode());
        assertEquals("UP", response.getData().get("status"));
        Map<?, ?> esl = (Map<?, ?>) response.getData().get("esl");
        assertEquals("UP", esl.get("status"));
        assertEquals(1L, esl.get("connectedNodes"));
    }

    @Test
    void redisCheckUsesRedisCacheWrapper() {
        when(redisCache.get("foundation:health")).thenReturn("ok");

        ApiResponse<Map<String, Object>> response = controller.redisCheck();

        verify(redisCache).set("foundation:health", "ok", 10, TimeUnit.SECONDS);
        assertEquals("UP", response.getData().get("status"));
        assertEquals("ok", response.getData().get("value"));
    }

    @Test
    void mqCheckPublishesFoundationEvent() {
        ApiResponse<Map<String, Object>> response = controller.mqCheck();

        verify(eventListener).reset();
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("UP", response.getData().get("status"));
        assertEquals("foundation.test", eventCaptor.getValue().getEventType());
        assertNotNull(response.getData().get("eventId"));
    }

    @Test
    void mqEventsReturnsConsumedEvents() {
        DomainEvent event = DomainEvent.of("foundation.test", "{}", "trace-001");
        when(eventListener.getEventCount()).thenReturn(1);
        when(eventListener.getConsumedEvents()).thenReturn(List.of(event));

        ApiResponse<Map<String, Object>> response = controller.mqEvents();

        assertEquals(1, response.getData().get("eventCount"));
        assertEquals(List.of(event), response.getData().get("events"));
    }
}
