-- openCallHub 知识库与话术 Schema
-- V14: 知识文档 / 话术模板

CREATE TABLE IF NOT EXISTS knowledge_document (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(256) NOT NULL COMMENT '标题',
    content        MEDIUMTEXT   COMMENT '内容(HTML)',
    summary        VARCHAR(512) COMMENT '摘要',
    category_id    BIGINT       COMMENT '分类ID',
    tags           VARCHAR(512) COMMENT '标签',
    view_count     INT          DEFAULT 0 COMMENT '浏览次数',
    use_count      INT          DEFAULT 0 COMMENT '使用次数',
    published      TINYINT(1)   DEFAULT 0,
    created_by     BIGINT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识文档';

CREATE TABLE IF NOT EXISTS script_template (
    id               BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    script_name      VARCHAR(256) NOT NULL COMMENT '话术名称',
    script_code      VARCHAR(64)  NOT NULL COMMENT '话术编码',
    category         VARCHAR(64)  COMMENT '分类',
    content          TEXT         COMMENT '话术内容',
    keywords         VARCHAR(512) COMMENT '关键词',
    current_version  INT          DEFAULT 1,
    published        TINYINT(1)   DEFAULT 0,
    created_by       BIGINT,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT(1)   NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_code (script_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术模板';
