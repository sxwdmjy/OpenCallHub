package com.och.infra.mq;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 事件通道配置
 * <p>
 * Phase 1 基础底座只声明统一事件交换机、业务队列、死信交换机和死信队列，
 * 业务模块后续通过 {@link EventPublisher} 发布领域事件，不需要关心底层队列声明细节。
 * </p>
 *
 * @author danmo
 */
@Configuration
@RequiredArgsConstructor
public class RabbitEventConfiguration {

    private final RabbitEventProperties properties;

    @Bean
    public TopicExchange foundationEventExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public TopicExchange foundationDeadLetterExchange() {
        return new TopicExchange(properties.getDeadLetterExchange(), true, false);
    }

    @Bean
    public Queue foundationEventQueue() {
        return QueueBuilder.durable(properties.getQueue())
                .deadLetterExchange(properties.getDeadLetterExchange())
                .deadLetterRoutingKey(properties.getQueue() + ".dead")
                .build();
    }

    @Bean
    public Queue foundationDeadLetterQueue() {
        return QueueBuilder.durable(properties.getDeadLetterQueue()).build();
    }

    @Bean
    public Binding foundationEventBinding() {
        return BindingBuilder.bind(foundationEventQueue())
                .to(foundationEventExchange())
                .with(properties.getRoutingKey());
    }

    @Bean
    public Binding foundationDeadLetterBinding() {
        return BindingBuilder.bind(foundationDeadLetterQueue())
                .to(foundationDeadLetterExchange())
                .with(properties.getQueue() + ".dead");
    }

    @Bean
    public MessageConverter rabbitJsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
