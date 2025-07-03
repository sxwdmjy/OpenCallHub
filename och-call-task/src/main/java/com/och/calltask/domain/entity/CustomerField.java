package com.och.calltask.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 客户字段管理表(CustomerField)表实体类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_field")
public class CustomerField extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 670064974807860665L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  字段显示名称 
     */
    @Schema(description = "字段显示名称")
    @TableField("field_label")
    private String fieldLabel;
    
    
     
    /**
     *  字段名称 
     */
    @Schema(description = "字段名称")
    @TableField("field_name")
    private String fieldName;
    
    
     
    /**
     *  是否启用 0-否 1-是 
     */
    @Schema(description = "是否启用 0-否 1-是")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间 
     */
    @Schema(description = "字段类型 0-电话 1-文本 2-数字 3-单选 4-多选 5-电子邮箱 6-日期 7-日期时间 8-时间")
    @TableField("field_type")
    private Integer fieldType;
    
    
     
    /**
     *  是否必填 0-非必填 1-必填 
     */
    @Schema(description = "是否必填 0-非必填 1-必填")
    @TableField("required")
    private Integer required;
    
    
     
    /**
     *  字段选项 
     */
    @Schema(description = "字段选项")
    @TableField("options")
    private String options;
    
    
     
    /**
     *  是否系统字段 0-系统字段 1-自定义字段 
     */
    @Schema(description = "是否系统字段 0-系统字段 1-自定义字段")
    @TableField("sys_type")
    private Integer sysType;


}

