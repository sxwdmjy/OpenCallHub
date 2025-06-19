package com.och.calltask.domain.query;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author danmo
 * @date 2025/06/16 15:02
 */
@Schema(description = "新增字段配置入参")
@Data
public class FieldAddQuery {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    /**
     * 字段名称
     */
    @NotEmpty(message = "字段名称不能为空")
    @Schema(description = "字段名称",  requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;


    /**
     * 字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL
     */
    @NotNull(message = "字段类型不能为空")
    @Schema(description = "字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fieldType;


    /**
     * 是否必填 0-非必填 1-必填
     */
    @NotNull(message = "是否必填不能为空")
    @Schema(description = "是否必填 0-非必填 1-必填", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer required;


    /**
     * 字段长度
     */
    @Schema(description = "字段长度", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer fieldLength;
}
