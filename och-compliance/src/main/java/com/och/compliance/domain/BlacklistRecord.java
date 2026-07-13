package com.och.compliance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 黑名单记录实体，对应 compliance_blacklist 表。
 * 存储被标记为拒绝服务的号码及分级信息，用于外呼前校验。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compliance_blacklist")
public class BlacklistRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phoneNumber;
    private String reason;
    private String level;
    private Long addedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Boolean enabled;
}
