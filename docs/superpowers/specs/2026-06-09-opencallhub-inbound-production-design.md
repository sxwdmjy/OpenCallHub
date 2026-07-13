# openCallHub 完整呼入域生产验收设计

日期：2026-06-09

## 1. 设计目标

本设计用于指导 openCallHub Phase 3 + Phase 4 的完整呼入域重构。目标不是最小闭环，而是把呼入、IVR、ACD、坐席工作台、记录沉淀、权限合规、报表和验收体系做到可演示、可回归、可真实联调的生产验收形态。

本次重构以赵工负责的呼入、IVR、ACD、坐席呼入闭环为主线，同时纳入权限、录音、报表、安全审计、自动化测试和真实 FreeSWITCH / ESL 联调要求。涉及李工、郑工、吴工等协作边界的能力以呼入域必需范围为准，不扩展到外呼、AI、工单、知识库、质检和全量合规平台。

核心业务链路如下：

```text
号码接入 → FreeSWITCH/ESL事件归一化 → 创建呼叫会话 → IVR流程执行
→ 日程判断 / 菜单DTMF / 语音信箱 / 转技能组 → ACD排队与策略分配
→ 坐席弹屏 → 电话条控制 → 后处理 → 通话记录 / 录音记录 / 服务记录 / 客户关联
→ 队列与坐席报表 → 权限、脱敏、审计、E2E验收
```

## 2. 范围边界

### 2.1 本次包含

1. 通信与呼叫核心：ESL 事件归一化、命令发送、通话会话、通话腿、通话事件、通话状态机、命令状态。
2. IVR 运行域：流程版本、节点、边、变量、发布、回滚、测试运行、执行轨迹、菜单、日程、语音信箱。
3. ACD 分配域：技能组、队列、坐席状态、分配策略、溢出规则、SLA、队列指标。
4. 坐席呼入闭环：坐席工作台、电话条、弹屏、客户卡、通话控制、转接、挂机、后处理。
5. 数据治理与运营：通话记录、录音记录、服务记录、客户关联、呼入日报、队列日报、坐席日报。
6. 权限合规：菜单权限、按钮权限、数据范围、字段脱敏、录音播放和下载审计。
7. 验收体系：Mock / Fake ESL 自动化回归、真实 FreeSWITCH / ESL 联调、前端 E2E、安全越权测试、生产验收清单。

### 2.2 本次不包含

1. 外呼任务、预览外呼、预测外呼、机器人外呼。
2. AI ASR / TTS / LLM / Dify / RAGFlow / LangFlow 完整链路。
3. 工单、知识库、质检和客户池全量业务。
4. 全平台通知、批处理、合规导出平台和生产部署总包。本次只保留呼入域必要的异常提醒、测试进度和联调配置。

### 2.3 关键决策

1. 数据库按新领域模型重建新表，不受旧呼叫、IVR、坐席相关表结构限制。
2. 旧呼叫、IVR、ACD、坐席相关入口直接替换为新接口和新页面，不做长期兼容层。
3. 验收采用双通道：本地 Mock / Fake ESL 自动化测试和真实 FreeSWITCH / ESL 联调入口并行。
4. 代码风格沿用当前仓库的 `com.och` 包名和 `Controller / Service / Mapper / Domain / Query / Vo` 分层习惯。
5. 中文 UTF-8 注释只解释业务规则、状态流转、边界约束、并发控制、幂等条件和设计意图，不为普通 getter、setter 或简单 CRUD 制造噪音。

## 3. 后端模块架构

### 3.1 模块职责

#### och-esl

`och-esl` 只负责 FreeSWITCH ESL 边界：连接管理、断线恢复、原始事件保留、事件归一化、命令发送和命令回执。它输出标准 `NormalizedEslEvent`，提供 answer、playback、collect digits、bridge、hold、unhold、mute、unmute、blind transfer、consult transfer、record、hangup 等命令接口。

`och-esl` 不判断 IVR、ACD、坐席、外呼或客户业务规则，避免通信层变成业务层。

