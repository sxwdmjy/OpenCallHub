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
 * RAGFlow 知识库检索适配器
 * <p>
 * 对接 RAGFlow API，采用两步调用模式:
 * 1. 先调用检索 API 从企业知识库中搜索相关文档片段
 * 2. 再调用生成 API 基于检索结果和原有上下文生成最终回复
 * </p>
 * <p>
 * RAGFlow API 文档参考:
 * - 检索: POST /api/v1/retrieval
 * - 对话: POST /api/v1/chat
 * </p>
 *
 * @author danmo
 */
@Slf4j
@Component
public class RAGFlowAdapter implements BotAdapter {

    /** 用于发送 HTTP 请求的 RestTemplate 实例 */
    private final RestTemplate restTemplate;

    /** 用于解析 JSON 响应的 ObjectMapper 实例 */
    private final ObjectMapper objectMapper;

    /** RAGFlow API Base URL 环境变量名，未配置时使用本地默认入口。 */
    private static final String ENV_BASE_URL = "RAGFLOW_BASE_URL";

    /** RAGFlow API Key 环境变量名，未配置时不添加认证头。 */
    private static final String ENV_API_KEY = "RAGFLOW_API_KEY";

    /** RAGFlow 知识库 ID 环境变量名，未配置时返回友好提示。 */
    private static final String ENV_KNOWLEDGE_BASE_ID = "RAGFLOW_KNOWLEDGE_BASE_ID";

    private static final String DEFAULT_BASE_URL = "http://localhost:9380/api/v1";

