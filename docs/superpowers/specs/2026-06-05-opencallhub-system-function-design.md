# openCallHub ESL 重构后完整系统功能设计

**日期：** 2026-06-05  
**范围：** openCallHub 完整呼叫中心平台功能设计、ESL 重构后的前后端产品闭环、角色权限、页面信息架构、接口与验收标准。  
**设计角色：** 架构师、产品专家、UI 专家。  

---

## 1. 系统定位

openCallHub 是企业级智能呼叫中心平台。系统目标不是只提供电话条，而是围绕客户触达、呼入接待、外呼任务、IVR 流程、座席协同、质检合规、AI 辅助和运营分析形成完整闭环。

系统能力分为六层：

1. **业务入口层**：座席工作台、外呼任务台、IVR 流程中心、ESL 运维面板、客户中心、报表中心。
2. **业务编排层**：呼入流程、外呼任务、预测外呼、技能组路由、座席状态、话后处理。
3. **通信控制层**：ESL 连接管理、FreeSWITCH 命令、通道状态、DTMF、WebSocket 实时推送。
4. **数据资产层**：客户、人群、客户字段、数据源、通话记录、录音、任务客户池。
5. **智能能力层**：ASR、TTS、LLM、机器人流程、智能质检、知识库辅助。
6. **治理运维层**：权限、审计、合规、报表、告警、批处理、系统配置。

架构原则：ESL 模块只处理连接、命令和事件归一化；呼叫、外呼、IVR、ACD、质检、报表分别在独立模块内闭环。前端通过 API adapter 和 WebSocket 接入，不直接暴露 SIP/ESL 底层细节。

---

## 2. 角色体系

### 2.1 核心角色

| 角色 | 目标 | 主要页面 |
| --- | --- | --- |
| 座席 | 接听、拨打、处理客户、提交话后 | 座席工作台、外呼任务台 |
| 班长/主管 | 管理任务、监督座席、查看团队状态 | 外呼任务台、团队监控、报表 |
| 运营配置员 | 配置 IVR、任务策略、客户数据、业务规则 | IVR 流程中心、客户中心、任务配置 |
| 系统运维 | 保障 FreeSWITCH、ESL、WebSocket、系统稳定 | ESL 运维面板、系统监控 |

### 2.2 扩展角色

| 角色 | 目标 |
| --- | --- |
| 质检员 | 查看录音、评分、质检任务、违规识别 |
| 合规管理员 | 管理敏感词、合规策略、投诉/风险记录 |
| 知识管理员 | 维护知识库、话术、FAQ |
| 工单处理员 | 处理客户问题、跟进工单 |
| 超级管理员 | 拥有全部权限，但不设计独立页面体系 |

产品原则：超级管理员只是权限集合，不应成为单独产品角色。页面应围绕真实业务职责设计。

---

## 3. 核心页面体系

ESL 重构后的第一阶段前端采用四个独立新页面，而不是继续修补旧页面：

1. **座席工作台**：座席日常通话和话后处理入口。
2. **外呼任务台**：座席执行任务、班长监督任务。
3. **IVR 流程中心**：运营配置和发布呼入流程。
4. **ESL 运维面板**：系统运维监控和处理 ESL/FreeSWITCH 状态。

### 3.1 页面与角色可见性

| 页面 | 座席 | 班长/主管 | 运营配置员 | 系统运维 |
| --- | --- | --- | --- | --- |
| 座席工作台 | 可操作 | 可只读查看团队通话摘要 | 不可见 | 不可见 |
| 外呼任务台 | 可执行个人任务 | 可管理任务生命周期 | 可只读查看任务引用策略 | 不可见 |
| IVR 流程中心 | 不可见 | 可只读查看已发布流程 | 可配置和发布 | 不可见 |
| ESL 运维面板 | 不可见 | 不可见 | 不可见 | 可查看和操作 |

---

## 4. 座席工作台设计

### 4.1 页面定位

座席工作台是座席每天主要使用的高频操作页面。页面重点是当前通话、客户信息、电话条动作、事件时间线和话后处理，不展示 IVR 配置、ESL 运维、全局任务配置等非座席职责能力。

### 4.2 功能清单