#### och-call

`och-call` 是通话事实中心，负责 `call_session`、`call_leg`、`call_event`、`call_command`、通话状态机、事件幂等、乱序保护、通话记录、录音记录、服务记录和电话条命令记录。

HTTP 命令成功只表示命令已受理，最终通话状态必须以 ESL 事件回流为准。

#### och-ivr

`och-ivr` 负责流程定义和运行时，包括流程、版本、节点、边、变量、发布、回滚、测试运行、执行轨迹、节点执行历史和语音信箱。

IVR 节点处理采用工厂模式和策略模式。节点工厂按节点类型选择处理器；菜单、日程、条件、播放、收号、转技能组、语音信箱、挂机等处理器只关心自己的节点规则。

#### och-acd

`och-acd` 负责排队和分配，包括技能组、队列、坐席状态、分配策略、溢出规则、SLA 和队列指标快照。ACD 不直接控制 FreeSWITCH，只产出分配结果、队列事件和坐席状态变化。

#### och-websocket

`och-websocket` 负责 `/ws/agent`、`/ws/monitor`、`/ws/notice`、`/ws/task` 的统一推送。它不直接写业务表，只根据事件类型、用户、角色、权限和数据范围做消息分发。

#### och-customer

`och-customer` 在本次只提供坐席工作台需要的客户查询、客户电话、客户关联和基础脱敏能力。客户池、分配、工单、知识库等完整客户服务能力不在本次范围内。

#### och-report

`och-report` 在本次只提供呼入总览日报、队列日报和坐席呼入日报的聚合与查询能力，不扩展外呼、AI、质检和全平台报表。

#### och-security

`och-security` 提供权限、数据范围、字段脱敏、录音访问控制和操作审计支撑。Controller 不应手工拼接复杂权限逻辑。

#### och-api

`och-api` 是正式 REST 入口和应用门面层。呼入编排服务在这里聚合 `och-call`、`och-ivr`、`och-acd`、`och-websocket`、`och-customer`、`och-report`，Controller 只负责参数接收、权限入口和响应封装。

### 3.2 数据流

1. FreeSWITCH 产生 ESL 事件，`och-esl` 归一化后发布应用事件。
2. `och-call` 消费事件，按 `callId + eventType + eventTime + uuid` 做幂等，更新通话会话和通话腿。
3. 呼入会话进入 `och-api` 的呼入编排服务，由编排服务启动 `och-ivr`。
4. `och-ivr` 执行节点。播放、收号、转接、挂机等媒体动作通过 `och-esl` 命令端口发出；菜单、日程、条件节点在 IVR 内部决策；转技能组节点输出明确 `skillGroupId`。
5. `och-api` 编排服务把转技能组结果交给 `och-acd` 入队。
6. `och-acd` 根据服务时间、容量、SLA、溢出和分配策略选择坐席。
7. 分配结果写回呼叫会话，推送 `/ws/agent` 给目标坐席，并推送 `/ws/monitor` 更新队列监控。
8. 坐席电话条命令进入 `och-call` 记录受理状态，再通过 `och-esl` 发命令。
9. ESL 事件回流后确认真实通话状态。
10. 挂机后进入后处理，写服务记录、通话记录、录音记录和客户关联，再由 `och-report` 聚合呼入指标。

### 3.3 事务与事件边界

核心写操作先本地事务落库，再发布事件。高频通信事件避免长事务。跨模块异步扩展通过事件发布，消费端用幂等键去重。

本地事务边界包括：呼叫事件摄入、IVR 发布、入队分配、坐席状态切换、电话条命令受理、后处理提交、录音访问审计和报表重算任务创建。

## 4. 设计模式使用边界

