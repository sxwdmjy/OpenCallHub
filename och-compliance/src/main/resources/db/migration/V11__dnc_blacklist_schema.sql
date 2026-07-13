-- openCallHub Compliance Schema
-- V11: DNC and blacklist

CREATE TABLE IF NOT EXISTS compliance_dnc (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone_number     VARCHAR(32)     NOT NULL COMMENT '电话号码',
    reason           VARCHAR(256)    COMMENT 'DNC原因',
    source           VARCHAR(64)     COMMENT '来源',
    added_by         BIGINT          COMMENT '添加人',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at        DATETIME        COMMENT '过期时间',
    enabled          TINYINT(1)      NOT NULL DEFAULT 1,
    INDEX idx_phone (phone_number),
    INDEX idx_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DNC列表';

CREATE TABLE IF NOT EXISTS compliance_blacklist (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone_number     VARCHAR(32)     NOT NULL COMMENT '电话号码',
    reason           VARCHAR(256)    COMMENT '黑名单原因',
    level            VARCHAR(16)     DEFAULT 'LOW' COMMENT '级别',
    added_by         BIGINT,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled          TINYINT(1)      NOT NULL DEFAULT 1,
    UNIQUE INDEX uk_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单';
