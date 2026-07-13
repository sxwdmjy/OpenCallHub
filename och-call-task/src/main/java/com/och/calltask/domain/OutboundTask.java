package com.och.calltask.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 外呼任务实体
 * <p>
 * 定义外呼活动的核心配置：任务名称/编码、外呼模式（预览/预测/机器人）、
 * 号码池、中继、并发控制、放弃阈值、调度配置、重试策略、过滤配置等。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("outbound_task")
public class OutboundTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String taskCode;
    private String mode;
    private String state;
    private Long numberPoolId;
    private Long trunkId;
    private Integer maxConcurrency;
    private Integer abandonThreshold;
    private String scheduleConfig;
    private String retryConfig;
    private String filterConfig;
    private Integer totalCustomers;
    private Integer completedCustomers;
    private Integer connectedCount;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String remark;
}
