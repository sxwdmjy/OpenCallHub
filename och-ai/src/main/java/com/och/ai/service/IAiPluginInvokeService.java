package com.och.ai.service;
import com.och.ai.domain.AiConversation;
import com.och.ai.domain.AiConversationTurn;
import com.och.ai.service.impl.AiPluginInvokeServiceImpl.AsrResult;
import com.och.ai.service.impl.AiPluginInvokeServiceImpl.TtsResult;
import com.och.ai.service.impl.AiPluginInvokeServiceImpl.LlmResult;
public interface IAiPluginInvokeService {
    AsrResult invokeAsr(String audioData, String pluginCode);
    TtsResult invokeTts(String text, String voice, String pluginCode);
    LlmResult invokeLlm(String prompt, String context, String pluginCode);
    AiConversation createConversation(String callId, String sessionId);
    AiConversationTurn recordTurn(Long conversationId, int turnNo, String userText, String aiResponse, String intent, String slotValuesJson, int asrLatency, int ttsLatency, int llmLatency);
}
