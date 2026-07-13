package com.och.common.exception;

import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;

/**
 * 通用业务异常（重定向至 BizException）
 * <p>
 * 所有旧式 CommonException 用法已迁移至 {@link BizException} + {@link ErrorCode} 体系。
 * 本类保留仅作编译参考，实际业务请直接使用 BizException。
 * </p>
 *
 * @deprecated 请使用 {@link BizException}
 */
@Deprecated
public class CommonException extends BizException {

    public CommonException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    public CommonException(Integer code, String message) {
        super(code, message);
    }
}