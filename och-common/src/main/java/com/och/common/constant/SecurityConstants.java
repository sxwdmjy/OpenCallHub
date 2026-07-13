package com.och.common.constant;

/**
 * Spring Security 相关常量定义
 * <p>定义 JWT Token 中存储的用户信息 claim 键名。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public class SecurityConstants {

    /** JWT claim: 用户名 */
    public static final String USER_NAME = "user_name";

    /** JWT claim: 用户ID */
    public static final String USER_ID = "user_id";

    /** JWT claim: 登录用户完整信息（JSON 序列化） */
    public static final String LOGIN_USER = "login_user";
}
