package com.och.calltask.domain.query;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 数据源新增参数
 * @author danmo
 * @date 2025/06/16 16:14
 */
@Schema(description = "数据源新增参数")
@Data
public class DataSourceAddQuery {

    @Schema(description = "主键ID", hidden = true)
    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    @Schema(description = "数据源名称")
    private String name;

    @Schema(description = "数据源备注")
    private String remark;

    @Schema(description = "字段ID数据集")
    private List<Long> fieldIdList;
}
