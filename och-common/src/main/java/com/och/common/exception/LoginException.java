package com.och.common.exception;

import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;

/**
 * 登录异常（重定向至 BizException）
 *
 * @deprecated 请使用 {@link BizException} + {@link ErrorCode#UNAUTHORIZED}
 */
@Deprecated
public class LoginException extends BizException {

    public LoginException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public LoginException(Integer code, String message) {
        super(code, message);
    }
}