- 座席状态展示：离线、空闲、振铃中、通话中、保持中、话后处理中。
- 电话条操作：接听、挂断、保持、取消保持、盲转、咨询转、拨号。
- 当前通话展示：主叫、被叫、客户、callId、通话时长、通话状态。
- 通话事件时间线：振铃、接听、保持、转接、挂断、话后处理。
- 客户信息卡：基础资料、标签、历史联系记录、最近工单。
- 话后处理：通话结果、客户意向、备注、下次跟进时间。
- 个人外呼任务：已领取客户、待拨打客户、拨打结果。
- WebSocket 实时状态：通话变化、命令回执、异常提醒。

### 4.3 UI 布局

三栏工作台：

- 顶部：座席状态、WebSocket 状态、当前技能组、在线时长。
- 左侧：客户信息、最近联系记录、客户标签。
- 中间：当前通话卡片、电话条操作区、拨号键盘。
- 右侧：事件时间线、话后处理表单。

### 4.4 API 映射

- `POST /api/agent/phone-bar/answer`
- `POST /api/agent/phone-bar/hangup`
- `POST /api/agent/phone-bar/hold`
- `POST /api/agent/phone-bar/unhold`
- `POST /api/agent/phone-bar/blind-transfer`
- `POST /api/agent/phone-bar/consult-transfer`
- `POST /api/agent/phone-bar/after-call-work`
- `GET /api/agent/phone-bar/records`
- `GET /api/call/sessions`
- `GET /api/call/sessions/{callId}/events`

### 4.5 权限

- `call:workspace:view`
- `call:phone:answer`
- `call:phone:hangup`
- `call:phone:hold`
- `call:phone:transfer`
- `call:phone:dial`
- `call:after-work:submit`
- `call:event:view`

### 4.6 产品评审

旧 `SoftPhone.vue` 中前端直连 JsSIP，并存在 SIP 账号、密码、服务器硬编码，不适合作为重构后的主方案。新座席工作台应以“后端电话条 API + WebSocket 实时状态”为核心，前端只负责发起业务动作和展示状态。

---

## 5. 外呼任务台设计

### 5.1 页面定位

外呼任务台是任务执行与监督入口。座席使用它领取客户并拨打；班长/主管使用它管理任务生命周期、监督进度和处理异常。

### 5.2 功能清单

班长/主管能力：

- 查看所有外呼任务。
- 查看任务状态、进度、成功率、失败率、接通率。
- 启动、暂停、取消任务。
- 查看客户池状态和执行座席。
- 应用客户过滤策略。
- 查看异常任务。

座席能力：

- 查看自己可执行的任务。
- 领取客户。
- 释放客户。
- 开始拨打。
- 填写拨打结果。
- 查看个人任务进度。

### 5.3 状态模型

任务状态：

- `DRAFT`：草稿。
- `SUBMITTED`：已提交。
- `RUNNING`：执行中。
- `PAUSED`：已暂停。
- `CANCELLED`：已取消。
- `COMPLETED`：已完成。
- `FAILED`：异常。

客户状态：

- `READY`：待领取。
- `CLAIMED`：已领取。
- `DIALING`：拨打中。
- `CONNECTED`：已接通。
- `FAILED`：拨打失败。
- `RETRY_WAIT`：等待重试。
- `RELEASED`：已释放。
- `DONE`：已完成。

### 5.4 API 映射

- `GET /api/outbound/tasks`
- `GET /api/outbound/tasks/{id}`
- `POST /api/outbound/tasks/{id}/submit`
- `POST /api/outbound/tasks/{id}/start`
- `POST /api/outbound/tasks/{id}/pause`
- `POST /api/outbound/tasks/{id}/cancel`
- `POST /api/outbound/tasks/{id}/filter`
- `POST /api/outbound/tasks/{taskId}/claim`
- `POST /api/outbound/tasks/{taskId}/release/{customerId}`
- `POST /api/outbound/tasks/{taskId}/dial/{customerId}`

### 5.5 权限

- `outbound:task:view`
- `outbound:task:submit`
- `outbound:task:start`
- `outbound:task:pause`
- `outbound:task:cancel`
- `outbound:task:filter`
- `outbound:customer:claim`
- `outbound:customer:release`
- `outbound:customer:dial`
- `outbound:task:monitor`

---

## 6. IVR 流程中心设计

### 6.1 页面定位

