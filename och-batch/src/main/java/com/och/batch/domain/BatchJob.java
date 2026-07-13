package com.och.batch.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 批处理任务 */
@Data @TableName("batch_job")
public class BatchJob {
    @TableId(type = IdType.AUTO) private Long id;
    private String jobName;            // 任务名称
    private String jobType;            // 类型: IMPORT/EXPORT
    private String state;              // 状态: PENDING/RUNNING/COMPLETED/FAILED
    private Integer totalItems;        // 总条目
    private Integer processedItems;    // 已处理
    private Integer successItems;      // 成功数
    private Integer errorItems;        // 失败数
    private String filePath;           // 文件路径
    private String errorFileUrl;       // 错误明细导出URL
    private String errorMessage;       // 任务失败原因
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
