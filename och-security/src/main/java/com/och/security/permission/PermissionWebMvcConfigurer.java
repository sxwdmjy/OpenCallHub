package com.och.security.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限拦截器 Web MVC 配置
 * <p>将 {@link PermissionInterceptor} 注册到 Spring MVC 拦截器链中，
 * 拦截 /api/** 路径下除 /api/auth/** 和 /api/public/** 外的所有请求。</p>
 *
 * @author danmo
 */
@Configuration
@RequiredArgsConstructor
public class PermissionWebMvcConfigurer implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/public/**");
    }
}
