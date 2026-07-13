package com.och.calltask.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 人群-客户关联实体，对应 customer_crowd_rel 表。
 * <p>
 * 记录人群与客户档案的多对多关系，通过 crowdId 关联人群，customerId 关联 CustomerProfile。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_crowd_rel")
public class CustomerCrowdRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人群ID，关联 customer_crowd.id */
    private Long crowdId;

    /** 客户ID，关联 customer_profile.id */
    private Long customerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
