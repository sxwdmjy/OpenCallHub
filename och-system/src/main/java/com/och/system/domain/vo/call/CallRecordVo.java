package com.och.system.domain.vo.call;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class CallRecordVo extends BaseVo {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;


    /**
     * 呼叫唯一ID
     */
    @Schema(description = "呼叫唯一ID")
    private String callId;


    /**
     * 主叫号码
     */
    @Schema(description = "主叫号码")
    private String callerNumber;


    /**
     * 主叫显号
     */
    @Schema(description = "主叫显号")
    private String callerDisplayNumber;


    /**
     * 被叫号码
     */
    @Schema(description = "被叫号码")
    private String calleeNumber;


    /**
     * 被叫显号
     */
    @Schema(description = "被叫显号")
    private String calleeDisplayNumber;


    /**
     * 号码归属地
     */
    @Schema(description = "号码归属地")
    private String numberLocation;


    /**
     * 坐席ID
     */
    @Schema(description = "坐席ID")
    private Long agentId;


    /**
     * 坐席号码
     */
    @Schema(description = "坐席号码")
    private String agentNumber;


    /**
     * 坐席名称
     */
    @Schema(description = "坐席名称")
    private String agentName;


    /**
     * 呼叫状态 1-成功 2-失败
     */
    @Schema(description = "呼叫状态 1-成功 2-失败")
    private Integer callState;


    /**
     * 呼叫方式 1-呼出 2-呼入
     */
    @Schema(description = "呼叫方式 1-呼出 2-呼入")
    private Integer direction;


    /**
     * 呼叫开始时间
     */
    @Schema(description = "呼叫开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date callStartTime;


    /**
     * 呼叫结束时间
     */
    @Schema(description = "呼叫结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date callEndTime;


    /**
     * 应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通
     */
    @Schema(description = "应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通")
    private Integer answerFlag;


    /**
     * 呼叫接通时间
     */
    @Schema(description = "呼叫接通时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date answerTime;


    /**
     * 振铃时间
     */
    @Schema(description = "振铃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ringingTime;


    /**
     * 挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机
     */
    @Schema(description = "挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机")
    private Integer hangupDir;


    /**
     * 挂机原因
     */
    @Schema(description = "挂机原因 ")
    private Integer hangupCauseCode;


    /**
     * 录音文件地址
     */
    @Schema(description = "录音文件地址")
    private String filePath;


    /**
     * 振铃文件地址
     */
    @Schema(description = "振铃文件地址")
    private String ringingPath;
}
