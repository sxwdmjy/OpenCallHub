package com.och.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户档案实体，对应 customer_profile 表。
 * 记录客户基本信息、归属坐席、客户池及状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_profile")
public class CustomerProfile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerName;
    private String companyName;
    private String level;
    private String source;
    private String tags;
    private Long ownerId;
    private Long poolId;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Long updatedBy;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String status;
    private String remark;
}
