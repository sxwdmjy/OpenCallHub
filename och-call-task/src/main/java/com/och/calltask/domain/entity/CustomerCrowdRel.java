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
 * 人群客户关联表(CustomerCrowdRel)表实体类
 *
 * @author danmo
 * @since 2025-06-27 14:13:06
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("customer_crowd_rel")
public class CustomerCrowdRel extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 565380506433284797L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  人群ID 
     */
    @Schema(description = "人群ID")
    @TableField("crowd_id")
    private Long crowdId;
    
    
     
    /**
     *  客户ID 
     */
    @Schema(description = "客户ID")
    @TableField("customer_id")
    private Long customerId;
    
    
    
    
    
    
    


}

