package com.och.calltask.application;

import com.och.calltask.domain.OutboundCustomerState;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 外呼客户状态机
 * <p>
 * 定义外呼客户的状态转换规则：
 * IMPORTED -> READY / BLOCKED
 * READY -> CLAIMED / SCHEDULED / DIALING
 * CLAIMED -> READY / DIALING
 * SCHEDULED -> DIALING
 * DIALING -> CONNECTED / RETRY_WAIT / BLOCKED / READY
 * CONNECTED -> FINISHED
 * RETRY_WAIT -> READY / FINISHED
 * </p>
 */
@Component
public class OutboundCustomerStateMachine {

    private static final Map<OutboundCustomerState, Set<OutboundCustomerState>> TRANSITIONS = Map.ofEntries(
        Map.entry(OutboundCustomerState.IMPORTED,  EnumSet.of(OutboundCustomerState.READY, OutboundCustomerState.BLOCKED)),
        Map.entry(OutboundCustomerState.READY,     EnumSet.of(OutboundCustomerState.CLAIMED, OutboundCustomerState.SCHEDULED, OutboundCustomerState.DIALING)),
        Map.entry(OutboundCustomerState.CLAIMED,   EnumSet.of(OutboundCustomerState.READY, OutboundCustomerState.DIALING)),
        Map.entry(OutboundCustomerState.SCHEDULED, EnumSet.of(OutboundCustomerState.DIALING)),
        Map.entry(OutboundCustomerState.DIALING,   EnumSet.of(OutboundCustomerState.CONNECTED, OutboundCustomerState.RETRY_WAIT, OutboundCustomerState.BLOCKED, OutboundCustomerState.READY)),
        Map.entry(OutboundCustomerState.CONNECTED, EnumSet.of(OutboundCustomerState.FINISHED)),
        Map.entry(OutboundCustomerState.RETRY_WAIT, EnumSet.of(OutboundCustomerState.READY, OutboundCustomerState.FINISHED))
    );

    public void validate(OutboundCustomerState from, OutboundCustomerState to) {
        Set<OutboundCustomerState> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BizException(ErrorCode.BIZ_ILLEGAL_STATE,
                    String.format("非法外呼客户状态流转: %s -> %s", from, to));
        }
    }
}
