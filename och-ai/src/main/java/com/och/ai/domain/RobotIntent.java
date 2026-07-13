package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机器人意图实体
 * <p>映射 robot_intent 表，存储机器人流程中定义的意图信息：意图名称、编码、示例语句、回复模板和触发动作。</p>
 */
@Data
@TableName("robot_intent")
public class RobotIntent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long flowId;
    private String intentName;
    private String intentCode;
    private String description;
    private String examplesJson;
    private String responsesJson;
    private String action;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
