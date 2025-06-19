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
 * 数据源联系人表(OchDataSourcesContact)表实体类
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("och_data_sources_contact")
public class OchDataSourcesContact extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 266705675364432889L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  数据源ID 
     */
    @Schema(description = "数据源ID")
    @TableField("source_id")
    private Long sourceId;
    
    
     
    /**
     *  联系人数据 
     */
    @Schema(description = "联系人数据")
    @TableField("contact")
    private String contact;

    
    
    
    


}

