package com.och.infra.mq;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * RabbitMQ 事件发布测试
 * <p>
 * 验证事件发布者使用配置中的交换机和默认发布路由键，并把事件类型、traceId 写入消息头，
 * 便于后续消费端做日志追踪和事件分发。
 * </p>
 *
 * @author danmo
 */
class RabbitEventPublisherTest {

    @Test
    void publishUsesConfiguredExchangeAndHeaders() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitEventProperties properties = new RabbitEventProperties();
        properties.setExchange("test.exchange");
        properties.setPublishRoutingKey("test.route");
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate, properties);
        DomainEvent event = DomainEvent.of("foundation.test", "{}", "trace-001");

        publisher.publish(event);

        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(eq("test.exchange"), eq("test.route"), same(event), processorCaptor.capture());

        Message message = new Message("{}".getBytes(StandardCharsets.UTF_8), new MessageProperties());
        Message processedMessage = processorCaptor.getValue().postProcessMessage(message);
        assertEquals(event.getEventId(), processedMessage.getMessageProperties().getMessageId());
        assertEquals("foundation.test", processedMessage.getMessageProperties().getHeaders().get("eventType"));
        assertEquals("trace-001", processedMessage.getMessageProperties().getHeaders().get("traceId"));
    }
}
