-- openCallHub Agent Inbound Schema
-- V7: call record, recording record, service record

CREATE TABLE IF NOT EXISTS call_record (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id       BIGINT          COMMENT '呼叫会话ID',
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫ID',
    caller_number    VARCHAR(32)     COMMENT '主叫号码',
    called_number    VARCHAR(32)     COMMENT '被叫号码',
    direction        VARCHAR(16)     COMMENT '方向',
    state            VARCHAR(32)     COMMENT '最终状态',
    agent_id         BIGINT          COMMENT '坐席ID',
    queue_id         BIGINT          COMMENT '队列ID',
    ring_seconds     INT             DEFAULT 0 COMMENT '振铃时长(秒)',
    talk_seconds     INT             DEFAULT 0 COMMENT '通话时长(秒)',
    hold_count       INT             DEFAULT 0 COMMENT '保持次数',
    transfer_count   INT             DEFAULT 0 COMMENT '转接次数',
    recording_url    VARCHAR(512)    COMMENT '录音URL',
    customer_id      BIGINT          COMMENT '关联客户ID',
    end_cause        VARCHAR(64)     COMMENT '结束原因',
    start_time       DATETIME        COMMENT '开始时间',
    end_time         DATETIME        COMMENT '结束时间',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_call_id (call_id),
    INDEX idx_agent (agent_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通话记录表';

CREATE TABLE IF NOT EXISTS recording_record (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_record_id   BIGINT          COMMENT '通话记录ID',
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫ID',
    recording_url    VARCHAR(512)    COMMENT '录音文件URL',
    file_size        BIGINT          COMMENT '文件大小',
    duration_seconds INT             COMMENT '录音时长(秒)',
    format           VARCHAR(16)     COMMENT '录音格式',
    accessible       TINYINT(1)      DEFAULT 0 COMMENT '是否可访问',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_call_id (call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录音记录表';

CREATE TABLE IF NOT EXISTS service_record (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_record_id   BIGINT          COMMENT '通话记录ID',
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫ID',
    agent_id         BIGINT          COMMENT '坐席ID',
    disposition      VARCHAR(64)     COMMENT '处理结果',
    service_summary  TEXT            COMMENT '服务摘要',
    customer_id      BIGINT          COMMENT '关联客户ID',
    follow_up_action VARCHAR(64)     COMMENT '后续动作',
    ticket_id        BIGINT          COMMENT '关联工单ID',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_call_id (call_id),
    INDEX idx_agent (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务记录表';
