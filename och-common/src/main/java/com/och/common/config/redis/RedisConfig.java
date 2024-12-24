package com.och.common.config.redis;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置
 *
 * @author danmo
 */
@Configuration
@EnableCaching
public class RedisConfig {


    @Bean(name = {"redisTemplate"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericFastJsonRedisSerializer genericFastJsonRedisSerializer = new GenericFastJsonRedisSerializer();

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(genericFastJsonRedisSerializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(genericFastJsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = {"stringRedisTemplate"})
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }
}