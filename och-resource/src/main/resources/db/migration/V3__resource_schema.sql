-- openCallHub Resource Schema
-- V3: resource management tables

-- 资源依赖关系表
CREATE TABLE IF NOT EXISTS res_dependency (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_type     VARCHAR(64)     NOT NULL COMMENT '源资源类型',
    source_id       BIGINT          NOT NULL COMMENT '源资源ID',
    target_type     VARCHAR(64)     NOT NULL COMMENT '目标资源类型',
    target_id       BIGINT          NOT NULL COMMENT '目标资源ID',
    relation        VARCHAR(64)     NOT NULL DEFAULT 'DEPENDS_ON' COMMENT '关系类型',
    remark          VARCHAR(512)    COMMENT '备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source (source_type, source_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源依赖关系表';

-- 资源审计表
CREATE TABLE IF NOT EXISTS res_audit (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    trace_id        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '链路追踪ID',
    user_id         BIGINT          COMMENT '操作用户ID',
    username        VARCHAR(64)     COMMENT '操作用户名',
    resource_type   VARCHAR(64)     NOT NULL COMMENT '资源类型',
    resource_id     BIGINT          NOT NULL COMMENT '资源ID',
    action          VARCHAR(32)     NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE',
    before_data     TEXT            COMMENT '变更前数据(JSON)',
    after_data      TEXT            COMMENT '变更后数据(JSON)',
    remark          VARCHAR(512)    COMMENT '备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源审计表';

-- SIP账号表
CREATE TABLE IF NOT EXISTS res_sip_account (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(128)    NOT NULL COMMENT 'SIP用户名',
    password        VARCHAR(256)    NOT NULL COMMENT 'SIP密码',
    display_name    VARCHAR(128)    COMMENT '显示名称',
    realm           VARCHAR(128)    COMMENT '域',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用',
    max_calls       INT             DEFAULT 5 COMMENT '最大并发数',
    created_by      BIGINT          COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT          COMMENT '更新人',
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'ACTIVE',
    remark          VARCHAR(512),
    UNIQUE INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SIP账号表';

-- 分机表
CREATE TABLE IF NOT EXISTS res_extension (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    extension_number VARCHAR(32)    NOT NULL COMMENT '分机号',
    sip_account_id  BIGINT          COMMENT '关联SIP账号',
    user_id         BIGINT          COMMENT '绑定用户ID',
    display_name    VARCHAR(128)    COMMENT '显示名称',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'IDLE',
    remark          VARCHAR(512),
    UNIQUE INDEX uk_extension (extension_number),
    INDEX idx_sip_account (sip_account_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分机表';

-- 电话号码表
CREATE TABLE IF NOT EXISTS res_phone_number (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone_number    VARCHAR(32)     NOT NULL COMMENT '电话号码',
    number_pool_id  BIGINT          COMMENT '所属号码池',
    provider        VARCHAR(64)     COMMENT '运营商',
    line_type       VARCHAR(32)     COMMENT '线路类型',
    area_code       VARCHAR(16)     COMMENT '区号',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'IDLE',
    remark          VARCHAR(512),
    UNIQUE INDEX uk_phone (phone_number),
    INDEX idx_pool (number_pool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电话号码表';

-- 号码池表
CREATE TABLE IF NOT EXISTS res_number_pool (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pool_name       VARCHAR(128)    NOT NULL COMMENT '号码池名称',
    pool_code       VARCHAR(64)     NOT NULL COMMENT '号码池编码',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'ACTIVE',
    remark          VARCHAR(512),
    UNIQUE INDEX uk_code (pool_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='号码池表';

-- 音频素材表
CREATE TABLE IF NOT EXISTS res_audio_asset (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    asset_name      VARCHAR(256)    NOT NULL COMMENT '素材名称',
    asset_code      VARCHAR(128)    NOT NULL COMMENT '素材编码',
    file_key        VARCHAR(256)    NOT NULL COMMENT '文件存储key',
    file_size       BIGINT          COMMENT '文件大小',
    duration_sec    INT             COMMENT '时长(秒)',
    format_type     VARCHAR(16)     COMMENT '格式',
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'ACTIVE',
    remark          VARCHAR(512),
    INDEX idx_code (asset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音频素材表';

-- AI插件资源表
CREATE TABLE IF NOT EXISTS res_ai_plugin (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plugin_name     VARCHAR(128)    NOT NULL COMMENT '插件名称',
    plugin_type     VARCHAR(32)     NOT NULL COMMENT '类型: ASR/TTS/LLM/WORKFLOW',
    provider        VARCHAR(64)     NOT NULL COMMENT '供应商',
    endpoint_url    VARCHAR(512)    COMMENT '端点URL',
    api_key         VARCHAR(512)    COMMENT 'API密钥(加密存储)',
    api_secret      VARCHAR(512)    COMMENT 'API密钥(加密存储)',
    config_json     TEXT            COMMENT '配置JSON',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(32)     DEFAULT 'ACTIVE',
    remark          VARCHAR(512),
    INDEX idx_type (plugin_type),
    INDEX idx_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI插件资源表';
