package com.och.call.application;

import com.och.call.domain.CallEvent;
import com.och.call.domain.CallSession;
import com.och.call.domain.CallState;
import com.och.call.mapper.CallEventMapper;
import com.och.call.mapper.CallSessionMapper;
import com.och.call.service.impl.CallEventIngestServiceImpl;
import com.och.esl.domain.EslEventType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 呼叫事件摄取服务单元测试，覆盖事件摄入、幂等、乱序保护等场景。
 *
 * @author danmo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("呼叫事件摄取服务测试")
class CallEventIngestServiceTest {

    @Mock
    private CallSessionMapper sessionMapper;

    @Mock
    private CallEventMapper eventMapper;

    @Mock
    private CallStateMachine stateMachine;

    @InjectMocks
    private CallEventIngestServiceImpl ingestService;

    @BeforeEach
    void setUp() { ReflectionTestUtils.setField(ingestService, "baseMapper", sessionMapper); }

    // ========================================================================
    // 基本事件摄入
    // ========================================================================

    @Nested
    @DisplayName("标准化 ESL 事件摄入")
    class NormalizedEventIngestion {

        @Test
        @DisplayName("CHANNEL_CREATE 事件创建新会话，填充完整字段")
        void ingestNormalizedEventShouldCreateSessionWithStableFields() {
            LocalDateTime eventTime = LocalDateTime.of(2026, 6, 5, 10, 0);
            NormalizedEslEvent eslEvent = NormalizedEslEvent.builder()
                    .callId("biz-call-001")
                    .eventType(EslEventType.CHANNEL_CREATE)
                    .eventTime(eventTime)
                    .freeswitchUuid("11111111-2222-3333-4444-555555555555")
                    .legId("11111111-2222-3333-4444-555555555555")
                    .callerNumber("13800138000")
                    .calledNumber("8001")
                    .channelVars(Map.of("direction", "inbound"))
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_CREATE"))
                    .build();
            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(null);

            ingestService.ingest(eslEvent);

            ArgumentCaptor<CallEvent> eventCaptor = ArgumentCaptor.forClass(CallEvent.class);
            verify(eventMapper).insert(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getDedupKey())
                    .isEqualTo("biz-call-001|CHANNEL_CREATE|11111111-2222-3333-4444-555555555555|2026-06-05T10:00");
            assertThat(eventCaptor.getValue().getRawHeaders()).contains("CHANNEL_CREATE");

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).insert(sessionCaptor.capture());
            CallSession session = sessionCaptor.getValue();
            assertThat(session.getCallId()).isEqualTo("biz-call-001");
            assertThat(session.getCallerNumber()).isEqualTo("13800138000");
            assertThat(session.getCalledNumber()).isEqualTo("8001");
            assertThat(session.getDirection()).isEqualTo("INBOUND");
            assertThat(session.getState()).isEqualTo(CallState.RINGING.name());
            assertThat(session.getStartTime()).isEqualTo(eventTime);
        }

