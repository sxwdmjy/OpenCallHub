package com.och.infra.redis;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存接口
 * <p>定义通用的 Redis 缓存操作契约，包括字符串读写、删除、存在性判断、
 * 分布式锁（setIfAbsent）、原子递增和 TTL 查询等。</p>
 *
 * @author danmo
 */
public interface RedisCache {

    /**
     * 设置缓存（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, String value);

    /**
     * 设置缓存（指定过期时间）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间数值
     * @param timeUnit 过期时间单位
     */
    void set(String key, String value, long timeout, TimeUnit timeUnit);

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，不存在时返回 null
     */
    String get(String key);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return true 表示删除成功
     */
    Boolean delete(String key);

    /**
     * 判断键是否存在
     *
     * @param key 缓存键
     * @return true 表示存在
     */
    Boolean exists(String key);

    /**
     * 仅当键不存在时设置值（可作分布式锁）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间数值
     * @param timeUnit 过期时间单位
     * @return true 表示设置成功
     */
    Boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit);

    /**
     * 原子递增（步长为 1）
     *
     * @param key 缓存键
     * @return 递增后的值
     */
    Long increment(String key);

    /**
     * 原子递增（指定步长）
     *
     * @param key   缓存键
     * @param delta 步长
     * @return 递增后的值
     */
    Long increment(String key, long delta);

    /**
     * 获取键的剩余过期时间
     *
     * @param key      缓存键
     * @param timeUnit 时间单位
     * @return 剩余过期时间
     */
    Long getExpire(String key, TimeUnit timeUnit);
}
