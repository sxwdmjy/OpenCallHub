package com.och.security.audit;

/**
 * 审计事件输出接口（函数式接口）
 * <p>定义审计事件的持久化契约，实现类可将事件写入数据库、日志文件或消息队列。</p>
 *
 * @author danmo
 */
@FunctionalInterface
public interface AuditSink {

    /**
     * 写入一条审计事件
     *
     * @param event 审计事件
     */
    void write(AuditEvent event);
}