1. 工厂模式：IVR 节点处理器、WebSocket 消息处理器、ESL 命令构造器。
2. 策略模式：ACD 分配策略、IVR 节点执行策略、权限和脱敏策略。
3. 状态模式：通话状态、坐席状态、IVR 执行状态、队列会话状态、电话条命令状态。
4. 门面模式：`och-api` 呼入编排服务聚合 Call / IVR / ACD / WebSocket。
5. 观察者 / 发布订阅：ESL 事件、队列监控、坐席事件、报表聚合。
6. 命令模式：电话条控制命令和 ESL 控制命令，用于记录、审计、重试和状态追踪。

设计模式必须服务业务变化点，不为了覆盖 23 种设计模式强行引入复杂度。

## 5. 数据库模型

数据库按新表设计。核心业务表统一包含 `id`、`tenant_id`、`created_by`、`created_at`、`updated_by`、`updated_at`、`deleted`、`status`、`remark`。事件表额外包含 `trace_id`、`call_id`、`event_time`、`source`、`raw_payload`。

### 5.1 呼叫核心表

1. `call_session`：通话主会话，保存 `call_id`、方向、主叫、被叫、当前状态、当前 IVR 执行、当前队列会话、当前坐席、开始时间、接听时间、结束时间、结束原因和 traceId。
2. `call_leg`：通话腿，保存 caller、agent、transfer、consult 等 leg，关联 FreeSWITCH `uuid`、`leg_id`、桥接关系和腿状态。
3. `call_event`：归一化事件流水，保存 eventId、eventType、eventTime、sequenceNo、uuid、legId、normalizedPayload、rawPayload。
4. `call_event_dedup`：事件幂等键，防止重复 ESL 事件造成状态重复流转。
5. `call_command`：电话控制命令受理记录，保存 answer、hangup、hold、unhold、mute、transfer、record 等命令、目标、状态、ESL correlationId 和失败原因。

### 5.2 IVR 表

1. `ivr_flow`：流程主表，保存流程编码、名称、启用状态和当前发布版本。
2. `ivr_flow_version`：流程版本，保存草稿、已发布、已下线状态，支持复制、发布和回滚。
3. `ivr_node`：流程节点，节点类型包括开始、播放、收号、菜单、条件、日程、接口调用、转坐席、转技能组、语音信箱、满意度和挂机。
4. `ivr_edge`：流程连线，保存来源节点、目标节点、条件表达式和标签。
5. `ivr_variable`：流程变量定义和默认值。
6. `ivr_execution`：一次通话的 IVR 执行实例，保存当前节点、状态、进入原因和退出原因。
7. `ivr_node_execution`：节点执行历史，保存输入、输出、变量快照、耗时和异常。
8. `ivr_execution_trace`：面向前端轨迹回放的时间线事件。
9. `ivr_voicemail_box`：语音信箱配置。
10. `ivr_voicemail_message`：留言记录。

### 5.3 ACD 表

1. `acd_skill_group`：技能组、服务时间、默认队列、最大等待和默认策略。
2. `acd_skill_group_agent`：坐席和技能组关系，包含技能等级、权重和优先级。
3. `acd_queue`：队列配置，包含容量、等待提示、SLA 和溢出目标。
4. `acd_agent_status`：坐席实时状态，保存 online、available、reserved、busy、after_call、break、offline 等状态。
5. `acd_queue_session`：呼叫排队实例，保存进入时间、位置、分配坐席、等待秒数和溢出原因。
6. `acd_dispatch_rule`：分配策略配置。
7. `acd_overflow_rule`：超时、队满、无坐席等溢出规则。
8. `acd_sla_rule`：应答 SLA 规则。
9. `acd_queue_metric_snapshot`：队列指标快照，用于监控和报表。

### 5.4 坐席工作台表

1. `agent_phone_bar_session`：坐席电话条当前状态，绑定 agentId、extensionNo、currentCallId 和 controlState。
2. `agent_call_command`：坐席操作记录，保存 answer、hold、transfer、hangup 等命令状态。
3. `agent_screen_pop`：弹屏记录，保存 caller、callee、客户匹配结果、IVR 轨迹摘要和队列信息。
4. `agent_after_call_work`：后处理记录，保存处理结果、摘要、标签、下次跟进时间和关联客户。

