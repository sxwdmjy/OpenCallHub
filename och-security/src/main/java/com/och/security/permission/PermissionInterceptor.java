package com.och.security.permission;

import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器
 * <p>
 * 在请求进入 Controller 方法之前，检查方法上标注的 {@link RequiresPermission} 注解，
 * 并通过 {@link PermissionContext#hasPermission(String)} 校验当前用户是否拥有所需权限。
 * 若无权限则抛出 {@link BizException}。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (requiresPermission == null) {
            requiresPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }

        if (requiresPermission != null) {
            String permission = requiresPermission.value();
            if (!PermissionContext.hasPermission(permission)) {
                log.warn("[PermissionInterceptor] Access denied: uri={}, requiredPermission={}",
                        request.getRequestURI(), permission);
                throw new BizException(ErrorCode.FORBIDDEN, "无访问权限: " + requiresPermission.description());
            }
        }

        return true;
    }
}
