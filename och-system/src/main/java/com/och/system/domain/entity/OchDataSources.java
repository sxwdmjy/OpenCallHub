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
 * 数据源管理表(OchDataSources)表实体类
 *
 * @author danmo
 * @since 2025-06-16 16:08:38
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("och_data_sources")
public class OchDataSources extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 638004339759621091L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  数据源名称 
     */
    @Schema(description = "数据源名称")
    @TableField("name")
    private String name;
    
    
     
    /**
     *  备注 
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
    
    
    
    
    
    
    


}

