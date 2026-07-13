package com.och.common.trace;

import java.util.UUID;

/**
 * TraceId 生成器
 * <p>使用 UUID 去除连字符后的 32 位字符串作为全局唯一的链路追踪ID。</p>
 *
 * @author danmo
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    /**
     * 生成一个新的 traceId
     *
     * @return 去除了连字符的 UUID 字符串
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
