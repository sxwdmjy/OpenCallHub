-- openCallHub Call Core Schema
-- V4: call sessions, legs, events with dedup

CREATE TABLE IF NOT EXISTS call_session (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫唯一ID',
    caller_number    VARCHAR(32)     COMMENT '主叫号码',
    called_number    VARCHAR(32)     COMMENT '被叫号码',
    direction        VARCHAR(16)     COMMENT '方向: INBOUND/OUTBOUND',
    state            VARCHAR(32)     NOT NULL DEFAULT 'NEW' COMMENT '呼叫状态',
    freeswitch_uuid  VARCHAR(64)     COMMENT 'FreeSWITCH UUID',
    route_info       VARCHAR(512)    COMMENT '路由信息',
    ivr_flow_id      BIGINT          COMMENT 'IVR流程ID',
    ivr_execution_id BIGINT          COMMENT 'IVR执行ID',
    queue_id         BIGINT          COMMENT 'ACD队列ID',
    agent_id         BIGINT          COMMENT '坐席ID',
    start_time       DATETIME        COMMENT '开始时间',
    answer_time      DATETIME        COMMENT '应答时间',
    end_time         DATETIME        COMMENT '结束时间',
    duration_seconds INT             COMMENT '通话时长(秒)',
    end_cause        VARCHAR(64)     COMMENT '结束原因',
    recording_url    VARCHAR(512)    COMMENT '录音URL',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    remark           VARCHAR(512),
    UNIQUE INDEX uk_call_id (call_id),
    INDEX idx_state (state),
    INDEX idx_start_time (start_time),
    INDEX idx_agent (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫会话表';

CREATE TABLE IF NOT EXISTS call_leg (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id       BIGINT          NOT NULL COMMENT '会话ID',
    leg_id           VARCHAR(64)     NOT NULL COMMENT 'Leg ID',
    leg_type         VARCHAR(16)     COMMENT 'Leg类型: A_LEG/B_LEG',
    number           VARCHAR(32)     COMMENT '号码',
    freeswitch_uuid  VARCHAR(64)     COMMENT 'FreeSWITCH UUID',
    state            VARCHAR(32)     COMMENT 'Leg状态',
    start_time       DATETIME        COMMENT '开始时间',
    end_time         DATETIME        COMMENT '结束时间',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_leg_id (leg_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫Leg表';

CREATE TABLE IF NOT EXISTS call_event (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id          VARCHAR(64)     NOT NULL COMMENT '呼叫ID',
    event_type       VARCHAR(32)     NOT NULL COMMENT '事件类型',
    event_time       DATETIME        NOT NULL COMMENT '事件时间',
    freeswitch_uuid  VARCHAR(64)     COMMENT 'FreeSWITCH UUID',
    leg_id           VARCHAR(64)     COMMENT 'Leg ID',
    raw_headers      TEXT            COMMENT '原始ESL头信息',
    dedup_key        VARCHAR(256)    COMMENT '去重键: callId+eventType+eventTime',
    processed        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已处理',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_dedup (dedup_key),
    INDEX idx_call_id (call_id),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫事件表(幂等)';
