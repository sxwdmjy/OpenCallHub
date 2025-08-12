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
 * 外呼任务表(CallTask)表实体类
 *
 * @author danmo
 * @since 2025-07-08 10:42:38
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_task")
public class CallTask extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -29835182615264258L;
   
    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  任务名称 
     */
    @Schema(description = "任务名称")
    @TableField("name")
    private String name;
    
    
     
    /**
     *  任务类型(0-预测 1-预览) 
     */
    @Schema(description = "任务类型(0-预测 1-预览)")
    @TableField("type")
    private Integer type;
    
    
     
    /**
     *  任务状态(0-未开始 1-进行中 2-暂停 3-结束) 
     */
    @Schema(description = "任务状态(0-未开始 1-进行中 2-暂停 3-结束)")
    @TableField("status")
    private Integer status;
    
    
     
    /**
     *  任务优先级 
     */
    @Schema(description = "任务优先级")
    @TableField("priority")
    private Integer priority;
    
    
     
    /**
     *  任务开始时间 
     */
    @Schema(description = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @TableField("start_day")
    private Date startDay;
    
    
     
    /**
     *  任务结束时间 
     */
    @Schema(description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @TableField("end_day")
    private Date endDay;
    
    
     
    /**
     *  每天开始时间 
     */
    @Schema(description = "每天开始时间")
    @TableField("s_time")
    private String sTime;
    
    
     
    /**
     *  每天结束时间 
     */
    @Schema(description = "每天结束时间")
    @TableField("e_time")
    private String eTime;
    
    
     
    /**
     *  周期时间 
     */
    @Schema(description = "周期时间")
    @TableField("work_cycle")
    private String workCycle;
    
    
     
    /**
     *  人群ID 
     */
    @Schema(description = "人群ID")
    @TableField("crowd_id")
    private Long crowdId;
    
    
     
    /**
     *  分配方式 1-轮流 2-空闲 
     */
    @Schema(description = "分配方式 1-轮流 2-空闲")
    @TableField("assignment_type")
    private Integer assignmentType;
    
    
     
    /**
     *  是否有限分配: 1-否 2-是 
     */
    @Schema(description = "是否有限分配: 1-否 2-是")
    @TableField("is_priority")
    private Integer isPriority;
    
    
     
    /**
     *  接待个数限制 
     */
    @Schema(description = "接待个数限制")
    @TableField("receive_limit")
    private Integer receiveLimit;
    
    
     
    /**
     *  执行坐席列表（为空全部坐席） 
     */
    @Schema(description = "执行坐席列表（为空全部坐席）")
    @TableField("agent_list")
    private String agentList;
    
    
     
    /**
     *  自动完成类型(0-是 1-否) 
     */
    @Schema(description = "自动完成类型(0-是 1-否)")
    @TableField("complete_type")
    private Integer completeType;
    
    
     
    /**
     *  外显号码池 
     */
    @Schema(description = "外显号码池")
    @TableField("phone_pool_id")
    private Long phonePoolId;
    
    
     
    /**
     *  转接类型(0-技能组 1-ivr 3-机器人) 
     */
    @Schema(description = "转接类型(0-技能组 1-ivr 3-机器人)")
    @TableField("transfer_type")
    private Integer transferType;
    
    
     
    /**
     *  转接类型值 
     */
    @Schema(description = "转接类型值")
    @TableField("transfer_value")
    private String transferValue;
    
    
     
    /**
     *  是否重呼(0-是 1-否) 
     */
    @Schema(description = "是否重呼(0-是 1-否)")
    @TableField("recall")
    private Integer recall;
    
    
     
    /**
     *  重呼次数 
     */
    @Schema(description = "重呼次数")
    @TableField("recall_num")
    private Integer recallNum;
    
    
     
    /**
     *  重呼间隔时长 
     */
    @Schema(description = "重呼间隔时长")
    @TableField("recall_time")
    private Integer recallTime;
    
    
     
    /**
     *  备注 
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
    
    
    
    
    
    
    


}

