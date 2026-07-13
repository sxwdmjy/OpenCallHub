package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通话记录实体
 * <p>
 * 通话结束后生成的归档记录，包含通话摘要统计信息：
 * 振铃时长、通话时长、保持次数、转接次数、录音URL、挂断原因等，
 * 用于报表统计和质检分析。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_record")
public class CallRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String callId;
    private String callerNumber;
    private String calledNumber;
    private String direction;
    private String state;
    private Long agentId;
    private Long queueId;
    private Integer ringSeconds;
    private Integer talkSeconds;
    private Integer holdCount;
    private Integer transferCount;
    private String recordingUrl;
    private Long customerId;
    private String endCause;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
