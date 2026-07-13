# openCallHub Inbound IVR Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement full inbound IVR flow management, node handlers, runtime execution, publish/rollback validation, test runs, voicemail handling, and execution trace replay.

**Architecture:** `och-ivr` owns IVR definitions and runtime state. Node execution uses factory + strategy patterns: the runtime engine loads the current published graph, delegates each node to a focused handler, records execution trace, and returns routing outcomes to the API facade. Media commands remain behind `och-esl` command ports, and ACD assignment remains outside `och-ivr`.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, Lombok, Jackson, JUnit 5, AssertJ, Mockito, Spring Boot Test, MySQL-compatible schema from the inbound schema plan.

---

## Scope Check

This plan implements the IVR slice from the full inbound production plan.

Prerequisites:

- `docs/superpowers/plans/2026-06-09-opencallhub-inbound-schema-plan.md`
- `docs/superpowers/plans/2026-06-09-opencallhub-inbound-esl-plan.md`
- `docs/superpowers/plans/2026-06-09-opencallhub-inbound-call-core-plan.md`

This plan does not implement ACD dispatch internals, phone-bar backend, frontend IVR designer, or real FreeSWITCH acceptance.

## File Structure

- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrFlow.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrFlowVersion.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrNode.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrEdge.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrVariable.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrExecution.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrNodeExecution.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrExecutionTrace.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/VoicemailBox.java`
- Create: `och-ivr/src/main/java/com/och/ivr/domain/VoicemailMessage.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrNodeType.java`
- Modify/Create: mapper interfaces under `och-ivr/src/main/java/com/och/ivr/mapper/**`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandler.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandlerResult.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandlerFactory.java`
- Modify/Create: node handlers under `och-ivr/src/main/java/com/och/ivr/application/node/**`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/IvrRuntimeEngine.java`
- Create: `och-ivr/src/main/java/com/och/ivr/application/IvrPublishService.java`
- Create: `och-ivr/src/main/java/com/och/ivr/application/IvrTestRunService.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/IvrExecutionService.java`
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/api/IvrFlowController.java`
- Create: `och-ivr/src/main/java/com/och/ivr/api/IvrExecutionController.java`
- Create: `och-ivr/src/main/java/com/och/ivr/api/VoicemailController.java`
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrNodeHandlerTest.java`
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrRuntimeEngineTest.java`
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrPublishServiceTest.java`
- Test: `och-ivr/src/test/java/com/och/ivr/api/IvrFlowControllerTest.java`

## Task 1: IVR Domain Entities and Mappers

**Files:**
- Domain and mapper files listed in File Structure.

- [ ] **Step 1: Implement domain entities**

Implement MyBatis-Plus entities matching schema table names. Use `@TableName`, `@TableId(type = IdType.AUTO)`, Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` where useful.

Required field naming:

```text
IvrFlow: id, tenantId, flowCode, flowName, publishedVersionId, enabled, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrFlowVersion: id, tenantId, flowId, versionNo, versionState, graphHash, publishedAt, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrNode: id, tenantId, versionId, nodeId, nodeType, nodeName, configJson, posX, posY, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrEdge: id, tenantId, versionId, edgeId, sourceNodeId, targetNodeId, conditionExpr, label, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrVariable: id, tenantId, versionId, variableName, defaultValue, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrExecution: id, tenantId, callId, flowId, versionId, currentNodeId, executionState, enterReason, exitReason, startedAt, endedAt, traceId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
IvrNodeExecution: id, tenantId, executionId, nodeId, nodeType, inputPayload, outputPayload, variableSnapshot, startedAt, endedAt, costMillis, errorMessage, createdAt
IvrExecutionTrace: id, tenantId, executionId, callId, traceType, traceMessage, payload, occurredAt, traceId, createdAt
VoicemailBox: id, tenantId, boxCode, boxName, skillGroupId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
VoicemailMessage: id, tenantId, boxId, callId, callerNumber, recordingId, messageState, traceId, createdBy, createdAt, updatedBy, updatedAt, deleted, status, remark
```

- [ ] **Step 2: Implement mapper interfaces**

Each mapper extends `BaseMapper<T>` and uses `@Mapper`. Add custom mapper methods only when a test requires them. Initial custom methods:

```java
List<IvrEdge> selectByVersionIdAndSourceNodeId(@Param("versionId") Long versionId, @Param("sourceNodeId") String sourceNodeId);
List<IvrNode> selectByVersionId(@Param("versionId") Long versionId);
```

If XML mappers are used in this project, create XML files under `och-ivr/src/main/resources/mapper/` for these custom methods.

- [ ] **Step 3: Compile IVR module**

Run:

```bash
mvn -pl och-ivr test -DskipTests
```

Expected result: `BUILD SUCCESS`.

## Task 2: Node Handler Tests and Implementations

**Files:**
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrNodeHandlerTest.java`
- Modify/Create: node handler files.

- [ ] **Step 1: Write node handler test**

Create `IvrNodeHandlerTest` with these cases:

```java
@Test
void menuNodeRoutesByDtmfAndFallsBackToDefault() { ... }

@Test
void scheduleNodeRoutesByInjectedBusinessTime() { ... }

@Test
void queueNodeReturnsSkillGroupIdFromConfig() { ... }

@Test
void voicemailAndHangupReturnTerminalActions() { ... }
```

Use concrete `IvrNode` and `IvrEdge` objects in memory. Do not use Spring context for handler tests.

- [ ] **Step 2: Run handler test and verify failure**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrNodeHandlerTest
```

Expected result before implementation: failing assertions or missing handlers.

- [ ] **Step 3: Implement `IvrNodeType`**

Use enum values:

```java
START, MENU, SCHEDULE_CHECK, TRANSFER_VOICEMAIL, PLAY, PLAYBACK, COLLECT_DIGITS, CONDITION, API_CALL, TRANSFER_AGENT, TRANSFER_SKILL_GROUP, QUEUE, HANGUP
```

- [ ] **Step 4: Implement handler result**

`IvrNodeHandlerResult` must contain:

```text
nextNodeId, nextNodeType, action, variables, commandType, commandPayload
```

Static factories:

```text
continueTo(nextNodeId, variables)
waitInput(variables)
enqueue(skillGroupId, queueNodeId, variables)
voicemail(boxId, variables)
hangup(variables)
play(commandPayload, nextNodeId, variables)
end(variables)
failed(errorMessage, variables)
```

- [ ] **Step 5: Implement handlers**

Implement these focused handlers:

1. `DefaultNodeHandler`: follows first edge or ends.
2. `MenuNodeHandler`: matches DTMF exact condition, then empty default edge.
3. `ScheduleCheckNodeHandler`: reads `workStart` and `workEnd` from JSON config and supports test-injected time through variables key `nowTime`.
4. `ConditionNodeHandler`: evaluates simple `variable == value` conditions from edge expressions.
5. `PlayNodeHandler`: returns play command and next edge.
6. `CollectDigitsNodeHandler`: returns wait-input result.
7. `QueueNodeHandler`: parses `skillGroupId` from config JSON and returns enqueue.
8. `TransferNodeHandler`: returns transfer command payload for agent or external target.
9. `VoicemailNodeHandler`: parses `voicemailBoxId` and returns voicemail.
10. `HangupNodeHandler`: returns hangup.

- [ ] **Step 6: Implement factory**

`IvrNodeHandlerFactory` maps every `IvrNodeType` to a handler. `TRANSFER_SKILL_GROUP` and `QUEUE` both map to `QueueNodeHandler`.

- [ ] **Step 7: Run handler tests and verify pass**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrNodeHandlerTest
```

Expected result: `BUILD SUCCESS`.

## Task 3: Runtime Engine and Trace

**Files:**
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrRuntimeEngineTest.java`
- Modify/Create: `IvrRuntimeEngine`, `IvrExecutionService`.

- [ ] **Step 1: Write runtime test**

Test two paths:

1. Work time path: start -> schedule -> menu, DTMF `1` -> transfer skill group result with `skillGroupId=10`.
2. Non-work time path: start -> schedule -> voicemail result with `voicemailBoxId=10`.

Assert `ivr_execution_trace` receives ordered trace messages for every node transition.

- [ ] **Step 2: Run runtime test and verify failure**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrRuntimeEngineTest
```

Expected result before implementation: runtime behavior missing or failing.

- [ ] **Step 3: Implement runtime engine**

`IvrRuntimeEngine` responsibilities:

1. Load flow version graph by version ID.
2. Start execution and insert `ivr_execution`.
3. Execute nodes until `WAIT_INPUT`, `ENQUEUE`, `VOICEMAIL`, `HANGUP`, `END`, or `FAILED`.
4. Record `ivr_node_execution` for each node.
5. Record `ivr_execution_trace` for each transition.
6. Return `IvrRuntimeResult` containing `callId`, `executionId`, `action`, `variables`, and `traceId`.

- [ ] **Step 4: Implement DTMF continuation**

Add method:

```java
IvrRuntimeResult continueWithDtmf(String callId, String digit)
```

It loads active execution, current node, current variables, and continues execution with the DTMF value.

- [ ] **Step 5: Run runtime test and verify pass**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrRuntimeEngineTest
```

Expected result: `BUILD SUCCESS`.

## Task 4: Publish, Rollback, Validation, and APIs

**Files:**
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrPublishServiceTest.java`
- Test: `och-ivr/src/test/java/com/och/ivr/api/IvrFlowControllerTest.java`
- Create: `IvrPublishService`, `IvrTestRunService`, controllers.

- [ ] **Step 1: Write publish validation test**

Test validation errors for:

1. Missing START node.
2. Isolated node.
3. MENU without default edge.
4. TRANSFER_SKILL_GROUP without `skillGroupId`.
5. TRANSFER_VOICEMAIL without `voicemailBoxId`.

- [ ] **Step 2: Implement publish service**

`IvrPublishService.validate(versionId)` returns a validation result with `passed` and `errors`. `publish(versionId)` requires validation success, updates version state, and updates `ivr_flow.published_version_id`. `rollback(flowId, versionId)` requires target version exists and is not deleted.

- [ ] **Step 3: Implement test run service**

`IvrTestRunService.run(versionId, callerNumber, calledNumber, nowTime, dtmfSequence)` executes with deterministic variables and returns execution trace.

- [ ] **Step 4: Implement controllers**

Implement endpoints from the design spec:

```text
/api/ivr/flows
/api/ivr/flows/{id}/versions
/api/ivr/versions/{versionId}/graph
/api/ivr/versions/{versionId}/validate
/api/ivr/versions/{versionId}/publish
/api/ivr/versions/{versionId}/rollback
/api/ivr/test-runs
/api/ivr/executions/{executionId}/trace
/api/ivr/voicemail/messages
/api/ivr/voicemail/messages/{id}/status
```

- [ ] **Step 5: Run IVR API tests**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrPublishServiceTest,IvrFlowControllerTest
```

Expected result: `BUILD SUCCESS`.

## Task 5: Full IVR Verification

**Files:**
- All IVR files.

- [ ] **Step 1: Run all IVR tests**

Run:

```bash
mvn -pl och-ivr test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 2: Compile API dependency chain**

Run:

```bash
mvn -pl och-api -am test -DskipTests
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 3: Commit IVR checkpoint**

Commit only after user approval to commit:

```bash
git add och-ivr/src/main/java/com/och/ivr och-ivr/src/test/java/com/och/ivr
git commit -m "feat: add inbound ivr runtime"
```

## Self-Review

### Spec coverage

This plan covers IVR flow definition, versioning, publish, rollback, validation, node handlers, runtime engine, test runs, trace replay, and voicemail endpoints.

### Placeholder scan

No placeholder terms are used. Every task contains concrete paths, commands, expected results, and behavior.

### Type consistency

Node types, result actions, variables, seed IDs, endpoint paths, and state names match the schema and production inbound spec.
