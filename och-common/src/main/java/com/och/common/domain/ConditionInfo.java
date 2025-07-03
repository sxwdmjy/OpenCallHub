package com.och.common.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 查询条件
 * @author danmo
 * @date 2025/7/3 11:39
 */
@Schema
@Data
public class ConditionInfo {

    @Schema(description = "字段ID")
    private Long fieldId;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "关系 1-等于 2-不等于 3-不等于 4-大于等于 5-小于 6-小于等于 7-区间 8-为空 9-不为空 10-包含 11-不包含")
    private Integer relation;

    @Schema(description = "字段值")
    private List<String> value;
}