### 5.5 记录与客户关联表

1. `call_record`：通话记录投影，面向查询和报表。
2. `recording_record`：录音记录，保存存储 key、时长、权限级别、可播放状态和可下载状态，不直接暴露真实存储 URI。
3. `service_record`：服务记录，保存服务摘要、处理结果、坐席、客户和通话关联。
4. `inbound_customer_association`：呼入与客户的关联关系。
5. `customer_profile`：坐席工作台所需的基础客户资料。
6. `customer_phone`：客户号码索引。

### 5.6 权限、审计和报表表

1. 权限菜单和按钮继续纳入系统权限体系，新增呼入域权限编码。
2. 操作审计表记录 IVR 发布、技能组配置、坐席状态切换、录音访问、报表重算和权限越权失败。
3. `report_inbound_daily`：呼入总览日报。
4. `report_queue_daily`：队列维度日报。
5. `report_agent_inbound_daily`：坐席呼入日报。

### 5.7 种子数据

local 和 test profile 提供确定性 IVR 流程、技能组、队列、坐席、客户和分机种子数据。种子链路支持工作时间 DTMF 1 转 support 技能组，非工作时间进语音信箱，坐席 9 分机 8001 可接入。

## 6. 关键状态机

### 6.1 通话状态机

```text
NEW → RINGING → IVR → QUEUEING → ASSIGNED → BRIDGED → AFTER_CALL → ENDED
```

异常可进入 `FAILED`，非工作时间可走 `VOICEMAIL → ENDED`，保持状态为 `BRIDGED ↔ HOLD`。

### 6.2 IVR 执行状态机

```text
CREATED → RUNNING → WAITING_INPUT → TRANSFERRED / VOICEMAIL / HANGUP / COMPLETED / FAILED
```

### 6.3 队列会话状态机

```text
WAITING → ASSIGNED → BRIDGED → ENDED
```

超时或队满可进入 `OVERFLOWED`。

### 6.4 坐席状态机

```text
OFFLINE → ONLINE → AVAILABLE → RESERVED → BUSY → AFTER_CALL → AVAILABLE
```

坐席可从 `AVAILABLE` 进入 `BREAK`，也可从 `BREAK` 回到 `AVAILABLE`。

### 6.5 电话条命令状态机

```text
ACCEPTED → SENT_TO_ESL → CONFIRMED / FAILED / TIMEOUT
```

最终状态由 ESL 事件确认。

### 6.6 录音记录状态机

```text
STARTED → AVAILABLE / FAILED / ARCHIVED
```

录音播放和下载需要权限审计。

## 7. REST 接口设计

### 7.1 呼叫核心接口

`/api/call/**` 提供通话会话、通话腿、通话事件、通话记录和测试模拟入口。

1. `GET /api/call/sessions`：查询通话会话列表，支持状态、号码、坐席、时间范围筛选。
2. `GET /api/call/sessions/{callId}`：查询通话详情，包含通话腿、当前状态、IVR 摘要、队列摘要和坐席摘要。
3. `GET /api/call/sessions/{callId}/events`：查询标准化事件时间线。
4. `GET /api/call/sessions/{callId}/legs`：查询通话腿和桥接关系。
5. `POST /api/call/test-fixtures/inbound-call`：仅 local/test profile 可用，用于创建确定性呼入会话并触发 Mock ESL 流程。
6. `POST /api/call/test-fixtures/esl-event`：仅 local/test profile 可用，用于注入 DTMF、ANSWER、BRIDGE、HANGUP 等模拟事件。

### 7.2 IVR 接口

`/api/ivr/**` 提供流程管理、设计器、发布、测试运行和轨迹回放。

