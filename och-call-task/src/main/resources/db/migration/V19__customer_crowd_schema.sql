-- openCallHub Customer Crowd Schema
-- V19: customer crowd definition and crowd-customer relationship

CREATE TABLE IF NOT EXISTS customer_crowd (
    id               BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(128)    NOT NULL COMMENT '人群名称',
    pool_id          BIGINT          COMMENT '关联客户池ID',
    filter_condition TEXT            COMMENT '筛选条件JSON',
    crowd_num        INT             DEFAULT 0 COMMENT '人群成员数量',
    progress         INT             DEFAULT 1 COMMENT '计算进度: 1-待计算, 2-计算中, 3-计算完成, 4-计算失败',
    reason           VARCHAR(512)    COMMENT '计算失败原因',
    status           INT             DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    type             INT             DEFAULT 1 COMMENT '更新方式: 1-手动, 2-自动',
    remark           VARCHAR(512)    COMMENT '备注',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    INDEX idx_pool_id (pool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户人群定义表';

CREATE TABLE IF NOT EXISTS customer_crowd_rel (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    crowd_id    BIGINT          NOT NULL COMMENT '人群ID',
    customer_id BIGINT          NOT NULL COMMENT '客户ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT(1)      NOT NULL DEFAULT 0,
    INDEX idx_crowd_deleted (crowd_id, deleted),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人群-客户关联表';
