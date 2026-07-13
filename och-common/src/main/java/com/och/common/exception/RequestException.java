package com.och.common.exception;

import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;

/**
 * 请求参数异常（重定向至 BizException）
 *
 * @deprecated 请使用 {@link BizException} + {@link ErrorCode#BIZ_VALIDATION_ERROR}
 */
@Deprecated
public class RequestException extends BizException {

    public RequestException(String message) {
        super(ErrorCode.BIZ_VALIDATION_ERROR, message);
    }

    public RequestException(Integer code, String message) {
        super(code, message);
    }
}