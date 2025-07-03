package com.och.calltask.domain.query;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @since 2025-06-30 11:35:44
 */

@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户模板查询参数")
@Data
public class CustomerTemplateQuery extends BaseQuery {

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板ID列表")
    private List<Long> templateIds;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "是否启用 0-否 1-是")
    private Integer status;
}
