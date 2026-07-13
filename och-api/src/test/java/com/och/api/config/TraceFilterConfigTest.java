package com.och.api.config;

import com.och.common.trace.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TraceFilterConfig 配置测试
 * <p>
 * TraceIdFilter 由 API 入口统一注册，公共模块只保留过滤器行为。
 * 这里固定注册路径和优先级，防止后续重构时重新出现自动注册与手动注册并存的问题。
 * </p>
 *
 * @author danmo
 */
class TraceFilterConfigTest {

    @Test
    void traceIdFilterRegistrationUsesSingleExplicitEntry() {
        TraceFilterConfig config = new TraceFilterConfig();

        FilterRegistrationBean<TraceIdFilter> registration = config.traceIdFilterRegistration();

        assertInstanceOf(TraceIdFilter.class, registration.getFilter());
        assertEquals(Integer.MIN_VALUE, registration.getOrder());
        assertTrue(registration.getUrlPatterns().contains("/*"));
    }
}
