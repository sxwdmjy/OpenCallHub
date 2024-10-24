package com.och.api.config;

import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.common.AnyMap;
import com.alibaba.fastjson.JSON;
import com.och.common.utils.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class CaptchaRedisCacheStore implements CacheStore {

    private static final RedisScript<String> SCRIPT_GET_CACHE = new DefaultRedisScript<>("local res = redis.call('get',KEYS[1])  if res == nil  then return nil  else  redis.call('del',KEYS[1]) return res end", String.class);
    protected StringRedisTemplate redisTemplate;

    public CaptchaRedisCacheStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AnyMap getCache(String key) {
        String jsonData = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }
        return JSON.parseObject(jsonData, AnyMap.class);
    }

    @Override
    public AnyMap getAndRemoveCache(String key) {
        String json = redisTemplate.execute(SCRIPT_GET_CACHE, Collections.singletonList(key));
        if (org.apache.commons.lang3.StringUtils.isBlank(json)) {
            return null;
        }
       return JSON.parseObject(json, AnyMap.class);
    }

    @Override
    public boolean setCache(String key, AnyMap data, Long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(data), expire, timeUnit);
        return true;
    }

    @Override
    public Long incr(String key, long delta, Long expire, TimeUnit timeUnit) {
        Long increment = redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, expire, timeUnit);
        return increment;
    }

    @Override
    public Long getLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }
}