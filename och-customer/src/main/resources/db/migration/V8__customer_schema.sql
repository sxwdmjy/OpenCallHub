-- openCallHub Customer Schema
-- V8: customer profile, phone

CREATE TABLE IF NOT EXISTS customer_profile (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_name    VARCHAR(128)    COMMENT '客户姓名',
    company_name     VARCHAR(256)    COMMENT '公司名称',
    level            VARCHAR(16)     DEFAULT 'NORMAL' COMMENT '客户等级',
    source           VARCHAR(32)     COMMENT '客户来源',
    tags             VARCHAR(512)    COMMENT '标签(逗号分隔)',
    owner_id         BIGINT          COMMENT '归属坐席ID',
    pool_id          BIGINT          COMMENT '客户池ID',
    created_by       BIGINT,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       BIGINT,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    status           VARCHAR(32)     DEFAULT 'ACTIVE',
    remark           VARCHAR(512),
    INDEX idx_owner (owner_id),
    INDEX idx_pool (pool_id),
    INDEX idx_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户资料表';

CREATE TABLE IF NOT EXISTS customer_phone (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id      BIGINT          NOT NULL COMMENT '客户ID',
    phone_number     VARCHAR(32)     NOT NULL COMMENT '电话号码',
    phone_type       VARCHAR(16)     DEFAULT 'MOBILE' COMMENT '号码类型',
    is_primary       TINYINT(1)      DEFAULT 0 COMMENT '是否主号码',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_phone_customer (phone_number, customer_id),
    INDEX idx_customer (customer_id),
    INDEX idx_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户电话表';
