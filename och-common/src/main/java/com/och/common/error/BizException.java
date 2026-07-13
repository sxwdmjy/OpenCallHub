package com.och.common.error;

import lombok.Getter;

/**
 * 业务异常
 * <p>用于业务层主动抛出异常，由全局异常处理器 {@link com.och.common.web.GlobalExceptionHandler}
 * 统一捕获并包装为 ApiResponse 返回。支持通过 ErrorCode 枚举快速构造。</p>
 *
 * <p>使用示例：
 * <pre>{@code
 * throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "用户名不能为空");
 * }</pre>
 * </p>
 *
 * @author danmo
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final String message;
    private Object data;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }

    public BizException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.data = data;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String message) {
        super(message);
        this.code = ErrorCode.INTERNAL_ERROR.getCode();
        this.message = message;
    }
}
