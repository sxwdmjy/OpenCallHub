package com.och.calltask.domain.query;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author danmo
 * @date 2025/06/16 15:02
 */
@Schema(description = "新增字段配置入参")
@Data
public class CustomerFieldAddQuery {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    /**
     * 字段显示名称
     */
    @NotBlank(message = "字段显示名称不能为空")
    @Schema(description = "字段显示名称")
    private String fieldLabel;


    /**
     * 字段名称
     */
    @NotBlank(message = "字段名称不能为空")
    @Schema(description = "字段名称")
    private String fieldName;


    /**
     * 是否启用 0-否 1-是
     */
    @NotBlank(message = "状态不能为空")
    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;


    /**
     * 字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间
     */
    @NotNull(message = "字段类型不能为空")
    @Schema(description = "字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间")
    private Integer fieldType;


    /**
     * 是否必填 0-非必填 1-必填
     */
    @Schema(description = "是否必填 0-非必填 1-必填")
    private Integer required;


    /**
     * 字段选项
     */
    @Schema(description = "字段选项")
    private String options;

}
