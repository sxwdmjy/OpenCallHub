package com.och.compliance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Do-Not-Call（DNC）记录实体，对应 compliance_dnc 表。
 * 存储禁止拨打号码的过期时间和来源信息，用于外呼合规检查。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compliance_dnc")
public class DncRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phoneNumber;
    private String reason;
    private String source;
    private Long addedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private Boolean enabled;
}
