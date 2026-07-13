-- openCallHub Outbound Task Schema
-- V10: outbound tasks, customers, dial attempts

CREATE TABLE IF NOT EXISTS outbound_task (
    id                  BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_name           VARCHAR(256)    NOT NULL COMMENT '任务名称',
    task_code           VARCHAR(64)     NOT NULL COMMENT '任务编码',
    mode                VARCHAR(16)     NOT NULL COMMENT '模式: PREVIEW/PREDICTIVE/ROBOT',
    state               VARCHAR(32)     NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
    number_pool_id      BIGINT          COMMENT '号码池ID',
    trunk_id            BIGINT          COMMENT '中继ID',
    max_concurrency     INT             DEFAULT 5 COMMENT '最大并发数',
    abandon_threshold   INT             DEFAULT 3 COMMENT '放弃率阈值(%)',
    schedule_config     TEXT            COMMENT '调度配置JSON',
    retry_config        TEXT            COMMENT '重试配置JSON',
    filter_config       TEXT            COMMENT '过滤配置JSON',
    total_customers     INT             DEFAULT 0 COMMENT '总客户数',
    completed_customers INT             DEFAULT 0 COMMENT '已完成数',
    connected_count     INT             DEFAULT 0 COMMENT '接通数',
    created_by          BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT(1)      NOT NULL DEFAULT 0,
    remark              VARCHAR(512),
    UNIQUE INDEX uk_code (task_code),
    INDEX idx_mode_state (mode, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外呼任务表';

CREATE TABLE IF NOT EXISTS outbound_customer (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id          BIGINT          NOT NULL COMMENT '任务ID',
    phone_number     VARCHAR(32)     NOT NULL COMMENT '电话号码',
    customer_name    VARCHAR(128)    COMMENT '客户姓名',
    state            VARCHAR(32)     NOT NULL DEFAULT 'IMPORTED' COMMENT '客户状态',
    attempt_count    INT             DEFAULT 0 COMMENT '尝试次数',
    next_retry_time  DATETIME        COMMENT '下次重试时间',
    filter_result    VARCHAR(256)    COMMENT '过滤结果',
    dial_result      VARCHAR(32)     COMMENT '拨打结果',
    agent_id         BIGINT          COMMENT '坐席ID',
    call_id          VARCHAR(64)     COMMENT '呼叫ID',
    robot_session_id VARCHAR(64)     COMMENT '机器人会话ID',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_state (task_id, state),
    INDEX idx_phone (phone_number),
    INDEX idx_agent (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外呼客户表';

CREATE TABLE IF NOT EXISTS outbound_dial_attempt (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id          BIGINT          NOT NULL COMMENT '任务ID',
    customer_id      BIGINT          NOT NULL COMMENT '客户ID',
    attempt_no       INT             NOT NULL COMMENT '第几次尝试',
    dedup_key        VARCHAR(256)    NOT NULL COMMENT '去重键: taskId:customerId:attemptNo',
    state            VARCHAR(32)     NOT NULL DEFAULT 'CREATED' COMMENT '拨号状态',
    dial_result      VARCHAR(32)     COMMENT '拨号结果',
    call_id          VARCHAR(64)     COMMENT '呼叫ID',
    agent_id         BIGINT          COMMENT '坐席ID',
    dial_time        DATETIME        COMMENT '拨号时间',
    answer_time      DATETIME        COMMENT '应答时间',
    end_time         DATETIME        COMMENT '结束时间',
    duration_seconds INT             COMMENT '通话时长(秒)',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_dedup (dedup_key),
    INDEX idx_task_customer (task_id, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外呼拨号尝试表';
