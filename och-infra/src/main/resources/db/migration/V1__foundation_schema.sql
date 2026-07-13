-- openCallHub Foundation Schema
-- V1: foundation_migration_check and audit_event tables

CREATE TABLE IF NOT EXISTS foundation_migration_check (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    applied_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     VARCHAR(64)     NOT NULL,
    description VARCHAR(255)    NOT NULL,
    success     TINYINT(1)      NOT NULL DEFAULT 1,
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='迁移检查表';

CREATE TABLE IF NOT EXISTS audit_event (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    trace_id        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '链路追踪ID',
    user_id         BIGINT          COMMENT '操作用户ID',
    username        VARCHAR(64)     COMMENT '操作用户名',
    action          VARCHAR(128)    NOT NULL COMMENT '操作类型',
    target_type     VARCHAR(64)     COMMENT '目标资源类型',
    target_id       VARCHAR(128)    COMMENT '目标资源ID',
    request_uri     VARCHAR(256)    COMMENT '请求URI',
    request_method  VARCHAR(16)     COMMENT '请求方法',
    request_params  TEXT            COMMENT '请求参数',
    response_status INT             COMMENT '响应状态码',
    client_ip       VARCHAR(64)     COMMENT '客户端IP',
    duration_ms     BIGINT          COMMENT '执行耗时(ms)',
    remark          VARCHAR(512)    COMMENT '备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计事件表';
