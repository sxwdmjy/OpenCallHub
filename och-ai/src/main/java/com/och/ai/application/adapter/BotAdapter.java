package com.och.ai.application.adapter;

import com.och.ai.domain.AiConversationTurn;

/**
 * 机器人引擎适配器接口
 * <p>所有外部 AI 平台（Dify/RAGFlow/LangFlow）和内置引擎都实现此接口，
 * 统一对话处理契约，包含意图识别和槽位填充。</p>
 */
public interface BotAdapter {

    /**
     * AI 回复结果
     *
     * @param text          回复文本（供 TTS 播报）
     * @param intent        识别到的意图编码
     * @param slotValuesJson 槽位值 JSON（如 {"product":"A", "city":"北京"}）
     * @param latencyMs     调用耗时（毫秒）
     */
    record BotResponse(String text, String intent, String slotValuesJson, int latencyMs) {}

    /** 引擎类型标识：DIFY / RAGFLOW / LANGFLOW / INTERNAL */
    String getType();

    /**
     * 处理用户输入
     *
     * @param userInput    用户语音转文字后的输入
     * @param context      对话上文（前几轮的摘要文本）
     * @param systemPrompt 系统提示词
     * @param turnNo       当前轮次号（从 1 开始）
     * @return AI 回复 + 意图 + 槽位 + 耗时
     */
    BotResponse process(String userInput, String context, String systemPrompt, Integer turnNo);

    /**
     * 构建对话轮次记录
     */
    default AiConversationTurn recordTurn(Long conversationId, int turnNo, String userInput, BotResponse response) {
        AiConversationTurn turn = new AiConversationTurn();
        turn.setConversationId(conversationId);
        turn.setTurnNo(turnNo);
        turn.setUserText(userInput);
        turn.setAiResponse(response.text());
        turn.setIntent(response.intent());
        turn.setSlotValuesJson(response.slotValuesJson());
        turn.setLlmLatencyMs(response.latencyMs());
        return turn;
    }
}
