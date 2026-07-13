package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 技能实体
 * <p>映射 call_skill 表，存储技能配置信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_skill")
public class CallSkill {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组ID */
    @TableField("group_id")
    private Long groupId;

    /** 技能名称 */
    @TableField("name")
    private String name;

    /** 优先级 */
    @TableField("priority")
    private Integer priority;

    /** 描述 */
    @TableField("`describe`")
    private String describe;

    /** 策略类型：0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长 */
    @TableField("strategy_type")
    private Integer strategyType;

    /** 全忙策略：0-排队 1-溢出 2-挂机 */
    @TableField("full_busy_type")
    private Integer fullBusyType;

    /** 溢出策略：0-挂机 1-转IVR */
    @TableField("overflow_type")
    private Integer overflowType;

    /** 溢出策略值 */
    @TableField("overflow_value")
    private String overflowValue;

    /** 排队超时时间（秒） */
    @TableField("time_out")
    private Integer timeOut;

    /** 最大排队人数 */
    @TableField("queue_length")
    private Integer queueLength;

    /** 排队音 */
    @TableField("queue_voice")
    private Long queueVoice;

    /** 转坐席音 */
    @TableField("agent_voice")
    private Long agentVoice;

    /** 主叫号码池 */
    @TableField("caller_phone_pool")
    private Long callerPhonePool;

    /** 被叫号码池 */
    @TableField("callee_phone_pool")
    private Long calleePhonePool;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

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
        this.agentVoice= query.getAgentVoice();
        this.callerPhonePool= query.getCallerPhonePool();
        this.calleePhonePool= query.getCalleePhonePool();
    }
}

