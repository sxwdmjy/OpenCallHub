package com.och.ticket.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 工单流转记录 */
@Data @TableName("ticket_flow")
public class TicketFlow {
    @TableId(type = IdType.AUTO) private Long id;
    private Long ticketId;
    private String fromState;
    private String toState;
    private Long operatorId;
    private String operatorName;
    private String comment;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
