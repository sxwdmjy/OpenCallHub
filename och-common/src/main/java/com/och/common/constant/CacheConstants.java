package com.och.common.constant;

/**
 * Redis 缓存常量定义
 * <p>统一定义项目中使用的 Redis 缓存键前缀和过期时间，
 * 避免硬编码分散在各处。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public class CacheConstants {

    /** 登录 token 缓存键前缀 */
    public static final String LOGIN_TOKEN_KEY = "login:token:";

    /** token 缓存过期时间（分钟），默认 30 分钟 */
    public static final long EXPIRATION = 30;

    /** 客户端 WebSocket 用户池缓存键 */
    public static final String CLIENT_USER_POOL_KEY = "ws:user:pool";

    /** 座席当前状态缓存键 */
    public static final String AGENT_CURRENT_STATUS_KEY = "agent:status";

    /** 阿里云 TTS token 缓存键 */
    public static final String ALI_TTS_TOKEN_KEY = "ali:tts:token";
}
