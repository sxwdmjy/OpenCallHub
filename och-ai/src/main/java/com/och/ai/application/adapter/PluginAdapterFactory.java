package com.och.ai.application.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 插件适配器工厂
 * <p>根据机器人流程版本配置的插件编码（LLM/TTS/ASR），自动选择合适的适配器实例。
 * 支持 Dify / RAGFlow / LangFlow / InternalBot 四种引擎。</p>
 */
@Slf4j
@Component
public class PluginAdapterFactory {

    private final Map<String, BotAdapter> adapterMap;

    /**
     * Spring 自动注入所有 BotAdapter 实现，按 getType() 建立索引
     */
    public PluginAdapterFactory(List<BotAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toMap(adapter -> normalize(adapter.getType()), Function.identity()));
        log.info("[PluginAdapterFactory] 已加载适配器: {}", adapterMap.keySet());
    }

    /**
     * 根据类型获取适配器。
     * <p>流程配置和接口入参可能来自页面、导入文件或外部任务，统一在工厂入口做标准化，避免大小写和空格导致误判。</p>
     *
     * @param type 适配器类型（DIFY / RAGFLOW / LANGFLOW / INTERNAL）
     * @return 对应的 BotAdapter 实现
     * @throws IllegalArgumentException 找不到对应适配器时抛出
     */
    public BotAdapter getAdapter(String type) {
        String adapterType = normalize(type);
        BotAdapter adapter = adapterMap.get(adapterType);
        if (adapter == null) {
            throw new IllegalArgumentException("不支持的机器人引擎类型: " + adapterType + "，可用类型: " + adapterMap.keySet());
        }
        return adapter;
    }

    private String normalize(String type) {
        return type == null || type.isBlank() ? "INTERNAL" : type.trim().toUpperCase(Locale.ROOT);
    }
}