    public RAGFlowAdapter() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return "RAGFLOW";
    }

    /**
     * 基于知识库检索的对话处理
     * <p>
     * 处理流程:
     * 1. 调用 searchKnowledge() 从 RAGFlow 知识库检索与用户问题相关的文档片段
     * 2. 将检索到的知识内容作为参考资料，结合系统提示词和上下文，拼接完整的 prompt
     * 3. 调用 generateReply() 让 RAGFlow 基于检索结果生成最终回复
     * 4. 从回复中推断用户意图并返回完整结果
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
        log.info("[RAGFlowAdapter] 第{}轮检索开始: inputLen={}", turnNo, userInput != null ? userInput.length() : 0);

        if (getKnowledgeBaseId() == null || getKnowledgeBaseId().isBlank()) {
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            log.warn("[RAGFlowAdapter] 未配置 RAGFLOW_KNOWLEDGE_BASE_ID，跳过真实 RAGFlow 调用");
            return new BotResponse("RAGFlow 插件未配置，请先在资源中心配置知识库。", "ERROR", "{}", latencyMs);
        }

        try {
            // ---- 步骤1: 知识库检索 ----
            // 从 RAGFlow 知识库中召回与用户问题最相关的文档片段
            String knowledgeContext = searchKnowledge(userInput);
            int searchLatency = (int) (System.currentTimeMillis() - startTime);
            log.info("[RAGFlowAdapter] 第{}轮知识库检索完成: 知识上下文长度={}, 检索耗时={}ms",
                    turnNo, knowledgeContext != null ? knowledgeContext.length() : 0, searchLatency);

            // ---- 步骤2: 构建 prompt 并生成回复 ----
            // 将系统提示词、检索到的知识、对话上下文和用户问题拼接为完整的生成请求
            String prompt = buildPrompt(systemPrompt, knowledgeContext, context, userInput);

            // ---- 步骤3: 调用 RAGFlow 生成 API ----
            String generatedText = generateReply(prompt);
            int totalLatency = (int) (System.currentTimeMillis() - startTime);
            log.info("[RAGFlowAdapter] 第{}轮生成完成: 回复长度={}, 总耗时={}ms",
                    turnNo, generatedText != null ? generatedText.length() : 0, totalLatency);

            // ---- 步骤4: 意图识别 ----
            // 基于生成的回复和检索到的知识内容推断用户意图
            String intent = IntentDetector.detect(generatedText);

            return new BotResponse(generatedText, intent, "{}", totalLatency);

        } catch (Exception e) {
            log.error("[RAGFlowAdapter] 第{}轮处理失败: {}", turnNo, e.getMessage(), e);
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            return new BotResponse(
                    "抱歉，知识库服务暂时不可用，请稍后再试。",
                    "ERROR",
                    "{}",
                    latencyMs
            );
        }
    }

    /**
     * 调用 RAGFlow 检索 API，从知识库中搜索相关内容
     * <p>
     * 请求格式:
     * POST /api/v1/retrieval
     * Body: {"question": "用户问题", "kb_id": "知识库ID", "page_size": 5}
     * </p>
     * <p>
     * 响应格式（预期）:
     * <pre>
     * {
     *   "code": 0,
     *   "data": {
     *     "chunks": [
     *       {"content": "文档片段1内容", "doc_name": "来源文档名", ...},
     *       ...
     *     ]
     *   }
     * }
     * </pre>
     * </p>
     *
     * @param userInput 用户的原始输入（用于知识库搜索）
     * @return 拼接后的知识上下文文本（多个文档片段的合并结果）
     * @throws Exception 当 API 调用失败时抛出异常
     */
    private String searchKnowledge(String userInput) {
        log.info("[RAGFlowAdapter] 开始知识库检索: questionLen={}", userInput != null ? userInput.length() : 0);

        String knowledgeBaseId = getKnowledgeBaseId();
        if (knowledgeBaseId == null || knowledgeBaseId.isBlank()) {
            log.warn("[RAGFlowAdapter] 未配置 RAGFLOW_KNOWLEDGE_BASE_ID，跳过知识库检索");
            return "知识库插件未配置，请先在资源中心配置知识库。";
        }

        // 构建检索请求体
        Map<String, Object> requestBody = new HashMap<>();
        // question: 用于语义检索的查询文本
        requestBody.put("question", userInput);
        // kb_id: 指定要检索的知识库 ID（多个知识库可传列表）
        requestBody.put("kb_id", knowledgeBaseId);
        // page_size: 返回的最相关文档片段数量
        requestBody.put("page_size", 5);

        // 发送检索请求
        String url = getBaseUrl() + "/retrieval";
        String responseJson = callHttpApi(url, getApiKey(), requestBody);
        log.info("[RAGFlowAdapter] 检索 API 响应: 长度={}", responseJson != null ? responseJson.length() : 0);

        // 解析检索结果，拼接为知识上下文
        return parseRetrievalResult(responseJson);
    }

    /**
     * 解析检索 API 的 JSON 响应，提取文档片段并拼接为上下文
     * <p>将多个检索到的文档片段按序号拼接，形成可供生成模型参考的知识上下文。</p>
     *
     * @param responseJson 检索 API 返回的 JSON 字符串
     * @return 拼接后的知识上下文文本
     */
    private String parseRetrievalResult(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // 检查 API 返回码（code: 0 表示成功）
            if (root.has("code") && root.get("code").asInt() != 0) {
                log.warn("[RAGFlowAdapter] 检索 API 返回异常: code={}, message={}",
                        root.get("code").asInt(),
                        root.has("message") ? root.get("message").asText() : "无消息");
                return "暂无相关知识。";
            }

            // 提取 data.chunks 中的文档片段
            JsonNode chunks = root.has("data") && root.get("data").has("chunks")
                    ? root.get("data").get("chunks")
                    : null;

            if (chunks == null || !chunks.isArray() || chunks.size() == 0) {
                log.warn("[RAGFlowAdapter] 检索结果为空，未找到相关文档");
                return "未找到相关知识。";
            }

            // 拼接所有检索到的文档片段
            StringBuilder sb = new StringBuilder();
            sb.append("以下是参考知识:\n");
            int count = 0;
            for (JsonNode chunk : chunks) {
                count++;
                String content = chunk.has("content") ? chunk.get("content").asText() : "";
                String docName = chunk.has("doc_name") ? chunk.get("doc_name").asText() : "未知来源";
                sb.append("\n[参考").append(count).append("] 来源: ").append(docName).append("\n");
                sb.append(content).append("\n");
            }
            log.info("[RAGFlowAdapter] 检索到 {} 条相关文档片段", count);
            return sb.toString();
        } catch (Exception e) {
            log.error("[RAGFlowAdapter] 解析检索结果失败: {}", e.getMessage(), e);
            // 解析失败时返回一个默认提示，避免中断后续生成流程
            return "检索结果解析失败，请尝试重新查询。";
        }
    }

    /**
     * 构建完整的生成 prompt
     * <p>按顺序组织: 系统提示词 -> 参考知识(Loc: 检索结果) -> 上文摘要(Loc: 对话历史) -> 用户问题</p>
     *
     * @param systemPrompt     系统提示词
     * @param knowledgeContext 知识库检索结果
     * @param context          对话上文
     * @param userInput        用户当前输入
     * @return 完整的 prompt 文本
     */
    private String buildPrompt(String systemPrompt, String knowledgeContext, String context, String userInput) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append(systemPrompt).append("\n\n");
        }
        if (knowledgeContext != null && !knowledgeContext.isEmpty()) {
            sb.append(knowledgeContext).append("\n\n");
        }
        if (context != null && !context.isEmpty()) {
            sb.append("上文摘要:\n").append(context).append("\n\n");
        }
        sb.append("用户问题: ").append(userInput);
        return sb.toString();
    }

    /**
     * 调用 RAGFlow 生成 API，基于 prompt 生成最终回复
     * <p>
     * 请求格式:
     * POST /api/v1/chat
     * Body: {"question": "完整 prompt", "stream": false}
     * </p>
     * <p>
     * 响应格式（预期非流式）:
     * <pre>
     * {
     *   "code": 0,
     *   "data": {
     *     "answer": "生成的回复文本"
     *   }
     * }
     * </pre>
     * </p>
     *
     * @param prompt 完整的提示词（包含系统提示词、知识上下文和用户问题）
     * @return AI 生成的回复文本
     * @throws Exception 当 API 调用失败或解析失败时抛出异常
     */
    private String generateReply(String prompt) {
        log.info("[RAGFlowAdapter] 调用生成 API: prompt长度={}", prompt.length());

        // 构建生成请求体
        Map<String, Object> requestBody = new HashMap<>();
        // question: 完整的 prompt 文本
        requestBody.put("question", prompt);
        // stream: false 表示非流式响应（一次性返回完整结果）
        requestBody.put("stream", false);

        // 发送生成请求
        String url = getBaseUrl() + "/chat";
        String responseJson = callHttpApi(url, getApiKey(), requestBody);
        log.info("[RAGFlowAdapter] 生成 API 响应: 长度={}", responseJson != null ? responseJson.length() : 0);

        // 解析生成结果
        return parseGenerationResult(responseJson);
    }

    /**
     * 解析生成 API 的 JSON 响应，提取回答文本
     *
     * @param responseJson 生成 API 返回的 JSON 字符串
     * @return 生成的回复文本
     */
    private String parseGenerationResult(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // 检查 API 返回码
            if (root.has("code") && root.get("code").asInt() != 0) {
                log.warn("[RAGFlowAdapter] 生成 API 返回异常: code={}, message={}",
                        root.get("code").asInt(),
                        root.has("message") ? root.get("message").asText() : "无消息");
                return "抱歉，生成回复时出现异常。";
            }

            // 提取 data.answer 字段
            if (root.has("data") && root.get("data").has("answer")) {
                return root.get("data").get("answer").asText();
            }

            // 兼容其他可能的响应格式: 直接取 answer 字段
            if (root.has("answer")) {
                return root.get("answer").asText();
            }

            log.warn("[RAGFlowAdapter] 未找到 answer 字段，使用原始响应作为回复");
            return responseJson;
        } catch (Exception e) {
            log.error("[RAGFlowAdapter] 解析生成结果失败: {}", e.getMessage(), e);
            return responseJson;
        }
    }

    /**
     * 发送 HTTP POST 请求到 RAGFlow API
     *
     * @param url    目标 URL（如 http://localhost:9380/api/v1/retrieval）
     * @param apiKey RAGFlow API Key
     * @param body   请求体（Map 格式）
     * @return 响应体字符串（JSON 格式）
     * @throws Exception 当 HTTP 请求失败时抛出异常
     */
    private String callHttpApi(String url, String apiKey, Map<String, Object> body) {
        log.debug("[RAGFlowAdapter] 发送请求: url={}", url);

        // 未配置 API Key 时不添加认证头，避免把占位值当成真实密钥发送。
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        log.debug("[RAGFlowAdapter] 收到响应: HTTP状态码={}", response.getStatusCode().value());
        return response.getBody();
    }

    /**
     * 基于关键词的意图识别（结合检索到的知识内容）
     * <p>委托 {@link IntentDetector#detect(String)} 进行关键词匹配。</p>
     *
     * @param answer           生成的回复文本
     * @param knowledgeContext 知识库检索到的内容（已弃用，保留参数以兼容旧调用）
     * @return 意图编码
     * @deprecated 请使用 {@link IntentDetector#detect(String)}
     */
    @Deprecated
    private String detectIntent(String answer, String knowledgeContext) {
        return IntentDetector.detect(answer);
    }

    /**
     * 获取 RAGFlow 的 Base URL
     * <p>生产环境应从配置中心或数据库读取，例如:
     * {@code @Value("${ragflow.base-url}")}</p>
     *
     * @return RAGFlow API 的 Base URL
     */
    private String getBaseUrl() {
        String baseUrl = System.getenv(ENV_BASE_URL);
        return baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
    }

    /**
     * 获取 RAGFlow 的 API Key
     *
     * @return RAGFlow API Key
     */
    private String getApiKey() {
        return System.getenv(ENV_API_KEY);
    }

    /**
     * 获取 RAGFlow 知识库 ID
     *
     * @return 知识库 ID
     */
    private String getKnowledgeBaseId() {
        return System.getenv(ENV_KNOWLEDGE_BASE_ID);
    }
}
