package com.och.ai.application.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify 工作流引擎适配器
 * <p>
 * 对接 Dify 开放 API（POST /v1/chat-messages），将用户输入发送到 Dify 工作流，
 * 获取 AI 回复、意图和槽位信息。
 * </p>
 * <p>
 * API 文档参考: https://api.dify.ai/v1/chat-messages
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class DifyAdapter implements BotAdapter {

    /** 用于发送 HTTP 请求的 RestTemplate 实例 */
    private final RestTemplate restTemplate;

    /** 用于解析 JSON 响应的 ObjectMapper 实例 */
    private final ObjectMapper objectMapper;

    /** Dify API Base URL 环境变量名，未配置时使用 Dify 默认入口。 */
    private static final String ENV_BASE_URL = "DIFY_BASE_URL";

    /** Dify API Key 环境变量名，未配置时不发起真实调用。 */
    private static final String ENV_API_KEY = "DIFY_API_KEY";

    private static final String DEFAULT_BASE_URL = "https://api.dify.ai/v1";

    public DifyAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return "DIFY";
    }

    /**
     * 调用 Dify 工作流 API 处理用户输入
     * <p>
     * 请求流程:
     * 1. 将 systemPrompt + context + userInput 拼接为完整 query
     * 2. 构建请求体（JSON），包含 inputs、query、response_mode、user 等字段
     * 3. 设置 Authorization 请求头（Bearer {apiKey}）
     * 4. 发送 HTTP POST 请求到 /v1/chat-messages
     * 5. 解析响应 JSON，提取 answer 文本、意图和槽位
     * </p>
     *
     * @param userInput    用户语音转文字后的输入
     * @param context      对话上文（前几轮摘要）
     * @param systemPrompt 系统提示词
     * @param turnNo       当前轮次号（从 1 开始）
     * @return AI 回复 + 意图 + 槽位 + 耗时
     */
    @Override
    public BotResponse process(String userInput, String context, String systemPrompt, Integer turnNo) {
        long startTime = System.currentTimeMillis();
        log.info("[DifyAdapter] 第{}轮处理开始: inputLen={}, contextLen={}",
                turnNo,
                userInput != null ? userInput.length() : 0,
                context != null ? context.length() : 0);

        // ---- 1. 构建请求体 ----
        // 将系统提示词、上下文和用户输入拼接为完整的 query 字符串
        String fullQuery = buildQuery(systemPrompt, context, userInput);

        Map<String, Object> requestBody = new HashMap<>();
        // inputs: 工作流的输入变量，此处为空（可根据业务需要传入结构化参数）
        requestBody.put("inputs", new HashMap<>());
        // query: 用户输入内容（Dify 工作流的入口文本）
        requestBody.put("query", fullQuery);
        // response_mode: "blocking" 表示同步等待结果返回（适用于实时对话场景）
        requestBody.put("response_mode", "blocking");
        // conversation_id: 会话 ID，传空字符串表示新会话（如需多轮对话记忆可传入上次返回的 conversation_id）
        requestBody.put("conversation_id", "");
        // user: 唯一用户标识，格式为 "call-{轮次号}"
        requestBody.put("user", "call-" + turnNo);

        log.info("[DifyAdapter] 请求体构建完成: query长度={}, user={}", fullQuery.length(), requestBody.get("user"));

        // ---- 2. 发送 HTTP POST 请求 ----
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            log.warn("[DifyAdapter] 未配置 DIFY_API_KEY，跳过真实 Dify 调用");
            return new BotResponse("Dify 插件未配置，请先在资源中心配置密钥。", "ERROR", "{}", latencyMs);
        }
        String url = baseUrl + "/chat-messages";

        try {
            String responseJson = callHttpApi(url, apiKey, requestBody);
            log.info("[DifyAdapter] API 响应体长度: {}", responseJson != null ? responseJson.length() : 0);

            // ---- 3. 解析响应并构建结果 ----
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            BotResponse result = parseDifyResponse(responseJson, turnNo, latencyMs);
            log.info("[DifyAdapter] 第{}轮处理完成: intent={}, latency={}ms", turnNo, result.intent(), result.latencyMs());
            return result;

        } catch (Exception e) {
            log.error("[DifyAdapter] 第{}轮 API 调用失败: {}", turnNo, e.getMessage(), e);
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            return new BotResponse(
                    "抱歉，AI 服务暂时不可用，请稍后再试。",
                    "ERROR",
                    "{}",
                    latencyMs
            );
        }
    }

    /**
     * 拼接完整的查询文本
     * <p>将系统提示词、对话上下文和用户输入合并为一条完整的 prompt，发送给 Dify 工作流。</p>
     *
     * @param systemPrompt 系统提示词（定义机器人的角色和行为）
     * @param context      对话上下文（前几轮的关键信息摘要）
     * @param userInput    当前用户输入
     * @return 拼接后的完整查询文本
     */
    private String buildQuery(String systemPrompt, String context, String userInput) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append(systemPrompt).append("\n\n");
        }
        if (context != null && !context.isEmpty()) {
            sb.append("上文摘要:\n").append(context).append("\n\n");
        }
        sb.append("当前输入: ").append(userInput);
        return sb.toString();
    }

    /**
     * 发送 HTTP POST 请求到指定 URL
     * <p>使用 Spring RestTemplate 发送 JSON 格式的 POST 请求，携带 Authorization 认证头。</p>
     *
     * @param url    目标 URL（完整地址，如 https://api.dify.ai/v1/chat-messages）
     * @param apiKey Dify API Key（Bearer Token）
     * @param body   请求体（Map 格式，会被序列化为 JSON）
     * @return 响应体字符串（JSON 格式）
     * @throws Exception 当 HTTP 请求失败时抛出异常
     */
    private String callHttpApi(String url, String apiKey, Map<String, Object> body) {
        log.info("[DifyAdapter] 发送请求: url={}", url);

        // 设置 HTTP 请求头
        HttpHeaders headers = new HttpHeaders();
        // Content-Type: application/json
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Authorization: Bearer {apiKey}
        headers.set("Authorization", "Bearer " + apiKey);

        // 将请求体和请求头封装为 HttpEntity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 发送 POST 请求，获取响应字符串
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        log.info("[DifyAdapter] 收到响应: HTTP状态码={}", response.getStatusCode().value());
        return response.getBody();
    }

    /**
     * 解析 Dify API 响应 JSON，提取关键信息
     * <p>
     * Dify 标准响应格式（blocking 模式）:
     * <pre>
     * {
     *   "event": "message",
     *   "answer": "AI 生成的回复文本",
     *   "conversation_id": "会话ID",
     *   "message_id": "消息ID",
     *   ...
     * }
     * </pre>
     * </p>
     * <p>意图识别策略: 从 answer 文本中通过关键词进行简单意图推断。
     * 生产环境建议通过 Dify 工作流的 outputs 节点返回结构化意图。</p>
     *
     * @param responseJson API 返回的 JSON 字符串
     * @param turnNo       当前轮次号
     * @param latencyMs    已计算的 API 调用耗时
     * @return 解析后的 BotResponse 对象
     */
    private BotResponse parseDifyResponse(String responseJson, int turnNo, int latencyMs) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // 提取 answer 字段 —— Dify 返回的回答文本
            String answer = root.has("answer") ? root.get("answer").asText() : "";

            // 提取 conversation_id（可用于后续轮次的多轮对话）
            String conversationId = root.has("conversation_id") ? root.get("conversation_id").asText() : "";
            log.info("[DifyAdapter] 解析成功: answer长度={}, conversationId={}", answer.length(), conversationId);

            // 从回答文本中推断意图（简单关键词匹配，生产环境建议由 Dify 工作流返回结构化意图）
            String intent = IntentDetector.detect(answer);

            // 当前版本槽位统一返回空对象，需要结构化槽位时可通过 Dify 工作流 outputs 节点传递
            String slotValuesJson = "{}";

            return new BotResponse(answer, intent, slotValuesJson, latencyMs);
        } catch (Exception e) {
            log.error("[DifyAdapter] 解析响应 JSON 失败: {}", e.getMessage(), e);
            // 解析失败时返回原始文本作为回复
            return new BotResponse(responseJson, "PARSE_ERROR", "{}", latencyMs);
        }
    }

    /**
     * 基于关键词的意图识别
     * <p>委托 {@link IntentDetector#detect(String)} 进行关键词匹配。</p>
     *
     * @param answer AI 回复文本
     * @return 意图编码（如 GREETING、QUERY、TRANSFER 等）
     */
    private String detectIntent(String answer) {
        return IntentDetector.detect(answer);
    }

    /**
     * 获取 Dify 的 Base URL
     * <p>
     * 当前返回默认值，生产环境应通过以下方式读取:
     * - 从 Spring 配置文件中读取（@Value 注解）
     * - 从数据库配置表中读取（如 ai_plugin_config 表）
     * - 从环境变量读取
     * </p>
     *
     * @return Dify API 的 Base URL
     */
    private String getBaseUrl() {
        String baseUrl = System.getenv(ENV_BASE_URL);
        return baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
    }

    /**
     * 获取 Dify 的 API Key
     * <p>
     * 当前返回占位符，生产环境应通过以下方式读取:
     * - 从 Spring 配置文件中读取（@Value("${dify.api-key}")）
     * - 从数据库配置表中读取
     * - 从环境变量 DIFY_API_KEY 读取
     * </p>
     *
     * @return Dify API Key
     */
    private String getApiKey() {
        return System.getenv(ENV_API_KEY);
    }
}
