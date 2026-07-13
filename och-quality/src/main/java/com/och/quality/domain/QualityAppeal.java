package com.och.quality.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 质检申诉 */
@Data @TableName("quality_appeal")
public class QualityAppeal {
    @TableId(type = IdType.AUTO) private Long id;
    private Long scoreId;              // 评分ID
    private Long appellantId;          // 申诉人
    private String reason;             // 申诉原因
    private String state;              // 状态: PENDING/APPROVED/REJECTED
    private Long reviewerId;           // 复核人
    private String reviewComment;      // 复核意见
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
