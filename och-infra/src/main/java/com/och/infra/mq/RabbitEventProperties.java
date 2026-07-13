package com.och.infra.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMQ 事件配置属性
 * <p>
 * 对应 application.yml 中 och.mq 前缀的配置项，包括交换机、队列、绑定路由模式、
 * 默认发布路由键、死信队列和重试策略等参数。
 * </p>
 *
 * @author danmo
 */
@Data
@ConfigurationProperties(prefix = "och.mq")
public class RabbitEventProperties {

    private String exchange = "och.events";
    private String queue = "och.events.queue";
    private String routingKey = "och.event.#";
    private String publishRoutingKey = "och.event.foundation";
    private String deadLetterExchange = "och.events.dlx";
    private String deadLetterQueue = "och.events.dlq";
    private Integer maxRetryCount = 3;
    private Long retryDelayMs = 5000L;
}
