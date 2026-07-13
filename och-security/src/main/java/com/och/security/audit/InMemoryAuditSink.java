package com.och.security.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存审计事件存储实现
 * <p>将审计事件保存到内存（CopyOnWriteArrayList）中，适用于开发和测试环境。
 * 生产环境建议替换为数据库或日志文件持久化实现。</p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class InMemoryAuditSink implements AuditSink {

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void write(AuditEvent event) {
        log.info("[Audit] action={}, targetType={}, targetId={}, userId={}, traceId={}",
                event.getAction(), event.getTargetType(), event.getTargetId(),
                event.getUserId(), event.getTraceId());
        events.add(event);
    }

    public List<AuditEvent> getAllEvents() {
        return events;
    }

    public void clear() {
        events.clear();
    }
}