IVR 流程中心服务运营配置员，用于配置呼入流程、语音导航、按键收号、转人工和发布版本。它不是座席页面，也不是系统运维页面。

### 6.2 功能清单

- IVR 流程列表。
- 新建、编辑、删除流程。
- 发布流程。
- 查看流程版本。
- LogicFlow 画布编辑。
- 节点属性配置。
- 连线条件配置。
- 发布前校验。
- 已发布/草稿状态区分。

### 6.3 节点类型

- 开始节点。
- 播放语音节点。
- 收号节点。
- 条件判断节点。
- 转人工节点。
- 转技能组节点。
- 机器人节点。
- 挂机节点。
- 结束节点。

### 6.4 发布校验

发布前必须检查：

- 存在开始节点。
- 存在结束节点或挂机节点。
- 不存在孤立节点。
- 不存在无效连线。
- 播放语音节点配置了语音资源。
- 转人工节点配置了技能组或目标。
- 条件分支存在默认出口。

### 6.5 API 映射

- `GET /api/ivr/flows`
- `GET /api/ivr/flows/{id}`
- `POST /api/ivr/flows`
- `PUT /api/ivr/flows/{id}`
- `DELETE /api/ivr/flows/{id}`
- `POST /api/ivr/flows/{id}/publish`

### 6.6 权限

- `ivr:flow:view`
- `ivr:flow:create`
- `ivr:flow:update`
- `ivr:flow:delete`
- `ivr:flow:publish`
- `ivr:flow:readonly`

产品原则：保存和发布必须分开。保存只是草稿；发布会影响真实呼入流量，必须二次确认并经过校验。

---

## 7. ESL 运维面板设计

### 7.1 页面定位

ESL 运维面板只给系统运维和超级管理员使用。它是生产稳定性页面，不面向普通业务人员。

### 7.2 功能清单

- ESL 连接状态总览。
- FreeSWITCH 节点列表。
- 单节点连接状态。
- 手动重连。
- 手动断开。
- 活跃通道列表。
- 通道详情。
- DTMF 缓冲查看与清理。
- 命令回执清理。
- 运行指标展示。
- 最近异常展示。
- 运维操作审计。

### 7.3 高风险操作

以下操作必须二次确认：

- 重连 ESL 节点。
- 断开 ESL 节点。
- 清理命令回执。
- 清理 DTMF 缓冲。

### 7.4 API 映射

- `GET /api/esl/status`
- `GET /api/esl/status/{freeswitchId}`
- `POST /api/esl/reconnect/{freeswitchId}`
- `POST /api/esl/disconnect/{freeswitchId}`
- `GET /api/esl/ops/channels`
- `GET /api/esl/ops/channels/{uuid}`
- `GET /api/esl/ops/stats`
- `POST /api/esl/ops/dtmf/flush/{callId}`
- `POST /api/esl/ops/dtmf/clean-expired`
- `POST /api/esl/ops/cmd/clean-expired`

### 7.5 权限

- `esl:ops:view`
- `esl:node:reconnect`
- `esl:node:disconnect`
- `esl:channel:view`
- `esl:dtmf:flush`
- `esl:cmd:clean`

---

## 8. 客户中心设计

客户中心是外呼任务、通话记录、工单、质检的基础数据来源。

功能清单：

- 客户列表。
- 客户详情。
- 客户标签。
- 客户字段配置。
- 客户模板。
- 客户导入。
- 客户分群。
- 客户公海。
- 客户归属。
- 客户联系记录。
- 客户任务关联。
- 客户工单关联。

动态字段类型：文本、数字、单选、多选、日期、手机号、地址、备注。

产品原则：客户字段不能写死。不同企业、不同外呼任务需要不同客户字段，所以必须支持模板化和动态字段。

---

## 9. ACD 与技能组设计

ACD 用于呼入分配和座席排队，是呼叫中心核心调度能力。

功能清单：

- 技能组管理。
- 座席加入/退出技能组。
- 技能组优先级。
- 队列策略。
- 空闲座席分配。
- 溢出策略。
- 无人接听策略。
- 呼入转接。
- 班长查看技能组状态。

分配策略：最长空闲优先、轮询、技能等级优先、固定优先级、随机。

权限：

- `acd:skill:view`
- `acd:skill:create`
- `acd:skill:update`
- `acd:skill:delete`
- `acd:agent:assign`
- `acd:queue:monitor`

