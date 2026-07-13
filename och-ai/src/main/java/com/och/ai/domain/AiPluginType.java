package com.och.ai.domain;

/**
 * AI 插件类型枚举
 * <p>定义系统支持的 AI 能力类型：ASR 语音识别、TTS 语音合成、LLM 大语言模型、WORKFLOW 工作流引擎。</p>
 */
public enum AiPluginType {
    ASR("语音识别"),
    TTS("语音合成"),
    LLM("大语言模型"),
    WORKFLOW("工作流引擎");

    private final String label;
    AiPluginType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
