package com.och.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 对话轮次实体
 * <p>映射 ai_conversation_turn 表，存储 AI 对话中每一轮的详细信息，
 * 包括用户输入、AI 回复、意图、槽位值及各环节延迟。</p>
 */
@Data
@TableName("ai_conversation_turn")
public class AiConversationTurn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Integer turnNo;
    private String userText;
    private String aiResponse;
    private String intent;
    private String slotValuesJson;
    private Integer asrLatencyMs;
    private Integer ttsLatencyMs;
    private Integer llmLatencyMs;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
