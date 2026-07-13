package com.och.ai.application.adapter;

/**
 * 意图检测工具类
 * <p>
 * 基于关键词匹配的简单意图识别，从 AI 回复文本中推断用户意图。
 * 这是一个轻量级实现，生产环境建议使用专用的意图识别模型或平台输出节点传递结构化意图。
 * </p>
 *
 * @author danmo
 * @since 2025-06-02
 */
public final class IntentDetector {

    private IntentDetector() {
        // 工具类，禁止实例化
    }

    /**
     * 基于关键词的意图识别
     * <p>从回复文本中匹配中文关键词，确定用户意图编码。</p>
     *
     * @param text 回复文本（AI 生成的回复，用于推断意图）
     * @return 意图编码（UNKNOWN, GREETING, QUERY, COMPLAINT, TRANSFER, PRICE_INQUIRY, PRODUCT_INQUIRY, GENERAL, ERROR, PARSE_ERROR）
     */
    public static String detect(String text) {
        if (text == null || text.isEmpty()) {
            return "UNKNOWN";
        }
        String lower = text.toLowerCase();

        if (lower.contains("你好") || lower.contains("欢迎") || lower.contains("您好")) {
            return "GREETING";
        }
        if (lower.contains("查询") || lower.contains("订单") || lower.contains("信息")) {
            return "QUERY";
        }
        if (lower.contains("投诉") || lower.contains("不满") || lower.contains("问题")) {
            return "COMPLAINT";
        }
        if (lower.contains("转接") || lower.contains("人工")) {
            return "TRANSFER";
        }
        if (lower.contains("价格") || lower.contains("费用") || lower.contains("多少钱")) {
            return "PRICE_INQUIRY";
        }
        if (lower.contains("产品") || lower.contains("服务") || lower.contains("功能")) {
            return "PRODUCT_INQUIRY";
        }
        return "GENERAL";
    }
}
