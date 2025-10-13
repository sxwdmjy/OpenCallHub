package com.och.security.handler;

import com.och.common.base.ResResult;
import com.och.common.constant.HttpStatus;
import com.och.common.enums.ExceptionStatusEnum;
import com.och.common.exception.CommonException;
import com.och.common.exception.LoginException;
import com.och.common.exception.RequestException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

;

/**
 * 全局异常处理器
 *
 * @author danmo
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        //增加异常信息
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod(), e);
        return ResResult.error(e.getMessage());
    }


    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        //增加异常信息
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        String msg = e.getMessage();
        if (StringUtils.isEmpty(msg)) {
            msg = "未知异常";
        }
        return ResResult.error(msg);
    }


    /**
     * 数据库操作异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResResult handleDataAccessException(DataAccessException e) {
        log.error("数据库操作异常：{}", e.getMessage(), e);
        //增加异常信息
        return ResResult.error("数据库操作异常，请联系管理员！");
    }


    /**
     * 登录异常
     */
    @ExceptionHandler(LoginException.class)
    public ResResult handleLoginException(LoginException e) {
        return ResResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResResult handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return ResResult.error(e.getMessage());
    }

    /**
     * 权限访问异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResResult handleAccessDeniedException(Exception e, HttpServletRequest request) {
        return ResResult.error(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    @ExceptionHandler(CommonException.class)
    public ResResult commonExceptionException(Exception e, HttpServletRequest request) {
        return ResResult.error(e.getMessage());
    }

    @ExceptionHandler(RequestException.class)
    public ResResult requestExceptionException(Exception e, HttpServletRequest request) {
        return ResResult.error(e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResResult authenticationExceptionException(Exception e, HttpServletRequest request) {
        return ResResult.error(ExceptionStatusEnum.ERROR_LOGIN_USER.getCode(), ExceptionStatusEnum.ERROR_LOGIN_USER.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResResult methodArgumentNotValidException(Exception e, HttpServletRequest request) {
        String message = "";
        try {
            message = e.getMessage().substring(e.getMessage().lastIndexOf("[") + 1, e.getMessage().lastIndexOf("]") - 1);
        } catch (Exception ex) {
            message = "参数校验失败";
        }
        return ResResult.error(message);
    }

}
