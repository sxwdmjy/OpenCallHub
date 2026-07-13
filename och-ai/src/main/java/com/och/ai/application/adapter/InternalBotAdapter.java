package com.och.ai.application.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内置机器人引擎
 * <p>不依赖外部 AI 平台，基于关键词匹配和简单规则的对话引擎。
 * 适用于快速启动、离线场景或简单问答。</p>
 */
@Slf4j
@Component
public class InternalBotAdapter implements BotAdapter {

    @Override
    public String getType() {
        return "INTERNAL";
    }

    /**
     * 关键词规则匹配
     * <p>根据用户输入匹配预定义的关键词规则，返回对应回复和意图。</p>
     */
    @Override
    public BotResponse process(String userInput, String context, String systemPrompt, Integer turnNo) {
        log.info("[InternalBot] 第{}轮处理: inputLen={}", turnNo, userInput != null ? userInput.length() : 0);
        if (userInput == null || userInput.isEmpty()) {
            return new BotResponse("您好，请问有什么可以帮您？", "GREETING", "{}", 50);
        }
        if (userInput.contains("价格") || userInput.contains("费用")) {
            return new BotResponse("了解，关于价格问题我为您转接人工客服详细解答。", "TRANSFER_PRICE", "{}", 80);
        }
        if (userInput.contains("投诉") || userInput.contains("不满")) {
            return new BotResponse("非常抱歉给您带来不便，马上为您转接人工客服。", "TRANSFER_COMPLAINT", "{}", 80);
        }
        if (userInput.contains("查询") || userInput.contains("订单")) {
            return new BotResponse("正在为您查询相关信息，请稍候。", "QUERY", "{}", 100);
        }
        return new BotResponse("感谢您的来电，请简要描述您的问题。", "FALLBACK", "{}", 60);
    }
}
