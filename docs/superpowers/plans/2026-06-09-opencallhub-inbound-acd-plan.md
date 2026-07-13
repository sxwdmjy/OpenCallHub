# openCallHub Inbound ACD Dispatch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement inbound ACD skill groups, queues, agent state transitions, dispatch strategies, overflow, SLA metrics, and queue monitor APIs.

**Architecture:** `och-acd` owns queueing and assignment after IVR transfers a call to a skill group. Dispatch uses strategy pattern, agent availability uses an explicit state machine, and monitor metrics are produced as read models plus WebSocket-ready events. `och-acd` never sends FreeSWITCH commands directly.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, Lombok, JUnit 5, AssertJ, Mockito, Spring Boot Test, Redis lock abstraction if present, MySQL-compatible schema from the inbound schema plan.

---

## Scope Check

This plan implements the ACD slice from the full inbound production plan.

Prerequisites:

- Schema plan completed.
- Call core plan completed enough to provide `callId` and call session references.
- IVR plan completed enough to return `skillGroupId` from transfer skill group nodes.

This plan does not implement phone-bar commands, screen pop frontend, recording access, reports, or real FS acceptance.

## File Structure

- Modify/Create: `och-acd/src/main/java/com/och/acd/domain/AgentState.java`
- Modify/Create: `och-acd/src/main/java/com/och/acd/domain/SkillGroup.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdQueue.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdSkillGroupAgent.java`
- Modify/Create: `och-acd/src/main/java/com/och/acd/domain/AcdAgentStatus.java`
- Modify/Create: `och-acd/src/main/java/com/och/acd/domain/AcdQueueSession.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdDispatchRule.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdOverflowRule.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdSlaRule.java`
- Create: `och-acd/src/main/java/com/och/acd/domain/AcdQueueMetricSnapshot.java`
- Modify/Create: `och-acd/src/main/java/com/och/acd/mapper/**`
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/AgentStateMachine.java`
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/QueueDispatchService.java`
- Create: `och-acd/src/main/java/com/och/acd/application/QueueMonitorService.java`
- Modify/Create: strategy classes under `och-acd/src/main/java/com/och/acd/application/strategy/**`
- Modify/Create: `och-acd/src/main/java/com/och/acd/api/AcdConfigController.java`
- Create: `och-acd/src/main/java/com/och/acd/api/QueueMonitorController.java`
- Test: `och-acd/src/test/java/com/och/acd/application/AgentStateMachineTest.java`
- Test: `och-acd/src/test/java/com/och/acd/application/DispatchStrategyTest.java`
- Test: `och-acd/src/test/java/com/och/acd/application/QueueDispatchServiceTest.java`
- Test: `och-acd/src/test/java/com/och/acd/application/QueueMonitorServiceTest.java`

## Task 1: ACD Entities and Mappers

**Files:**
- Domain and mapper files listed above.

- [ ] **Step 1: Implement ACD entities**

Create entities matching schema fields. Required field names:

```text
SkillGroup: id, tenantId, groupCode, groupName, defaultQueueId, dispatchStrategy, enabled, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdQueue: id, tenantId, queueCode, queueName, skillGroupId, capacity, slaSeconds, overflowTargetType, overflowTargetId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdSkillGroupAgent: id, tenantId, skillGroupId, agentId, skillLevel, weight, priority, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdAgentStatus: id, tenantId, agentId, extensionNo, currentState, currentCalls, maxConcurrency, lastStateChangedAt, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdQueueSession: id, tenantId, queueId, skillGroupId, callId, callSessionId, callerNumber, queueState, position, waitSeconds, assignedAgentId, enteredAt, assignedAt, bridgedAt, endedAt, overflowReason, traceId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdDispatchRule: id, tenantId, queueId, strategyCode, strategyConfig, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdOverflowRule: id, tenantId, queueId, overflowType, thresholdSeconds, targetType, targetId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdSlaRule: id, tenantId, queueId, answerSeconds, warnSeconds, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
AcdQueueMetricSnapshot: id, tenantId, queueId, waitingCalls, availableAgents, longestWaitSeconds, averageWaitSeconds, slaBreachedCalls, snapshotAt, traceId, createdAt
```

