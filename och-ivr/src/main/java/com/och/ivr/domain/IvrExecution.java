package com.och.ivr.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * IVR执行实例实体
 * <p>
 * 记录单次呼叫的IVR执行状态，包括关联的通话ID、流程版本、当前节点、
 * 执行状态和起止时间。用于追踪呼叫在IVR流程中的实时位置。
 * </p>
 */
@Data
@TableName("ivr_execution")
public class IvrExecution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String callId;
    private Long flowId;
    private Long versionId;
    private String state;
    private String currentNodeId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
