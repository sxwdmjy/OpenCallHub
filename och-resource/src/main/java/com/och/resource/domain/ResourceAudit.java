package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源审计日志实体，记录资源的所有变更操作。
 * 包括创建、更新、删除等操作，记录操作前后的数据快照。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_audit")
public class ResourceAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceId;
    private Long userId;
    private String username;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String beforeData;
    private String afterData;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
