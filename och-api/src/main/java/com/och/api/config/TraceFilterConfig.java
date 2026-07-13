package com.och.api.config;

import com.och.common.trace.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TraceId 过滤器配置
 * <p>
 * TraceIdFilter 位于 och-common 公共模块，只提供过滤器行为，不在公共模块中直接注册为 Spring Bean。
 * 应用入口在这里显式注册过滤器，统一控制过滤器顺序，避免组件扫描和手动注册同时存在导致同一请求执行两次。
 * </p>
 *
 * @author danmo
 */
@Configuration
public class TraceFilterConfig {

    /**
     * 注册全链路 TraceId 过滤器
     * <p>
     * 使用最高优先级，确保请求进入认证、权限、业务 Controller 前已经完成 traceId 初始化。
     * </p>
     *
     * @return TraceId 过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE);
        registration.setName("traceIdFilter");
        return registration;
    }
}
