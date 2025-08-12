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
 * 客户模板字段关联表(CustomerTemplateFieldRel)表实体类
 *
 * @author danmo
 * @since 2025-06-30 11:35:45
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_template_field_rel")
public class CustomerTemplateFieldRel extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 381280812977341673L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  模板ID 
     */
    @Schema(description = "模板ID")
    @TableField("template_id")
    private Long templateId;
    
    
     
    /**
     *  字段ID 
     */
    @Schema(description = "字段ID")
    @TableField("field_id")
    private Long fieldId;
    
    
     
    /**
     *  是否隐藏 0-否 1-是 
     */
    @Schema(description = "是否隐藏 0-否 1-是")
    @TableField("hidden")
    private Integer hidden;
    
    
     
    /**
     *  排序 
     */
    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;
    
    
    
    
    
    
    


}

