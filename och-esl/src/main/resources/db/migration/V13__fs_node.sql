-- FreeSWITCH 节点管理表
-- 存储 FreeSWITCH 服务器的 ESL 连接信息，支持多节点管理
CREATE TABLE IF NOT EXISTS `fs_node` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `node_id`     VARCHAR(64)  NOT NULL                COMMENT '节点唯一标识（如 fs-primary、fs-backup）',
    `node_name`   VARCHAR(128) DEFAULT ''              COMMENT '节点名称（显示用）',
    `host`        VARCHAR(128) NOT NULL                COMMENT 'ESL 服务地址',
    `port`        INT          DEFAULT 8021            COMMENT 'ESL 端口',
    `password`    VARCHAR(64)  DEFAULT 'ClueCon'       COMMENT 'ESL 认证密码',
    `enabled`     TINYINT      DEFAULT 1               COMMENT '是否启用 0-禁用 1-启用',
    `remark`      VARCHAR(255) DEFAULT ''              COMMENT '备注',
    `create_by`   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    TINYINT      DEFAULT 0               COMMENT '删除标志 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_id` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FreeSWITCH节点表';

-- 默认插入本地开发节点
INSERT INTO `fs_node` (`node_id`, `node_name`, `host`, `port`, `password`, `enabled`, `remark`)
VALUES ('fs-local', '本地FreeSWITCH', '127.0.0.1', 8021, 'ClueCon', 1, '开发环境默认节点');
