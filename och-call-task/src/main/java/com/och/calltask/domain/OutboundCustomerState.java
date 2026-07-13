package com.och.calltask.domain;

/**
 * 外呼客户状态枚举
 * <ul>
 *   <li>IMPORTED - 已导入</li>
 *   <li>READY - 就绪（通过过滤）</li>
 *   <li>CLAIMED - 已被座席领取</li>
 *   <li>SCHEDULED - 已排程</li>
 *   <li>DIALING - 拨号中</li>
 *   <li>CONNECTED - 已接通</li>
 *   <li>RETRY_WAIT - 等待重试</li>
 *   <li>BLOCKED - 已拦截（DNC/时间窗）</li>
 *   <li>ABANDONED - 已放弃</li>
 *   <li>FINISHED - 已完成</li>
 * </ul>
 */
public enum OutboundCustomerState {
    IMPORTED, READY, CLAIMED, SCHEDULED, DIALING, CONNECTED,
    RETRY_WAIT, BLOCKED, ABANDONED, FINISHED;

    /** 是否为待处理状态 */
    public boolean isPending() { return this == READY || this == SCHEDULED; }
    /** 是否为活跃状态 */
    public boolean isActive() { return this == CLAIMED || this == DIALING || this == CONNECTED; }
}
