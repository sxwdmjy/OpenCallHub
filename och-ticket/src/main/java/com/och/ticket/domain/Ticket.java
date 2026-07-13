package com.och.ticket.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 工单 */
@Data @TableName("ticket")
public class Ticket {
    @TableId(type = IdType.AUTO) private Long id;
    private String ticketNo;           // 工单编号
    private String title;              // 标题
    private String description;        // 描述
    private String state;              // 状态
    private String priority;           // 优先级: LOW/MEDIUM/HIGH/URGENT
    private Long customerId;           // 关联客户
    private Long callRecordId;         // 关联通话记录
    private Long assigneeId;           // 处理人
    private Long creatorId;            // 创建人
    private String slaLevel;           // SLA等级
    private LocalDateTime slaDeadline; // SLA截止时间
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE) private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    @TableLogic private Integer deleted;
    private String remark;
}
