package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 技能表(CallSkill)表实体类
 *
 * @author danmo
 * @since 2024-10-29 14:21:52
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_skill")
public class CallSkill extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 195826343045315436L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    @TableField("group_id")
    private Long groupId;


    /**
     * 技能名称
     */
    @Schema(description = "技能名称")
    @TableField("name")
    private String name;


    /**
     * 话后空闲时间 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    @Schema(description = "话后空闲时间 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h")
    @TableField("after_time")
    private Integer afterTime;


    /**
     * 优先级
     */
    @Schema(description = "优先级")
    @TableField("priority")
    private Integer priority;


    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("describe")
    private String describe;


    /**
     * 策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长
     */
    @Schema(description = "策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长")
    @TableField("strategy_type")
    private Integer strategyType;


    /**
     * 全忙 0-排队 1-溢出 2-挂机
     */
    @Schema(description = "全忙 0-排队 1-溢出 2-挂机")
    @TableField("full_busy_type")
    private Integer fullBusyType;


    /**
     * 溢出策略 0-挂机 1-转IVR
     */
    @Schema(description = "溢出策略 0-挂机 1-转IVR")
    @TableField("overflow_type")
    private Integer overflowType;


    /**
     * 溢出策略值
     */
    @Schema(description = "溢出策略值")
    @TableField("overflow_value")
    private String overflowValue;


    /**
     * 排队超时时间（秒）
     */
    @Schema(description = "排队超时时间（秒）")
    @TableField("time_out")
    private Integer timeOut;


    /**
     * 最大排队人数
     */
    @Schema(description = "最大排队人数")
    @TableField("queue_length")
    private Integer queueLength;


    /**
     * 排队音
     */
    @Schema(description = "排队音")
    @TableField("queue_voice")
    private Long queueVoice;


    /**
     * 排队音
     */
    @Schema(description = "排队音")
    @TableField("queue_voice_value")
    private String queueVoiceValue;


    /**
     * 转坐席音
     */
    @Schema(description = "转坐席音")
    @TableField("agent_voice")
    private Long agentVoice;


    /**
     * 转坐席音名称
     */
    @Schema(description = "转坐席音名称")
    @TableField("agent_voice_value")
    private String agentVoiceValue;

    public void setQuery2Entity(CallSkillAddQuery query) {
        this.id = query.getId();
        this.groupId = query.getGroupId();
        this.name= query.getName();
        this.describe= query.getDescribe();
        this.strategyType= query.getStrategyType();
        this.fullBusyType= query.getFullBusyType();
        this.overflowType= query.getOverflowType();
        this.overflowValue= query.getAgentVoiceValue();
        this.timeOut= query.getTimeOut();
        this.queueLength= query.getQueueLength();
        this.queueVoice= query.getQueueVoice();
        this.queueVoiceValue= query.getQueueVoiceValue();
        this.agentVoice= query.getAgentVoice();
        this.agentVoiceValue= query.getAgentVoiceValue();
    }
}

