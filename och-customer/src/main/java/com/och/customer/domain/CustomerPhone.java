package com.och.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户电话实体，对应 customer_phone 表。
 * 一个客户可关联多个电话号码，支持标记主号码。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_phone")
public class CustomerPhone {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private String phoneNumber;
    private String phoneType;
    private Boolean isPrimary;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
