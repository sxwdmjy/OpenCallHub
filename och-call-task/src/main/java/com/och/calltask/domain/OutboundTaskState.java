package com.och.calltask.domain;

/**
 * 外呼任务状态枚举
 * <ul>
 *   <li>DRAFT - 草稿</li>
 *   <li>READY - 就绪（已提交待启动）</li>
 *   <li>RUNNING - 运行中</li>
 *   <li>PAUSED - 暂停</li>
 *   <li>COMPLETED - 已完成</li>
 *   <li>CANCELLED - 已取消</li>
 *   <li>FAILED - 失败</li>
 * </ul>
 */
public enum OutboundTaskState {
    DRAFT, READY, RUNNING, PAUSED, COMPLETED, CANCELLED, FAILED;

    /** 是否为终态 */
    public boolean isTerminal() { return this == COMPLETED || this == CANCELLED || this == FAILED; }
    /** 是否为活跃状态（运行中或暂停） */
    public boolean isActive() { return this == RUNNING || this == PAUSED; }
}
