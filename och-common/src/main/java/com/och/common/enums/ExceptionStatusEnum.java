package com.och.common.enums;

import com.och.common.error.ErrorCode;
import lombok.Getter;

/**
 * 异常状态枚举（已废弃，请使用 ErrorCode）
 * <p>
 * 所有旧式 ExceptionStatusEnum 用法已迁移至 {@link ErrorCode} 体系。
 * 本枚举保留仅作参考索引，实际业务请直接使用 ErrorCode。
 * </p>
 *
 * @deprecated 请使用 {@link ErrorCode}
 */
@Getter
@Deprecated
public enum ExceptionStatusEnum {

    ERROR_USERID_NOT_NULL(ErrorCode.BIZ_USER_ID_REQUIRED),
    ERROR_ROLEID_NOT_NULL(ErrorCode.BIZ_ROLE_ID_REQUIRED),
    ERROR_USER_NAME_EXISTENCE(ErrorCode.BIZ_USERNAME_DUPLICATE),
    ERROR_USERID_NOT_EXIST(ErrorCode.BIZ_USER_NOT_FOUND),
    ERROR_LOGIN_USER(ErrorCode.BIZ_LOGIN_FAILED),
    SUCCESS(ErrorCode.SUCCESS);

    private final Integer code;
    private final String msg;

    ExceptionStatusEnum(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getDefaultMessage();
    }
}