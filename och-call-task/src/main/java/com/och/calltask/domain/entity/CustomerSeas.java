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
 * 客户公海表(CustomerSeas)表实体类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_seas")
public class CustomerSeas extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 455670806553262271L;
   
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
     *  第三方ID 
     */
    @Schema(description = "第三方ID")
    @TableField("third_id")
    private String thirdId;
    
    
     
    /**
     *  客户数据 
     */
    @Schema(description = "客户数据")
    @TableField("customer_info")
    private String customerInfo;
    
    
     
    /**
     *  虚列客户手机号 
     */
    @Schema(description = "虚列客户手机号")
    @TableField("phone")
    private String phone;
    
    
     
    /**
     *  虚列客户名称 
     */
    @Schema(description = "虚列客户名称")
    @TableField("name")
    private String name;
    
    
     
    /**
     *  虚列客户性别 0-未知 1-男 2-女 
     */
    @Schema(description = "虚列客户性别 0-未知 1-男 2-女")
    @TableField("sex")
    private Integer sex;
    
    
     
    /**
     *  来源 0-手动创建 1-文件导入 2-API导入 
     */
    @Schema(description = "来源 0-手动创建 1-文件导入 2-API导入")
    @TableField("source")
    private Integer source;
    
    
    
    
    
    
    


}

