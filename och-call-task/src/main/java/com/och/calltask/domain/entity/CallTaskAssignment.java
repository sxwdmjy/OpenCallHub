package com.och.calltask.domain.entity;

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
 * 任务分配联系人表(CallTaskAssignment)表实体类
 *
 * @author danmo
 * @since 2025-07-14 10:06:14
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_task_assignment")
public class CallTaskAssignment extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 926853607531361433L;
   
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
     *  手机号 
     */
    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;
    
    
     
    /**
     *  姓名 
     */
    @Schema(description = "姓名")
    @TableField("name")
    private String name;
    
    
     
    /**
     *  性别 0-未知 1-男 2-女 
     */
    @Schema(description = "性别 0-未知 1-男 2-女")
    @TableField("sex")
    private Integer sex;
    
    
     
    /**
     *  扩展字段 
     */
    @Schema(description = "扩展字段")
    @TableField("ext")
    private String ext;
    
    
     
    /**
     *  来源 0-人群导入 1-文件导入 2-API导入 
     */
    @Schema(description = "来源 0-人群导入 1-文件导入 2-API导入")
    @TableField("source")
    private Integer source;

    /**
     *  人群ID
     */
    @Schema(description = "人群ID")
    @TableField("crowd_id")
    private Long crowdId;

    /**
     *  模板ID
     */
    @Schema(description = "模板ID")
    @TableField("template_id")
    private Long templateId;
    
    
     
    /**
     *  分配状态 0-未分配 1-已分配 
     */
    @Schema(description = "分配状态 0-未分配 1-已分配")
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
    @TableField("assignment_time")
    private Date assignmentTime;
    
    
     
    /**
     *  拨打状态 0-未拨打 1-已拨打 
     */
    @Schema(description = "拨打状态 0-未拨打 1-已拨打")
    @TableField("call_status")
    private Integer callStatus;
    
    
     
    /**
     *  拨打次数 
     */
    @Schema(description = "拨打次数")
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