- [ ] **Step 2: Implement mappers**

Each mapper extends `BaseMapper<T>` and uses `@Mapper`. Add custom methods only when tests require ordered query behavior.

- [ ] **Step 3: Compile ACD module**

Run:

```bash
mvn -pl och-acd test -DskipTests
```

Expected result: `BUILD SUCCESS`.

## Task 2: Agent State Machine

**Files:**
- Test: `och-acd/src/test/java/com/och/acd/application/AgentStateMachineTest.java`
- Modify/Create: `AgentState.java`
- Modify/Create: `AgentStateMachine.java`

- [ ] **Step 1: Write agent state machine test**

Create tests for valid and invalid paths:

```java
assertThat(machine.isValidTransition(AgentState.OFFLINE, AgentState.ONLINE)).isTrue();
assertThat(machine.isValidTransition(AgentState.ONLINE, AgentState.AVAILABLE)).isTrue();
assertThat(machine.isValidTransition(AgentState.AVAILABLE, AgentState.RESERVED)).isTrue();
assertThat(machine.isValidTransition(AgentState.RESERVED, AgentState.BUSY)).isTrue();
assertThat(machine.isValidTransition(AgentState.BUSY, AgentState.AFTER_CALL)).isTrue();
assertThat(machine.isValidTransition(AgentState.AFTER_CALL, AgentState.AVAILABLE)).isTrue();
assertThat(machine.isValidTransition(AgentState.AVAILABLE, AgentState.BREAK)).isTrue();
assertThat(machine.isValidTransition(AgentState.BREAK, AgentState.AVAILABLE)).isTrue();
assertThat(machine.isValidTransition(AgentState.OFFLINE, AgentState.BUSY)).isFalse();
```

- [ ] **Step 2: Implement `AgentState`**

Use states: `OFFLINE`, `ONLINE`, `AVAILABLE`, `RESERVED`, `BUSY`, `AFTER_CALL`, `BREAK`.

Add `isAssignable()` returning true only for `AVAILABLE`.

- [ ] **Step 3: Implement `AgentStateMachine`**

Use explicit transition map. Add Chinese comment explaining `RESERVED` prevents two queue sessions from assigning the same agent before bridge confirmation.

- [ ] **Step 4: Run state machine test**

Run:

```bash
mvn -pl och-acd test -Dtest=AgentStateMachineTest
```

Expected result: `BUILD SUCCESS`.

## Task 3: Dispatch Strategies

**Files:**
- Test: `och-acd/src/test/java/com/och/acd/application/DispatchStrategyTest.java`
- Modify/Create: strategy classes.

- [ ] **Step 1: Write strategy test**

Create tests for:

1. `IDLE_FIRST`: picks agent with oldest `lastStateChangedAt`.
2. `ROUND_ROBIN`: rotates through agents for the same skill group.
3. `LEAST_CALLS`: picks lowest `currentCalls`.
4. `SKILL_PRIORITY`: picks highest `skillLevel`, then oldest idle time.
5. `WEIGHTED`: picks highest `weight`, then priority.
6. `RECENTLY_MISSED`: avoids agent id listed in recently missed set.

- [ ] **Step 2: Implement strategy interface**

```java
public interface DispatchStrategy {
    String strategyCode();
    AcdAgentStatus select(List<AcdAgentStatus> availableAgents, DispatchContext context);
}
```

Create `DispatchContext` with `queueId`, `skillGroupId`, `callId`, and `Set<Long> recentlyMissedAgentIds`.

- [ ] **Step 3: Implement strategy classes**

Create one focused class per strategy. Do not put all strategy logic into `QueueDispatchService`.

- [ ] **Step 4: Implement strategy factory**

`DispatchStrategyFactory.getStrategy(String code)` returns matching strategy. Unknown code throws a business exception with message `未知ACD分配策略: <code>`.

- [ ] **Step 5: Run strategy tests**

Run:

```bash
mvn -pl och-acd test -Dtest=DispatchStrategyTest
```

Expected result: `BUILD SUCCESS`.

## Task 4: Queue Dispatch Service

**Files:**
- Test: `och-acd/src/test/java/com/och/acd/application/QueueDispatchServiceTest.java`
- Modify/Create: `QueueDispatchService.java`

