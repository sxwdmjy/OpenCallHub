package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 字段管理表(OchFieldInfo)表实体类
 *
 * @author danmo
 * @since 2025-06-16 14:53:21
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("och_field_info")
public class OchFieldInfo extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -63619362379715748L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  字段名称 
     */
    @Schema(description = "字段名称")
    @TableField("field_name")
    private String fieldName;
    
    
     
    /**
     *  字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL 
     */
    @Schema(description = "字段类型 0-PHONE 1-STRING 2-LONG 3-FLOAT 4-INTEGER 5-BOOLEAN 6-EMAIL")
    @TableField("field_type")
    private Integer fieldType;
    
    
     
    /**
     *  是否必填 0-非必填 1-必填 
     */
    @Schema(description = "是否必填 0-非必填 1-必填")
    @TableField("required")
    private Integer required;
    
    
     
    /**
     *  字段长度 
     */
    @Schema(description = "字段长度")
    @TableField("field_length")
    private Integer fieldLength;
    
    
     
    /**
     *  是否系统字段 0-系统字段 1-自定义字段 
     */
    @Schema(description = "是否系统字段 0-系统字段 1-自定义字段")
    @TableField("sys_type")
    private Integer sysType;


}

