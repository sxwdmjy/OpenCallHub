package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 呼叫事件实体
 * <p>
 * 记录 FreeSWITCH 上报的每个通话事件（创建、接听、桥接、挂断等），
 * 通过 dedupKey (callId + eventType + eventTime) 实现幂等去重，
 * processed 标记是否已完成事件处理。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_event")
public class CallEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String callId;
    private String eventType;
    private LocalDateTime eventTime;
    private String freeswitchUuid;
    private String legId;
    private String rawHeaders;
    private String dedupKey;
    private Boolean processed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
