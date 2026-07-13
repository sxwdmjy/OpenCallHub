package com.och.calltask.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 外呼客户实体
 * <p>
 * 每个外呼任务下的目标客户，记录号码、姓名、状态、拨打次数、重试时间、
 * 过滤结果、拨号结果、关联座席、通话ID、机器人会话ID等信息。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("outbound_customer")
public class OutboundCustomer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String phoneNumber;
    private String customerName;
    private String state;
    private Integer attemptCount;
    private LocalDateTime nextRetryTime;
    private String filterResult;
    private String dialResult;
    private Long agentId;
    private String callId;
    private String robotSessionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
