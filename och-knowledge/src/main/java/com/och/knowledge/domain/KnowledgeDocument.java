package com.och.knowledge.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 知识文档 */
@Data @TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO) private Long id;
    private String title;              // 标题
    private String content;            // 内容(HTML)
    private String summary;            // 摘要
    private Long categoryId;           // 分类ID
    private String tags;               // 标签(逗号分隔)
    private Integer viewCount;         // 浏览次数
    private Integer useCount;          // 使用次数
    private Boolean published;         // 是否发布
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE) private LocalDateTime updatedAt;
    @TableLogic private Integer deleted;
}
