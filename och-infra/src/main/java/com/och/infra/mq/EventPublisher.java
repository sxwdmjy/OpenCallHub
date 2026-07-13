package com.och.infra.mq;

/**
 * 事件发布者接口
 * <p>定义领域事件的发布契约，实现类负责将事件发送到消息队列。</p>
 *
 * @author danmo
 */
public interface EventPublisher {

    /**
     * 发布事件（使用默认路由键）
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);

    /**
     * 发布事件（指定路由键）
     *
     * @param routingKey 路由键
     * @param event      领域事件
     */
    void publish(String routingKey, DomainEvent event);
}
