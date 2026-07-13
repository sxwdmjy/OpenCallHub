package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通话会话实体
 * <p>
 * 记录每一通电话的完整生命周期信息，包括主被叫号码、方向、状态、IVR/队列/座席关联、
 * 时间节点（开始、接听、结束）、通话时长、挂断原因、录音URL等。
 * 会话通过 callId 全局唯一标识，与 FreeSWITCH 的 UUID 关联。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_session")
public class CallSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String callId;
    private String callerNumber;
    private String calledNumber;
    private String direction;
    private String state;
    private String freeswitchUuid;
    private String routeInfo;
    private Long ivrFlowId;
    private Long ivrExecutionId;
    private Long queueId;
    private Long agentId;
    private LocalDateTime startTime;
    private LocalDateTime answerTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private String endCause;
    private String recordingUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
    private String remark;
}
