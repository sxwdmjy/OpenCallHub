package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 号码池实体，用于将多个电话号码分组管理。
 * 支持按池分配号码，用于呼入路由或外呼号码选择策略。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_number_pool")
public class NumberPool {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String poolName;
    private String poolCode;
    private Boolean enabled;
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
