package com.och.calltask.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @since 2025-06-30
 */

@Schema(description = "客户模板字段关系")
@Data
public class CustomerTemplateFieldRelVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;


    @Schema(description = "字段ID")
    private Long fieldId;


    @Schema(description = "字段显示名称")
    private String fieldLabel;


    @Schema(description = "字段名称")
    private String fieldName;


    @Schema(description = "字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间")
    private Integer fieldType;

    @Schema(description = "是否必填 0-非必填 1-必填")
    private Integer required;


    @Schema(description = "字段选项")
    private String options;

    @Schema(description = "是否隐藏 0-否 1-是")
    private Integer hidden;

    @Schema(description = "排序")
    private Integer sort;

}
