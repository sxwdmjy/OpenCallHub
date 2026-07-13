package com.och.acd.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ACD座席状态实体
 * <p>
 * 记录座席在技能组中的实时状态，包括当前状态、最大并发呼叫数、
 * 当前通话数、累计接听量、最后状态/通话时间等。
 * 用于队列分配时的座席选择和状态管理。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("acd_agent_status")
public class AcdAgentStatus {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long agentId;
    private Long skillGroupId;
    private String state;
    private Integer skillLevel;
    private Integer maxConcurrency;
    private Integer currentCalls;
    private Integer totalAnswered;
    private LocalDateTime lastStateTime;
    private LocalDateTime lastCallTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
