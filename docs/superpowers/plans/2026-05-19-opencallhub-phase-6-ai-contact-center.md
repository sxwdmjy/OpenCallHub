# openCallHub Phase 6 AI 联络中心实施计划

> **执行说明：** 本文件是 Phase 6 编码前的执行计划。实施时按任务小步推进，每个任务完成后运行对应验证命令，不跨阶段实现客户、工单、报表、生产部署等后续范围。

## 目标

Phase 6 由刘工负责，目标是在 `och-ai` 模块内形成可配置、可测试、可追踪的 AI 联络中心能力：ASR/TTS/LLM/Workflow 插件抽象，Dify/RAGFlow/LangFlow/内置机器人适配，机器人流程版本、机器人会话、对话轮次、意图、槽位、摘要、标签、成本统计、插件调用日志、静音/无响应/转人工条件。

本阶段不直接接管 FreeSWITCH ESL 通信，不实现客户中心、工单中心、质检中心完整业务；只提供稳定的 AI 服务边界，供外呼、IVR、质检和报表后续阶段调用。

## 架构边界

- `och-ai` 负责 AI 插件抽象、机器人流程、会话追踪、供应商适配、插件调用日志和成本统计。
- `och-resource` 负责 AI 插件资源配置、密钥加密和脱敏展示；`och-ai` 只读取已授权配置，不硬编码真实密钥。
- `och-call-task` 负责机器人外呼任务调度；接通后的 AI 会话通过 `och-ai` 的机器人会话接口启动。
- `och-esl` 负责音频播放、录音、DTMF 和通话事件归一化；`och-ai` 不直接拼装 ESL 命令。
- `och-quality` 后续负责质检模板和评分流程；Phase 6 只保留 AI 质检结果和复核接口的扩展点。
- `och-report` 后续负责报表聚合；Phase 6 只记录可聚合的成本、延迟、失败和会话结果。

## 设计模式使用边界

- **适配器模式：** `BotAdapter` 统一 Dify、RAGFlow、LangFlow、InternalBot 的调用契约，隔离外部平台差异。
- **工厂模式：** `PluginAdapterFactory` 按插件类型选择适配器，避免业务代码直接依赖具体供应商实现。
- **策略模式：** 内置机器人、意图识别和转人工判断存在多种可替换规则时使用；当前只有单一逻辑时保留私有方法，不提前抽象。
- **模板方法模式：** 仅当 ASR/TTS/LLM 调用链出现稳定公共流程后再引入，本阶段不为了形式强行套用。
- 禁止为了覆盖 23 种设计模式新增单例、桥接、访问者等无业务变化点支撑的结构。

## 角色职责

- AI 后端负责人（刘工）：插件抽象、供应商适配、机器人流程、会话、意图、槽位、摘要、标签、成本和失败日志。
- 架构评审（周工）：模块边界、设计模式落点、事务边界、跨模块调用规范。
- 通信后端（陈工）：确认 ASR/TTS 与 ESL 音频播放、录音、转人工事件的集成边界。
- 外呼后端（孙工）：确认机器人外呼任务与 `och-ai` 会话启动边界。
- 安全运维（郑工）：确认 AI Token、ASR 文本、摘要、供应商响应摘要的脱敏和审计要求。
- QA（吴工）：提供插件失败、超时、静音、无响应、转人工和成本统计测试用例。

## 后端文件范围

### 需要创建或完善

- `och-ai/src/main/java/com/och/ai/application/RobotFlowApplicationService.java`：机器人流程应用服务，负责列表、创建、发布版本、删除。
- `och-ai/src/test/java/com/och/ai/application/RobotFlowApplicationServiceTest.java`：流程发布和版本号递增测试。
- `och-ai/src/test/java/com/och/ai/application/RobotFlowEngineTest.java`：机器人会话启动、轮次处理、结束测试。
- `och-ai/src/test/java/com/och/ai/application/adapter/PluginAdapterFactoryTest.java`：适配器工厂测试。

