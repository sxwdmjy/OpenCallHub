package com.och.batch.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 批处理条目明细，对应 batch_job_item 表。
 * 每条记录对应一个导入/导出条目的处理结果，用于追踪失败明细和支持重试。
 */
@Data
@TableName("batch_job_item")
public class BatchJobItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long jobId;

    /** 行号/序号 */
    private Integer rowIndex;

    /** 条目唯一标识（如订单号、客户ID） */
    private String itemKey;

    /** 状态：PENDING / SUCCESS / FAILED / FAILED_RETRY */
    private String state;

    /** 失败原因 */
    private String errorMessage;

    /** 原始数据（JSON） */
    private String rawData;

    /** 处理时间 */
    private LocalDateTime processedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
