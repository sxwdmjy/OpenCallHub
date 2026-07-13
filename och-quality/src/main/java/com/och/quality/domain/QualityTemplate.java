package com.och.quality.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 质检模板 */
@Data @TableName("quality_template")
public class QualityTemplate {
    @TableId(type = IdType.AUTO) private Long id;
    private String templateName;       // 模板名称
    private String templateCode;       // 模板编码
    private String rulesJson;          // 评分规则JSON
    private Integer totalScore;        // 总分
    private Integer passScore;         // 及格分
    private Boolean published;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE) private LocalDateTime updatedAt;
    @TableLogic private Integer deleted;
}
