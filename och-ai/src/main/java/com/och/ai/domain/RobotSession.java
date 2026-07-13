package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机器人会话实体
 * <p>映射 robot_session 表，存储 AI 机器人通话会话的运行时状态：当前意图、槽位值、会话摘要、标签、各环节累计延迟。</p>
 */
@Data
@TableName("robot_session")
public class RobotSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String callId;
    private String sessionId;
    private Long flowId;
    private Long versionId;
    private String state;
    private String currentIntent;
    private String slotValuesJson;
    private String summary;
    private String tags;
    private Integer turnCount;
    private Integer totalAsrLatencyMs;
    private Integer totalTtsLatencyMs;
    private Integer totalLlmLatencyMs;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private String endReason;
}
