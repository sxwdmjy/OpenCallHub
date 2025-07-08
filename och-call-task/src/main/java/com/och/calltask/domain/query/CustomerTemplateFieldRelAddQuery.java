package com.och.calltask.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @since 2025-06-30
 */
@Schema(description = "客户模板字段关系添加参数")
@Data
public class CustomerTemplateFieldRelAddQuery {

    @Schema(description = "字段ID")
    private Long fieldId;

    @Schema(description = "是否隐藏 0-否 1-是")
    private Integer hidden;

    @Schema(description = "排序")
    private Integer sort;
}
