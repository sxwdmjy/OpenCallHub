-- openCallHub Report Schema
-- V9: inbound daily and agent inbound daily aggregation

CREATE TABLE IF NOT EXISTS report_inbound_daily (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date      DATE            NOT NULL COMMENT '报表日期',
    total_calls      INT             DEFAULT 0 COMMENT '总呼叫数',
    answered_calls   INT             DEFAULT 0 COMMENT '接听数',
    missed_calls     INT             DEFAULT 0 COMMENT '未接数',
    abandoned_calls  INT             DEFAULT 0 COMMENT '放弃数',
    voicemail_calls  INT             DEFAULT 0 COMMENT '语音信箱数',
    avg_ring_seconds DECIMAL(10,2)   DEFAULT 0 COMMENT '平均振铃时长',
    avg_talk_seconds DECIMAL(10,2)   DEFAULT 0 COMMENT '平均通话时长',
    avg_wait_seconds DECIMAL(10,2)   DEFAULT 0 COMMENT '平均等待时长',
    avg_after_call_seconds DECIMAL(10,2) DEFAULT 0 COMMENT '平均后处理时长',
    total_hold_count INT             DEFAULT 0 COMMENT '总保持次数',
    total_transfer_count INT         DEFAULT 0 COMMENT '总转接次数',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼入日报表';

CREATE TABLE IF NOT EXISTS report_agent_inbound_daily (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date      DATE            NOT NULL COMMENT '报表日期',
    agent_id         BIGINT          NOT NULL COMMENT '坐席ID',
    answered_calls   INT             DEFAULT 0 COMMENT '接听数',
    missed_calls     INT             DEFAULT 0 COMMENT '未接数',
    avg_talk_seconds DECIMAL(10,2)   DEFAULT 0 COMMENT '平均通话时长',
    total_talk_seconds INT           DEFAULT 0 COMMENT '总通话时长',
    avg_after_call_seconds DECIMAL(10,2) DEFAULT 0 COMMENT '平均后处理时长',
    hold_count       INT             DEFAULT 0 COMMENT '保持次数',
    transfer_count   INT             DEFAULT 0 COMMENT '转接次数',
    available_seconds INT            DEFAULT 0 COMMENT '可用时长(秒)',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_agent_date (agent_id, report_date),
    INDEX idx_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='坐席呼入日报表';
