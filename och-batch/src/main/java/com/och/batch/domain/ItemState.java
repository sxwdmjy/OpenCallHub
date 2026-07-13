package com.och.batch.domain;

/**
 * 批处理条目状态枚举。
 */
public enum ItemState {
    PENDING,
    SUCCESS,
    FAILED,
    FAILED_RETRY
}
