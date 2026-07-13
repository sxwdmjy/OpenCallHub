package com.och.common.constant;

/**
 * HTTP 状态码常量
 * <p>定义常用的 HTTP 响应状态码，避免代码中出现魔法数字。</p>
 *
 * @author danmo
 */
public class HttpStatus {

    /** 操作成功 */
    public static final int SUCCESS = 200;

    /** 未授权 */
    public static final int UNAUTHORIZED = 401;

    /** 访问受限 */
    public static final int FORBIDDEN = 403;

    /** 资源不存在 */
    public static final int NOT_FOUND = 404;

    /** 服务器内部错误 */
    public static final int ERROR = 500;

    private HttpStatus() {
    }
}