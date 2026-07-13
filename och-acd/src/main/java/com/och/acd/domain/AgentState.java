package com.och.acd.domain;

/**
 * 座席状态枚举
 * <ul>
 *   <li>OFFLINE - 离线</li>
 *   <li>IDLE - 空闲（可接听新呼叫）</li>
 *   <li>BUSY - 忙碌（通话中）</li>
 *   <li>AFTER_CALL - 后处理（通话结束后的小结状态）</li>
 *   <li>BREAK - 休息</li>
 *   <li>TRAINING - 培训</li>
 * </ul>
 */
public enum AgentState {

    OFFLINE("离线"),
    IDLE("空闲"),
    BUSY("忙碌"),
    AFTER_CALL("后处理"),
    BREAK("休息"),
    TRAINING("培训");

    private final String label;

    AgentState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAvailable() {
        return this == IDLE;
    }
}