架构原则：IVR 只决定转到哪个技能组；ACD 决定哪个座席接。

---

## 10. 呼叫会话与通话记录设计

呼叫会话是一次通话的实时状态。

呼叫会话字段：

- callId。
- caller。
- callee。
- direction：呼入/呼出。
- state。
- agentId。
- customerId。
- taskId。
- freeswitchUuid。
- startTime。
- answerTime。
- hangupTime。
- hangupCause。

状态：`CREATED`、`RINGING`、`ANSWERED`、`HELD`、`TRANSFERRED`、`HANGUP`、`AFTER_WORK`、`CLOSED`。

通话事件字段：

- eventId。
- callId。
- eventType。
- eventTime。
- freeswitchUuid。
- rawEvent。
- operator。
- result。

通话事件用于座席时间线、班长异常定位、报表统计、质检回放和合规审计。

---

## 11. WebSocket 实时推送设计

WebSocket 用于推送实时业务变化，避免页面频繁轮询。

推送事件类型：

- `CALL_RINGING`
- `CALL_ANSWERED`
- `CALL_HELD`
- `CALL_UNHELD`
- `CALL_TRANSFERRED`
- `CALL_HANGUP`
- `CALL_AFTER_WORK_STARTED`
- `CALL_AFTER_WORK_SUBMITTED`
- `OUTBOUND_TASK_STARTED`
- `OUTBOUND_TASK_PAUSED`
- `OUTBOUND_TASK_CANCELLED`
- `OUTBOUND_CUSTOMER_CLAIMED`
- `OUTBOUND_DIAL_STARTED`
- `OUTBOUND_DIAL_FINISHED`
- `ESL_NODE_CONNECTED`
- `ESL_NODE_DISCONNECTED`
- `ESL_CHANNEL_CREATED`
- `ESL_CHANNEL_DESTROYED`

前端原则：页面进入时先 HTTP 拉取当前快照，再建立 WebSocket 订阅。WebSocket 用于增量更新，不能取代 HTTP 快照。

---

## 12. AI 能力设计

AI 模块用于提升呼叫中心效率，而不是替代基础呼叫能力。

功能清单：

- ASR：通话语音转文本。
- TTS：IVR 或机器人语音播报。
- LLM：话术辅助、摘要生成、意图识别。
- 机器人流程：机器人外呼或机器人客服。
- 智能质检：识别违规话术。
- 智能摘要：通话后自动生成总结。
- 智能标签：自动识别客户意向。

第一阶段建议优先落地话后摘要、智能标签、智能质检，因为这些最容易和通话记录闭环。

---

## 13. 质检中心设计

质检中心用于保证服务质量和合规性。

功能清单：

- 通话录音列表。
- 通话转写文本。
- 质检任务。
- 人工评分。
- 自动评分。
- 敏感词命中。
- 违规项标记。
- 质检申诉。
- 质检报告。

评分维度：开场语、身份核验、服务态度、业务解释、敏感词、承诺用语、挂机礼貌、客户满意度。

---

## 14. 合规中心设计

合规中心用于控制敏感操作、违规话术、投诉风险。

功能清单：

- 敏感词库。
- 合规规则。
- 投诉记录。
- 黑名单。
- 呼叫频次限制。
- 禁呼时间段。
- 号码脱敏。
- 操作审计。
- 数据导出审计。

产品原则：外呼系统天然有监管风险，频次、禁呼、敏感词、黑名单应作为基础能力设计。

---

## 15. 报表中心设计

报表中心用于运营分析、任务复盘和管理决策。

报表类型：

- 座席报表：接听数、外呼数、接通数、平均通话时长、话后处理时长、空闲时长、登录时长、挂断原因分布。
- 外呼任务报表：任务总量、客户总数、已拨打数、接通率、成功率、失败原因、重试次数、任务完成率。
- IVR 报表：入口呼叫数、节点访问次数、按键分布、转人工率、挂断率、平均停留时长。
- ESL 运维报表：节点在线率、断连次数、命令失败次数、活跃通道峰值、异常通道数。

---

## 16. 工单中心设计

工单中心用于沉淀客户问题和后续处理。

功能清单：

- 新建工单。
- 工单分配。
- 工单状态流转。
- 工单关联客户。
- 工单关联通话。
- 工单备注。
- 工单催办。
- 工单关闭。

