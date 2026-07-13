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
 * LangFlow 流程编排适配器
 * <p>
 * 对接 LangFlow API，执行可视化编排的 AI 流程。
 * LangFlow 是一个低代码 AI 流程编排平台，通过拖拽节点构建 RAG、Agent 等工作流，
 * 每个流程提供一个独立的 HTTP API 端点供外部调用。
 * </p>
 * <p>
 * LangFlow API 文档参考:
 * - 运行流程: POST /api/v1/run/{flow_id} 或 POST /api/v1/process/{flow_id}
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class LangFlowAdapter implements BotAdapter {

    /** 用于发送 HTTP 请求的 RestTemplate 实例 */
    private final RestTemplate restTemplate;

    /** 用于解析 JSON 响应的 ObjectMapper 实例 */
    private final ObjectMapper objectMapper;

    /** LangFlow API Base URL 环境变量名，未配置时使用本地默认入口。 */
    private static final String ENV_BASE_URL = "LANGFLOW_BASE_URL";

    /** LangFlow API Key 环境变量名，未配置时不添加认证头。 */
    private static final String ENV_API_KEY = "LANGFLOW_API_KEY";

    /** LangFlow 流程 ID 环境变量名，未配置时不发起真实流程调用。 */
    private static final String ENV_FLOW_ID = "LANGFLOW_FLOW_ID";

    private static final String DEFAULT_BASE_URL = "http://localhost:7860/api/v1";

    public LangFlowAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return "LANGFLOW";
    }

    /**
     * 调用 LangFlow 流程 API 处理用户输入
     * <p>
     * 处理流程:
     * 1. 构建请求 payload，包含用户输入、系统提示词和流程配置参数（tweaks）
     * 2. 发送 HTTP POST 请求到 LangFlow 流程端点
     * 3. 解析响应 JSON，提取流程执行结果（输出文本、意图等）
     * </p>
     * <p>
     * LangFlow 标准请求格式:
     * <pre>
     * {
     *   "input_value": "用户输入消息",
     *   "output_type": "chat",
     *   "input_type": "chat",
     *   "tweaks": {}  // 可选的流程参数覆盖
     * }
     * </pre>
     * </p>
     * <p>
     * LangFlow 标准响应格式（预期）:
     * <pre>
     * {
     *   "outputs": [
     *     {
     *       "outputs": [
     *         {
     *           "results": {
     *             "message": {
     *               "text": "生成的回复文本"
     *             }
     *           }
     *         }
     *       ]
     *     }
     *   ]
     * }
     * </pre>
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
        log.info("[LangFlowAdapter] 第{}轮执行开始: inputLen={}", turnNo, userInput != null ? userInput.length() : 0);

        try {
            // ---- 1. 构建请求 payload ----
            // 将系统提示词、上下文和用户输入合并后传递给 LangFlow 流程
            String fullInput = buildInput(systemPrompt, context, userInput);

            Map<String, Object> payload = new HashMap<>();
            // input_value: LangFlow 流程的入口输入（ChatInput 节点的默认字段名）
            payload.put("input_value", fullInput);
            // output_type: 期望的输出类型，"chat" 表示对话模式
            payload.put("output_type", "chat");
            // input_type: 输入类型，"chat" 表示对话输入
            payload.put("input_type", "chat");
            // tweaks: 流程节点参数覆盖，可用于动态调整流程行为（如切换模型、调整温度等）
            Map<String, Object> tweaks = buildTweaks(systemPrompt);
            payload.put("tweaks", tweaks);

            log.info("[LangFlowAdapter] 第{}轮 payload 构建完成: input长度={}, tweaks键数={}",
                    turnNo, fullInput.length(), tweaks.size());

            // ---- 2. 发送 HTTP POST 请求 ----
            // 目标 URL: {baseUrl}/run/{flowId}
            String flowId = getFlowId();
            if (flowId == null || flowId.isBlank()) {
                int latencyMs = (int) (System.currentTimeMillis() - startTime);
                log.warn("[LangFlowAdapter] 未配置 LANGFLOW_FLOW_ID，跳过真实 LangFlow 调用");
                return new BotResponse("LangFlow 插件未配置，请先在资源中心配置流程 ID。", "ERROR", "{}", latencyMs);
            }
            String url = getBaseUrl() + "/run/" + flowId;
            String responseJson = callLangFlowApi(url, getApiKey(), payload);

            log.info("[LangFlowAdapter] 第{}轮 API 响应长度: {}", turnNo, responseJson != null ? responseJson.length() : 0);

            // ---- 3. 解析响应 ----
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            BotResponse result = parseLangFlowResponse(responseJson, userInput, turnNo, latencyMs);

            log.info("[LangFlowAdapter] 第{}轮执行完成: intent={}, latency={}ms",
                    turnNo, result.intent(), result.latencyMs());
            return result;

        } catch (Exception e) {
            log.error("[LangFlowAdapter] 第{}轮 API 调用失败: {}", turnNo, e.getMessage(), e);
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            return new BotResponse(
                    "抱歉，AI 流程服务暂时不可用，请稍后再试。",
                    "ERROR",
                    "{}",
                    latencyMs
            );
        }
    }

    /**
     * 拼接完整的输入文本
     * <p>将系统提示词、对话上下文和用户当前输入合并，作为 LangFlow 流程的入口消息。</p>
     *
     * @param systemPrompt 系统提示词
     * @param context      对话上下文
     * @param userInput    用户输入
     * @return 拼接后的完整输入文本
     */
    private String buildInput(String systemPrompt, String context, String userInput) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append(systemPrompt).append("\n\n");
        }
        if (context != null && !context.isEmpty()) {
            sb.append("上文摘要:\n").append(context).append("\n\n");
        }
        sb.append("用户: ").append(userInput);
        return sb.toString();
    }

    /**
     * 构建流程 tweaks（节点参数覆盖）
     * <p>
     * tweaks 用于在运行时动态调整 LangFlow 流程中某些节点的参数，
     * 例如修改 Prompt 节点的模板内容、调整 LLM 节点的温度等。
     * 格式: {"节点ID": {"参数名": "参数值"}}
     * </p>
     *
     * @param systemPrompt 系统提示词（可以覆盖流程中的 Prompt 模板）
     * @return tweaks 参数 Map
     */
    private Map<String, Object> buildTweaks(String systemPrompt) {
        Map<String, Object> tweaks = new HashMap<>();
        // 此处可根据流程定义传入具体的节点参数覆盖
        // 例如: tweaks.put("ChatPrompt-xxxxx", Map.of("template", systemPrompt));
        // 当前版本留空，表示使用流程中已有的默认配置
        return tweaks;
    }

    /**
     * 发送 HTTP POST 请求到 LangFlow API
     *
     * @param url    目标 URL（LangFlow 流程端点）
     * @param apiKey LangFlow API Key（如果未启用认证可为空字符串）
     * @param body   请求体（Map 格式，会被序列化为 JSON）
     * @return 响应体字符串（JSON 格式）
     * @throws Exception 当 HTTP 请求失败时抛出异常
     */
    private String callLangFlowApi(String url, String apiKey, Map<String, Object> body) {
        log.info("[LangFlowAdapter] 发送请求: url={}", url);

        // 设置 HTTP 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 如果 API Key 非空且非占位符，则添加认证头
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
            log.debug("[LangFlowAdapter] 已添加 Authorization 头");
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        log.info("[LangFlowAdapter] 收到响应: HTTP状态码={}", response.getStatusCode().value());
        return response.getBody();
    }

    /**
     * 解析 LangFlow API 响应 JSON，提取关键信息
     * <p>
     * LangFlow 标准响应格式:
     * <pre>
     * {
     *   "outputs": [
     *     {
     *       "outputs": [
     *         {
     *           "results": {
     *             "message": {
     *               "text": "生成文本"
     *             }
     *           }
     *         }
     *       ]
     *     }
     *   ]
     * }
     * </pre>
     * 也兼容简化格式:
     * <pre>
     * {
     *   "result": "生成文本"
     * }
     * </pre>
     * </p>
     *
     * @param responseJson API 返回的 JSON 字符串
     * @param userInput    用户输入（作为降级回复的依据）
     * @param turnNo       当前轮次号
     * @param latencyMs    已计算的 API 调用耗时
     * @return 解析后的 BotResponse 对象
     */
    private BotResponse parseLangFlowResponse(String responseJson, String userInput, int turnNo, int latencyMs) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // 尝试多种响应格式提取回复文本
            String answer = extractAnswer(root);

            if (answer.isEmpty()) {
                log.warn("[LangFlowAdapter] 无法从响应中提取文本，使用原始 JSON 作为回复");
                answer = responseJson;
            }

            // 从回复文本推断意图
            String intent = IntentDetector.detect(answer);

            // 槽位信息默认为空 JSON，需要结构化输出时可通过 LangFlow 流程输出节点传递
            String slotValuesJson = "{}";

            return new BotResponse(answer, intent, slotValuesJson, latencyMs);
        } catch (Exception e) {
            log.error("[LangFlowAdapter] 解析响应 JSON 失败: {}", e.getMessage(), e);
            return new BotResponse(responseJson, "PARSE_ERROR", "{}", latencyMs);
        }
    }

    /**
     * 从 LangFlow 响应的 JSON 树中提取回复文本
     * <p>
     * 兼容多种 LangFlow 版本和流程配置可能的输出格式:
     * <ol>
     *   <li>标准格式: outputs[0].outputs[0].results.message.text</li>
     *   <li>简化格式: result</li>
     *   <li>直接格式: answer / text / message</li>
     *   <li>ChatOutput 格式: outputs[0].outputs[0].messages[0].message</li>
     * </ol>
     * </p>
     *
     * @param root 响应 JSON 的根节点
     * @return 提取到的回复文本，未找到时返回空字符串
     */
    private String extractAnswer(JsonNode root) {
        // 格式1: outputs -> [0] -> outputs -> [0] -> results -> message -> text
        try {
            JsonNode outputs = root.get("outputs");
            if (outputs != null && outputs.isArray() && outputs.size() > 0) {
                JsonNode firstOutput = outputs.get(0);
                if (firstOutput != null && firstOutput.has("outputs")) {
                    JsonNode innerOutputs = firstOutput.get("outputs");
                    if (innerOutputs != null && innerOutputs.isArray() && innerOutputs.size() > 0) {
                        JsonNode innerFirst = innerOutputs.get(0);
                        if (innerFirst != null && innerFirst.has("results")) {
                            JsonNode results = innerFirst.get("results");
                            if (results != null && results.has("message")) {
                                JsonNode message = results.get("message");
                                if (message != null) {
                                    // message 可能是对象 {"text": "..."} 或直接是字符串
                                    if (message.isObject() && message.has("text")) {
                                        return message.get("text").asText();
                                    }
                                    if (message.isTextual()) {
                                        return message.asText();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[LangFlowAdapter] 标准格式解析失败，尝试其他格式: {}", e.getMessage());
        }

        // 格式2: result 字段（简化格式）
        if (root.has("result") && root.get("result").isTextual()) {
            return root.get("result").asText();
        }

        // 格式3: answer 字段
        if (root.has("answer") && root.get("answer").isTextual()) {
            return root.get("answer").asText();
        }

        // 格式4: text 字段
        if (root.has("text") && root.get("text").isTextual()) {
            return root.get("text").asText();
        }

        // 格式5: message 字段
        if (root.has("message") && root.get("message").isTextual()) {
            return root.get("message").asText();
        }

        // 如果以上格式都不匹配，返回空字符串
        return "";
    }

    /**
     * 基于关键词的意图识别
     * <p>委托 {@link IntentDetector#detect(String)} 进行关键词匹配。</p>
     *
     * @param answer AI 回复文本
     * @return 意图编码
     */
    private String detectIntent(String answer) {
        return IntentDetector.detect(answer);
    }

    /**
     * 获取 LangFlow 的 Base URL
     * <p>生产环境应从配置中心或数据库读取。</p>
     *
     * @return LangFlow API 的 Base URL
     */
    private String getBaseUrl() {
        String baseUrl = System.getenv(ENV_BASE_URL);
        return baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
    }

    /**
     * 获取 LangFlow 的 API Key
     *
     * @return LangFlow API Key
     */
    private String getApiKey() {
        return System.getenv(ENV_API_KEY);
    }

    /**
     * 获取 LangFlow 流程 ID
     * <p>
     * 流程 ID 可在 LangFlow 控制台中复制（每个流程卡片下方显示）。
     * 生产环境应从数据库配置表读取。
     * </p>
     *
     * @return 流程 ID
     */
    private String getFlowId() {
        return System.getenv(ENV_FLOW_ID);
    }
}
