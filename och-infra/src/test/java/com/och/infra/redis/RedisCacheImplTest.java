package com.och.infra.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 缓存封装测试
 * <p>
 * 验证公共 Redis 封装会统一添加 key 前缀和默认过期时间，
 * 后续业务模块不需要重复处理缓存命名空间规则。
 * </p>
 *
 * @author danmo
 */
class RedisCacheImplTest {

    private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private RedisCacheImpl redisCache;

    @BeforeEach
    void setUp() {
        RedisCacheProperties properties = new RedisCacheProperties();
        properties.setKeyPrefix("test:");
        properties.setDefaultTtlSeconds(60L);
        redisCache = new RedisCacheImpl(stringRedisTemplate, properties);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void setUsesPrefixAndDefaultTtl() {
        redisCache.set("key", "value");

        verify(valueOperations).set("test:key", "value", 60L, TimeUnit.SECONDS);
    }

    @Test
    void getUsesPrefixedKey() {
        when(valueOperations.get("test:key")).thenReturn("value");

        String value = redisCache.get("key");

        assertEquals("value", value);
    }

    @Test
    void setIfAbsentUsesPrefixedKey() {
        when(valueOperations.setIfAbsent("test:lock", "1", 5L, TimeUnit.SECONDS)).thenReturn(true);

        Boolean locked = redisCache.setIfAbsent("lock", "1", 5L, TimeUnit.SECONDS);

        assertTrue(locked);
    }
}
