-- openCallHub AI 机器人引擎 Schema
-- V12: 机器人流程、版本、会话、意图、插件调用日志、成本记录

-- 机器人流程定义
CREATE TABLE IF NOT EXISTS robot_flow (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flow_name         VARCHAR(256)    NOT NULL COMMENT '流程名称',
    flow_code         VARCHAR(64)     NOT NULL COMMENT '流程编码',
    bot_type          VARCHAR(32)     DEFAULT 'INTERNAL' COMMENT '引擎类型: DIFY/RAGFLOW/LANGFLOW/INTERNAL',
    greeting_text     VARCHAR(512)    COMMENT '欢迎语',
    max_turns         INT             DEFAULT 10 COMMENT '最大对话轮数',
    silence_timeout_sec INT           DEFAULT 5 COMMENT '静音超时(秒)',
    transfer_condition VARCHAR(512)   COMMENT '转人工条件(意图列表逗号分隔)',
    enabled           TINYINT(1)      NOT NULL DEFAULT 1,
    created_by        BIGINT,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT(1)      NOT NULL DEFAULT 0,
    remark            VARCHAR(512),
    UNIQUE INDEX uk_code (flow_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机器人流程定义';

-- 机器人流程版本
CREATE TABLE IF NOT EXISTS robot_flow_version (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flow_id             BIGINT        NOT NULL COMMENT '流程ID',
    version_no          INT           NOT NULL COMMENT '版本号',
    llm_plugin_code     VARCHAR(64)   COMMENT 'LLM插件编码',
    asr_plugin_code     VARCHAR(64)   COMMENT 'ASR插件编码',
    tts_plugin_code     VARCHAR(64)   COMMENT 'TTS插件编码',
    system_prompt       TEXT          COMMENT '系统提示词',
    intent_definitions_json TEXT      COMMENT '意图定义JSON',
    slot_definitions_json   TEXT      COMMENT '槽位定义JSON',
    transfer_rules_json TEXT          COMMENT '转人工规则JSON',
    state               VARCHAR(32)   DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/DISABLED',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_by        BIGINT        COMMENT '发布人',
    published_at        DATETIME      COMMENT '发布时间',
    INDEX idx_flow (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机器人流程版本';

-- 机器人会话记录
CREATE TABLE IF NOT EXISTS robot_session (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id             VARCHAR(64)   NOT NULL COMMENT '呼叫ID',
    session_id          VARCHAR(64)   NOT NULL COMMENT '会话ID',
    flow_id             BIGINT        COMMENT '流程ID',
    version_id          BIGINT        COMMENT '版本ID',
    state               VARCHAR(32)   DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ENDED/TRANSFERRED',
    current_intent      VARCHAR(64)   COMMENT '当前意图',
    slot_values_json    TEXT          COMMENT '槽位值JSON',
    summary             TEXT          COMMENT '对话摘要',
    tags                VARCHAR(256)  COMMENT '标签',
    turn_count          INT           DEFAULT 0 COMMENT '对话轮数',
    total_asr_latency_ms INT          DEFAULT 0 COMMENT 'ASR累计延迟(ms)',
    total_tts_latency_ms INT          DEFAULT 0 COMMENT 'TTS累计延迟(ms)',
    total_llm_latency_ms INT          DEFAULT 0 COMMENT 'LLM累计延迟(ms)',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at            DATETIME      COMMENT '结束时间',
    end_reason          VARCHAR(64)   COMMENT '结束原因',
    UNIQUE INDEX uk_session (session_id),
    INDEX idx_call (call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机器人会话记录';

-- AI 对话主记录
CREATE TABLE IF NOT EXISTS ai_conversation (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id             VARCHAR(64)   NOT NULL COMMENT '呼叫ID',
    session_id          VARCHAR(64)   NOT NULL COMMENT '会话ID',
    state               VARCHAR(32)   DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ENDED/TRANSFERRED',
    asr_plugin_code     VARCHAR(64)   COMMENT 'ASR插件编码',
    tts_plugin_code     VARCHAR(64)   COMMENT 'TTS插件编码',
    llm_plugin_code     VARCHAR(64)   COMMENT 'LLM或Workflow插件编码',
    turn_count          INT           DEFAULT 0 COMMENT '对话轮数',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at            DATETIME      COMMENT '结束时间',
    UNIQUE INDEX uk_session (session_id),
    INDEX idx_call (call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话主记录';

-- AI 对话轮次记录
CREATE TABLE IF NOT EXISTS ai_conversation_turn (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    conversation_id     BIGINT        NOT NULL COMMENT 'AI对话ID',
    turn_no             INT           NOT NULL COMMENT '轮次号',
    user_text           TEXT          COMMENT '用户文本',
    ai_response         TEXT          COMMENT 'AI回复',
    intent              VARCHAR(64)   COMMENT '识别意图',
    slot_values_json    TEXT          COMMENT '槽位值JSON',
    asr_latency_ms      INT           DEFAULT 0 COMMENT 'ASR延迟(ms)',
    tts_latency_ms      INT           DEFAULT 0 COMMENT 'TTS延迟(ms)',
    llm_latency_ms      INT           DEFAULT 0 COMMENT 'LLM或Workflow延迟(ms)',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_conversation_turn (conversation_id, turn_no),
    INDEX idx_intent (intent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话轮次记录';

-- 意图定义
CREATE TABLE IF NOT EXISTS robot_intent (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flow_id           BIGINT          NOT NULL COMMENT '流程ID',
    intent_name       VARCHAR(128)    NOT NULL COMMENT '意图名称',
    intent_code       VARCHAR(64)     NOT NULL COMMENT '意图编码',
    description       VARCHAR(512)    COMMENT '意图描述',
    examples_json     TEXT            COMMENT '训练例句JSON',
    responses_json    TEXT            COMMENT '预设回复JSON',
    action            VARCHAR(32)     COMMENT '动作: CONTINUE/TRANSFER/HANGUP',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_flow (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机器人意图定义';

-- 插件调用日志
CREATE TABLE IF NOT EXISTS plugin_call_log (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id        VARCHAR(64)     COMMENT '会话ID',
    call_id           VARCHAR(64)     COMMENT '呼叫ID',
    plugin_type       VARCHAR(16)     NOT NULL COMMENT '插件类型: ASR/TTS/LLM/WORKFLOW',
    plugin_code       VARCHAR(64)     NOT NULL COMMENT '插件编码',
    turn_no           INT             COMMENT '对话轮次',
    request_data      TEXT            COMMENT '请求参数',
    response_data     TEXT            COMMENT '响应数据',
    latency_ms        INT             COMMENT '响应耗时(ms)',
    success           TINYINT(1)      DEFAULT 1 COMMENT '是否成功',
    error_message     VARCHAR(512)    COMMENT '错误信息',
    cost              DECIMAL(10,6)   DEFAULT 0 COMMENT '调用费用',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_call (call_id),
    INDEX idx_plugin (plugin_type, plugin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI插件调用日志';

-- AI 调用成本记录
CREATE TABLE IF NOT EXISTS ai_cost_record (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id           VARCHAR(64)     COMMENT '呼叫ID',
    session_id        VARCHAR(64)     COMMENT '会话ID',
    plugin_type       VARCHAR(16)     NOT NULL COMMENT '插件类型',
    plugin_code       VARCHAR(64)     COMMENT '插件编码',
    model_name        VARCHAR(64)     COMMENT '模型名称',
    input_tokens      INT             DEFAULT 0 COMMENT '输入Token数',
    output_tokens     INT             DEFAULT 0 COMMENT '输出Token数',
    cost              DECIMAL(10,6)   DEFAULT 0 COMMENT '费用(元)',
    record_date       DATE            NOT NULL COMMENT '记录日期',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date (record_date),
    INDEX idx_call (call_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI调用成本记录';

-- 播报打断检测（预留）
CREATE TABLE IF NOT EXISTS ai_barge_in_log (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id        VARCHAR(64)     COMMENT '会话ID',
    call_id           VARCHAR(64)     COMMENT '呼叫ID',
    turn_no           INT             COMMENT '轮次',
    audio_energy_db   DECIMAL(6,2)    COMMENT '音频能量(dB)',
    duration_ms       INT             COMMENT '打断时长(ms)',
    trigger_time      DATETIME        COMMENT '触发时间',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI播报打断检测日志';
