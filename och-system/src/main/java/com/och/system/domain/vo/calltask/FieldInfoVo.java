package com.och.system.domain.vo.calltask;


import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字段信息
 *
 * @author danmo
 * @date 2025/06/16 15:17
 */
@Schema(description = "字段配置出参")
@Data
public class FieldInfoVo extends BaseVo {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    private Long id;


    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    private String fieldName;


    /**
     * 字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL
     */
    @Schema(description = "字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL")
    private Integer fieldType;


    /**
     * 是否必填 0-非必填 1-必填
     */
    @Schema(description = "是否必填 0-非必填 1-必填")
    private Integer required;


    /**
     * 字段长度
     */
    @Schema(description = "字段长度")
    private Integer fieldLength;


    /**
     * 是否系统字段 0-系统字段 1-自定义字段
     */
    @Schema(description = "是否系统字段 0-系统字段 1-自定义字段")
    private Integer sysType;
}
