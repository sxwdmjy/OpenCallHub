package com.och.common.utils;

import com.och.common.trace.TraceContext;

/**
 * TraceId 工具类
 * <p>对 {@link com.och.common.trace.TraceContext} 的便捷静态方法封装，
 * 提供线程安全的方式来设置、获取和清除 traceId。</p>
 *
 * @author danmo
 */
public final class TraceUtil {

    private TraceUtil() {
    }

    /**
     * 设置当前线程的 traceId
     *
     * @param traceId 链路追踪ID
     */
    public static void setTraceId(String traceId) {
        TraceContext.setTraceId(traceId);
    }

    /**
     * 获取当前线程的 traceId
     *
     * @return traceId，可能为 null
     */
    public static String getTraceId() {
        return TraceContext.getTraceId();
    }

    /**
     * 清除当前线程的 traceId
     */
    public static void clear() {
        TraceContext.remove();
    }
}
