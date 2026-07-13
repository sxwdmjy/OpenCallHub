-- openCallHub ACD Schema
-- V6: skill groups, agent status, queue sessions

CREATE TABLE IF NOT EXISTS acd_skill_group (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    group_name       VARCHAR(128)    NOT NULL COMMENT '技能组名称',
    group_code       VARCHAR(64)     NOT NULL COMMENT '技能组编码',
    max_queue_size   INT             DEFAULT 50 COMMENT '最大排队数',
    max_wait_seconds INT             DEFAULT 300 COMMENT '最大等待秒数',
    dispatch_strategy VARCHAR(32)    DEFAULT 'IDLE_FIRST' COMMENT '分配策略',
    overflow_group_id BIGINT         COMMENT '溢出技能组ID',
    service_hours_json TEXT          COMMENT '服务时间JSON',
    enabled          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    remark           VARCHAR(512),
    UNIQUE INDEX uk_code (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能组表';

CREATE TABLE IF NOT EXISTS acd_agent_status (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id         BIGINT          NOT NULL COMMENT '坐席用户ID',
    skill_group_id   BIGINT          NOT NULL COMMENT '技能组ID',
    state            VARCHAR(32)     NOT NULL DEFAULT 'OFFLINE' COMMENT '坐席状态',
    max_concurrency  INT             DEFAULT 1 COMMENT '最大并发数',
    current_calls    INT             DEFAULT 0 COMMENT '当前通话数',
    total_answered   INT             DEFAULT 0 COMMENT '累计接听数',
    last_state_time  DATETIME        COMMENT '最后状态变化时间',
    last_call_time   DATETIME        COMMENT '最后通话时间',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_agent_group (agent_id, skill_group_id),
    INDEX idx_state (state),
    INDEX idx_group (skill_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='坐席状态表';

CREATE TABLE IF NOT EXISTS acd_queue_session (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_group_id   BIGINT          NOT NULL COMMENT '技能组ID',
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫ID',
    call_session_id  BIGINT          COMMENT '呼叫会话ID',
    caller_number    VARCHAR(32)     COMMENT '主叫号码',
    state            VARCHAR(32)     NOT NULL DEFAULT 'WAITING' COMMENT '队列状态: WAITING/ASSIGNED/ENDED',
    position         INT             COMMENT '排队位置',
    wait_seconds     INT             COMMENT '等待秒数',
    assigned_agent_id BIGINT         COMMENT '分配的坐席ID',
    enter_time       DATETIME        COMMENT '进入队列时间',
    assigned_time    DATETIME        COMMENT '分配时间',
    end_time         DATETIME        COMMENT '结束时间',
    end_cause        VARCHAR(64)     COMMENT '结束原因',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_group_state (skill_group_id, state),
    INDEX idx_call (call_id),
    INDEX idx_enter_time (enter_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACD队列会话表';
