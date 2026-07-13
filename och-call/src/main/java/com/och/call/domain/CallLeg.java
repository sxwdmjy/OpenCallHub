package com.och.call.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通话腿实体
 * <p>
 * 一通电话可能包含多个通道（leg），如 A-leg（主叫侧）和 B-leg（被叫侧）。
 * 每个 leg 对应一个 FreeSWITCH 通道，拥有独立的 UUID 和状态。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_leg")
public class CallLeg {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private String legId;
    private String legType;
    private String number;
    private String freeswitchUuid;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
