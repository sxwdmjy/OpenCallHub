package com.och.call.application;

import com.och.call.domain.CallState;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 通话状态机
 * <p>
 * 定义通话的生命周期状态转换规则，确保状态流转的合法性。
 * 当收到事件或执行操作导致状态变更时，通过本状态机校验转换是否允许。
 * <p>
 * 设计说明：
 * <ul>
 *   <li>NEW - 初始状态，可转换为 RINGING(振铃)、IVR(IVR处理)、BRIDGED(直接通话)、ENDED(直接结束)、FAILED(失败)</li>
 *   <li>RINGING - 振铃中，可转换为 IVR、BRIDGED(接听)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>IVR - IVR处理中，可转换为 QUEUEING(排队)、VOICEMAIL(语音信箱)、BRIDGED(转接)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>QUEUEING - 排队中，可转换为 ASSIGNED(分配座席)、VOICEMAIL(转语音信箱)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>ASSIGNED - 已分配座席，可转换为 BRIDGED(接通)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>BRIDGED - 通话中，可转换为 HOLD(保持)、AFTER_CALL(后处理)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>HOLD - 保持中，可转换为 BRIDGED(恢复通话)、ENDED(挂断)、FAILED(失败)</li>
 *   <li>VOICEMAIL - 语音信箱，可转换为 ENDED(结束)、FAILED(失败)</li>
 *   <li>AFTER_CALL - 后处理，可转换为 ENDED(结束)、FAILED(失败)</li>
 *   <li>ENDED - 已结束，终态，不可转换</li>
 *   <li>FAILED - 已失败，终态，不可转换</li>
 * </ul>
 *
 * <p>注意：FreeSWITCH 事件时序中，CHANNEL_CREATE 后可能直接收到 CHANNEL_ANSWER（如呼出被立即接听），
 * 因此需要允许 NEW -> BRIDGED 的转换。同样，呼出时可能被立即挂断，因此允许 NEW -> ENDED。
 * IVR 处理过程中如果出现异常，也应允许 IVR -> FAILED 的转换。</p>
 */
@Component
public class CallStateMachine {

    private static final Map<CallState, Set<CallState>> TRANSITIONS = Map.ofEntries(
            Map.entry(CallState.NEW,          EnumSet.of(CallState.RINGING, CallState.IVR, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.RINGING,      EnumSet.of(CallState.IVR, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.IVR,          EnumSet.of(CallState.QUEUEING, CallState.VOICEMAIL, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.QUEUEING,     EnumSet.of(CallState.ASSIGNED, CallState.VOICEMAIL, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.ASSIGNED,     EnumSet.of(CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.BRIDGED,      EnumSet.of(CallState.HOLD, CallState.AFTER_CALL, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.HOLD,         EnumSet.of(CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.VOICEMAIL,    EnumSet.of(CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.AFTER_CALL,   EnumSet.of(CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.ENDED,        EnumSet.noneOf(CallState.class)),
            Map.entry(CallState.FAILED,       EnumSet.noneOf(CallState.class))
    );

    public boolean isValidTransition(CallState from, CallState to) {
        Set<CallState> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validate(CallState from, CallState to) {
        if (!isValidTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("Invalid call state transition: %s -> %s", from, to));
        }
    }
}
