package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.AiConversationTurn;

/**
 * AI 对话轮次 Mapper
 * <p>提供 ai_conversation_turn 表的基础 CRUD 操作。
 * ai_conversation_turn 表存储 AI 对话中每一轮的详细记录，
 * 包括用户输入、AI 回复、识别到的意图、槽位值以及 ASR/TTS/LLM 各环节的延迟数据。</p>
 */
public interface AiConversationTurnMapper extends BaseMapper<AiConversationTurn> {

}
