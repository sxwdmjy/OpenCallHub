package com.och.calltask.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 任务分配表(CallTaskAssignment)表实体类
 *
 * @author danmo
 * @since 2025-07-08 09:36:32
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_task_assignment")
public class CallTaskAssignment extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 622683314526782267L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  任务ID 
     */
    @Schema(description = "任务ID")
    @TableField("task_id")
    private Long taskId;
    
    
     
    /**
     *  客户ID 
     */
    @Schema(description = "客户ID")
    @TableField("customer_id")
    private Long customerId;
    
    
     
    /**
     *  0-未分配 1-已分配 
     */
    @Schema(description = "0-未分配 1-已分配")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  坐席ID 
     */
    @Schema(description = "坐席ID")
    @TableField("agent_id")
    private Long agentId;
    
    
     
    /**
     *  分配时间 
     */
    @Schema(description = "分配时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("assignment")
    private Date assignment;
    
    
     
    /**
     *  拨打状态 0-未拨打 1-已拨打 
     */
    @Schema(description = "拨打状态 0-未拨打 1-已拨打")
    @TableField("call_status")
    private Integer callStatus;
    
    
     
    /**
     *  完成时间 
     */
    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("completed_time")
    private Date completedTime;
    
    
     
    /**
     *  尝试次数 
     */
    @Schema(description = "尝试次数")
    @TableField("attempt_count")
    private Integer attemptCount;
    
    
     
    /**
     *  计划呼叫时间(预约回访) 
     */
    @Schema(description = "计划呼叫时间(预约回访)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("scheduled_time")
    private Date scheduledTime;
    
    
     
    /**
     *  备注 
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
    
    
    
    
    
    
    


}