状态：`OPEN`、`ASSIGNED`、`PROCESSING`、`PENDING_CUSTOMER`、`RESOLVED`、`CLOSED`、`CANCELLED`。

产品原则：座席在话后处理时应能直接创建工单，并自动关联通话记录和客户。

---

## 17. 知识库设计

知识库用于支持座席话术、FAQ、业务解释和 AI 回答。

功能清单：

- 知识分类。
- 文档管理。
- FAQ 管理。
- 话术模板。
- 搜索。
- 推荐。
- AI 引用。
- 版本管理。

使用场景：座席通话中搜索知识；AI 根据客户问题推荐答案；质检时判断座席回答是否符合标准话术。

---

## 18. 通知中心设计

通知中心用于系统消息、任务提醒、异常告警。

通知类型：任务开始、任务暂停、客户领取成功、通话异常、ESL 节点断开、IVR 发布成功、质检任务分配、工单超时。

通知渠道：页面站内信、WebSocket 实时提醒、邮件、企业微信或钉钉。

---

## 19. 文件与资源中心设计

文件与资源中心用于管理录音、语音文件、导入模板、客户文件等。

功能清单：

- 录音文件。
- IVR 语音文件。
- TTS 生成文件。
- 客户导入文件。
- 导入模板。
- 文件上传。
- 文件下载。
- 文件权限。
- 文件清理策略。

产品原则：IVR 语音、通话录音、客户导入文件都属于资源，但生命周期不同。前端可统一入口，后端按业务类型分开管理。

---

## 20. 批处理中心设计

批处理中心用于客户导入、任务生成、录音转写、报表生成等异步任务。

功能清单：

- 批量导入客户。
- 批量生成外呼客户池。
- 批量转写录音。
- 批量生成报表。
- 查看任务进度。
- 查看失败明细。
- 重新执行失败项。

状态：`PENDING`、`RUNNING`、`SUCCESS`、`PARTIAL_SUCCESS`、`FAILED`、`CANCELLED`。

---

## 21. 系统管理设计

系统管理提供用户、角色、菜单、权限和数据权限能力。

功能清单：

- 用户管理。
- 角色管理。
- 菜单管理。
- 权限管理。
- 部门/组织管理。
- 数据权限。
- 登录日志。
- 操作日志。
- 字典管理。
- 系统参数。

权限模型：页面权限 + 按钮权限 + 数据权限 + 接口权限。

示例：

- 页面权限：`ivr:flow:view`。
- 按钮权限：`ivr:flow:publish`。
- 数据权限：只能看本部门任务。
- 接口权限：`@PreAuthorize("@authz.hasPerm('ivr:flow:publish')")`。

---

## 22. 前端信息架构

建议菜单结构：

```text
呼叫中心
  座席工作台
  外呼任务台
  团队监控

流程配置
  IVR 流程中心
  语音资源管理
  技能组配置

客户运营
  客户中心
  客户分群
  客户公海
  数据源管理

质量合规
  质检中心
  合规中心
  敏感词库
  投诉记录

智能能力
  机器人流程
  知识库
  AI 配置
  话术模板

报表中心
  座席报表
  外呼任务报表
  IVR 报表
  通话报表
  运维报表

系统运维
  ESL 运维面板
  WebSocket 监控
  系统日志
  批处理中心

系统管理
  用户管理
  角色管理
  菜单权限
  字典配置
  系统参数
```

UI 原则：菜单按用户理解命名，不按后端模块名命名。例如使用“外呼任务台”“IVR 流程中心”“ESL 运维面板”，而不是 `och-call-task`、`och-ivr`、`och-esl`。

---

## 23. 后端模块边界

