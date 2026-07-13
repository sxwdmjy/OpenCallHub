package com.och.common.config.redis;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务接口
 * <p>封装通用的 Redis 操作，包括字符串缓存、哈希缓存、
 * 过期时间设置和键存在性检查等。由 och-security 或 och-system 模块提供实现。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
public interface RedisService {

    // ==================== 字符串缓存 ====================

    /**
     * 获取缓存对象
     *
     * @param key 缓存键
     * @return 缓存值，不存在时返回 null
     */
    Object getCacheObject(String key);

    /**
     * 设置缓存对象（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void setCacheObject(String key, Object value);

    /**
     * 设置缓存对象（指定过期时间）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间数值
     * @param unit    时间单位
     */
    void setCacheObject(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 刷新缓存过期时间
     *
     * @param key        缓存键
     * @param expiration 过期时间（分钟）
     */
    void expire(String key, long expiration);

    /**
     * 判断键是否存在
     *
     * @param key 缓存键
     * @return true 表示存在
     */
    boolean keyIsExists(String key);

    /**
     * 删除缓存对象
     *
     * @param key 缓存键
     */
    void deleteObject(String key);

    // ==================== 哈希缓存 ====================

    /**
     * 设置哈希缓存中的字段值
     *
     * @param key     缓存键
     * @param hashKey 哈希字段名
     * @param value   字段值
     */
    void setCacheMapValue(String key, String hashKey, Object value);

    /**
     * 获取哈希缓存中的字段值
     *
     * @param key     缓存键
     * @param hashKey 哈希字段名
     * @return 字段值，不存在时返回 null
     * @param <T> 返回值类型
     */
    <T> T getCacheMapValue(String key, String hashKey);

    /**
     * 删除哈希缓存中的一个或多个字段
     *
     * @param key      缓存键
     * @param hashKeys 要删除的哈希字段名数组
     */
    void delCacheMapValue(String key, String... hashKeys);

    /**
     * 判断哈希缓存中是否存在指定字段
     *
     * @param key     缓存键
     * @param hashKey 哈希字段名
     * @return true 表示存在
     */
    Boolean getCacheMapHasKey(String key, String hashKey);

    /**
     * 获取整个哈希缓存
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 哈希缓存 Map，不存在时返回空 Map
     */
    <T> Map<String, T> getCacheMap(String key);
}
