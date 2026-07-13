package com.och.common.error;

import lombok.Getter;

/**
 * 错误码枚举
 * <p>定义系统所有错误码，分三个区间：HTTP状态码映射、业务错误码、系统错误码。</p>
 *
 * @author danmo
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    BIZ_VALIDATION_ERROR(1001, "数据校验失败"),
    BIZ_DUPLICATE_KEY(1002, "数据重复"),
    BIZ_OPERATION_DENIED(1003, "操作不允许"),
    BIZ_RESOURCE_LOCKED(1004, "资源已被锁定"),
    BIZ_ILLEGAL_STATE(1005, "状态不正确"),
    BIZ_USER_ID_REQUIRED(1006, "用户ID不能为空"),
    BIZ_ROLE_ID_REQUIRED(1007, "角色ID不能为空"),
    BIZ_USERNAME_DUPLICATE(1008, "用户名称重复"),
    BIZ_USER_NOT_FOUND(1009, "用户不存在"),
    BIZ_LOGIN_FAILED(1010, "登录认证失败"),
    BIZ_RESOURCE_ID_REQUIRED(1011, "资源ID不能为空"),
    BIZ_NAME_DUPLICATE(1012, "名称已存在"),
    BIZ_ACCOUNT_DUPLICATE(1013, "账号已存在"),

    SYS_DATABASE_ERROR(2001, "数据库操作异常"),
    SYS_REDIS_ERROR(2002, "Redis操作异常"),
    SYS_MQ_ERROR(2003, "消息队列异常"),
    SYS_FILE_ERROR(2004, "文件操作异常"),
    SYS_NETWORK_ERROR(2005, "网络异常");

    private final Integer code;
    private final String defaultMessage;

    ErrorCode(Integer code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}