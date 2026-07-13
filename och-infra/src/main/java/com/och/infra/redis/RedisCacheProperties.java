package com.och.infra.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 缓存配置属性
 * <p>对应 application.yml 中 och.redis 前缀的配置项，包括 key 前缀和默认过期时间。</p>
 *
 * @author danmo
 */
@Data
@ConfigurationProperties(prefix = "och.redis")
public class RedisCacheProperties {

    private String keyPrefix = "och:";
    private Long defaultTtlSeconds = 3600L;
}
