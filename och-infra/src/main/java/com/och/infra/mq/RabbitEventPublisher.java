package com.och.infra.mq;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 事件发布者实现
 * <p>
 * 通过 Spring RabbitTemplate 将领域事件发送到统一事件交换机。
 * 事件序列化由 RabbitEventConfiguration 中的 JSON MessageConverter 统一处理，
 * 避免发布端和消费端各自维护一套序列化规则。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitEventProperties properties;

    @Override
    public void publish(DomainEvent event) {
        publish(properties.getPublishRoutingKey(), event);
    }

    @Override
    public void publish(String routingKey, DomainEvent event) {
        rabbitTemplate.convertAndSend(properties.getExchange(), routingKey, event, message -> {
            message.getMessageProperties().setMessageId(event.getEventId());
            message.getMessageProperties().setHeader("eventType", event.getEventType());
            message.getMessageProperties().setHeader("traceId", event.getTraceId());
            return message;
        });
        log.debug("[RabbitEventPublisher] Published event: type={}, eventId={}", event.getEventType(), event.getEventId());
    }
}
