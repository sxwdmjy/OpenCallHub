package com.och.calltask.application;

import com.och.calltask.domain.OutboundTaskState;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 外呼任务状态机
 * <p>
 * 定义外呼任务的状态转换规则：
 * DRAFT -> READY / CANCELLED
 * READY -> RUNNING / CANCELLED
 * RUNNING <-> PAUSED
 * RUNNING -> COMPLETED / CANCELLED / FAILED
 * PAUSED -> CANCELLED
 * </p>
 */
@Component
public class OutboundTaskStateMachine {

    private static final Map<OutboundTaskState, Set<OutboundTaskState>> TRANSITIONS = Map.of(
        OutboundTaskState.DRAFT,     EnumSet.of(OutboundTaskState.READY, OutboundTaskState.CANCELLED),
        OutboundTaskState.READY,     EnumSet.of(OutboundTaskState.RUNNING, OutboundTaskState.CANCELLED),
        OutboundTaskState.RUNNING,   EnumSet.of(OutboundTaskState.PAUSED, OutboundTaskState.COMPLETED, OutboundTaskState.CANCELLED, OutboundTaskState.FAILED),
        OutboundTaskState.PAUSED,    EnumSet.of(OutboundTaskState.RUNNING, OutboundTaskState.CANCELLED)
    );

    public void validate(OutboundTaskState from, OutboundTaskState to) {
        Set<OutboundTaskState> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BizException(ErrorCode.BIZ_ILLEGAL_STATE,
                    String.format("非法外呼任务状态流转: %s -> %s", from, to));
        }
    }
}