### 需要修改

- `och-ai/src/main/resources/db/migration/V12__ai_robot_schema.sql`：补齐机器人流程、会话、对话轮次、插件日志、成本记录相关表。
- `och-ai/src/main/java/com/och/ai/api/RobotFlowController.java`：Controller 只接收参数和封装响应，业务委托应用服务。
- `och-ai/src/main/java/com/och/ai/application/RobotFlowEngine.java`：写入机器人会话、AI 对话、轮次和插件调用日志。
- `och-ai/src/main/java/com/och/ai/application/AiCostTracker.java`：成本记录落库，提供按日期统计。
- `och-ai/src/main/java/com/och/ai/application/adapter/*.java`：保留适配器边界，避免记录密钥和敏感内容。

## 分步任务

### Task 1：计划与边界确认

- [ ] 补齐本 Phase 6 计划文件。
- [ ] 确认 `och-ai` 只承担 AI 能力，不越界实现外呼调度、ESL 命令、报表聚合和客户工单。
- [ ] 确认代码注释只解释业务规则、状态流转、边界约束和设计意图，不为简单 getter/setter 或 CRUD 制造噪音。

### Task 2：流程发布闭环

- [ ] 创建 `RobotFlowApplicationService`。
- [ ] `RobotFlowController` 改为委托应用服务。
- [ ] 实现流程发布：读取流程、生成递增版本号、写入 `robot_flow_version`，状态为 `PUBLISHED`。
- [ ] 保留审计注解，发布和删除操作写审计入口。

### Task 3：数据库与会话追踪

- [ ] 在 V12 迁移中补齐 `ai_conversation` 和 `ai_conversation_turn`。
- [ ] 启动机器人会话时写入 `robot_session` 和 `ai_conversation`。
- [ ] 每轮对话写入 `ai_conversation_turn` 和 `plugin_call_log`。
- [ ] 结束会话时更新会话和对话状态、结束时间、结束原因。

### Task 4：成本统计持久化

- [ ] `AiCostTracker` 使用 `AiCostRecordMapper` 持久化成本记录。
- [ ] 按日期统计从数据库读取，避免只依赖内存。
- [ ] 成本日志不输出 Token、完整 Prompt、完整供应商响应等敏感内容。

### Task 5：单元测试与失败场景

- [ ] 测试适配器工厂正常选择与未知类型失败。
- [ ] 测试流程发布版本号递增。
- [ ] 测试机器人会话 start/turn/end 的最小追踪闭环。
- [ ] 测试转人工意图可从内置机器人触发。

## 验收门槛

Phase 6 后端最小验收通过条件：

1. `och-ai` 可以编译通过。
2. ASR/TTS/LLM 测试接口可返回统一响应和 traceId。
3. Dify/RAGFlow/LangFlow/InternalBot 适配器能通过工厂按类型选择。
4. 机器人流程可创建并发布版本。
5. 机器人会话可启动、处理至少一轮对话、结束，并留下会话、轮次、插件调用日志。
6. 成本记录可写入并按日期统计。
7. 插件失败、未知插件类型、会话过期不会导致 Controller 直接抛未封装异常。
8. 不出现真实密钥硬编码，不在日志输出密钥、完整敏感文本或供应商 Token。

## 验证命令

```bash
mvn -pl och-ai test
mvn -pl och-api -am -DskipTests compile
git diff --check
```

如果全仓编译因其他大范围重构模块失败，只记录失败模块和错误摘要；本阶段优先修复 `och-ai` 作用域内的问题。

## 提交检查点

实施者可按以下粒度分批提交：

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-6-ai-contact-center.md
git commit -m "docs: add phase 6 ai contact center plan"

git add och-ai
git commit -m "refactor: add ai robot conversation trace"

git add och-ai/src/test
git commit -m "test: cover ai robot phase 6 foundation"
```

提交前必须确认 `git diff --check` 通过，并确认没有把密钥、环境配置或临时日志加入版本库。