1. `GET /api/ivr/flows`
2. `POST /api/ivr/flows`
3. `PUT /api/ivr/flows/{id}`
4. `DELETE /api/ivr/flows/{id}`
5. `POST /api/ivr/flows/{id}/versions`
6. `PUT /api/ivr/versions/{versionId}/graph`
7. `POST /api/ivr/versions/{versionId}/validate`
8. `POST /api/ivr/versions/{versionId}/publish`
9. `POST /api/ivr/versions/{versionId}/rollback`
10. `POST /api/ivr/test-runs`
11. `GET /api/ivr/executions/{executionId}/trace`
12. `GET /api/ivr/voicemail/messages`
13. `PUT /api/ivr/voicemail/messages/{id}/status`

发布前校验必须检查开始节点、孤立节点、菜单分支、技能组配置、语音资源、死循环风险和兜底分支。

### 7.3 ACD 接口

`/api/acd/**` 提供技能组、队列、坐席状态、策略和监控。

1. `GET /api/acd/skill-groups`
2. `POST /api/acd/skill-groups`
3. `PUT /api/acd/skill-groups/{id}`
4. `DELETE /api/acd/skill-groups/{id}`
5. `PUT /api/acd/skill-groups/{id}/agents`
6. `GET /api/acd/queues`
7. `POST /api/acd/queues`
8. `PUT /api/acd/queues/{id}`
9. `PUT /api/acd/queues/{id}/rules`
10. `GET /api/acd/agents/status`
11. `PUT /api/acd/agents/{agentId}/state`
12. `GET /api/acd/queues/{queueId}/monitor`
13. `POST /api/acd/dispatch/retry`

### 7.4 坐席工作台接口

`/api/agent/**` 提供坐席工作台、电话条、弹屏和后处理接口。

1. `POST /api/agent/phone-bar/online`
2. `POST /api/agent/phone-bar/offline`
3. `POST /api/agent/phone-bar/break/start`
4. `POST /api/agent/phone-bar/break/end`
5. `GET /api/agent/phone-bar/session`
6. `POST /api/agent/phone-bar/answer`
7. `POST /api/agent/phone-bar/hangup`
8. `POST /api/agent/phone-bar/hold`
9. `POST /api/agent/phone-bar/unhold`
10. `POST /api/agent/phone-bar/mute`
11. `POST /api/agent/phone-bar/unmute`
12. `POST /api/agent/phone-bar/transfer/blind`
13. `POST /api/agent/phone-bar/transfer/consult/start`
14. `POST /api/agent/phone-bar/transfer/consult/complete`
15. `POST /api/agent/phone-bar/transfer/consult/cancel`
16. `GET /api/agent/screen-pop/current`
17. `POST /api/agent/after-call-work`
18. `GET /api/agent/records/calls`
19. `GET /api/agent/records/recordings`
20. `GET /api/agent/records/services`

### 7.5 客户、记录和报表接口

`/api/customer/**`：

1. `GET /api/customer/lookup/by-phone`
2. `POST /api/customer/associations`
3. `GET /api/customer/{id}/inbound-history`

`/api/records/**`：

1. `GET /api/records/calls`
2. `GET /api/records/recordings`
3. `POST /api/records/recordings/{id}/play-token`
4. `POST /api/records/recordings/{id}/download-token`
5. `GET /api/records/services`

`/api/report/inbound/**`：

1. `GET /api/report/inbound/daily`
2. `GET /api/report/inbound/queues/daily`
3. `GET /api/report/inbound/agents/daily`
4. `POST /api/report/inbound/recalculate`

## 8. WebSocket 设计

WebSocket 分为四条通道。

1. `/ws/agent`：坐席工作台专用，推送 RINGING、SCREEN_POP、ASSIGNED、BRIDGED、HELD、UNHELD、TRANSFER_STARTED、TRANSFER_COMPLETED、HANGUP、AFTER_CALL_REQUIRED、COMMAND_RESULT。
2. `/ws/monitor`：班长和管理员队列监控，推送 QUEUE_METRIC、AGENT_STATUS、SLA_WARNING、OVERFLOW、ABANDONED。
3. `/ws/notice`：系统通知，推送配置变更、异常提醒、录音失败、ESL 断连等。
4. `/ws/task`：测试运行和异步重算进度，推送 IVR_TEST_PROGRESS、REPORT_RECALCULATE_PROGRESS。

