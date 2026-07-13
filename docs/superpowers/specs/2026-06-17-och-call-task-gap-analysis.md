# och-call-task 模块功能缺口分析报告

**日期**: 2026-06-17  
**分支**: refactoring  
**分析维度**: 旧架构（92个已删除文件） vs 新架构（18个新文件）

---

## 1. 新旧架构概览

### 新架构（18个文件）

| 层 | 文件 | 职责 |
|---|---|---|
| **domain** | `OutboundTask.java` | 外呼任务实体（替代旧 CallTask） |
| **domain** | `OutboundCustomer.java` | 外呼客户实体（替代旧 CallTaskAssignment） |
| **domain** | `OutboundDialAttempt.java` | 拨号尝试记录（新增） |
| **domain** | `OutboundTaskMode.java` | 枚举: PREVIEW/PREDICTIVE/ROBOT |
| **domain** | `OutboundTaskState.java` | 任务状态枚举 |
| **domain** | `OutboundCustomerState.java` | 客户状态枚举 |
| **domain** | `DialResult.java` | 拨号结果枚举 |
| **mapper** | `OutboundTaskMapper.java` | 任务表 Mapper（MyBatis-Plus，无XML） |
| **mapper** | `OutboundCustomerMapper.java` | 客户表 Mapper + findReadyCustomers/countPending |
| **mapper** | `OutboundDialAttemptMapper.java` | 拨号尝试表 Mapper + countByDedupKey |
| **application** | `OutboundTaskService.java` | 任务CRUD + 生命周期管理 + 进度/指标查询 |
| **application** | `OutboundTaskStateMachine.java` | 任务状态流转校验 |
| **application** | `OutboundCustomerStateMachine.java` | 客户状态流转校验 |
| **application** | `OutboundFilterService.java` | 合规过滤（DNC + 时间窗口） |
| **application** | `CustomerImportService.java` | CSV客户导入 + 合规检查 |
| **application** | `PredictiveDialerService.java` | 预测拨号速率计算 |
| **application** | `PreviewDialingService.java` | 预览式外呼（领取/释放/拨号） |
| **api** | `OutboundTaskController.java` | REST 控制器 |
| **db** | `V10__outbound_task_schema.sql` | DDL: outbound_task/outbound_customer/outbound_dial_attempt |

### 旧架构（92个已删除文件，按功能域分类）

