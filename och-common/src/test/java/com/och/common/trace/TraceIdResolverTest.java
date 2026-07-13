package com.och.common.trace;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TraceId 公共链路测试
 * <p>
 * 覆盖 traceId 的请求头复用、历史请求头兼容、线程上下文复用、自动生成和过滤器清理行为。
 * 这些规则是后续呼叫、IVR、外呼、AI、报表等模块串联日志和事件的基础，
 * 一旦出现同一请求多个 traceId，会直接影响问题追踪和故障恢复。
 * </p>
 *
 * @author danmo
 */
class TraceIdResolverTest {

    @AfterEach
    void clearTraceContext() {
        TraceContext.remove();
    }

    @Test
    void resolveUsesRequestHeaderTraceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdResolver.TRACE_ID_HEADER, "trace-from-gateway");

        String traceId = TraceIdResolver.resolve(request);

        assertEquals("trace-from-gateway", traceId);
    }

    @Test
    void resolveUsesLegacyRequestHeaderTraceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdResolver.LEGACY_TRACE_ID_HEADER, "trace-from-old-client");

        String traceId = TraceIdResolver.resolve(request);

        assertEquals("trace-from-old-client", traceId);
    }

    @Test
    void resolveUsesCurrentContextWhenHeaderIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        TraceContext.setTraceId("trace-from-filter");

        String traceId = TraceIdResolver.resolve(request);

        assertEquals("trace-from-filter", traceId);
    }

    @Test
    void resolveGeneratesTraceIdWhenHeaderAndContextAreMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String traceId = TraceIdResolver.resolve(request);

        assertFalse(traceId.isBlank());
        assertEquals(32, traceId.length());
    }

    @Test
    void writeUsesUnifiedResponseHeader() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        TraceIdResolver.write(response, "trace-from-response");

        assertEquals("trace-from-response", response.getHeader(TraceIdResolver.TRACE_ID_HEADER));
    }

    @Test
    void filterWritesTraceIdAndClearsContextAfterRequest() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(TraceIdResolver.TRACE_ID_HEADER, "trace-from-request");

        FilterChain chain = (servletRequest, servletResponse) ->
                assertEquals("trace-from-request", TraceContext.getTraceId());

        filter.doFilter(request, response, chain);

        assertEquals("trace-from-request", response.getHeader(TraceIdResolver.TRACE_ID_HEADER));
        assertNull(TraceContext.getTraceId());
    }
}
