-- openCallHub Customer Schema
-- V22: 客户跟进记录表

CREATE TABLE IF NOT EXISTS customer_follow_up (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT       NOT NULL COMMENT '客户ID',
    agent_id        BIGINT       COMMENT '坐席ID',
    call_record_id  BIGINT       COMMENT '关联通话记录ID',
    ticket_id       BIGINT       COMMENT '关联工单ID',
    follow_type     VARCHAR(16)  DEFAULT 'CALL' COMMENT '跟进方式: CALL/VISIT/MESSAGE/EMAIL',
    content         TEXT         COMMENT '跟进内容',
    result          VARCHAR(128) COMMENT '跟进结果',
    next_action     VARCHAR(256) COMMENT '下一步计划',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer (customer_id, created_at),
    INDEX idx_agent (agent_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户跟进记录';
