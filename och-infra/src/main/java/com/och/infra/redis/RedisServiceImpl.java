package com.och.infra.redis;

import com.alibaba.fastjson2.JSON;
import com.och.common.config.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务实现
 * <p>基于 Spring StringRedisTemplate 实现 {@link RedisService} 接口。
 * 支持字符串缓存和哈希缓存操作，值对象自动序列化为 JSON 存储。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCacheProperties properties;

    private String buildKey(String key) {
        return properties.getKeyPrefix() + key;
    }

    // ==================== 字符串缓存 ====================

    @Override
    public Object getCacheObject(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(buildKey(key));
        } catch (Exception e) {
            log.error("[RedisService] getCacheObject error, key={}", key, e);
            return null;
        }
    }

    @Override
    public void setCacheObject(String key, Object value) {
        setCacheObject(key, value, properties.getDefaultTtlSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void setCacheObject(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = (value instanceof String) ? (String) value : JSON.toJSONString(value);
            stringRedisTemplate.opsForValue().set(buildKey(key), jsonValue, timeout, unit);
        } catch (Exception e) {
            log.error("[RedisService] setCacheObject error, key={}", key, e);
        }
    }

    @Override
    public void expire(String key, long expirationMinutes) {
        try {
            stringRedisTemplate.expire(buildKey(key), expirationMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("[RedisService] expire error, key={}", key, e);
        }
    }

    @Override
    public boolean keyIsExists(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(key)));
        } catch (Exception e) {
            log.error("[RedisService] keyIsExists error, key={}", key, e);
            return false;
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            stringRedisTemplate.delete(buildKey(key));
        } catch (Exception e) {
            log.error("[RedisService] deleteObject error, key={}", key, e);
        }
    }

    // ==================== 哈希缓存 ====================

    @Override
    public void setCacheMapValue(String key, String hashKey, Object value) {
        try {
            String jsonValue = (value instanceof String) ? (String) value : JSON.toJSONString(value);
            stringRedisTemplate.opsForHash().put(buildKey(key), hashKey, jsonValue);
        } catch (Exception e) {
            log.error("[RedisService] setCacheMapValue error, key={}, hashKey={}", key, hashKey, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCacheMapValue(String key, String hashKey) {
        try {
            Object value = stringRedisTemplate.opsForHash().get(buildKey(key), hashKey);
            if (value == null) {
                return null;
            }
            // 返回 String 类型，由调用方自行反序列化
            return (T) value;
        } catch (Exception e) {
            log.error("[RedisService] getCacheMapValue error, key={}, hashKey={}", key, hashKey, e);
            return null;
        }
    }

    @Override
    public void delCacheMapValue(String key, String... hashKeys) {
        try {
            stringRedisTemplate.opsForHash().delete(buildKey(key), (Object[]) hashKeys);
        } catch (Exception e) {
            log.error("[RedisService] delCacheMapValue error, key={}, hashKeys={}", key, hashKeys, e);
        }
    }

    @Override
    public Boolean getCacheMapHasKey(String key, String hashKey) {
        try {
            return stringRedisTemplate.opsForHash().hasKey(buildKey(key), hashKey);
        } catch (Exception e) {
            log.error("[RedisService] getCacheMapHasKey error, key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getCacheMap(String key) {
        try {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(buildKey(key));
            if (entries == null || entries.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, T> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                result.put(String.valueOf(entry.getKey()), (T) entry.getValue());
            }
            return result;
        } catch (Exception e) {
            log.error("[RedisService] getCacheMap error, key={}", key, e);
            return Collections.emptyMap();
        }
    }
}
