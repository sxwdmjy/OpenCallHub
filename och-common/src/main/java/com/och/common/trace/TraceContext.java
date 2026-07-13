package com.och.common.trace;

/**
 * 链路追踪上下文
 * <p>基于 ThreadLocal 保存当前请求的 traceId，贯穿整个请求生命周期。
 * 由 {@link TraceIdFilter} 在请求进入时设置，请求结束后调用 remove() 清理，
 * 防止内存泄漏和线程池复用时数据串扰。</p>
 *
 * @author danmo
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceContext() {
    }

    /**
     * 设置当前线程的 traceId
     *
     * @param traceId 链路追踪ID
     */
    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    /**
     * 获取当前线程的 traceId
     *
     * @return traceId，可能为 null
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 移除当前线程的 traceId，防止内存泄漏
     */
    public static void remove() {
        TRACE_ID_HOLDER.remove();
    }
}
