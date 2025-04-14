package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.common.base.BaseEntity;
import com.och.common.domain.CallInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * 呼叫记录表(CallRecord)表实体类
 *
 * @author danmo
 * @since 2024-11-21 11:23:02
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_record")
public class CallRecord extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 373517321891756184L;

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 呼叫唯一ID
     */
    @Schema(description = "呼叫唯一ID")
    @TableField("call_id")
    private String callId;


    /**
     * 主叫号码
     */
    @Schema(description = "主叫号码")
    @TableField("caller_number")
    private String callerNumber;


    /**
     * 主叫显号
     */
    @Schema(description = "主叫显号")
    @TableField("caller_display_number")
    private String callerDisplayNumber;


    /**
     * 被叫号码
     */
    @Schema(description = "被叫号码")
    @TableField("callee_number")
    private String calleeNumber;


    /**
     * 被叫显号
     */
    @Schema(description = "被叫显号")
    @TableField("callee_display_number")
    private String calleeDisplayNumber;


    /**
     * 号码归属地
     */
    @Schema(description = "号码归属地")
    @TableField("number_location")
    private String numberLocation;


    /**
     * 坐席ID
     */
    @Schema(description = "坐席ID")
    @TableField("agent_id")
    private Long agentId;


    /**
     * 坐席号码
     */
    @Schema(description = "坐席号码")
    @TableField("agent_number")
    private String agentNumber;


    /**
     * 坐席名称
     */
    @Schema(description = "坐席名称")
    @TableField("agent_name")
    private String agentName;


    /**
     * 呼叫状态 1-成功 2-失败
     */
    @Schema(description = "呼叫状态 1-成功 2-失败")
    @TableField("call_state")
    private Integer callState;


    /**
     * 呼叫方式 1-呼出 2-呼入
     */
    @Schema(description = "呼叫方式 1-呼出 2-呼入")
    @TableField("direction")
    private Integer direction;


    /**
     * 呼叫开始时间
     */
    @Schema(description = "呼叫开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("call_start_time")
    private Date callStartTime;


    /**
     * 呼叫结束时间
     */
    @Schema(description = "呼叫结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("call_end_time")
    private Date callEndTime;


    /**
     * 应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通
     */
    @Schema(description = "应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通")
    @TableField("answer_flag")
    private Integer answerFlag;


    /**
     * 呼叫接通时间
     */
    @Schema(description = "呼叫接通时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("answer_time")
    private Date answerTime;


    /**
     * 振铃时间
     */
    @Schema(description = "振铃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("ringing_time")
    private Date ringingTime;


    /**
     * 挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机
     */
    @Schema(description = "挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机")
    @TableField("hangup_dir")
    private Integer hangupDir;


    /**
     * 挂机原因
     */
    @Schema(description = "挂机原因 ")
    @TableField("hangup_cause_code")
    private Integer hangupCauseCode;


    /**
     * 录音文件地址
     */
    @Schema(description = "录音文件地址")
    @TableField("file_path")
    private String filePath;


    /**
     * 振铃文件地址
     */
    @Schema(description = "振铃文件地址")
    @TableField("ringing_path")
    private String ringingPath;


    public void transfer(CallInfo callInfo) {
        this.setCallId(String.valueOf(callInfo.getCallId()));
        this.setAgentId(callInfo.getAgentId());
        this.setAgentNumber(callInfo.getAgentNumber());
        this.setAgentName(callInfo.getAgentName());
        this.setCallerNumber(callInfo.getCaller());
        this.setCallerDisplayNumber(callInfo.getCallerDisplay());
        this.setCalleeNumber(callInfo.getCallee());
        this.setCalleeDisplayNumber(callInfo.getCalleeDisplay());
        this.setNumberLocation(callInfo.getNumberLocation());
        if(Objects.nonNull(callInfo.getCallTime())){
            this.setCallStartTime(new Date(callInfo.getCallTime()));
        }
        if(Objects.nonNull(callInfo.getEndTime())){
            this.setCallEndTime(new Date(callInfo.getEndTime()));
        }

        this.setDirection(callInfo.getDirection());
        this.setAnswerFlag(callInfo.getAnswerCount());
        if (Objects.nonNull(callInfo.getAnswerTime())) {
            this.setAnswerTime(new Date(callInfo.getAnswerTime()));
        }
        if (Objects.nonNull(callInfo.getRecordStartTime())) {
            this.setRingingTime(new Date(callInfo.getRecordStartTime()));
        }

        this.setHangupDir(callInfo.getHangupDir());
        this.setHangupCauseCode(callInfo.getHangupCause());
        this.setFilePath(callInfo.getRecord());
        //this.setRingingPath(callInfo.getRecord());
        //this.setCallState(callInfo.getProcess().getCode());
    }
}

