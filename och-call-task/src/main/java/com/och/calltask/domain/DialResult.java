package com.och.calltask.domain;

/**
 * 拨号结果枚举
 * <ul>
 *   <li>CONNECTED - 已接通</li>
 *   <li>NO_ANSWER - 无应答</li>
 *   <li>BUSY - 占线</li>
 *   <li>FAILED - 失败</li>
 *   <li>ABANDONED - 放弃</li>
 *   <li>DNC_DETECTED - 检测到请勿呼叫</li>
 *   <li>BLACKLIST_DETECTED - 检测到黑名单</li>
 *   <li>ROBOT_HANDOFF - 机器人转人工</li>
 * </ul>
 */
public enum DialResult {
    CONNECTED, NO_ANSWER, BUSY, FAILED, ABANDONED, DNC_DETECTED, BLACKLIST_DETECTED, ROBOT_HANDOFF
}
