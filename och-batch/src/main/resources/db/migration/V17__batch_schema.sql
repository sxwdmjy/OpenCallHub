-- openCallHub 批处理 Schema
-- V17: 批处理任务

CREATE TABLE IF NOT EXISTS batch_job (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_name        VARCHAR(256) NOT NULL COMMENT '任务名称',
    job_type        VARCHAR(16)  NOT NULL COMMENT 'IMPORT/EXPORT',
    state           VARCHAR(16)  DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/COMPLETED/FAILED',
    total_items     INT          DEFAULT 0,
    processed_items INT          DEFAULT 0,
    success_items   INT          DEFAULT 0,
    error_items     INT          DEFAULT 0,
    file_path       VARCHAR(512) COMMENT '文件路径',
    error_file_url  VARCHAR(512) COMMENT '错误明细URL',
    created_by      BIGINT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    DATETIME,
    INDEX idx_type_state (job_type, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批处理任务';
