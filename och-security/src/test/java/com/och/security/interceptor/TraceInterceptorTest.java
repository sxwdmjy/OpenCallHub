package com.och.security.interceptor;

import com.och.common.trace.TraceIdResolver;
import com.och.common.utils.TraceUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TraceInterceptor 兼容性测试
 * <p>
 * 旧工程中 MVC 拦截器可能和新的 TraceIdFilter 同时存在。
 * 这里重点验证拦截器只在自己兜底创建 traceId 时清理上下文，
 * 如果 traceId 已经由前置过滤器创建，则保持上下文交给过滤器统一收尾。
 * </p>
 *
 * @author danmo
 */
class TraceInterceptorTest {

    private final TraceInterceptor traceInterceptor = new TraceInterceptor();

    @AfterEach
    void clearTraceContext() {
        TraceUtil.clear();
    }

    @Test
    void preHandleReusesTraceContextCreatedByFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        TraceUtil.setTraceId("trace-from-filter");

        traceInterceptor.preHandle(request, response, new Object());
        traceInterceptor.afterCompletion(request, response, new Object(), null);

        assertEquals("trace-from-filter", response.getHeader(TraceIdResolver.TRACE_ID_HEADER));
        assertEquals("trace-from-filter", TraceUtil.getTraceId());
    }

    @Test
    void afterCompletionClearsTraceWhenInterceptorCreatesContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        traceInterceptor.preHandle(request, response, new Object());
        String traceId = TraceUtil.getTraceId();
        traceInterceptor.afterCompletion(request, response, new Object(), null);

        assertFalse(traceId.isBlank());
        assertEquals(traceId, response.getHeader(TraceIdResolver.TRACE_ID_HEADER));
        assertNull(TraceUtil.getTraceId());
    }

    @Test
    void preHandleUsesInboundTraceHeaderWhenNoFilterContextExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(TraceIdResolver.TRACE_ID_HEADER, "trace-from-gateway");

        traceInterceptor.preHandle(request, response, new Object());
        traceInterceptor.afterCompletion(request, response, new Object(), null);

        assertEquals("trace-from-gateway", response.getHeader(TraceIdResolver.TRACE_ID_HEADER));
        assertNull(TraceUtil.getTraceId());
    }
}
