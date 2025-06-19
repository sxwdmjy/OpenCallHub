package com.och.calltask.domain.query;


import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字段配置查询参数
 * @author danmo
 * @date 2025/06/16 15:21
 */
@Schema(description = "字段配置查询参数")
@Data
public class FieldQuery extends BaseQuery {

    @Schema(description = "字段ID")
    private Long id;

    @Schema(description = "字段ID数据集")
    private List<Long> idList;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private Integer fieldType;

    @Schema(description = "是否必填")
    private Integer required;
}
