-- openCallHub 质检中心 Schema
-- V15: 质检模板 / 评分 / 申诉

CREATE TABLE IF NOT EXISTS quality_template (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    template_name  VARCHAR(256) NOT NULL COMMENT '模板名称',
    template_code  VARCHAR(64)  NOT NULL COMMENT '模板编码',
    rules_json     TEXT         COMMENT '评分规则JSON',
    total_score    INT          DEFAULT 100 COMMENT '总分',
    pass_score     INT          DEFAULT 60 COMMENT '及格分',
    published      TINYINT(1)   DEFAULT 0,
    created_by     BIGINT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检模板';

CREATE TABLE IF NOT EXISTS quality_score (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    template_id    BIGINT       NOT NULL COMMENT '模板ID',
    call_record_id BIGINT       COMMENT '通话记录ID',
    call_id        VARCHAR(64)  COMMENT '呼叫ID',
    scorer_id      BIGINT       COMMENT '评分人',
    score_json     TEXT         COMMENT '详细评分JSON',
    total_score    INT          COMMENT '总分',
    pass_score     INT          COMMENT '及格分',
    type           VARCHAR(16)  DEFAULT 'MANUAL' COMMENT 'MANUAL/AI',
    comment        TEXT         COMMENT '评语',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_call (call_id),
    INDEX idx_scorer (scorer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检评分';

CREATE TABLE IF NOT EXISTS quality_appeal (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    score_id       BIGINT       NOT NULL COMMENT '评分ID',
    appellant_id   BIGINT       COMMENT '申诉人',
    reason         TEXT         COMMENT '申诉原因',
    state          VARCHAR(16)  DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reviewer_id    BIGINT       COMMENT '复核人',
    review_comment TEXT         COMMENT '复核意见',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at    DATETIME     COMMENT '复核时间',
    INDEX idx_score (score_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检申诉';
