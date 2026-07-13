package com.och.websocket.handler;

import com.och.websocket.domain.WsEventEnum;
import com.och.websocket.domain.WsMsgPayload;
import com.och.websocket.service.IWebSocketService;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WsQueueEventHandler 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class WsQueueEventHandlerTest {

    @Mock
    private IWebSocketService webSocketService;

    private WsQueueEventHandler eventHandler;

    @BeforeEach
    void setUp() throws Exception {
        eventHandler = new WsQueueEventHandler(webSocketService);
        var parentField = eventHandler.getClass().getSuperclass().getDeclaredField("webSocketService");
        parentField.setAccessible(true);
        parentField.set(eventHandler, webSocketService);
    }

    @Test
    @DisplayName("队列开始事件 -- 应广播消息给所有在线班长")
    void testHandler_QueueStartEvent_ShouldBroadcastMessage() throws IOException {
        // given
        Long userId = 1001L;
        Long timestamp = System.currentTimeMillis();
        String queueData = "{\"queueId\":\"q1\",\"action\":\"start\",\"callerId\":\"13800138000\"}";

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS) // 队列事件的 event 字段通过 @WsEventName 注解指定
                .data(queueData)
                .timestamp(timestamp)
                .build();

        // when: 调用公共 handler 方法（内部会调用 beforeHandler -> doHandler -> afterHandler）
        when(webSocketService.getSessionId(String.valueOf(userId))).thenReturn(null);
        eventHandler.handler(userId, payload);

        // then
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(webSocketService, times(1)).sendBroadcastMsg(msgCaptor.capture());

        String broadcastMsg = msgCaptor.getValue();
        assertNotNull(broadcastMsg);
    }

    @Test
    @DisplayName("队列更新事件 -- 应广播消息给所有在线班长")
    void testHandler_QueueUpdateEvent_ShouldBroadcastMessage() throws IOException {
        // given
        Long userId = 1002L;
        Long timestamp = System.currentTimeMillis();
        String queueData = "{\"queueId\":\"q1\",\"action\":\"update\",\"position\":3}";

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-002")
                .event(WsEventEnum.AGENT_STATUS)
                .data(queueData)
                .timestamp(timestamp)
                .build();

        when(webSocketService.getSessionId(String.valueOf(userId))).thenReturn(null);

        // when
        eventHandler.handler(userId, payload);

        // then
        verify(webSocketService, times(1)).sendBroadcastMsg(anyString());
    }

    @Test
    @DisplayName("队列结束事件 -- 应广播消息给所有在线班长")
    void testHandler_QueueEndEvent_ShouldBroadcastMessage() throws IOException {
        // given
        Long userId = 1003L;
        Long timestamp = System.currentTimeMillis();
        String queueData = "{\"queueId\":\"q1\",\"action\":\"end\",\"reason\":\"assigned\"}";

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-003")
                .event(WsEventEnum.AGENT_STATUS)
                .data(queueData)
                .timestamp(timestamp)
                .build();

        when(webSocketService.getSessionId(String.valueOf(userId))).thenReturn(null);

        // when
        eventHandler.handler(userId, payload);

        // then
        verify(webSocketService, times(1)).sendBroadcastMsg(anyString());
    }

    @Test
    @DisplayName("广播消息时抛出 IOException -- 异常被 handler 捕获，不会向外传播")
    void testHandler_WhenIOExceptionThrown_ShouldNotPropagateException() throws IOException {
        // given
        Long userId = 1001L;
        Long timestamp = System.currentTimeMillis();

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data("{\"queueId\":\"q1\"}")
                .timestamp(timestamp)
                .build();

        when(webSocketService.getSessionId(String.valueOf(userId))).thenReturn(null);
        doThrow(new IOException("广播失败")).when(webSocketService).sendBroadcastMsg(anyString());

        // when & then: handler 方法内部捕获 Exception，不会向外抛出
        eventHandler.handler(userId, payload);

        // 验证即便抛出异常，sendBroadcastMsg 仍被调用
        verify(webSocketService, times(1)).sendBroadcastMsg(anyString());
    }

    @Test
    @DisplayName("注入的 IWebSocketService 不为 null")
    void testHandler_ShouldHaveNonNullDependency() throws IOException {
        assertNotNull(webSocketService);
        assertNotNull(eventHandler);
    }
}
