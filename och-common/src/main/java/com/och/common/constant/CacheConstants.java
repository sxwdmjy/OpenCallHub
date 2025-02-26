package com.och.common.constant;

/**
 * 缓存的key 常量
 *
 * @author danmo
 */
public class CacheConstants {
    /**
     * 缓存有效期，默认720（分钟）
     */
    public final static Integer EXPIRATION = 720;

    /**
     * 缓存刷新时间，默认120（分钟）
     */
    public final static Integer REFRESH_TIME = 120;

    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "login_tokens:";


    /**
     * 通话记录key
     */
    public static final String  CALL_INFO_CACHE_KEY = "fs:callInfo:{}";

    /**
     * 通话记录两条腿关联key
     */
    public static final String  CALL_REL_MAP_CACHE_KEY = "fs:callRel:Map";

    /**
     * 坐席当前状态key
     */
    public static final String AGENT_CURRENT_STATUS_KEY = "fs:agent:status";

    /**
     * socket登录用户池key
     */
    public static final String CLIENT_USER_POOL_KEY = "client_user_pool";

    /**
     * 技能组轮训key
     */
    public static final String CALL_SKILL_POLLING_KEY = "fs:skill:polling:{}";

    /**
     * ivr流程实例key
     */
    public static final String CALL_IVR_INSTANCES_KEY = "fs:ivr:instances:{}";

    public static final String CALL_IVR_FLOW_INFO_NODE_KEY = "fs:ivr:flow:node:{}";

    public static final String ALI_TTS_TOKEN_KEY = "ali_tts_token";

}
