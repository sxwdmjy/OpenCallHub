package com.och.common.constant;

/**
 * JWT Token 常量定义
 * <p>定义 JWT 相关的请求头名称、前缀和签名密钥等常量。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public class TokenConstants {

    /** HTTP 请求头中携带 token 的字段名 */
    public static final String AUTHENTICATION = "Authorization";

    /** Token 前缀，如 "Bearer " */
    public static final String PREFIX = "Bearer ";

    /** JWT 签名密钥（HMAC512），生产环境应从配置中心获取 */
    public static final String SECRET = "OpenCallHubJwtSecret2024";
}
