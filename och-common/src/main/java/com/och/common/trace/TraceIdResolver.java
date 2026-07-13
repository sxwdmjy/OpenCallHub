package com.och.common.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TraceId 解析器
 * <p>
 * 统一处理 HTTP 请求中的 traceId 读取、复用和响应头写回。
 * 项目中同时存在 Servlet Filter 和旧版 MVC Interceptor 两类入口，
 * 如果各自独立生成 traceId，同一次请求在日志、响应和异步事件中会出现不同链路号。
 * 因此这里集中封装解析规则，保证所有入口使用同一套 traceId 语义。
 * </p>
 *
 * @author danmo
 */
public final class TraceIdResolver {

    /**
     * 标准链路追踪请求头
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 历史代码中使用过的请求头名称，HTTP Header 本身大小写不敏感，这里保留常量用于兼容旧代码语义。
     */
    public static final String LEGACY_TRACE_ID_HEADER = "X-Trace-ID";

    private TraceIdResolver() {
    }

    /**
     * 解析当前请求的 traceId
     * <p>
     * 优先使用前端或网关传入的 traceId；如果当前线程已经存在 traceId，说明前置过滤器已经完成解析，
     * 此时直接复用，避免后续拦截器重复生成；如果两者都不存在，则生成新的 traceId。
     * </p>
     *
     * @param request HTTP 请求
     * @return 当前请求应使用的 traceId
     */
    public static String resolve(HttpServletRequest request) {
        String traceId = getHeader(request, TRACE_ID_HEADER);
        if (isBlank(traceId)) {
            traceId = getHeader(request, LEGACY_TRACE_ID_HEADER);
        }
        if (isBlank(traceId)) {
            traceId = TraceContext.getTraceId();
        }
        if (isBlank(traceId)) {
            traceId = TraceIdGenerator.generate();
        }
        return traceId.trim();
    }

    /**
     * 将 traceId 写入响应头
     * <p>
     * 响应统一写标准头名，HTTP Header 大小写不敏感，旧客户端按历史头名读取仍可兼容。
     * </p>
     *
     * @param response HTTP 响应
     * @param traceId  当前请求 traceId
     */
    public static void write(HttpServletResponse response, String traceId) {
        if (response == null || isBlank(traceId)) {
            return;
        }
        response.setHeader(TRACE_ID_HEADER, traceId.trim());
    }

    private static String getHeader(HttpServletRequest request, String headerName) {
        if (request == null) {
            return null;
        }
        return request.getHeader(headerName);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
