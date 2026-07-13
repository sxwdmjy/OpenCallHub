package com.och.common.trace;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 全链路 TraceId 过滤器
 * <p>
 * 在每个 HTTP 请求进入系统时初始化 {@link TraceContext}，并在响应头中写回 traceId，
 * 方便前端、网关、日志和异步事件使用同一个链路号排查问题。
 * </p>
 * <p>
 * traceId 的读取、复用和生成规则统一委托给 {@link TraceIdResolver}，
 * 避免过滤器、拦截器、后续模块各自生成不同 traceId。
 * </p>
 * <p>
 * 本类只负责过滤器行为本身，不直接声明 Spring Bean。
 * 具体注册顺序由应用入口模块统一配置，避免公共模块被组件扫描后和显式注册产生重复过滤器。
 * </p>
 *
 * @author danmo
 */
public class TraceIdFilter implements Filter {

    /**
     * 保留旧常量入口，避免历史代码直接引用过滤器常量时编译失败。
     */
    public static final String TRACE_ID_HEADER = TraceIdResolver.TRACE_ID_HEADER;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = TraceIdResolver.resolve(httpRequest);
        TraceContext.setTraceId(traceId);
        TraceIdResolver.write(httpResponse, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            TraceContext.remove();
        }
    }
}
