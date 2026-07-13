-- openCallHub 通知消息 Schema
-- V16: 通知消息

CREATE TABLE IF NOT EXISTS notice_message (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(256) NOT NULL COMMENT '标题',
    content       TEXT         COMMENT '内容',
    type          VARCHAR(32)  DEFAULT 'INFO' COMMENT 'SYSTEM/WARNING/INFO',
    sender_id     BIGINT       COMMENT '发送者',
    sender_name   VARCHAR(64),
    receiver_id   BIGINT       NOT NULL COMMENT '接收者',
    is_read       TINYINT(1)   DEFAULT 0 COMMENT '已读',
    read_time     DATETIME     COMMENT '读取时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息';
