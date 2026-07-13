package com.och.resource.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源依赖关系实体，记录资源之间的引用关系。
 * 用于在删除资源前检查是否存在被依赖的情况，防止误删。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("res_dependency")
public class ResourceDependency {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;
    private Long sourceId;
    private String targetType;
    private Long targetId;
    private String relation;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
