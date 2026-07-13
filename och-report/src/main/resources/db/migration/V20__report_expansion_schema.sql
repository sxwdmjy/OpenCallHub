-- openCallHub Report Expansion Schema
-- V20: outbound daily, queue daily, AI robot daily aggregation tables

CREATE TABLE IF NOT EXISTS report_outbound_daily (
    id                     BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date            DATE            NOT NULL COMMENT '报表日期',
    task_id                BIGINT          NOT NULL COMMENT '外呼任务ID',
    task_name              VARCHAR(128)    DEFAULT '' COMMENT '任务名称',
    total_customers        INT             DEFAULT 0 COMMENT '总客户数',
    completed_customers    INT             DEFAULT 0 COMMENT '已完成客户数',
    connected_count        INT             DEFAULT 0 COMMENT '接通数',
    total_attempts         INT             DEFAULT 0 COMMENT '总拨打次数',
    answered_attempts      INT             DEFAULT 0 COMMENT '应答次数',
    failed_attempts        INT             DEFAULT 0 COMMENT '失败次数',
    total_duration_seconds INT             DEFAULT 0 COMMENT '总通话时长(秒)',
    avg_duration_seconds   DECIMAL(10,2)   DEFAULT 0 COMMENT '平均通话时长(秒)',
    created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_task_date (task_id, report_date),
    INDEX idx_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外呼日报表';

CREATE TABLE IF NOT EXISTS report_queue_daily (
    id                     BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date            DATE            NOT NULL COMMENT '报表日期',
    skill_group_id         BIGINT          NOT NULL COMMENT '技能组ID',
    skill_group_name       VARCHAR(128)    DEFAULT '' COMMENT '技能组名称',
    total_queued           INT             DEFAULT 0 COMMENT '进入队列总数',
    total_answered         INT             DEFAULT 0 COMMENT '已分配应答数',
    total_abandoned        INT             DEFAULT 0 COMMENT '放弃数',
    total_timeout          INT             DEFAULT 0 COMMENT '超时数',
    avg_wait_seconds       DECIMAL(10,2)   DEFAULT 0 COMMENT '平均等待时长(秒)',
    max_wait_seconds       INT             DEFAULT 0 COMMENT '最大等待时长(秒)',
    sla_target_seconds     INT             DEFAULT 0 COMMENT 'SLA目标时长(秒)',
    sla_met_count          INT             DEFAULT 0 COMMENT 'SLA达标数',
    sla_rate               DECIMAL(5,4)    DEFAULT NULL COMMENT 'SLA达标率',
    created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_group_date (skill_group_id, report_date),
    INDEX idx_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='队列日报表';

CREATE TABLE IF NOT EXISTS report_ai_robot_daily (
    id                     BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date            DATE            NOT NULL COMMENT '报表日期',
    total_sessions         INT             DEFAULT 0 COMMENT '总会话数',
    completed_sessions     INT             DEFAULT 0 COMMENT '正常结束会话数',
    failed_sessions        INT             DEFAULT 0 COMMENT '失败会话数',
    interrupted_sessions   INT             DEFAULT 0 COMMENT '中断会话数',
    total_turns            INT             DEFAULT 0 COMMENT '总交互轮次',
    avg_turns_per_session  DECIMAL(10,2)   DEFAULT 0 COMMENT '平均轮次',
    total_asr_latency_ms   BIGINT          DEFAULT 0 COMMENT 'ASR总延迟(毫秒)',
    total_tts_latency_ms   BIGINT          DEFAULT 0 COMMENT 'TTS总延迟(毫秒)',
    total_llm_latency_ms   BIGINT          DEFAULT 0 COMMENT 'LLM总延迟(毫秒)',
    avg_asr_latency_ms     INT             DEFAULT 0 COMMENT '平均ASR延迟(毫秒)',
    avg_tts_latency_ms     INT             DEFAULT 0 COMMENT '平均TTS延迟(毫秒)',
    avg_llm_latency_ms     INT             DEFAULT 0 COMMENT '平均LLM延迟(毫秒)',
    total_input_tokens     BIGINT          DEFAULT 0 COMMENT '输入Token总数',
    total_output_tokens    BIGINT          DEFAULT 0 COMMENT '输出Token总数',
    total_cost             DECIMAL(10,4)   DEFAULT 0 COMMENT '总成本',
    created_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI机器人日报表';
