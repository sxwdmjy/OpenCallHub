package com.och.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.ai.domain.AiConversation;

/**
 * AI 对话记录 Mapper
 * <p>提供 ai_conversation 表的基础 CRUD 操作。
 * ai_conversation 表存储 AI 对话的完整记录，包括关联的通话 ID、会话 ID、
 * 对话状态、使用的 ASR/TTS/LLM 插件编码以及轮次计数等信息。</p>
 */
public interface AiConversationMapper extends BaseMapper<AiConversation> {

}
