package com.och.infra.redis;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存接口实现
 * <p>基于 Spring StringRedisTemplate 实现 {@link RedisCache} 接口，
 * 支持 key 前缀自动拼接（通过 {@link RedisCacheProperties#getKeyPrefix()} 配置）。</p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheImpl implements RedisCache {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheProperties properties;

    private String buildKey(String key) {
        return properties.getKeyPrefix() + key;
    }

    @Override
    public void set(String key, String value) {
        set(key, value, properties.getDefaultTtlSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            stringRedisTemplate.opsForValue().set(buildKey(key), value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("[RedisCache] set error, key={}", key, e);
            throw new RuntimeException("Redis set error", e);
        }
    }

    @Override
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(buildKey(key));
        } catch (Exception e) {
            log.error("[RedisCache] get error, key={}", key, e);
            throw new RuntimeException("Redis get error", e);
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            return stringRedisTemplate.delete(buildKey(key));
        } catch (Exception e) {
            log.error("[RedisCache] delete error, key={}", key, e);
            throw new RuntimeException("Redis delete error", e);
        }
    }

    @Override
    public Boolean exists(String key) {
        try {
            return stringRedisTemplate.hasKey(buildKey(key));
        } catch (Exception e) {
            log.error("[RedisCache] exists error, key={}", key, e);
            throw new RuntimeException("Redis exists error", e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit timeUnit) {
        try {
            return stringRedisTemplate.opsForValue().setIfAbsent(buildKey(key), value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("[RedisCache] setIfAbsent error, key={}", key, e);
            throw new RuntimeException("Redis setIfAbsent error", e);
        }
    }

    @Override
    public Long increment(String key) {
        return increment(key, 1L);
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().increment(buildKey(key), delta);
        } catch (Exception e) {
            log.error("[RedisCache] increment error, key={}", key, e);
            throw new RuntimeException("Redis increment error", e);
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        try {
            return stringRedisTemplate.getExpire(buildKey(key), timeUnit);
        } catch (Exception e) {
            log.error("[RedisCache] getExpire error, key={}", key, e);
            throw new RuntimeException("Redis getExpire error", e);
        }
    }
}
