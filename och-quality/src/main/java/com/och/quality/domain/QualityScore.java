package com.och.quality.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 质检评分 */
@Data @TableName("quality_score")
public class QualityScore {
    @TableId(type = IdType.AUTO) private Long id;
    private Long templateId;           // 模板ID
    private Long callRecordId;         // 通话记录ID
    private String callId;             // 呼叫ID
    private Long scorerId;             // 评分人
    private String scoreJson;          // 详细评分JSON
    private Integer totalScore;        // 总分
    private Integer passScore;         // 是否及格
    private String type;               // 类型: MANUAL/AI
    private String comment;            // 评语
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
