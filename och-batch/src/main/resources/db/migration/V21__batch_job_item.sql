-- openCallHub Batch Schema Expansion
-- V21: 批处理条目明细表

CREATE TABLE IF NOT EXISTS batch_job_item (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_id          BIGINT       NOT NULL COMMENT '关联任务ID',
    row_index       INT          NOT NULL COMMENT '行号/序号',
    item_key        VARCHAR(256) COMMENT '条目唯一标识',
    state           VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED/FAILED_RETRY',
    error_message   VARCHAR(1024) COMMENT '失败原因',
    raw_data        JSON         COMMENT '原始数据',
    processed_at    DATETIME     COMMENT '处理时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_job_state (job_id, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批处理条目明细';

-- 批处理任务表增加错误信息字段（兼容已有表）
ALTER TABLE batch_job
    ADD COLUMN IF NOT EXISTS error_message VARCHAR(1024) COMMENT '任务失败原因' AFTER error_file_url;
