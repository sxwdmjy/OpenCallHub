package com.och.infra.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础事件监听器
 * <p>
 * 通过 RabbitMQ 监听器消费领域事件，将事件保存到内存队列中用于测试和诊断。
 * 生产环境可替换为持久化或分发到各业务处理器的实现。
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class FoundationEventListener {

    private final List<DomainEvent> consumedEvents = new CopyOnWriteArrayList<>();
    private final AtomicInteger eventCount = new AtomicInteger(0);

    @RabbitListener(queues = "${och.mq.queue:och.events.queue}")
    public void onFoundationEvent(DomainEvent event) {
        log.info("[FoundationEventListener] Received event: type={}, eventId={}", event.getEventType(), event.getEventId());
        consumedEvents.add(event);
        eventCount.incrementAndGet();
    }

    public List<DomainEvent> getConsumedEvents() {
        return consumedEvents;
    }

    public int getEventCount() {
        return eventCount.get();
    }

    public void reset() {
        consumedEvents.clear();
        eventCount.set(0);
    }
}