        @Test
        @DisplayName("channelVars 为 null 时方向默认 INBOUND")
        void shouldDefaultDirectionToInboundWhenChannelVarsNull() {
            LocalDateTime eventTime = LocalDateTime.of(2026, 6, 15, 10, 0);
            NormalizedEslEvent eslEvent = NormalizedEslEvent.builder()
                    .callId("call-001")
                    .eventType(EslEventType.CHANNEL_CREATE)
                    .eventTime(eventTime)
                    .freeswitchUuid("uuid-001")
                    .callerNumber("13800138000")
                    .calledNumber("8001")
                    .build();
            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(null);

            ingestService.ingest(eslEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).insert(sessionCaptor.capture());
            assertThat(sessionCaptor.getValue().getDirection()).isEqualTo("INBOUND");
        }
    }

    // ========================================================================
    // 为 null / 缺少必要字段
    // ========================================================================

    @Nested
    @DisplayName("入参校验与忽略")
    class NullAndInvalidInput {

        @Test
        @DisplayName("ESL 事件为 null 时静默忽略")
        void shouldIgnoreNullEslEvent() {
            ingestService.ingest((NormalizedEslEvent) null);

            verify(eventMapper, never()).insert(any());
            verify(eventMapper, never()).updateById(any());
            verify(sessionMapper, never()).insert(any());
            verify(sessionMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("缺少 callId 的 ESL 事件被忽略")
        void shouldIgnoreEslEventWithoutCallId() {
            NormalizedEslEvent eslEvent = NormalizedEslEvent.builder()
                    .callId(null)
                    .eventType(EslEventType.CHANNEL_CREATE)
                    .build();

            ingestService.ingest(eslEvent);

            verify(eventMapper, never()).insert(any());
            verify(sessionMapper, never()).insert(any());
        }

        @Test
        @DisplayName("CallEvent 为 null 时静默忽略")
        void shouldIgnoreNullCallEvent() {
            ingestService.ingest((CallEvent) null);

            verify(eventMapper, never()).insert(any());
            verify(sessionMapper, never()).insert(any());
        }

        @Test
        @DisplayName("CallEvent 缺少 callId 时被忽略")
        void shouldIgnoreCallEventWithoutCallId() {
            CallEvent event = CallEvent.builder()
                    .eventType("CHANNEL_CREATE")
                    .eventTime(LocalDateTime.now())
                    .build();

            ingestService.ingest(event);

            verify(eventMapper, never()).insert(any());
            verify(sessionMapper, never()).insert(any());
        }
    }

    // ========================================================================
    // 终态会话乱序保护
    // ========================================================================

    @Nested
    @DisplayName("终态会话乱序事件保护")
    class TerminalStateOutOfOrder {

        @Test
        @DisplayName("终态会话不因乱序事件回滚")
        void ingestShouldNotRollbackTerminalSessionForOutOfOrderEvent() {
            CallSession endedSession = CallSession.builder()
                    .callId("biz-call-002")
                    .state(CallState.ENDED.name())
                    .freeswitchUuid("11111111-2222-3333-4444-555555555555")
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .endTime(LocalDateTime.of(2026, 6, 5, 10, 1))
                    .build();
            NormalizedEslEvent answerEvent = NormalizedEslEvent.builder()
                    .callId("biz-call-002")
                    .eventType(EslEventType.CHANNEL_ANSWER)
                    .eventTime(LocalDateTime.of(2026, 6, 5, 9, 59))
                    .freeswitchUuid("11111111-2222-3333-4444-555555555555")
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_ANSWER"))
                    .build();
            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(endedSession);

            ingestService.ingest(answerEvent);

            verify(sessionMapper, never()).updateById(any());
            verify(eventMapper).updateById(any(CallEvent.class));
        }

        @Test
        @DisplayName("FAILED 终态会话同样拒绝乱序事件")
        void shouldNotRollbackFailedSessionForOutOfOrderEvent() {
            CallSession failedSession = CallSession.builder()
                    .callId("biz-call-003")
                    .state(CallState.FAILED.name())
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .endTime(LocalDateTime.of(2026, 6, 5, 10, 1))
                    .build();
            NormalizedEslEvent answerEvent = NormalizedEslEvent.builder()
                    .callId("biz-call-003")
                    .eventType(EslEventType.CHANNEL_ANSWER)
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 2))
                    .freeswitchUuid("uuid-003")
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_ANSWER"))
                    .build();
            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(failedSession);

            ingestService.ingest(answerEvent);

            verify(sessionMapper, never()).updateById(any());
        }
    }

    // ========================================================================
    // 幂等与重复事件
    // ========================================================================

    @Nested
    @DisplayName("幂等与重复事件")
    class IdempotencyAndDuplicates {

        @Test
        @DisplayName("已处理的重复事件被跳过")
        void shouldSkipAlreadyProcessedDuplicate() {
            CallEvent duplicateEvent = CallEvent.builder()
                    .callId("call-001")
                    .eventType("CHANNEL_CREATE")
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .freeswitchUuid("uuid-001")
                    .dedupKey("call-001|CHANNEL_CREATE|uuid-001|2026-06-05T10:00")
                    .build();
            CallEvent existingProcessed = CallEvent.builder()
                    .callId("call-001")
                    .eventType("CHANNEL_CREATE")
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .freeswitchUuid("uuid-001")
                    .dedupKey("call-001|CHANNEL_CREATE|uuid-001|2026-06-05T10:00")
                    .processed(true)
                    .build();

            when(eventMapper.selectOne(any())).thenReturn(existingProcessed);

            ingestService.ingest(duplicateEvent);

            verify(eventMapper, never()).insert(any());
            verify(eventMapper, never()).updateById(any());
            verify(sessionMapper, never()).insert(any());
            verify(sessionMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("DuplicateKeyException 时重试查找并处理")
        void shouldHandleDuplicateKeyExceptionByRequerying() {
            CallEvent event = CallEvent.builder()
                    .callId("call-dup")
                    .eventType("CHANNEL_CREATE")
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .freeswitchUuid("uuid-dup")
                    .dedupKey("call-dup|CHANNEL_CREATE|uuid-dup|2026-06-05T10:00")
                    .build();
            CallEvent existingUnprocessed = CallEvent.builder()
                    .id(999L)
                    .callId("call-dup")
                    .eventType("CHANNEL_CREATE")
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .freeswitchUuid("uuid-dup")
                    .dedupKey("call-dup|CHANNEL_CREATE|uuid-dup|2026-06-05T10:00")
                    .processed(false)
                    .build();

            // selectOne: 第1次查不到 → insert 抛异常 → 第2次重试查到
            when(eventMapper.selectOne(any())).thenReturn(null, existingUnprocessed);
            when(eventMapper.insert(any(CallEvent.class)))
                    .thenThrow(new DuplicateKeyException("duplicate key"));
            when(sessionMapper.selectOne(any())).thenReturn(null);

            ingestService.ingest(event);

            // 最终应处理已有事件
            verify(eventMapper).updateById(any(CallEvent.class));
            verify(sessionMapper).insert(any(CallSession.class));
        }
    }

    // ========================================================================
    // HANGUP 事件处理
    // ========================================================================

    @Nested
    @DisplayName("挂断事件处理")
    class HangupEventHandling {

        @Test
        @DisplayName("CHANNEL_HANGUP 将状态转为 ENDED，设置 endTime 和 endCause")
        void shouldTransitionToEndedOnHangup() {
            CallSession session = CallSession.builder()
                    .callId("call-001")
                    .state(CallState.BRIDGED.name())
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .answerTime(LocalDateTime.of(2026, 6, 5, 10, 0, 10))
                    .build();
            LocalDateTime hangupTime = LocalDateTime.of(2026, 6, 5, 10, 5);
            NormalizedEslEvent hangupEvent = NormalizedEslEvent.builder()
                    .callId("call-001")
                    .eventType(EslEventType.CHANNEL_HANGUP)
                    .eventTime(hangupTime)
                    .freeswitchUuid("uuid-001")
                    .hangupCause("NORMAL_CLEARING")
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_HANGUP"))
                    .build();

            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(session);
            when(stateMachine.isValidTransition(CallState.BRIDGED, CallState.ENDED)).thenReturn(true);

            ingestService.ingest(hangupEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).updateById(sessionCaptor.capture());
            CallSession updated = sessionCaptor.getValue();
            assertThat(updated.getState()).isEqualTo(CallState.ENDED.name());
            assertThat(updated.getEndTime()).isEqualTo(hangupTime);
            assertThat(updated.getEndCause()).isEqualTo("NORMAL_CLEARING");
            assertThat(updated.getDurationSeconds()).isEqualTo(300);
        }
    }

    // ========================================================================
    // HOLD / UNHOLD 事件 (仅状态变化，不改时间)
    // ========================================================================

    @Nested
    @DisplayName("保持/取消保持事件")
    class HoldUnholdEventHandling {

        @Test
        @DisplayName("CHANNEL_HOLD 转换状态为 HOLD")
        void shouldTransitionToHoldOnHoldEvent() {
            CallSession session = CallSession.builder()
                    .callId("call-001")
                    .state(CallState.BRIDGED.name())
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .answerTime(LocalDateTime.of(2026, 6, 5, 10, 0, 10))
                    .build();
            NormalizedEslEvent holdEvent = NormalizedEslEvent.builder()
                    .callId("call-001")
                    .eventType(EslEventType.CHANNEL_HOLD)
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 2))
                    .freeswitchUuid("uuid-001")
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_HOLD"))
                    .build();

            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(session);
            when(stateMachine.isValidTransition(CallState.BRIDGED, CallState.HOLD)).thenReturn(true);

            ingestService.ingest(holdEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).updateById(sessionCaptor.capture());
            CallSession updated = sessionCaptor.getValue();
            assertThat(updated.getState()).isEqualTo(CallState.HOLD.name());
            // endTime 不应被设置
            assertThat(updated.getEndTime()).isNull();
        }
    }

    // ========================================================================
    // 非法状态转换
    // ========================================================================

    @Nested
    @DisplayName("非法状态转换")
    class InvalidStateTransitions {

        @Test
        @DisplayName("非法状态转换被忽略，session 状态不变")
        void shouldIgnoreInvalidTransition() {
            CallSession session = CallSession.builder()
                    .callId("call-001")
                    .state(CallState.BRIDGED.name())
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .build();
            // HOLD 事件从 BRIDGED 是合法的，改成 RINGING 试试
            NormalizedEslEvent createEvent = NormalizedEslEvent.builder()
                    .callId("call-001")
                    .eventType(EslEventType.CHANNEL_CREATE)
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 2))
                    .freeswitchUuid("uuid-001")
                    .rawHeaders(Map.of("Event-Name", "CHANNEL_CREATE"))
                    .build();

            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(session);
            when(stateMachine.isValidTransition(CallState.BRIDGED, CallState.RINGING)).thenReturn(false);

            ingestService.ingest(createEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).updateById(sessionCaptor.capture());
            // 状态不应被非法转换覆盖
            assertThat(sessionCaptor.getValue().getState()).isEqualTo(CallState.BRIDGED.name());
        }
    }

    // ========================================================================
    // 未知事件类型
    // ========================================================================

    @Nested
    @DisplayName("未知事件类型")
    class UnknownEventType {

        @Test
        @DisplayName("未映射的事件类型不改变状态，但更新 session 其他字段")
        void shouldNotChangeStateForUnknownEventType() {
            CallSession session = CallSession.builder()
                    .callId("call-001")
                    .state(CallState.BRIDGED.name())
                    .startTime(LocalDateTime.of(2026, 6, 5, 10, 0))
                    .freeswitchUuid(null)
                    .build();
            NormalizedEslEvent heartbeatEvent = NormalizedEslEvent.builder()
                    .callId("call-001")
                    .eventType(EslEventType.HEARTBEAT)
                    .eventTime(LocalDateTime.of(2026, 6, 5, 10, 1))
                    .freeswitchUuid("uuid-001")
                    .rawHeaders(Map.of("Event-Name", "HEARTBEAT"))
                    .build();

            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(session);

            ingestService.ingest(heartbeatEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).updateById(sessionCaptor.capture());
            CallSession updated = sessionCaptor.getValue();
            // 状态不变
            assertThat(updated.getState()).isEqualTo(CallState.BRIDGED.name());
            // 但 freeswitchUuid 被更新
            assertThat(updated.getFreeswitchUuid()).isEqualTo("uuid-001");
        }
    }

    // ========================================================================
    // 新会话创建时的方向推断
    // ========================================================================

    @Nested
    @DisplayName("新会话方向推断")
    class DirectionInference {

        @Test
        @DisplayName("channelVars 的 direction 为 outbound 时应大写")
        void shouldInferDirectionFromChannelVars() {
            LocalDateTime eventTime = LocalDateTime.of(2026, 6, 15, 10, 0);
            NormalizedEslEvent eslEvent = NormalizedEslEvent.builder()
                    .callId("call-outbound-001")
                    .eventType(EslEventType.CHANNEL_OUTGOING)
                    .eventTime(eventTime)
                    .freeswitchUuid("uuid-002")
                    .callerNumber("8001")
                    .calledNumber("13800138000")
                    .channelVars(Map.of("direction", "outbound"))
                    .build();
            when(eventMapper.selectOne(any())).thenReturn(null);
            when(sessionMapper.selectOne(any())).thenReturn(null);

            ingestService.ingest(eslEvent);

            ArgumentCaptor<CallSession> sessionCaptor = ArgumentCaptor.forClass(CallSession.class);
            verify(sessionMapper).insert(sessionCaptor.capture());
            assertThat(sessionCaptor.getValue().getDirection()).isEqualTo("OUTBOUND");
        }
    }
}
