package com.och.ai.service.impl;

import com.och.ai.domain.AiConversation;
import com.och.ai.domain.AiConversationTurn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI 插件调用服务
 * <p>提供 ASR（语音识别）、TTS（语音合成）、LLM（大语言模型）三大核心 AI 能力的统一调用入口。
 * 当前返回可预测的模拟结果，真实供应商调用后续由插件配置和适配器统一接入。</p>
 */
@Slf4j
@Service
public class AiPluginInvokeServiceImpl implements com.och.ai.service.IAiPluginInvokeService {

    public record AsrResult(String text, int latencyMs, boolean isFinal) {}

    public record TtsResult(String audioUrl, int latencyMs) {}

    public record LlmResult(String responseText, String intent, String slotValuesJson, int latencyMs) {}

    /**
     * ASR: 语音转文字
     */
    public AsrResult invokeAsr(String audioData, String pluginCode) {
        log.info("[AI-ASR] Invoking ASR plugin: {}, dataSize={}", pluginCode, audioData != null ? audioData.length() : 0);
        return new AsrResult("模拟语音识别结果", 200, true);
    }

    /**
     * TTS: 文字转语音
     */
    public TtsResult invokeTts(String text, String voice, String pluginCode) {
        log.info("[AI-TTS] Invoking TTS plugin: {}, textLen={}", pluginCode, text != null ? text.length() : 0);
        return new TtsResult("/audio/tts/" + UUID.randomUUID() + ".wav", 300);
    }

    /**
     * LLM: 对话生成
     */
    public LlmResult invokeLlm(String prompt, String context, String pluginCode) {
        log.info("[AI-LLM] Invoking LLM plugin: {}, promptLen={}", pluginCode, prompt != null ? prompt.length() : 0);
        return new LlmResult("模拟大模型回复", "INQUIRY", "{\"product\":\"demo\"}", 500);
    }

    /**
     * 创建AI对话记录
     */
    public AiConversation createConversation(String callId, String sessionId) {
        AiConversation conv = new AiConversation();
        conv.setCallId(callId);
        conv.setSessionId(sessionId);
        conv.setState("ACTIVE");
        conv.setTurnCount(0);
        conv.setCreatedAt(LocalDateTime.now());
        return conv;
    }

    /**
     * 记录轮次
     */
    public AiConversationTurn recordTurn(Long conversationId, int turnNo, String userText,
                                          String aiResponse, String intent, String slotValuesJson,
                                          int asrLatency, int ttsLatency, int llmLatency) {
        AiConversationTurn turn = new AiConversationTurn();
        turn.setConversationId(conversationId);
        turn.setTurnNo(turnNo);
        turn.setUserText(userText);
        turn.setAiResponse(aiResponse);
        turn.setIntent(intent);
        turn.setSlotValuesJson(slotValuesJson);
        turn.setAsrLatencyMs(asrLatency);
        turn.setTtsLatencyMs(ttsLatency);
        turn.setLlmLatencyMs(llmLatency);
        turn.setCreatedAt(LocalDateTime.now());
        return turn;
    }
}
