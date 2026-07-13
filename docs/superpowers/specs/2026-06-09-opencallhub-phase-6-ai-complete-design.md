# openCallHub Phase 6 AI 联络中心完整功能设计

日期：2026-06-09

## 1. 背景与目标

Phase 6 由刘工负责，目标不再按最小闭环收敛，而是完成 `och-ai` 后端完整功能实现。完整功能包括 ASR、TTS、LLM、Workflow 插件调用，Dify、RAGFlow、LangFlow 和内置机器人适配，机器人流程版本、机器人会话、对话轮次、意图、槽位、摘要、标签、AI 质检结果、插件调用日志、成本统计、失败场景、安全脱敏和测试验证。

本阶段仍保持模块边界清晰：`och-ai` 负责 AI 能力、会话轨迹和 AI 输出结果；`och-resource` 负责 AI 插件资源配置、密钥脱敏、审计和删除保护；`och-quality` 后续负责质检模板、人工复核和完整质量流程；前端页面由前端负责人后续按接口实现。

## 2. 设计原则

1. 不做最小闭环，按 Phase 6 完整功能清单实现后端能力。
2. 不重复建设插件配置，统一复用 `och-resource` 的 `res_ai_plugin`。
3. `och-ai` 不从环境变量读取 Dify、RAGFlow、LangFlow、ASR、TTS 配置，所有插件调用均以资源中心插件编码为入口。
4. 不记录密钥、完整 Prompt、完整 ASR 文本、完整供应商响应或供应商 Token。
5. 设计模式只服务业务变化点：适配器隔离供应商差异，工厂选择插件实现，策略承载转人工/摘要/质检规则，模板方法只用于稳定公共调用流程。
6. 保持旧代码风格：Spring Service / Controller / Mapper 分层、MyBatis-Plus、Lombok、中文 UTF-8 注释和必要业务注释。

## 3. 架构边界

### 3.1 `och-resource`

`och-resource` 继续作为 AI 插件资源管理中心，负责：

- `res_ai_plugin` 的增删改查。
- 插件类型、供应商、端点、API Key、API Secret、配置 JSON、启用状态管理。
- 接口返回脱敏。
- 资源审计。
- 删除保护。

`och-ai` 只读取已授权的插件配置，不暴露资源管理接口。

### 3.2 `och-ai`

`och-ai` 负责完整 AI 后端能力：

- 插件配置读取与校验。
- ASR/TTS/LLM/Workflow 统一调用。
- Dify/RAGFlow/LangFlow/InternalBot 适配。
- 机器人流程草稿与发布版本快照。
- 机器人会话、对话轮次、意图、槽位、摘要、标签和转人工原因。
- 插件调用日志、成本统计、AI 质检结果。
- 失败降级与安全摘要。

### 3.3 非本阶段范围

本阶段不接管 FreeSWITCH ESL 命令、不实现外呼任务调度、不实现 `och-quality` 的质检模板和人工复核流程、不实现前端页面、不实现报表聚合。

## 4. 核心组件

### 4.1 AI 插件配置服务

新增 `AiPluginConfigService`，按插件编码读取 `res_ai_plugin`，返回内部调用对象。该对象包含：

- `pluginCode`
- `pluginType`
- `provider`
- `endpointUrl`
- `apiKey`
- `apiSecret`
- `configJson`
- `enabled`

服务职责：

1. 校验插件存在。
2. 校验插件启用。
3. 校验插件类型匹配调用场景。
4. 不把密钥写日志。
5. 为适配器提供只读配置快照。

### 4.2 插件适配器

保留 `BotAdapter` 作为 LLM/Workflow 统一接口，新增 ASR/TTS 适配器接口：

- `AsrAdapter`：处理语音识别插件。
- `TtsAdapter`：处理语音合成插件。
- `BotAdapter`：处理 Dify/RAGFlow/LangFlow/InternalBot 等对话插件。

适配器工厂按插件配置选择实现，不再仅按字符串类型直接选择。

### 4.3 统一插件调用服务

`AiPluginInvokeService` 从模拟结果升级为真实插件调用编排入口，统一流程为：

