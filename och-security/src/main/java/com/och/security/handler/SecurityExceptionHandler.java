package com.och.security.handler;

import com.och.common.api.ApiResponse;
import com.och.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 安全模块异常处理器
 * <p>
 * 统一拦截认证和授权异常，返回标准 ApiResponse 格式。
 * 通用业务异常、参数校验异常和 HTTP 层异常由 och-common 的 GlobalExceptionHandler 统一处理。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ResponseEntity.status(403).body(ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), "没有权限，请联系管理员授权"));
    }

    /** 认证失败 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return ResponseEntity.status(401).body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "认证失败，请重新登录"));
    }
}