消息结构统一为：

```json
{
  "eventId": "evt-xxx",
  "eventType": "QUEUE_METRIC",
  "traceId": "trace-xxx",
  "targetUserId": 9,
  "targetRole": "SUPERVISOR",
  "bizType": "ACD_QUEUE",
  "bizId": "20",
  "occurredAt": "2026-06-09T10:00:00",
  "payload": {}
}
```

推送前必须做鉴权和数据范围过滤。坐席只能收到自己的 `/ws/agent` 消息；班长只能看授权技能组；管理员按权限查看全局；录音、客户敏感字段不通过 WebSocket 推送原始值。

## 9. 前端页面设计

前端直接替换旧呼入域入口，形成四组正式页面。

### 9.1 IVR 流程中心

1. 流程列表：新建、复制、发布、回滚、启停。
2. IVR 设计器：LogicFlow 画布、节点面板、配置抽屉、连线校验、资源引用提示。
3. 测试运行页：输入模拟号码、时间场景、DTMF 序列，查看节点轨迹。
4. 执行轨迹页：按真实 callId 回放 IVR 执行过程。
5. 语音信箱页：留言列表、播放权限、处理状态。

### 9.2 ACD 配置与监控

1. 技能组管理：坐席绑定、技能等级、权重。
2. 队列配置：容量、服务时间、等待提示、SLA、溢出。
3. 分配策略配置：选择策略和参数。
4. 坐席状态页：在线、空闲、忙碌、后处理、小休。
5. 队列监控页：等待数、可用坐席、最长等待、SLA 预警、溢出。

### 9.3 坐席工作台

1. 顶部电话条：上线、离线、小休、接听、保持、转接、挂机。
2. 左侧弹屏：来电号码、被叫号码、IVR 路径、队列、等待时长。
3. 中间客户卡：客户基础信息、号码、历史通话、服务记录。
4. 右侧辅助区：呼入摘要、备注、标签和后处理辅助。
5. 底部事件流：通话状态、命令结果、转接过程、录音状态。
6. 后处理面板：处理结果、服务摘要、客户关联、下次跟进时间。

### 9.4 记录、报表与审计

1. 通话记录页。
2. 录音记录页，播放和下载受权限控制。
3. 服务记录页。
4. 呼入日报、队列日报、坐席日报。
5. 录音访问审计和关键配置审计。

### 9.5 前端状态管理

1. `agentWorkspaceStore` 负责电话条、弹屏、当前客户、当前通话、后处理和事件流。
2. `ivrDesignerStore` 负责流程图、节点配置、校验错误、版本状态。
3. `queueMonitorStore` 负责队列指标、坐席状态、SLA 告警。
4. API 请求和 WebSocket 消息都归并到 store，页面组件只负责展示和触发动作。

## 10. 错误处理

错误分为通信类、业务配置类、并发幂等类、权限数据类四种。

### 10.1 通信类错误

包括 ESL 断连、FreeSWITCH 命令超时、桥接失败、录音失败、DTMF 丢失。系统记录通信事件、更新命令状态、推送告警，不直接把通话会话改成失败，除非收到明确挂断或失败事件。

### 10.2 业务配置类错误

包括 IVR 流程无开始节点、菜单无默认分支、转技能组缺少 `skillGroupId`、队列无可用策略、语音资源不存在。发布前必须强校验；运行时仍遇到时进入兜底节点，例如转语音信箱或播放异常提示后挂机。

### 10.3 并发与幂等类错误

包括重复 ESL 事件、重复电话条命令、坐席被同时分配、同一通电话重复入队。系统通过幂等键、状态机校验、Redis 锁和数据库唯一约束组合处理。

### 10.4 权限和数据范围类错误