| 功能域 | 数量 | 示例 |
|---|---|---|
| **config/** | 4 | PredictiveDialerConfig, QuartzJobConfig 等 |
| **domain/entity/** | 8 | CallTask, CallTaskAssignment, CustomerCrowd, CustomerSeas 等 |
| **domain/query/** | 14 | CallTaskQuery, CustomerCrowdQuery 等各种查询对象 |
| **domain/vo/** | 13 | CallTaskVo, PredictiveDialingResult, CallQueueItem 等 |
| **domain/ (事件)** | 4 | CallTaskContactImportEvent, CustomerCrowdEvent 等 |
| **handler/** | 4 | PredictiveDialerHandler, PreviewOutboundHandler, RobotOutboundHandler, CallTaskHandler(接口) |
| **job/** | 2 | PredictiveDialerJob, CustomerCrowdJob |
| **listener/** | 2 | CallTaskContactImportEventListener, CustomerCrowdEventListener |
| **mapper/** | 8 | CallTaskMapper, CustomerCrowdMapper, CustomerFieldMapper 等 |
| **service/** | 11 | 各种服务接口 |
| **service/impl/** | 13 | 各种服务实现 |
| **mapper XML** | 8 | MyBatis XML 映射文件 |

---

## 2. 逐功能域对比分析

### 2.1 配置类 (config/)

| 旧文件 | 功能 | 新架构覆盖 | 状态 |
|---|---|---|---|
| `PredictiveDialerConfig.java` | Spring Bean 装配 PredictiveDialerHandler | 不再需要（新 PredictiveDialerService 直接用 @RequiredArgsConstructor 注入） | **已由DI框架替代** |
| `PredictiveAlgorithmConfig.java` | 预测算法配置 | 无对等 | **缺口** |
| `PredictiveAlgorithmSelectorConfig.java` | 算法选择器配置 | 无对等 | **缺口** |
| `QuartzJobConfig.java` | Quartz 定时任务配置 | 无对等（新架构无定时任务） | **缺口** |

**结论**: 配置类缺口主要来自预测算法配置的缺失和 Quartz 调度框架的移除。如果不再使用 Quartz，这些配置不需要迁移。

### 2.2 领域实体 (domain/entity/)

| 旧实体 | 职责 | 新架构对等 | 状态 |
|---|---|---|---|
| `CallTask` | 外呼任务 | `OutboundTask` | **已覆盖** |
| `CallTaskAssignment` | 任务-坐席分配 | `OutboundCustomer`（内聚到客户记录中） | **已覆盖** |
| `CustomerCrowd` | 客户人群定义 | 无对等 | **缺口** |
| `CustomerCrowdRel` | 客户-人群关联 | 无对等 | **缺口** |
| `CustomerSeas` | 客户公海 | 已移至 `och-customer` 模块（独立管理） | **模块职责迁移** |
| `CustomerField` | 客户自定义字段 | 无对等 | **缺口** |
| `CustomerTemplate` | 客户模板 | 无对等 | **缺口** |
| `CustomerTemplateFieldRel` | 模板-字段关联 | 无对等 | **缺口** |

**结论**: 核心实体（任务、客户、分配）已覆盖。**客户人群（CustomerCrowd）体系完全缺失，客户字段/模板体系缺失**。这两个功能域是新架构中的明确空白。

### 2.3 查询对象 (domain/query/)

旧有14个查询对象用于封装查询条件。新架构直接使用 MyBatis-Plus 的 `LambdaQueryWrapper` 在 Service 方法内构建查询，因此不需要独立的 Query 对象。**这是架构风格变化，而非功能缺失。**

### 2.4 视图对象 (domain/vo/)

| 旧VO | 职责 | 新架构对等 | 状态 |
|---|---|---|---|
| `CallTaskVo` | 任务详情VO | `OutboundTask` 实体直接返回 | **已由实体替代** |
| `CallTaskContactVo` | 客户接触VO | `OutboundCustomer` 实体直接返回 | **已由实体替代** |
| `PredictiveDialingResult` | 预测结果 | 新 `PredictiveDialerService` 仅返回 double dialRate | **简化版**（缺失推荐拨号数、优先级、风险等级等） |
| `PredictiveDialingMetrics` | 预测指标历史 | 无对等 | **缺口** |
| `PredictiveDialingDashboard` | 预测仪表盘 | 无对等 | **缺口** |
| `CallQueueItem` | 队列项 | 无对等 | **缺口**（队列功能缺失） |
| `QueuePriority` | 队列优先级 | 无对等 | **缺口**（队列功能缺失） |
| `CustomerCrowdVo` | 人群VO | 无对等 | **缺口** |
| `CustomerSeasVo` | 公海客户VO | 已移至 och-customer 模块 | **模块职责迁移** |
| `CustomerFieldVo` | 自定义字段VO | 无对等 | **缺口** |
| `CustomerTemplateVo` / `CustomerTemplateFieldRelVo` | 模板VO | 无对等 | **缺口** |

**结论**: 大部分VO缺失对应底层功能的缺失。

### 2.5 处理器 (handler/)

| 旧文件 | 功能概述 | 新架构对等 | 状态 |
|---|---|---|---|
| `CallTaskHandler` (接口) | 统一的任务处理器接口 `execute(taskId)` | **无对等** — 新架构使用独立 Service 而非策略模式 | **架构风格变化** |
| `PredictiveDialerHandler` | 预测式外呼全流程：坐席发现 -> 预测算法 -> 队列管理 -> 坐席分配 -> 拨号执行 | `PredictiveDialerService` 仅做了其中的**速率计算**部分 | **严重缺口** |
| `PreviewOutboundHandler` | 预览式外呼：客户分配（轮询/空闲优先） | `PreviewDialingService` 仅做了**已领取客户的拨号**部分 | **缺口**（缺失批量分配逻辑） |
| `RobotOutboundHandler` | 机器人外呼 | `OutboundTaskMode.ROBOT` 枚举已定义，但无对应 Service | **完整缺口** |

**结论 — 这是最关键的缺口点**:
- **PredictiveDialerHandler**: 旧版是一个完整的680行处理器，涵盖坐席可用性检测、预测算法调用、队列初始化、坐席分配、拨号执行、统计记录、模型更新。新 `PredictiveDialerService`（134行）只做了**速率计算**，缺失：
  - 坐席分配逻辑
  - 队列管理集成
  - 实际拨号执行
  - 模型更新闭环
  - 定时调度触发
- **RobotOutboundHandler**: 旧版是空壳（execute为空），但框架存在。新版定义了 ROBOT 枚举但没有任何 Service 实现。
- **预览式分配**: 旧 `PreviewOutboundHandler` 包含批量分配逻辑（轮询/空闲优先），新 `PreviewDialingService` 仅支持座席主动领取单个客户。

### 2.6 定时任务 (job/)

| 旧文件 | 功能 | 新架构对等 | 状态 |
|---|---|---|---|
| `PredictiveDialerJob` | Quartz 定时触发预测外呼 | **无对等** | **缺口** |
| `CustomerCrowdJob` | Quartz 定时触发人群计算 | **无对等** | **缺口** |

**结论**: 新架构完全移除了 Quartz 调度框架。如果没有替代的调度机制（如 Spring `@Scheduled` 或外部调度器），预测式外呼和人群计算将无法自动触发。

### 2.7 监听器 (listener/)

| 旧文件 | 功能 | 新架构对等 | 状态 |
|---|---|---|---|
| `CallTaskContactImportEventListener` | 监听客户导入事件，写入任务分配表 | `CustomerImportService` 部分覆盖（导入到 CustomerPhone 表，而非任务分配表） | **路径变化** |
| `CustomerCrowdEventListener` | 监听客户人群事件（新增/删除/计算） | **无对等** | **缺口** |

**结论**: 新架构的 `CustomerImportService` 将客户导入到通用的 `customer_profile`/`customer_phone` 表，而非任务特定的 `call_task_assignment` 表。这改变了导入后的数据流——**任务如何关联到导入的客户现在没有明确的实现**。人群事件机制完全缺失。

### 2.8 Mapper (mapper/)

| 旧Mapper | 新架构 | 状态 |
|---|---|---|
| `CallTaskMapper` -> XML | `OutboundTaskMapper` (MyBatis-Plus 注解，无XML) | **已覆盖** |
| `CallTaskAssignmentMapper` -> XML | `OutboundCustomerMapper` (含自定义SQL) | **已覆盖** |
| `CustomerCrowdMapper` | 无 | **缺口** |
| `CustomerCrowdRelMapper` | 无 | **缺口** |
| `CustomerFieldMapper` | 无 | **缺口** |
| `CustomerSeasMapper` | 移至 och-customer 模块 | **模块职责迁移** |
| `CustomerTemplateMapper` | 无 | **缺口** |
| `CustomerTemplateFieldRelMapper` | 无 | **缺口** |
| `OutboundDialAttemptMapper` | **新增**（旧架构无独立拨号记录） | **新增能力** |

### 2.9 服务接口和实现 (service/ + service/impl/)

关键缺口汇总：

| 旧服务 | 新架构状态 |
|---|---|
| `ICallTaskService` / impl | **已覆盖** — `OutboundTaskService` |
| `ICallTaskAssignmentService` / impl | **已覆盖** — 合并到 `OutboundCustomerService` 逻辑中 |
| `IPredictiveDialingService` / impl | **部分覆盖** — `PredictiveDialerService` 仅计算速率 |
| `IPredictiveAlgorithmService` / impl | **缺口** — 预测算法（最优拨号数、接通概率、模型更新）完全缺失 |
| `ICallQueueService` / impl | **缺口** — 呼叫队列管理完全缺失 |
| `ICustomerCrowdService` / impl | **缺口** |
| `ICustomerCrowdRelService` / impl | **缺口** |
| `ICustomerFieldService` / impl | **缺口** |
| `ICustomerSeasService` / impl | **模块职责迁移** |
| `ICustomerTemplateService` / impl | **缺口** |
| `ICustomerTemplateFieldRelService` / impl | **缺口** |

---

## 3. 专题分析

### 3.1 预测式外呼 (Predictive Dialer)

**旧架构流程**（`PredictiveDialerJob` -> `PredictiveDialerHandler`）:
```
定时触发 -> 获取任务 -> 获取可用坐席 -> 预测算法计算最优拨号数 
-> 初始化呼叫队列 -> 出队客户 -> 分配坐席 -> 执行拨号 -> 统计记录 -> 模型更新
```

**新架构覆盖度**（`PredictiveDialerService`）:
```
手动调用 -> 计算空闲坐席数 -> 计算近期接通率 -> 返回拨号速率（double值）
```

**缺失功能**:
1. 预测算法引擎（`IPredictiveAlgorithmService`）——最优拨号数、客户接通概率、模型更新
2. 呼叫队列管理（`ICallQueueService`）——入队、出队、优先级、重排序
3. 坐席分配逻辑（轮询、空闲优先、过载保护）
4. 实际拨号执行触发
5. 定时调度（无 Quartz job 替代方案）
6. 统计仪表盘（`PredictiveDialingResult`/`Metrics`/`Dashboard`）

**缺口等级**: 严重。新架构仅实现了预测式外呼的"计算"环节，而非完整的"执行"闭环。

### 3.2 预览式外呼 (Preview Outbound)

**旧架构**: `PreviewOutboundHandler` 批量分配客户到坐席（支持轮询和空闲优先策略）

**新架构**: `PreviewDialingService` 支持座席主动领取(claim)、释放(release)、拨号(startDial)单个客户

**缺失功能**:
1. 批量自动分配能力（旧版支持定时批量分配）
2. 多种分配策略（轮询、空闲优先）

**缺口等级**: 中等。新架构更偏向"拉取模式"（座席主动领取），旧架构支持"推送模式"（系统主动分配）。两种模式互补，如果产品需求仅需拉取模式则无缺口。

### 3.3 机器人外呼 (Robot Outbound)

**旧架构**: `RobotOutboundHandler` 是空壳实现（execute方法为空）

**新架构**: `OutboundTaskMode.ROBOT` 枚举已定义，数据库DDL支持 ROBOT 模式

**状态**: 虽然旧版也是空壳，但新版没有任何对应的 Service 类。`OutboundController` 也没有机器人外呼相关的API端点。机器人模式目前在领域模型层面已定义，但应用层完全空白。

**缺口等级**: 中等。枚举和DDL已预留，无实现。

### 3.4 客户人群 (Customer Crowd)

**旧架构**: 完整的人群管理体系：
- `CustomerCrowd` / `CustomerCrowdRel` 实体
- `CustomerCrowdJob` 定时任务
- `CustomerCrowdEventListener` 监听客户增删改事件
- 条件筛选引擎（14种关系运算符：EQUAL, NOT_EQUAL, MORE_THAN, LESS_THAN, INTERVAL, INCLUDE, NOT_INCLUDE, NULL, NOT_NULL等）
- `CustomerField` / `CustomerTemplate` 自定义字段和模板

**新架构**: `CustomerImportService` 仅提供 CSV 文件导入到通用 `customer_profile`/`customer_phone` 表

**缺失功能**:
1. 人群定义和CRUD管理
2. 动态条件筛选引擎（JSON字段查询）
3. 人群-客户关联管理
4. 定时人群计算任务
5. 客户自定义字段和模板

**缺口等级**: 严重。客户人群是从营销维度组织外呼目标的核心能力，新架构完全没有覆盖。

### 3.5 呼叫队列管理 (Call Queue)

**旧架构**: `ICallQueueService` 定义了完整的优先级队列：
- 入队/出队（含预测接通概率、客户价值评分）
- 队列状态查询（总项数、等待/处理/完成/取消/失败计数、平均等待时间）
- 队列重排序、暂停/恢复、清空
- 优先级级别：URGENT/HIGH/NORMAL/LOW/LOWEST

**新架构**: 无对等实现。`OutboundCustomerState` 有 `SCHEDULED` 状态预留，但没有队列管理逻辑。

**缺口等级**: 高。对于预测式外呼模式，队列管理是实现"提前拨号等待坐席空闲"的关键机制。没有队列，预测式外呼无法在高负载下保持坐席利用率。

---

## 4. 缺口汇总与优先级

### 严重缺口（影响核心业务闭环）

| # | 功能域 | 具体缺失 |
|---|---|---|
| 1 | **预测式外呼执行引擎** | 坐席分配、拨号触发、模型更新闭环 |
| 2 | **呼叫队列管理** | 优先级队列、出入队、暂停/恢复 |
| 3 | **客户人群管理体系** | 人群定义、条件筛选、人群-客户关联、定时计算 |
| 4 | **预测算法引擎** | 最优拨号数计算、客户接通概率预测、模型更新 |
| 5 | **定时任务调度** | 预测外呼和人群计算的自动触发机制 |

### 中等缺口（影响特定功能模式）

| # | 功能域 | 具体缺失 |
|---|---|---|
| 6 | **机器人外呼实现** | 枚举和DDL已预留，无Service实现 |
| 7 | **预览式批量分配** | 仅支持座席主动领取，无批量自动分配 |
| 8 | **客户自定义字段/模板** | CustomerField / CustomerTemplate 体系 |

### 低影响缺口（架构风格变化或已迁移）

| # | 功能域 | 具体缺失 |
|---|---|---|
| 9 | **Query 对象** | 改用 MyBatis-Plus LambdaQueryWrapper，不需要独立Query类 |
| 10 | **大部分VO对象** | 实体直接返回，部分缺失对应底层功能缺失 |
| 11 | **CallTaskHandler 策略接口** | 改用独立Service，非策略模式 |
| 12 | **QuartzJobConfig** | 如不再使用Quartz则不需要 |

---

## 5. 架构变化总结

### 5.1 设计改进
- **数据模型统一**: 三表设计（outbound_task + outbound_customer + outbound_dial_attempt）比旧版的 CallTask + CallTaskAssignment 更清晰，拨号记录独立建模
- **状态机模式**: 显式的 `OutboundTaskStateMachine` / `OutboundCustomerStateMachine` 比旧版隐式状态管理更安全
- **合规过滤独立**: `OutboundFilterService` 将过滤逻辑从业务流中解耦，职责单一
- **MyBatis-Plus 注解驱动**: 移除XML映射文件，减少配置复杂度
- **分层更清晰**: application 层替代旧的 service + handler 双层结构

### 5.2 设计退化/缺失
- **功能范围大幅缩减**: 92个文件缩减到18个，不仅是架构重构，更是功能删减
- **算法能力退化**: 旧版有独立算法服务（策略模式 + 生产实现），新版将预测逻辑硬编码为简单的 `idleAgents / answerRate * 1.2` 公式
- **无调度机制**: 移除 Quartz 后没有替代的定时任务方案
- **无队列管理**: 预测式外呼的核心基础设施缺失

### 5.3 疑似跨模块迁移
- **客户公海 (CustomerSeas)**: 可能已迁移到 `och-customer` 模块
- **坐席管理 (SipAgent)**: 仍依赖 `och-system` 模块
- **合规检查 (ComplianceCheckService)**: 依赖 `och-compliance` 模块
- **队列调度 (QueueDispatchService)**: 依赖 `och-acd` 模块

---

## 6. 建议

1. **预测式外呼完整链路**: 需补充坐席分配、拨号触发和模型更新，`PredictiveDialerService` 仅是计算器，不是执行器
2. **呼叫队列**: 对预测式外呼是必需的，否则预测拨号速率 > 坐席数时会导致呼损
3. **客户人群**: 需确认是否业务已不需要此功能，如需则须重建完整的人群管理体系
4. **定时调度**: 需确定替代方案（Spring @Scheduled、XXL-Job、外部调度器）
5. **机器人外呼**: 需确认是否有计划实现，目前仅在枚举层面预留
