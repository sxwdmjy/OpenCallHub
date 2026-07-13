package com.och.infra.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 领域事件
 * <p>
 * RabbitMQ 消息的载体对象，包含事件ID、事件类型、发生时间和业务负载。
 * 通过静态工厂方法 {@link #of(String, String, String)} 快速构建。
 * </p>
 *
 * @author danmo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String traceId;
    private String payload;

    public static DomainEvent of(String eventType, String payload, String traceId) {
        DomainEvent event = new DomainEvent();
        event.setEventId(java.util.UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(eventType);
        event.setOccurredAt(Instant.now());
        event.setTraceId(traceId);
        event.setPayload(payload);
        return event;
    }
}