包括坐席查看非本人弹屏、班长查看未授权技能组、无权限播放录音、无权限查看客户明文。系统返回明确权限错误，并写审计日志。

## 11. 权限、脱敏和审计

### 11.1 权限编码

IVR 权限：

1. `ivr:flow:view`
2. `ivr:flow:create`
3. `ivr:flow:update`
4. `ivr:flow:publish`
5. `ivr:flow:rollback`
6. `ivr:trace:view`
7. `ivr:voicemail:view`
8. `ivr:voicemail:handle`

ACD 权限：

1. `acd:skill:view`
2. `acd:skill:update`
3. `acd:queue:view`
4. `acd:queue:update`
5. `acd:agent-state:view`
6. `acd:agent-state:update`
7. `acd:monitor:view`

坐席工作台权限：

1. `agent:workspace:view`
2. `agent:phone:online`
3. `agent:phone:control`
4. `agent:after-call:submit`
5. `agent:records:view`

记录和录音权限：

1. `record:call:view`
2. `record:recording:view`
3. `record:recording:play`
4. `record:recording:download`
5. `record:service:view`

报表权限：

1. `report:inbound:view`
2. `report:inbound:recalculate`

### 11.2 数据范围

1. 坐席只能看自己的当前通话、弹屏、电话条状态、服务记录和权限范围内的录音。
2. 班长能看被授权技能组下的队列、坐席、通话和报表。
3. 管理员能看全局配置和报表，但录音下载仍需要单独权限。
4. 审计员能看访问记录和操作日志，但不默认拥有录音播放或客户明文权限。

### 11.3 脱敏和录音访问

1. 手机号默认显示为 `138****0001`，坐席当前服务中可看完整号码，历史数据按权限控制。
2. 客户姓名默认部分脱敏，坐席当前通话可看完整。
3. 录音列表只显示元数据，不返回真实存储 URI。
4. 播放录音由后端生成短期播放凭证，并写 `RECORDING_PLAY` 审计。
5. 下载录音必须有下载权限，由后端生成短期下载凭证，并写 `RECORDING_DOWNLOAD` 审计。
6. WebSocket 不推送客户明文字段和录音地址，只推送必要摘要和业务 ID。

### 11.4 必审动作

以下动作必须写审计：

1. IVR 流程创建、修改、发布、回滚、删除。
2. 技能组、队列、分配策略、SLA、溢出规则修改。
3. 坐席手工状态修改。
4. 电话条关键命令：接听、挂机、保持、恢复、转接。
5. 后处理提交和修改。
6. 客户关联和取消关联。
7. 录音播放和下载。
8. 报表重算和导出。
9. 权限越权访问失败。

审计记录至少包含操作人、角色、目标对象、动作、结果、失败原因、traceId、callId、agentId、skillGroupId、请求 IP 和发生时间。

## 12. 测试与验收

### 12.1 后端单元测试

1. `CallStateMachineTest`：通话状态转换合法和非法路径。
2. `CallEventIngestServiceTest`：重复事件、乱序事件、终态事件保护。
3. `IvrNodeHandlerTest`：菜单、日程、条件、播放、转技能组、语音信箱、挂机。
4. `IvrRuntimeEngineTest`：DTMF 路由、非工作时间语音信箱、工作时间转 ACD、轨迹记录。
5. `AgentStateMachineTest`：坐席上线、空闲、预占、忙碌、后处理、小休、离线。
6. `QueueDispatchServiceTest`：空闲优先、轮询、最少接听、技能优先、权重优先、最近未接策略。
7. `PhoneBarApplicationServiceTest`：命令受理、重复命令、状态不允许操作、ESL 失败。
8. `AfterCallWorkServiceTest`：后处理提交、客户关联、坐席释放。
9. `RecordingAccessPolicyTest`：录音播放和下载权限。

### 12.2 后端集成测试

