package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机器人流程版本实体
 * <p>映射 robot_flow_version 表，存储流程版本快照，包含 LLM/ASR/TTS 插件配置、系统提示词、意图定义、槽位定义和转人工规则。</p>
 */
@Data
@TableName("robot_flow_version")
public class RobotFlowVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long flowId;
    private Integer versionNo;
    private String llmPluginCode;
    private String asrPluginCode;
    private String ttsPluginCode;
    private String systemPrompt;
    private String intentDefinitionsJson;
    private String slotDefinitionsJson;
    private String transferRulesJson;
    private String state;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private Long publishedBy;
    private LocalDateTime publishedAt;
}
