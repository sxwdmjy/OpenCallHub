package com.och.infra.mq;

/**
 * 事件订阅者接口（函数式接口）
 * <p>定义事件消费的回调契约，实现类通过泛型指定反序列化后的负载类型。</p>
 *
 * @param <T> 事件负载类型
 * @author danmo
 */
@FunctionalInterface
public interface EventSubscriber<T> {

    /**
     * 接收事件
     *
     * @param event   原始领域事件
     * @param payload 反序列化后的负载对象
     */
    void onEvent(DomainEvent event, T payload);
}
