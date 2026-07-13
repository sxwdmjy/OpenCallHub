package com.och.calltask.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户人群定义实体，对应 customer_crowd 表。
 * <p>
 * 人群通过 poolId 关联客户池（CustomerProfile.poolId），
 * 通过 filterCondition 定义筛选条件，支持定时/手动计算人群成员。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_crowd")
public class CustomerCrowd {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人群名称 */
    private String name;

    /** 关联客户池ID，对应 customer_profile.pool_id */
    private Long poolId;

    /** 筛选条件（JSON格式，如 {"level":"VIP","status":"ACTIVE","tags":"新客户"}） */
    private String filterCondition;

    /** 人群成员数量 */
    private Integer crowdNum;

    /** 计算进度：1-待计算，2-计算中，3-计算完成，4-计算失败 */
    private Integer progress;

    /** 计算失败原因 */
    private String reason;

    /** 是否启用：0-禁用，1-启用 */
    private Integer status;

    /** 更新方式：1-手动，2-自动 */
    private Integer type;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