- [ ] **Step 1: Write dispatch service test**

Test these cases:

1. `enqueue` creates one waiting session for a new call.
2. Repeated `enqueue` with same `callId` returns existing active queue session.
3. `tryDispatch` reserves available agent and updates session to `ASSIGNED`.
4. No available agent leaves session in `WAITING`.
5. Queue full returns overflow result with reason `QUEUE_FULL`.
6. Agent release from bridged call moves agent to `AFTER_CALL`.

- [ ] **Step 2: Implement queue dispatch service**

Required public methods:

```java
AcdQueueSession enqueue(Long skillGroupId, String callId, Long callSessionId, String callerNumber, String traceId)
AcdDispatchResult tryDispatch(Long queueId)
void confirmBridge(String callId, Long agentId)
void releaseAgent(String callId, String endReason)
QueueStats getQueueStats(Long queueId)
```

- [ ] **Step 3: Add idempotency and concurrency guard**

Before insert, query active queue session by `callId` and states `WAITING`, `ASSIGNED`, `BRIDGED`. Use database unique key or Redis lock if available. If both exist, use Redis lock plus database uniqueness.

- [ ] **Step 4: Run dispatch service test**

Run:

```bash
mvn -pl och-acd test -Dtest=QueueDispatchServiceTest
```

Expected result: `BUILD SUCCESS`.

## Task 5: Queue Monitor Service and APIs

**Files:**
- Test: `och-acd/src/test/java/com/och/acd/application/QueueMonitorServiceTest.java`
- Create: `QueueMonitorService.java`
- Modify/Create: `AcdConfigController.java`
- Create: `QueueMonitorController.java`

- [ ] **Step 1: Write queue monitor test**

Test that monitor snapshot returns waiting call count, available agent count, longest wait seconds, average wait seconds, SLA breach count, and queue state label.

- [ ] **Step 2: Implement queue monitor service**

`QueueMonitorService.snapshot(queueId)` reads queue sessions and agent statuses, writes `acd_queue_metric_snapshot`, and returns a DTO ready for REST and WebSocket fanout.

- [ ] **Step 3: Implement REST APIs**

Implement:

```text
GET /api/acd/skill-groups
POST /api/acd/skill-groups
PUT /api/acd/skill-groups/{id}
DELETE /api/acd/skill-groups/{id}
PUT /api/acd/skill-groups/{id}/agents
GET /api/acd/queues
POST /api/acd/queues
PUT /api/acd/queues/{id}
PUT /api/acd/queues/{id}/rules
GET /api/acd/agents/status
PUT /api/acd/agents/{agentId}/state
GET /api/acd/queues/{queueId}/monitor
POST /api/acd/dispatch/retry
```

- [ ] **Step 4: Run queue monitor test**

Run:

```bash
mvn -pl och-acd test -Dtest=QueueMonitorServiceTest
```

Expected result: `BUILD SUCCESS`.

## Task 6: Full ACD Verification

**Files:**
- All ACD files.

- [ ] **Step 1: Run all ACD tests**

Run:

```bash
mvn -pl och-acd test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 2: Compile API dependency chain**

Run:

```bash
mvn -pl och-api -am test -DskipTests
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 3: Commit ACD checkpoint**

Commit only after user approval to commit:

```bash
git add och-acd/src/main/java/com/och/acd och-acd/src/test/java/com/och/acd
git commit -m "feat: add inbound acd dispatch"
```

## Self-Review

### Spec coverage

This plan covers skill groups, queues, agent state, dispatch strategies, overflow handling, queue monitor metrics, and ACD APIs.

### Placeholder scan

No placeholder terms are used. Each state, strategy, method signature, endpoint, command, and expected result is specified.

### Type consistency

State names match the production inbound spec: `OFFLINE`, `ONLINE`, `AVAILABLE`, `RESERVED`, `BUSY`, `AFTER_CALL`, `BREAK`. Strategy names match the spec: `IDLE_FIRST`, `ROUND_ROBIN`, `LEAST_CALLS`, `SKILL_PRIORITY`, `WEIGHTED`, `RECENTLY_MISSED`.
