package com.och.calltask.domain.vo;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @since 2025-06-30
 */

@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户模板")
@Data
public class CustomerTemplateVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "是否启用 0-否 1-是")
    private Integer status;

    @Schema(description = "字段列表")
    private List<CustomerTemplateFieldRelVo> fieldList;
}
