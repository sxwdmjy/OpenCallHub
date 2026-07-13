package com.och.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户跟进记录，对应 customer_follow_up 表。
 * 记录坐席对客户的每次跟进交互，关联通话记录和工单。
 */
@Data
@TableName("customer_follow_up")
public class CustomerFollowUp {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long agentId;
    private Long callRecordId;
    private Long ticketId;
    private String followType;     // CALL/VISIT/MESSAGE/EMAIL
    private String content;
    private String result;
    private String nextAction;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
