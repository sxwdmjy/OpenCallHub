package com.och.calltask.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @since 2025-06-30
 */
@Schema(description = "客户模板添加参数")
@Data
public class CustomerTemplateAddQuery {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Schema(description = "模板名称")
    private String name;


    /**
     * 是否启用 0-否 1-是
     */
    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用 0-否 1-是")
    private Integer status;

    /**
     * 字段关系列表
     */
    @Size(min = 1, message = "字段列表不能为空")
    @Schema(description = "字段列表")
    private List<CustomerTemplateFieldRelAddQuery> fieldList;

}
