package com.och.websocket.handler;

import com.och.system.service.ISipAgentService;
import com.och.websocket.domain.WsEventEnum;
import com.och.websocket.domain.WsMsgPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * WsAgentStatusEventHandler 单元测试
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
class WsAgentStatusEventHandlerTest {

    @Mock
    private ISipAgentService iSipAgentService;

    @InjectMocks
    private WsAgentStatusEventHandler eventHandler;

    @Test
    @DisplayName("有效坐席状态数据 -- 应调用 updateOnlineStatus")
    void testDoHandler_WithValidData_ShouldUpdateOnlineStatus() {
        // given
        Long userId = 1001L;
        Long agentId = 2001L;
        Integer onlineStatus = 1; // 登录
        Long timestamp = System.currentTimeMillis();

        String data = "{\"agentId\":2001,\"onlineStatus\":1}";
        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data(data)
                .timestamp(timestamp)
                .build();

        // when
        eventHandler.doHandler(userId, payload);

        // then
        ArgumentCaptor<Long> agentIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> timestampCaptor = ArgumentCaptor.forClass(Long.class);

        verify(iSipAgentService, times(1))
                .updateOnlineStatus(agentIdCaptor.capture(), statusCaptor.capture(), timestampCaptor.capture());

        assertEquals(agentId, agentIdCaptor.getValue());
        assertEquals(onlineStatus, statusCaptor.getValue());
        assertEquals(timestamp, timestampCaptor.getValue());
    }

    @Test
    @DisplayName("示忙状态 -- 正确传递 onlineStatus=2")
    void testDoHandler_WithBusyStatus_ShouldUpdateOnlineStatus() {
        // given
        Long userId = 1001L;
        Long agentId = 2001L;
        Integer busyStatus = 2;
        Long timestamp = System.currentTimeMillis();

        String data = "{\"agentId\":2001,\"onlineStatus\":2}";
        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data(data)
                .timestamp(timestamp)
                .build();

        // when
        eventHandler.doHandler(userId, payload);

        // then
        verify(iSipAgentService, times(1))
                .updateOnlineStatus(agentId, busyStatus, timestamp);
    }

    @Test
    @DisplayName("示闲状态 -- 正确传递 onlineStatus=3")
    void testDoHandler_WithIdleStatus_ShouldUpdateOnlineStatus() {
        // given
        Long userId = 1001L;
        Long agentId = 2001L;
        Integer idleStatus = 3;
        Long timestamp = System.currentTimeMillis();

        String data = "{\"agentId\":2001,\"onlineStatus\":3}";
        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data(data)
                .timestamp(timestamp)
                .build();

        // when
        eventHandler.doHandler(userId, payload);

        // then
        verify(iSipAgentService, times(1))
                .updateOnlineStatus(agentId, idleStatus, timestamp);
    }

    @Test
    @DisplayName("事后整理状态 -- 正确传递 onlineStatus=4")
    void testDoHandler_WithAfterCallWorkStatus_ShouldUpdateOnlineStatus() {
        // given
        Long userId = 1001L;
        Long agentId = 2001L;
        Integer afterCallWorkStatus = 4;
        Long timestamp = System.currentTimeMillis();

        String data = "{\"agentId\":2001,\"onlineStatus\":4}";
        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data(data)
                .timestamp(timestamp)
                .build();

        // when
        eventHandler.doHandler(userId, payload);

        // then
        verify(iSipAgentService, times(1))
                .updateOnlineStatus(agentId, afterCallWorkStatus, timestamp);
    }

    @Test
    @DisplayName("data 为空字符串 -- 应抛出 NullPointerException（JSON 解析结果为 null）")
    void testDoHandler_WithEmptyData_ShouldThrowNullPointerException() {
        // given
        Long userId = 1001L;
        Long timestamp = System.currentTimeMillis();

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data("")
                .timestamp(timestamp)
                .build();

        // when & then
        assertThrows(NullPointerException.class, () -> eventHandler.doHandler(userId, payload));
        verify(iSipAgentService, never()).updateOnlineStatus(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("data 为 null -- JSONObject.parseObject 返回 null，调用 getAgentId 时抛出 NullPointerException")
    void testDoHandler_WithNullData_ShouldThrowNullPointerException() {
        // given
        Long userId = 1001L;
        Long timestamp = System.currentTimeMillis();

        WsMsgPayload payload = WsMsgPayload.builder()
                .sessionId("session-001")
                .event(WsEventEnum.AGENT_STATUS)
                .data(null)
                .timestamp(timestamp)
                .build();

        // when & then: JSONObject.parseObject(null, ...) returns null,
        // calling .getAgentId() on null throws NullPointerException
        assertThrows(NullPointerException.class, () -> eventHandler.doHandler(userId, payload));
    }

    @Test
    @DisplayName("注入的 ISipAgentService 不为 null")
    void testDoHandler_ShouldHaveNonNullDependency() {
        assertNotNull(iSipAgentService);
        assertNotNull(eventHandler);
    }
}
