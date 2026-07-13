package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务记录实体
 * <p>
 * 座席在通话结束后提交的后处理记录，包括处置结果（disposition）、
 * 服务小结、客户ID、后续动作、关联工单ID等。
 * 是通话后质检和客户跟进的核心数据。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_record")
public class ServiceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long callRecordId;
    private String callId;
    private Long agentId;
    private String disposition;
    private String serviceSummary;
    private Long customerId;
    private String followUpAction;
    private Long ticketId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
