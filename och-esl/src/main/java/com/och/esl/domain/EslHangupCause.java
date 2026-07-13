package com.och.esl.domain;

/**
 * FreeSWITCH 挂断原因枚举
 * <p>映射 FreeSWITCH 的 Hangup-Cause 字段为强类型枚举，
 * 替代 {@link NormalizedEslEvent} 中的 {@code String hangupCause} 字段。</p>
 *
 * <h3>常见原因分类</h3>
 * <ul>
 *   <li><b>正常结束</b>：NORMAL_CLEARING, NORMAL_UNSPECIFIED</li>
 *   <li><b>用户行为</b>：USER_BUSY, NO_ANSWER, NO_USER_RESPONSE, USER_NOT_REGISTERED</li>
 *   <li><b>网络/承载</b>：CALL_REJECTED, DESTINATION_OUT_OF_ORDER, RECOVERY_ON_TIMER_EXPIRE</li>
 *   <li><b>系统</b>：ORIGINATOR_CANCEL, LOSE_RACE, MANDATORY_IE_MISSING</li>
 * </ul>
 *
 * @author danmo
 */
public enum EslHangupCause {

    /* ── 正常 ── */
    NORMAL_CLEARING("正常挂断"),
    NORMAL_UNSPECIFIED("正常未指定"),

    /* ── 用户行为 ── */
    USER_BUSY("用户忙"),
    NO_ANSWER("无应答"),
    NO_USER_RESPONSE("无用户响应"),
    USER_NOT_REGISTERED("用户未注册"),

    /* ── 网络/承载 ── */
    CALL_REJECTED("呼叫被拒绝"),
    DESTINATION_OUT_OF_ORDER("目标不可达"),
    RECOVERY_ON_TIMER_EXPIRE("超时恢复"),

    /* ── 系统 ── */
    ORIGINATOR_CANCEL("主叫取消"),
    LOSE_RACE("竞争失败"),

    /** 未识别或自定义原因 */
    UNKNOWN("未知");

    private final String label;

    EslHangupCause(String label) { this.label = label; }
    public String getLabel() { return label; }

    /**
     * 从 FreeSWITCH 原始字符串解析挂断原因
     *
     * @param fsCause FreeSWITCH 挂断原因字符串
     * @return 对应的枚举值，无法匹配时返回 UNKNOWN
     */
    public static EslHangupCause fromFsString(String fsCause) {
        if (fsCause == null || fsCause.isEmpty()) return UNKNOWN;
        try {
            return valueOf(fsCause.replace("-", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /** 是否为正常挂断 */
    public boolean isNormalClearing() {
        return this == NORMAL_CLEARING || this == NORMAL_UNSPECIFIED;
    }
}
