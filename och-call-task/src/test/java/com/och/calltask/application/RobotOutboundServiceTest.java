package com.och.calltask.application;

import com.och.ai.service.impl.RobotFlowEngineImpl;
import com.och.ai.domain.RobotSession;
import com.och.ai.mapper.RobotSessionMapper;
import com.och.calltask.domain.*;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.calltask.mapper.OutboundDialAttemptMapper;
import com.och.calltask.service.impl.RobotOutboundServiceImpl;
import com.och.common.error.BizException;
import org.springframework.test.util.ReflectionTestUtils;
import com.och.common.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RobotOutboundService}.
 * <p>
 * Covers startRobotCall, handleRobotResponse, and onRobotComplete with various DialResult paths.
 * Uses real {@link OutboundCustomerStateMachine} for accurate state transition validation.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class RobotOutboundServiceTest {

    @Mock
    private OutboundCustomerMapper customerMapper;

    @Mock
    private OutboundDialAttemptMapper attemptMapper;

    @Mock
    private RobotFlowEngineImpl robotFlowEngine;

    @Mock
    private RobotSessionMapper robotSessionMapper;

    private OutboundCustomerStateMachine stateMachine;
    private RobotOutboundServiceImpl robotOutboundService;

    @BeforeEach
    void setUp() {
        stateMachine = new OutboundCustomerStateMachine();
        robotOutboundService = new RobotOutboundServiceImpl(
                attemptMapper, stateMachine, robotFlowEngine, robotSessionMapper);
        ReflectionTestUtils.setField(robotOutboundService, "baseMapper", customerMapper);
    }

    // ======================== startRobotCall ========================

    @Test
    void startRobotCall_shouldCreateSessionAndTransitionToDialing() {
        Long taskId = 10L;
        Long customerId = 1L;
        OutboundCustomer customer = buildCustomer(customerId, taskId, OutboundCustomerState.SCHEDULED);
        RobotSession robotSession = new RobotSession();
        robotSession.setSessionId("session-12345");

        when(customerMapper.selectById(customerId)).thenReturn(customer);
        when(robotFlowEngine.startSession(eq("robot_" + taskId + ":" + customerId + ":1"), eq(100L), eq(200L)))
                .thenReturn(robotSession);

        String sessionId = robotOutboundService.startRobotCall(taskId, customerId, "INTERNAL", 100L, 200L, "system prompt");

        assertThat(sessionId).isEqualTo("session-12345");
        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.DIALING.name());
        assertThat(customer.getAttemptCount()).isEqualTo(1);
        assertThat(customer.getRobotSessionId()).isEqualTo("session-12345");

        verify(customerMapper, atLeastOnce()).updateById(customer);
        verify(attemptMapper).insert(any(OutboundDialAttempt.class));
    }

    @Test
    void startRobotCall_shouldIncrementAttemptCount() {
        Long taskId = 10L;
        Long customerId = 1L;
        OutboundCustomer customer = buildCustomer(customerId, taskId, OutboundCustomerState.SCHEDULED);
        customer.setAttemptCount(2);
        RobotSession robotSession = new RobotSession();
        robotSession.setSessionId("session-abc");

        when(customerMapper.selectById(customerId)).thenReturn(customer);
        when(robotFlowEngine.startSession(any(), any(), any())).thenReturn(robotSession);

        robotOutboundService.startRobotCall(taskId, customerId, "INTERNAL", 100L, 200L, "system prompt");

        assertThat(customer.getAttemptCount()).isEqualTo(3);
    }

    @Test
    void startRobotCall_shouldThrowWhenCustomerNotFound() {
        when(customerMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() ->
                robotOutboundService.startRobotCall(10L, 1L, "INTERNAL", 100L, 200L, "prompt"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.NOT_FOUND.getCode());
    }

    @Test
    void startRobotCall_shouldThrowWhenTaskIdMismatch() {
        OutboundCustomer customer = buildCustomer(1L, 99L, OutboundCustomerState.SCHEDULED);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        assertThatThrownBy(() ->
                robotOutboundService.startRobotCall(10L, 1L, "INTERNAL", 100L, 200L, "prompt"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.NOT_FOUND.getCode());
    }

    @Test
    void startRobotCall_shouldThrowWhenStateTransitionInvalid() {
        OutboundCustomer customer = buildCustomer(1L, 10L, OutboundCustomerState.FINISHED);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        assertThatThrownBy(() ->
                robotOutboundService.startRobotCall(10L, 1L, "INTERNAL", 100L, 200L, "prompt"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.BIZ_ILLEGAL_STATE.getCode());
    }

    // ======================== handleRobotResponse ========================

    @Test
    void handleRobotResponse_shouldDelegateToRobotFlowEngine() {
        RobotFlowEngineImpl.TurnResult expected = new RobotFlowEngineImpl.TurnResult("回复文本", "QUERY_PRICE", false, 150);
        when(robotFlowEngine.processTurn("session-1", "用户输入", "INTERNAL", "系统提示"))
                .thenReturn(expected);

        RobotFlowEngineImpl.TurnResult result = robotOutboundService.handleRobotResponse(
                "session-1", "用户输入", "INTERNAL", "系统提示");

        assertThat(result.text()).isEqualTo("回复文本");
        assertThat(result.intent()).isEqualTo("QUERY_PRICE");
        assertThat(result.needTransfer()).isFalse();
        assertThat(result.latencyMs()).isEqualTo(150);
    }

    // ======================== onRobotComplete ========================

    @Test
    void onRobotComplete_withConnectedResult_shouldTransitionDialingToConnected() {
        OutboundCustomer customer = buildCustomer(2L, 10L, OutboundCustomerState.DIALING);
        customer.setRobotSessionId("session-xyz");
        OutboundDialAttempt attempt = buildAttempt(1L, 10L, 2L, "call-xyz");
        RobotSession session = new RobotSession();
        session.setSessionId("session-xyz");
        session.setCallId("call-xyz");

        when(customerMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(customer);
        when(robotSessionMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(session);
        when(attemptMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(attempt);

        robotOutboundService.onRobotComplete("session-xyz", DialResult.CONNECTED);

        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.CONNECTED.name());
        verify(customerMapper).updateById(customer);
        verify(robotFlowEngine).endSession("session-xyz", "CONNECTED");
        verify(attemptMapper).updateById(attempt);
        assertThat(attempt.getState()).isEqualTo("COMPLETED");
    }

    @Test
    void onRobotComplete_withRobotHandoff_shouldTransitionToReady() {
        // Use RETRY_WAIT which has a valid transition to READY
        OutboundCustomer customer = buildCustomer(2L, 10L, OutboundCustomerState.RETRY_WAIT);
        customer.setRobotSessionId("session-abc");
        OutboundDialAttempt attempt = buildAttempt(1L, 10L, 2L, "call-abc");
        RobotSession session = new RobotSession();
        session.setSessionId("session-abc");
        session.setCallId("call-abc");

        when(customerMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(customer);
        when(robotSessionMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(session);
        when(attemptMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(attempt);

        robotOutboundService.onRobotComplete("session-abc", DialResult.ROBOT_HANDOFF);

        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.READY.name());
        verify(customerMapper).updateById(customer);
        verify(robotFlowEngine).endSession("session-abc", "ROBOT_HANDOFF");
        verify(attemptMapper).updateById(attempt);
    }

    @Test
    void onRobotComplete_withOtherResultAndConnectedState_shouldTransitionToFinished() {
        OutboundCustomer customer = buildCustomer(2L, 10L, OutboundCustomerState.CONNECTED);
        customer.setRobotSessionId("session-def");
        OutboundDialAttempt attempt = buildAttempt(1L, 10L, 2L, "call-def");
        RobotSession session = new RobotSession();
        session.setSessionId("session-def");
        session.setCallId("call-def");

        when(customerMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(customer);
        when(robotSessionMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(session);
        when(attemptMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(attempt);

        robotOutboundService.onRobotComplete("session-def", DialResult.NO_ANSWER);

        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.FINISHED.name());
        verify(customerMapper).updateById(customer);
        verify(robotFlowEngine).endSession("session-def", "NO_ANSWER");
        verify(attemptMapper).updateById(attempt);
    }

    @Test
    void onRobotComplete_withConnectedResultAndNonDialingState_shouldNotChangeState() {
        // Customer is already CONNECTED; CONNECTED result only transitions from DIALING
        OutboundCustomer customer = buildCustomer(2L, 10L, OutboundCustomerState.CONNECTED);
        customer.setRobotSessionId("session-ghi");
        OutboundDialAttempt attempt = buildAttempt(1L, 10L, 2L, "call-ghi");
        RobotSession session = new RobotSession();
        session.setSessionId("session-ghi");
        session.setCallId("call-ghi");

        when(customerMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(customer);
        when(robotSessionMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(session);
        when(attemptMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(attempt);

        robotOutboundService.onRobotComplete("session-ghi", DialResult.CONNECTED);

        // State stays CONNECTED — the CONNECTED result only transitions DIALING -> CONNECTED
        assertThat(customer.getState()).isEqualTo(OutboundCustomerState.CONNECTED.name());
        // But dial attempt should still be updated
        verify(attemptMapper).updateById(attempt);
    }

    @Test
    void onRobotComplete_shouldHandleCustomerNotFound() {
        when(customerMapper.selectOne(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(null);

        // Should not throw
        robotOutboundService.onRobotComplete("session-unknown", DialResult.CONNECTED);

        verify(robotFlowEngine).endSession("session-unknown", "CONNECTED");
        verify(customerMapper, never()).updateById(any());
    }

    // ======================== helper ========================

    private OutboundCustomer buildCustomer(Long id, Long taskId, OutboundCustomerState state) {
        return OutboundCustomer.builder()
                .id(id)
                .taskId(taskId)
                .phoneNumber("13800138000")
                .customerName("Customer-" + id)
                .state(state.name())
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private OutboundDialAttempt buildAttempt(Long id, Long taskId, Long customerId, String callId) {
        return OutboundDialAttempt.builder()
                .id(id)
                .taskId(taskId)
                .customerId(customerId)
                .attemptNo(1)
                .dedupKey(taskId + ":" + customerId + ":1")
                .callId(callId)
                .state("DIALING")
                .dialTime(LocalDateTime.now())
                .build();
    }
}
