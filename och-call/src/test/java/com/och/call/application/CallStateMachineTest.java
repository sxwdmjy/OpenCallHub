package com.och.call.application;

import com.och.call.domain.CallState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 通话状态机单元测试，覆盖所有合法/非法转换以及边界条件。
 *
 * @author danmo
 */
@DisplayName("通话状态机测试")
class CallStateMachineTest {

    private final CallStateMachine stateMachine = new CallStateMachine();

    @Nested
    @DisplayName("正常入向通话转换链")
    class NormalInboundTransitions {

        @Test
        @DisplayName("NEW -> RINGING -> BRIDGED -> HOLD -> BRIDGED -> ENDED")
        void shouldAllowNormalInboundTransitions() {
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.RINGING)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.RINGING, CallState.BRIDGED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.HOLD)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.HOLD, CallState.BRIDGED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.ENDED)).isTrue();
        }
    }

    @Nested
    @DisplayName("终态不可再转换")
    class TerminalStatesRejectTransitions {

        @ParameterizedTest
        @EnumSource(value = CallState.class, names = {"ENDED", "FAILED"})
        @DisplayName("终态不能再转换到任何状态")
        void shouldRejectTransitionFromTerminalState(CallState terminalState) {
            for (CallState target : CallState.values()) {
                assertThat(stateMachine.isValidTransition(terminalState, target))
                        .as("从 %s 不应允许转换到 %s", terminalState, target)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("终态回滚到 BRIDGED 应抛出异常")
        void shouldThrowForTerminalRollback() {
            assertThat(stateMachine.isValidTransition(CallState.ENDED, CallState.BRIDGED)).isFalse();
            assertThatThrownBy(() -> stateMachine.validate(CallState.ENDED, CallState.BRIDGED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid call state transition");
        }
    }

    @Nested
    @DisplayName("自转换（同一状态）")
    class SelfTransitions {

        @ParameterizedTest
        @EnumSource(CallState.class)
        @DisplayName("任意状态到自身的转换无效")
        void shouldRejectSelfTransition(CallState state) {
            assertThat(stateMachine.isValidTransition(state, state))
                    .as("从 %s 到自身的转换应为无效", state)
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("NEW 状态允许的转换")
    class NewStateTransitions {

        @Test
        @DisplayName("NEW 可转换为 RINGING, IVR, BRIDGED, ENDED, FAILED")
        void shouldAllowNewToExpectedTargets() {
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.RINGING)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.IVR)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.BRIDGED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.ENDED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.FAILED)).isTrue();
        }

        @Test
        @DisplayName("NEW 不能直接到 HOLD 或 ASSIGNED")
        void shouldRejectNewToNonTarget() {
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.HOLD)).isFalse();
            assertThat(stateMachine.isValidTransition(CallState.NEW, CallState.ASSIGNED)).isFalse();
        }
    }

    @Nested
    @DisplayName("QUEUEING 排队状态转换")
    class QueueingTransitions {

        @Test
        @DisplayName("QUEUEING 可转换为 ASSIGNED, VOICEMAIL, ENDED, FAILED")
        void shouldAllowQueueingToExpectedTargets() {
            assertThat(stateMachine.isValidTransition(CallState.QUEUEING, CallState.ASSIGNED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.QUEUEING, CallState.VOICEMAIL)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.QUEUEING, CallState.ENDED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.QUEUEING, CallState.FAILED)).isTrue();
        }

        @Test
        @DisplayName("QUEUEING 不能直接到 BRIDGED（需先 ASSIGNED）")
        void shouldRejectQueueingToBridged() {
            assertThat(stateMachine.isValidTransition(CallState.QUEUEING, CallState.BRIDGED)).isFalse();
        }
    }

    @Nested
    @DisplayName("BRIDGED 通话中状态转换")
    class BridgedTransitions {

        @Test
        @DisplayName("BRIDGED 可转换为 HOLD, AFTER_CALL, ENDED, FAILED")
        void shouldAllowBridgedToExpectedTargets() {
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.HOLD)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.AFTER_CALL)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.ENDED)).isTrue();
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.FAILED)).isTrue();
        }

        @Test
        @DisplayName("BRIDGED 不能回到 RINGING")
        void shouldRejectBridgedToRinging() {
            assertThat(stateMachine.isValidTransition(CallState.BRIDGED, CallState.RINGING)).isFalse();
        }
    }

    @Nested
    @DisplayName("validate 校验方法")
    class ValidateMethod {

        @Test
        @DisplayName("合法转换不抛异常")
        void shouldNotThrowForValidTransition() {
            stateMachine.validate(CallState.ASSIGNED, CallState.BRIDGED);
        }

        @Test
        @DisplayName("非法转换抛出 IllegalStateException")
        void shouldThrowForInvalidTransition() {
            assertThatThrownBy(() -> stateMachine.validate(CallState.BRIDGED, CallState.RINGING))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid call state transition")
                    .hasMessageContaining("BRIDGED -> RINGING");
        }
    }
}
