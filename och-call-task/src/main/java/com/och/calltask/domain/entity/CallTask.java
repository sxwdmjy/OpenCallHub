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
 * @since 2025-06-18 15:53:57
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_task")
public class CallTask extends BaseEntity implements Serializable {
  private static final long serialVersionUID = -84826557394494890L;
   
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField("start_day")
    private Date startDay;
    
    
     
    /**
     *  任务结束时间 
     */
    @Schema(description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
     *  数据源ID 
     */
    @Schema(description = "数据源ID")
    @TableField("source_id")
    private Long sourceId;
    
    
     
    /**
     *  自动完成类型(0-是 1-否) 
     */
    @Schema(description = "自动完成类型(0-是 1-否)")
    @TableField("complete_type")
    private Integer completeType;
    
    
     
    /**
     *  号码模式(0-轮询) 
     */
    @Schema(description = "号码模式(0-轮询)")
    @TableField("phone_mode")
    private Integer phoneMode;
    
    
     
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
     *  轮次配置 
     */
    @Schema(description = "轮次配置")
    @TableField("rounds_conf")
    private String roundsConf;
    
    
     
    /**
     *  当前轮次 
     */
    @Schema(description = "当前轮次")
    @TableField("current_round")
    private Integer currentRound;
    
    
     
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

