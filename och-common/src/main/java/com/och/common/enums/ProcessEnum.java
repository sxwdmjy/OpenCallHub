package com.och.common.enums;

import lombok.Getter;

@Getter
public enum ProcessEnum {
    /**
     * 路由
     */
    CALL_ROUTE(),

    /**
     * 桥接
     */
    CALL_BRIDGE(),

    /**
     * 呼叫任务
     */
    CALL_TASK(),
}
