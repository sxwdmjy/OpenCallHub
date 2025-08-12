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
 * 客户模板管理表(CustomerTemplate)表实体类
 *
 * @author danmo
 * @since 2025-06-30 11:35:45
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_template")
public class CustomerTemplate extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -98358023223876043L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  模板名称 
     */
    @Schema(description = "模板名称")
    @TableField("name")
    private String name;
    

     
    /**
     *  是否启用 0-否 1-是 
     */
    @Schema(description = "是否启用 0-否 1-是")
    @TableField("status")
    private Integer status;
    
    
    
    
    
    
    


}

