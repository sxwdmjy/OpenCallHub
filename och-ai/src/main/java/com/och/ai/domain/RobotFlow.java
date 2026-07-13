package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机器人流程实体
 * <p>映射 robot_flow 表，定义 AI 机器人对话流程的全局配置：bot 引擎类型、问候语、最大轮次、静默超时和转人工条件。</p>
 */
@Data
@TableName("robot_flow")
public class RobotFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String flowName;
    private String flowCode;
    private String botType;
    private String greetingText;
    private Integer maxTurns;
    private Integer silenceTimeoutSec;
    private String transferCondition;
    private Boolean enabled;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    private String remark;
}
