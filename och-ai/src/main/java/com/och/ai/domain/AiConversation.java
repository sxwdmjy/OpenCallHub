package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 对话记录实体
 * <p>映射 ai_conversation 表，存储 AI 对话的完整记录，包括通话 ID、会话 ID、使用的 ASR/TTS/LLM 插件编码。</p>
 */
@Data
@TableName("ai_conversation")
public class AiConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String callId;
    private String sessionId;
    private String state;
    private String asrPluginCode;
    private String ttsPluginCode;
    private String llmPluginCode;
    private Integer turnCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
