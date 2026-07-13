package com.och.esl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标准化 ESL 事件
 * <p>将 FreeSWITCH 原始事件头（Map 形式）统一转换为强类型 POJO，
 * 包含通话标识、事件类型、时间戳、主被叫号码、DTMF、挂断原因、通道状态、SIP 信息等字段。
 * 下游模块（och-call 的 CallEventIngestService）直接消费此类进行状态转换。</p>
 *
 * <h3>关键字段映射</h3>
 * <ul>
 *   <li>callId          → Caller-Unique-ID（主叫方唯一ID）或 Unique-ID</li>
 *   <li>freeswitchUuid  → Unique-ID（FreeSWITCH 通道 UUID）</li>
 *   <li>callerNumber    → Caller-Caller-ID-Number（主叫号码）</li>
 *   <li>calledNumber    → Caller-Destination-Number（被叫号码）</li>
 *   <li>dtmfDigit       → DTMF-Digit（按键值）</li>
 *   <li>hangupCause     → Hangup-Cause（挂断原因，如 NORMAL_CLEARING）</li>
 *   <li>channelState    → Channel-State（通道状态 CS_NEW/CS_EXECUTE 等）</li>
 *   <li>sipCallId       → variable_sip_call_id（SIP 层的 Call-ID 头）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedEslEvent {

    /** 呼叫唯一标识（用于关联同一通话的多个通道） */
    private String callId;

    /** ESL 事件类型（强类型枚举，替代原始字符串） */
    private EslEventType eventType;

    /** 事件发生时间 */
    private LocalDateTime eventTime;

    /** FreeSWITCH 通道 UUID */
    private String freeswitchUuid;

    /** 通道腿标识，默认使用当前 FreeSWITCH 通道 UUID */
    private String legId;

    /** 对端通道 UUID，桥接、转接等多腿场景用于关联 B-leg */
    private String otherLegId;

    /** 主叫号码 */
    private String callerNumber;

    /** 被叫号码 */
    private String calledNumber;

    /** DTMF 按键值 */
    private String dtmfDigit;

    /** 挂断原因码（如 NORMAL_CLEARING、USER_BUSY、NO_ANSWER 等） */
    private String hangupCause;

    /** 通道状态（CS_NEW / CS_INIT / CS_ROUTING / CS_EXECUTE / CS_HANGUP 等） */
    private String channelState;

    /** 应答状态（如 answered、early_media、ringing） */
    private String answerState;

    /** SIP Call-ID（用于对接外部 SIP 系统或排查网络问题） */
    private String sipCallId;

    /** 通道变量列表（FreeSWITCH variable_* 前缀的变量） */
    private Map<String, String> channelVars;

    /** 原始事件头键值对（保留全部原始信息以便排查问题） */
    private Map<String, String> rawHeaders;

    // ═══════════════════ 类型安全的枚举转换方法 ═══════════════════

    /**
     * 获取事件类型枚举（类型安全，同 {@link #getEventType}）。
     * <p>保留此方法以兼容可能的外部调用方，内部建议直接使用 {@link #getEventType}。</p>
     *
     * @return 对应的 EslEventType
     */
    public EslEventType getEventTypeEnum() {
        return eventType != null ? eventType : EslEventType.CUSTOM;
    }

    /**
     * 获取通道状态枚举（类型安全）
     * @return 对应的 EslChannelState，无法解析时返回 CS_NONE
     */
    public EslChannelState getChannelStateEnum() {
        return EslChannelState.fromFsString(channelState);
    }

    /**
     * 获取挂断原因枚举（类型安全）
     * @return 对应的 EslHangupCause，无法解析时返回 UNKNOWN
     */
    public EslHangupCause getHangupCauseEnum() {
        return EslHangupCause.fromFsString(hangupCause);
    }

    /** 是否为通话开始事件（CREATE/ANSWER 等） */
    public boolean isCallStartEvent() {
        return eventType == EslEventType.CHANNEL_CREATE || eventType == EslEventType.CHANNEL_ANSWER;
    }

    /** 是否为通话结束事件（HANGUP/DESTROY） */
    public boolean isCallEndEvent() {
        return eventType == EslEventType.CHANNEL_HANGUP || eventType == EslEventType.CHANNEL_DESTROY;
    }
}
