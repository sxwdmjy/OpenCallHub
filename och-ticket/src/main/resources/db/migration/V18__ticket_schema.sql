-- openCallHub 工单管理 Schema
-- V13: 工单 / 流转记录

CREATE TABLE IF NOT EXISTS ticket (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ticket_no      VARCHAR(32)  NOT NULL COMMENT '工单编号',
    title          VARCHAR(256) NOT NULL COMMENT '标题',
    description    TEXT         COMMENT '描述',
    state          VARCHAR(32)  DEFAULT 'NEW' COMMENT '状态',
    priority       VARCHAR(16)  DEFAULT 'MEDIUM' COMMENT '优先级',
    customer_id    BIGINT       COMMENT '客户ID',
    call_record_id BIGINT       COMMENT '通话记录ID',
    assignee_id    BIGINT       COMMENT '处理人',
    creator_id     BIGINT       COMMENT '创建人',
    sla_level      VARCHAR(16)  COMMENT 'SLA等级',
    sla_deadline   DATETIME     COMMENT 'SLA截止',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    resolved_at    DATETIME     COMMENT '解决时间',
    deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    remark         VARCHAR(512),
    UNIQUE INDEX uk_no (ticket_no),
    INDEX idx_state (state),
    INDEX idx_assignee (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单';

CREATE TABLE IF NOT EXISTS ticket_flow (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ticket_id      BIGINT       NOT NULL,
    from_state     VARCHAR(32),
    to_state       VARCHAR(32),
    operator_id    BIGINT,
    operator_name  VARCHAR(64),
    comment        TEXT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单流转记录';