| 模块 | 职责 |
| --- | --- |
| `och-common` | 基础工具、统一响应、常量、异常、基础实体 |
| `och-infra` | Redis、MQ、数据库基础设施、事件发布 |
| `och-security` | 登录、鉴权、权限、JWT、安全上下文 |
| `och-system` | 用户、角色、菜单、字典、系统配置 |
| `och-customer` | 客户、人群、字段、模板、公海 |
| `och-call` | 呼叫会话、通话事件、电话条接口 |
| `och-esl` | ESL 连接、命令、事件归一化、通道状态 |
| `och-ivr` | IVR 流程、节点、版本、运行引擎 |
| `och-call-task` | 外呼任务、客户池、拨打尝试、任务生命周期 |
| `och-acd` | 技能组、路由、座席分配 |
| `och-ai` | ASR/TTS/LLM/机器人适配 |
| `och-quality` | 质检规则、评分、质检任务 |
| `och-compliance` | 合规规则、敏感词、投诉、审计 |
| `och-report` | 统计报表 |
| `och-ticket` | 工单 |
| `och-knowledge` | 知识库 |
| `och-notice` | 通知 |
| `och-batch` | 批处理 |
| `och-file` | 文件资源 |
| `och-websocket` | 实时推送 |

---

## 24. 核心业务流程

### 24.1 呼入流程

```text
客户来电
  -> FreeSWITCH 收到呼叫
  -> ESL 事件进入 och-esl
  -> 事件归一化为 NormalizedEslEvent
  -> och-call 创建 CallSession
  -> IVR 判断流程
  -> ACD 分配技能组/座席
  -> WebSocket 推送给座席
  -> 座席接听
  -> 通话结束
  -> 话后处理
  -> 生成通话记录/报表/质检数据
```

### 24.2 外呼流程

```text
运营或班长创建外呼任务
  -> 导入客户池
  -> 提交任务
  -> 班长启动任务
  -> 座席领取客户
  -> 座席拨打
  -> PhoneBar/CallTask 触发呼叫命令
  -> ESL 发送 FreeSWITCH 命令
  -> 通话事件回流
  -> 更新客户状态
  -> 话后处理
  -> 生成任务报表
```

### 24.3 IVR 发布流程

```text
运营创建流程
  -> 编辑节点和连线
  -> 保存草稿
  -> 发布前校验
  -> 发布版本
  -> 呼入流程引用已发布版本
  -> 呼叫运行时按版本执行
```

### 24.4 质检流程

```text
通话结束
  -> 录音生成
  -> ASR 转写
  -> 自动质检规则匹配
  -> 生成初始评分
  -> 质检员复核
  -> 座席可申诉
  -> 主管查看质检结果
```

---

## 25. UI 设计原则

- 座席页面轻量高频，突出当前通话和下一步操作。
- 班长页面强调任务监督、状态统计和异常处理。
- 运营页面强调配置、版本、发布和校验。
- 运维页面强调健康状态、风险操作和审计结果。
- 状态颜色统一：绿色正常、蓝色进行中、橙色等待/暂停、红色异常、灰色未知。
- 危险操作必须二次确认。
- 空状态必须提供下一步引导。
- 中文注释只解释复杂约束和业务原因，不解释变量表面含义。

---

## 26. 验收标准

### 26.1 产品闭环

- 座席能完成接听、挂断、保持、转接、话后处理。
- 班长能完成外呼任务启动、暂停、取消、监督。
- 运营能完成 IVR 创建、编辑、发布。
- 运维能查看 ESL 状态并执行重连/断开。
- 客户能关联任务、通话、工单、质检。
- 报表能反映任务和通话结果。

### 26.2 权限

- 座席看不到 ESL 运维面板。
- 座席不能发布 IVR。
- 运营不能重连 ESL。
- 班长不能绕过权限操作运维接口。
- 后端接口有 `@PreAuthorize`。
- 前端按钮和路由按权限展示。

### 26.3 技术

- 后端 `mvn -DskipTests compile` 通过。
- 前端 `npm --prefix waihu-app run build` 通过。
- API adapter 不散落硬编码路径。
- 旧 `/call/v1/ivr/*`、`/call/task/v1/*` 不再作为核心页面依赖。
- WebSocket 断开有提示和重连机制。
- ESL stub 或未接真实客户端的风险明确记录。

---

## 27. 第一阶段落地范围

第一阶段优先落地：

1. 座席工作台。
2. 外呼任务台。
3. IVR 流程中心。
4. ESL 运维面板。
5. `callCenter` 前端 API adapter。
6. 页面级和按钮级权限常量。
7. 新路由模块。
8. 移除全局旧软电话条入口，避免旧 JsSIP 硬编码继续作为主流程。

其他完整系统功能，如客户中心、质检中心、合规中心、报表中心、工单中心、知识库、通知中心、批处理中心，作为完整系统设计留档，后续按模块分阶段实施。
