package com.och.acd.application;

import com.och.acd.domain.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 座席状态机（状态模式）
 * <p>
 * 定义座席的工作状态转换规则，确保状态流转的合法性。
 * </p>
 *
 * <h3>转换规则</h3>
 * <pre>
 * OFFLINE    → IDLE
 * IDLE       → BUSY | BREAK | TRAINING | OFFLINE
 * BUSY       → AFTER_CALL | IDLE
 * AFTER_CALL → IDLE | BUSY
 * BREAK      → IDLE | OFFLINE
 * TRAINING   → IDLE | OFFLINE
 * </pre>
 *
 * @author danmo
 */
@Slf4j
@Component
public class AgentStateMachine {

    private static final Map<AgentState, Set<AgentState>> TRANSITIONS = Map.ofEntries(
            Map.entry(AgentState.OFFLINE,     EnumSet.of(AgentState.IDLE)),
            Map.entry(AgentState.IDLE,        EnumSet.of(AgentState.BUSY, AgentState.BREAK, AgentState.TRAINING, AgentState.OFFLINE)),
            Map.entry(AgentState.BUSY,        EnumSet.of(AgentState.AFTER_CALL, AgentState.IDLE)),
            Map.entry(AgentState.AFTER_CALL,  EnumSet.of(AgentState.IDLE, AgentState.BUSY)),
            Map.entry(AgentState.BREAK,       EnumSet.of(AgentState.IDLE, AgentState.OFFLINE)),
            Map.entry(AgentState.TRAINING,    EnumSet.of(AgentState.IDLE, AgentState.OFFLINE))
    );

    public void validate(AgentState from, AgentState to) {
        Set<AgentState> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            String msg = String.format("非法座席状态转换: %s -> %s", from.getLabel(), to.getLabel());
            log.warn("[AgentStateMachine] {}", msg);
            throw new IllegalStateException(msg);
        }
        log.debug("[AgentStateMachine] {} -> {}", from.getLabel(), to.getLabel());
    }

    public boolean isReceivable(AgentState state) {
        return state == AgentState.IDLE;
    }

    public boolean canSignOut(AgentState state) {
        return state == AgentState.IDLE || state == AgentState.BREAK || state == AgentState.TRAINING;
    }
}