package com.och.security.interceptor;


import cn.hutool.core.util.IdUtil;
import com.och.common.utils.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 链路ID拦截器
 *
 * @author danmo
 * @date 2025/7/2 16:26
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = IdUtil.randomUUID();
        TraceUtil.setTraceId(traceId);
        response.setHeader("X-Trace-ID", traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TraceUtil.clear();
    }
}
