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
 * 客户人群管理表(CustomerCrowd)表实体类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_crowd")
public class CustomerCrowd extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 503912467637058533L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  人群名称 
     */
    @Schema(description = "人群名称")
    @TableField("name")
    private String name;
    
     
    /**
     *  是否启用 0-否 1-是 
     */
    @Schema(description = "是否启用 0-否 1-是")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  更新方式 1-手动 2-自动 
     */
    @Schema(description = "更新方式 1-手动 2-自动")
    @TableField("type")
    private Integer type;
    
    
     
    /**
     *  进度 1-待计算 2-计算中 3-计算完成 4-计算失败 
     */
    @Schema(description = "进度 1-待计算 2-计算中 3-计算完成 4-计算失败")
    @TableField("progress")
    private Integer progress;
    
    
     
    /**
     *  失败原因 
     */
    @Schema(description = "失败原因")
    @TableField("reason")
    private String reason;
    
    
     
    /**
     *  策略条件 
     */
    @Schema(description = "策略条件")
    @TableField("swipe")
    private String swipe;
    
    
     
    /**
     *  人群数量 
     */
    @Schema(description = "人群数量")
    @TableField("crowd_num")
    private Integer crowdNum;
    
    
     
    /**
     *  备注 
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
    
    
    
    
    
    
    


}