1. 读取插件配置。
2. 选择适配器。
3. 构建请求。
4. 调用供应商或通用 HTTP 插件。
5. 解析响应。
6. 写插件调用日志。
7. 写成本记录。
8. 返回统一结果。

### 4.4 机器人流程服务

`RobotFlowApplicationService` 负责流程创建、编辑、发布、禁用和版本查询。发布时生成版本快照，固化以下信息：

- ASR 插件编码。
- TTS 插件编码。
- LLM / Workflow 插件编码。
- 系统提示词。
- 意图定义。
- 槽位定义。
- 摘要标签规则。
- 转人工规则。

运行中的会话只引用已发布版本，草稿修改不影响历史和正在执行的会话。

### 4.5 机器人会话引擎

`RobotFlowEngine` 负责完整会话编排：

1. 启动会话，写入 `robot_session` 和 `ai_conversation`。
2. 处理 ASR 文本或用户文本轮次。
3. 调用 LLM/Workflow 插件。
4. 识别意图和槽位。
5. 更新摘要、标签、当前意图和槽位。
6. 判断是否转人工。
7. 写入 `ai_conversation_turn` 和 `plugin_call_log`。
8. 结束会话，写入结束原因和最终摘要。

### 4.6 AI 质检结果服务

新增 `AiQualityResultService`，在 `och-ai` 内记录 AI 质检结果。它只负责 AI 结果沉淀，不实现质检模板、人工复核和质检报表。

## 5. 数据模型

现有 `V12__ai_robot_schema.sql` 已包含机器人流程、版本、会话、对话轮次、插件日志、成本记录和打断日志。本次完整实现需要补齐 `ai_quality_result` 表，并根据需要扩展现有表字段。

### 5.1 `ai_quality_result`

字段建议：

- `id`
- `call_id`
- `session_id`
- `conversation_id`
- `score`
- `risk_level`
- `summary`
- `tags`
- `hit_rules_json`
- `evidence_json`
- `status`
- `created_at`
- `updated_at`

该表存 AI 质检输出和证据摘要，不存完整敏感原文。

### 5.2 `plugin_call_log`

插件日志只存摘要：

- 请求类型。
- 输入长度。
- 输出长度。
- HTTP 状态码。
- 延迟。
- 成功状态。
- 错误类型。
- 成本。

不得存完整请求、完整响应、密钥、Token、完整 Prompt。

### 5.3 `robot_session`

会话表保留并更新：

- 当前意图。
- 槽位 JSON。
- 摘要。
- 标签。
- 轮次数。
- ASR/TTS/LLM 累计延迟。
- 结束原因。

## 6. 通用 HTTP 插件契约

ASR/TTS 采用通用 HTTP 插件契约，不直接绑定具体 SDK。

### 6.1 ASR 请求

```json
{
  "audioUrl": "https://example/audio.wav",
  "audioDataRef": "file-id-or-recording-id",
  "language": "zh-CN",
  "format": "wav",
  "sampleRate": 16000,
  "sessionId": "robot-session-id"
}
```

### 6.2 ASR 响应

```json
{
  "text": "识别文本",
  "isFinal": true,
  "confidence": 0.98,
  "latencyMs": 230
}
```

### 6.3 TTS 请求

```json
{
  "text": "播报文本",
  "voice": "zhixiaomei",
  "format": "wav",
  "speed": 1.0,
  "sessionId": "robot-session-id"
}
```

### 6.4 TTS 响应

```json
{
  "audioUrl": "https://example/tts.wav",
  "audioDataRef": "file-id",
  "latencyMs": 300
}
```

### 6.5 LLM / Workflow 请求

```json
{
  "userInput": "用户输入",
  "context": "最近几轮摘要",
  "systemPrompt": "系统提示词",
  "turnNo": 1,
  "sessionId": "robot-session-id",
  "variables": {}
}
```

### 6.6 LLM / Workflow 响应

```json
{
  "text": "机器人回复",
  "intent": "QUERY",
  "slotValues": {},
  "summary": "当前摘要",
  "tags": ["咨询"],
  "needTransfer": false,
  "latencyMs": 500,
  "usage": {
    "inputTokens": 100,
    "outputTokens": 50,
    "cost": 0.01
  }
}
```

