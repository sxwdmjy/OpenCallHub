package com.och.security.interceptor;


import com.och.common.trace.TraceIdResolver;
import com.och.common.utils.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 链路ID拦截器
 * <p>
 * 兼容旧版 MVC 拦截器链路。项目新底座已经通过 {@link com.och.common.trace.TraceIdFilter}
 * 在请求最前置位置初始化 traceId，但部分历史配置仍可能继续注册拦截器。
 * 这里统一委托 {@link TraceIdResolver} 解析 traceId，避免过滤器和拦截器重复生成不同链路号。
 * </p>
 *
 * @author danmo
 * @date 2025/7/2 16:26
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    /**
     * 标记当前 traceId 是否由拦截器创建。
     * <p>
     * 如果前置过滤器已经创建 traceId，拦截器只复用，不在 afterCompletion 中清理，
     * 避免影响过滤器链后置日志；如果没有前置过滤器，则由拦截器兜底创建并负责清理。
     * </p>
     */
    private static final String TRACE_CREATED_BY_INTERCEPTOR = TraceInterceptor.class.getName() + ".created";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean createdByInterceptor = TraceUtil.getTraceId() == null || TraceUtil.getTraceId().trim().isEmpty();
        String traceId = TraceIdResolver.resolve(request);
        TraceUtil.setTraceId(traceId);
        TraceIdResolver.write(response, traceId);
        request.setAttribute(TRACE_CREATED_BY_INTERCEPTOR, createdByInterceptor);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (Boolean.TRUE.equals(request.getAttribute(TRACE_CREATED_BY_INTERCEPTOR))) {
            TraceUtil.clear();
        }
    }
}
