package com.och.infra.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import com.och.infra.mq.RabbitEventProperties;
import com.och.infra.redis.RedisCacheProperties;
import com.och.infra.file.FileStorageProperties;

/**
 * 基础设施自动配置类
 * <p>
 * 启用 RabbitMQ (@EnableRabbit)、Spring 异步 (@EnableAsync)，
 * 扫描 com.och.infra 包下的所有 Bean，并注册 RabbitMQ、Redis、文件存储的配置属性类。
 * </p>
 *
 * @author danmo
 */
@Configuration
@EnableAsync
@EnableRabbit
@ComponentScan(basePackages = "com.och.infra")
@EnableConfigurationProperties({RabbitEventProperties.class, RedisCacheProperties.class, FileStorageProperties.class})
public class InfraAutoConfiguration {
}