## 7. 后端接口

### 7.1 插件测试接口

`/api/ai/plugins/**`：

- 测试 ASR。
- 测试 TTS。
- 测试 LLM。
- 测试 Workflow。

请求只传测试文本、音频引用、插件编码和必要业务参数，不传密钥。

### 7.2 机器人流程接口

`/api/ai/robot-flows/**`：

- 创建流程。
- 编辑流程。
- 发布版本。
- 禁用流程。
- 查询流程列表。
- 查询版本列表。
- 查询版本详情。

### 7.3 机器人会话接口

`/api/ai/robot-sessions/**`：

- 启动会话。
- 处理一轮文本。
- 结束会话。
- 查询会话详情。
- 查询会话轮次轨迹。
- 查询转人工原因。

### 7.4 AI 质检与成本接口

`/api/ai/quality-results/**`：查询和写入 AI 质检结果。

`/api/ai/costs/**`：查询成本明细、按日期统计成本。

## 8. 错误处理

错误分为四类：

1. 配置错误：插件不存在、禁用、类型不匹配，返回业务错误。
2. 调用错误：HTTP 超时、状态码异常、供应商不可用，返回可降级 AI 结果并写失败日志。
3. 解析错误：供应商响应格式不符合契约，返回解析失败结果并写失败日志。
4. 会话错误：会话不存在、已结束、过期，返回统一会话错误。

所有错误日志只记录错误类型、状态码、延迟和摘要，不记录敏感正文。

## 9. 安全与脱敏

1. `och-resource` 对外返回插件密钥时继续脱敏。
2. `och-ai` 内部读取密钥后只用于调用供应商，不写日志、不写插件日志、不写审计摘要。
3. ASR 文本、Prompt、供应商响应只在会话轮次表中按业务必要性保存，不在插件调用日志中重复保存完整内容。
4. 插件调用日志和成本记录只存摘要。
5. AI 质检证据只存摘要和规则命中，不存超出业务需要的敏感原文。

## 10. 测试策略

### 10.1 单元测试

覆盖：

- 插件配置读取和启用校验。
- 适配器工厂选择。
- ASR/TTS/LLM/Workflow 成功解析。
- 供应商失败和解析失败。
- 会话启动、轮次处理、结束。
- 意图、槽位、摘要、标签、转人工判断。
- 插件调用日志摘要脱敏。
- 成本记录。
- AI 质检结果保存。

### 10.2 Mock HTTP 测试

覆盖：

- 通用 ASR HTTP 成功/失败。
- 通用 TTS HTTP 成功/失败。
- Dify 成功/失败。
- RAGFlow 检索成功、检索为空、生成失败。
- LangFlow 成功/失败。

### 10.3 构建验证

执行：

```bash
mvn -pl och-ai test
mvn -pl och-api -am -DskipTests compile
git diff --check
```

同时搜索旧 AI 残留：`DashScopeService`、`DeepSeekService`、`IntentInfo`、`IntentPattern`、`IntentVector`、`dashscope`、`deepseek`。

## 11. 验收标准

Phase 6 完整后端实现通过标准：

1. AI 插件配置只来自 `res_ai_plugin`。
2. ASR/TTS 通过通用 HTTP 插件契约调用。
3. Dify/RAGFlow/LangFlow/InternalBot 可通过已发布机器人版本选择。
4. 机器人流程支持创建、编辑、发布、禁用、版本查询。
5. 会话支持 start/turn/end/detail/trace。
6. 每轮对话记录意图、槽位、摘要、标签、延迟和转人工判断。
7. 插件调用日志和成本记录完整落库。
8. AI 质检结果在 `och-ai` 内落库并提供接口。
9. 插件失败、超时、禁用、类型错误、解析错误、会话过期均有测试覆盖。
10. 不存在真实密钥硬编码，不在日志或插件调用日志中输出敏感内容。
11. `mvn -pl och-ai test` 通过。
12. `mvn -pl och-api -am -DskipTests compile` 通过。
13. `git diff --check` 通过。
