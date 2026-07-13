package com.och.calltask.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 外呼拨号尝试实体
 * <p>
 * 记录每次向外呼客户打电话的尝试，包括尝试序号、去重键、状态、拨号结果、
 * 关联座席、呼叫ID、时间节点（拨号/接听/结束）和通话时长。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("outbound_dial_attempt")
public class OutboundDialAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long customerId;
    private Integer attemptNo;
    private String dedupKey;
    private String state;
    private String dialResult;
    private String callId;
    private Long agentId;
    private LocalDateTime dialTime;
    private LocalDateTime answerTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
