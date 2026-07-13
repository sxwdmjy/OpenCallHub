package com.och.acd.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ACD队列会话实体
 * <p>
 * 记录呼叫在技能组队列中的排队信息，包括位置、等待时间、分配座席、进入/分配/结束时间等。
 * 呼叫进入队列后由 {@link com.och.acd.application.QueueDispatchServiceImpl} 负责分配。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("acd_queue_session")
public class AcdQueueSession {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skillGroupId;
    private String callId;
    private Long callSessionId;
    private String callerNumber;
    private String state;
    private Integer position;
    private Integer waitSeconds;
    private Long assignedAgentId;
    private LocalDateTime enterTime;
    private LocalDateTime assignedTime;
    private LocalDateTime endTime;
    private String endCause;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