1. `Phase3InboundFlowTest`：Mock ESL 下完成呼入、IVR、DTMF、ACD、分配。
2. `Phase4AgentInboundFlowTest`：坐席上线、弹屏、接听、保持、转接、挂机、后处理、记录。
3. `Phase4SecurityAccessTest`：坐席越权、班长数据范围、录音无权限访问。
4. `MigrationSmokeTest`：新表和种子数据可用。
5. `WebSocketFanoutTest`：只向授权坐席和班长推送消息。

### 12.3 前端测试

1. store 测试：`agentWorkspaceStore`、`ivrDesignerStore`、`queueMonitorStore`。
2. 组件测试：电话条、弹屏、IVR 节点配置、队列监控、后处理面板。
3. API adapter 测试：接口参数和响应映射。
4. Playwright E2E：IVR 流程设计、发布、测试运行；ACD 队列配置和监控；坐席完整呼入处理；录音权限和报表查看。

### 12.4 真实 FreeSWITCH / ESL 联调验收

1. 分机 8001 注册成功。
2. 呼入号码进入 FreeSWITCH。
3. Java 后端收到 CHANNEL_CREATE、CHANNEL_ANSWER、DTMF、CHANNEL_BRIDGE、CHANNEL_HANGUP。
4. DTMF 1 转 support 技能组。
5. ACD 分配在线空闲坐席。
6. 坐席工作台收到弹屏。
7. 坐席接听后状态变为通话中。
8. 保持、恢复、盲转、咨询转、挂机命令可发送并收到事件确认。
9. 录音开始和结束事件可记录。
10. 后处理提交后生成通话记录、录音记录和服务记录。
11. 队列监控和坐席日报能看到指标。
12. 无权限用户不能播放或下载录音。

### 12.5 性能与稳定性验收

1. Mock ESL 事件每秒 500 条摄入不丢事件。
2. 单队列 100 个等待呼叫查询和监控推送不卡顿。
3. 50 个坐席同时在线时 WebSocket 推送不串用户。
4. 电话条命令重复点击不会产生重复控制命令。
5. 事件乱序不会把已结束通话恢复成进行中。
6. 报表重算不阻塞电话条和呼入主链路。

## 13. 生产联调配置

需要补齐以下配置项：

1. FreeSWITCH ESL host、port、password、timeout。
2. 分机域、默认上下文、录音目录、播放文件目录。
3. Mock ESL 开关，仅 local/test profile 可启用。
4. WebSocket 心跳、断线重连、消息缓存策略。
5. 录音存储配置和短期访问凭证有效期。
6. 队列 SLA 默认阈值、溢出默认语音信箱。
7. 报表聚合调度时间。
8. 敏感字段脱敏规则。

## 14. 验收出口标准

方案三完成的出口标准如下：

1. 后端呼入域相关模块测试通过。
2. 前端构建和相关测试通过。
3. Playwright 呼入 E2E 通过。
4. Mock ESL 自动化链路通过。
5. 真实 FreeSWITCH / ESL 联调 checklist 通过。
6. 权限越权测试通过。
7. 录音播放和下载审计可查。
8. IVR、ACD、坐席工作台、记录、报表页面可从正式菜单进入。
9. 旧呼叫、IVR、ACD、坐席入口已替换，不再作为主入口。
10. 设计模式使用清单和阶段验收报告可追溯。

## 15. 实施拆分建议

虽然目标是完整功能实现，但实施必须按可验证阶段推进：

1. 数据库新表和模块依赖重建。
2. ESL 事件和命令边界。
3. 呼叫核心状态机和事件摄入。
4. IVR 流程、运行时、发布和轨迹。
5. ACD 队列、状态、策略和监控。
6. 坐席电话条、弹屏、通话控制和后处理。
7. 记录、录音、客户关联和呼入报表。
8. 权限、脱敏、审计和安全越权测试。
9. 前端四组页面和 WebSocket 状态管理。
10. Mock ESL 自动化、真实 FS/ESL 联调和最终验收。

每个阶段必须有可运行、可测试、可回归的验收结果。完整功能实现不等于一次性无序修改所有文件。
