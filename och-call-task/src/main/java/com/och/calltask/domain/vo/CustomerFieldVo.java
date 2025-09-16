package com.och.calltask.domain.vo;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author danmo
 * @since 2025/06/27
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户字段配置")
@Data
public class CustomerFieldVo extends BaseVo {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    private Long id;


    /**
     * 字段显示名称
     */
    @Schema(description = "字段显示名称")
    private String fieldLabel;


    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    private String fieldName;


    /**
     * 是否启用 0-否 1-是
     */
    @Schema(description = "是否启用 0-禁用 1-启用")
    private Integer status;


    /**
     * 字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间
     */
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

    /**
     * 是否系统字段 0-系统字段 1-自定义字段
     */
    @Schema(description = "是否系统字段 0-系统字段 1-自定义字段")
    private Integer sysType;
}
