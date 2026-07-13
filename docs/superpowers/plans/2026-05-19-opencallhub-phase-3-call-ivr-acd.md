# openCallHub Phase 3 Call, IVR, and ACD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Phase 3 call core, ESL boundary, IVR runtime, ACD queues, queue monitoring, and IVR execution tracing so inbound calls can route through IVR and reach agents or voicemail without depending on phases 4 through 9.

**Architecture:** Phase 3 keeps communication transport, call state, IVR execution, and ACD dispatch in separate modules: `och-esl` normalizes FreeSWITCH events and sends phone-control commands, `och-call` owns call sessions/legs/events/state transitions, `och-ivr` owns flow versions and runtime node execution, and `och-acd` owns skill groups, queues, agent availability snapshots, dispatch strategies, overflow, SLA, and queue metrics. All inbound processing uses local transactions plus idempotent event handling keyed by `callId + eventType + eventTime`; frontend pages consume REST APIs and `/ws/monitor` updates created by the Phase 1 WebSocket wrapper.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Web MVC, Flyway, MySQL, Redis locks from Phase 1, RabbitMQ/outbox abstraction from Phase 1, JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, Vue 3, TypeScript, Vite, Pinia, Axios, LogicFlow, Ant Design Vue, Vitest, Playwright, FreeSWITCH ESL.

---

## Agentic Worker Note

This is planning-only guidance. Execute it only after Phase 1 and Phase 2 acceptance gates pass. Use an isolated worktree at execution time, keep source changes scoped to Phase 3, and do not mix Phase 4 agent-workspace implementation into these checkpoints. Commit commands below are execution checkpoints for implementers; this planning task does not run them.

## Goal

Phase 3 implements:

- `och-call` call session, call leg, call event, call state machine, idempotent event ingestion, event ordering, inbound call route entry, and call data access control checks.
- `och-esl` FreeSWITCH ESL event normalization and command wrappers for answer, play, collect digits, bridge, record voicemail, hangup, and recovery-safe command correlation.
- `och-ivr` IVR flow/version/node/edge/variable model, runtime engine, test runs, execution traces, and nodes for menu, schedule check, transfer voicemail, play, collect digits, condition, API call, transfer agent, transfer skill group, hangup, and voicemail messages.
- `och-acd` skill groups, queues, agent status machine, dispatch strategies, overflow rules, SLA rules, queue monitor, and real-time queue metric updates.
- Frontend IVR designer, node panels, ACD config pages, queue monitor, and IVR execution trace viewer.
- Backend and frontend tests proving state machines, idempotency, event ordering, DTMF routing, non-business-hours voicemail routing, business-hours ACD routing, dispatch by configured strategy, trace visibility, and queue metrics.

## Architecture

### Backend boundaries

- `och-esl` owns only external communication with FreeSWITCH. It stores raw ESL headers, maps them into normalized `NormalizedEslEvent`, and exposes command wrappers returning accepted command records. It never chooses IVR or ACD business behavior.
- `och-call` owns canonical call state. It consumes normalized ESL events through `CallEventIngestService`, writes `call_session`, `call_leg`, and `call_event`, applies `CallStateMachine`, and publishes domain events to IVR/ACD through Phase 1 reliable event abstraction.
- `och-ivr` owns IVR definitions and execution. It loads the published `ivr_flow_version`, walks nodes through `IvrRuntimeEngine`, writes `ivr_execution` and `ivr_node_execution`, records variable snapshots, and invokes `och-esl` command ports for media actions.
- `och-acd` owns routing once IVR transfers to a skill group. It creates `acd_queue_session`, evaluates queue capacity/service hours/overflow/SLA, dispatches by configured strategy, updates agent state snapshots, and publishes monitor metrics.
- Cross-module direction is `och-api -> och-call/och-ivr/och-acd`, `och-call -> och-ivr/och-acd` by application ports, and `och-ivr/och-acd -> och-esl` by command ports. `och-esl` does not import `och-ivr` or `och-acd`.

### Frontend boundaries

- `waihu-app/src/views/ivr`: IVR flow list, LogicFlow designer, version publishing, test run, and trace viewer.
- `waihu-app/src/components/ivr`: focused node panels for menu, schedule, voicemail, play, collect digits, condition, API call, transfer agent, transfer skill group, and hangup.
- `waihu-app/src/views/acd`: skill group, queue, agent status snapshot, dispatch strategy, overflow/SLA config, and queue monitor pages.
- `waihu-app/src/services/callApi.ts`, `ivrApi.ts`, and `acdApi.ts`: typed REST clients for Phase 3 endpoints.
- `waihu-app/src/stores/queueMonitor.ts`: subscribes to `/ws/monitor` and exposes waiting calls, available agents, SLA breaches, and average wait seconds.

### Role owners

- Architect: call/IVR/ACD state machines and event consistency
- Backend Expert: domain models, transactions, idempotency, concurrency
- Communications Expert: FreeSWITCH, ESL, DTMF, bridge, recording, recovery
- Frontend Expert: IVR designer, node panels, ACD config and monitor pages
- Product Expert: IVR and ACD business closure
- QA Expert: inbound E2E and failure simulation
- Security Expert: call data and recording access controls

## Tech Stack

- Backend runtime: Java 17 and Spring Boot from Phase 1.
- Backend persistence: Flyway SQL migrations and MySQL tables with common `id`, `tenant_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `status`, and `remark` columns where applicable.
- Backend events: Phase 1 RabbitMQ/outbox abstraction, `eventId`, idempotent consumers, retry queue, and dead-letter queue behavior.
- Backend concurrency: Redis locks from Phase 1 for IVR version publish, queue dispatch, and agent assignment.
- Communication: FreeSWITCH ESL command/event integration behind `och-esl` ports; unit tests use fake ESL clients and normalized event fixtures.
- Frontend runtime: Vue 3, TypeScript, Vue Router, Pinia, Axios, LogicFlow, Ant Design Vue, and Phase 1 layout/pattern components.
- Frontend tests: Vitest for stores/components/API adapters and Playwright for the Phase 3 inbound acceptance path.

## File Structure

### Backend files to create

- `och-call/pom.xml`: call-core module dependencies.
- `och-esl/pom.xml`: ESL boundary module dependencies.
- `och-ivr/pom.xml`: IVR domain/runtime module dependencies.
- `och-acd/pom.xml`: ACD domain/runtime module dependencies.
- `och-call/src/main/resources/db/migration/V4__call_core_schema.sql`: `call_session`, `call_leg`, `call_event`, and `call_event_dedup`.
- `och-ivr/src/main/resources/db/migration/V5__ivr_schema.sql`: IVR flow/version/node/edge/variable/execution/trace/voicemail schema and deterministic local/test seed flow.
- `och-acd/src/main/resources/db/migration/V6__acd_schema.sql`: skill groups, queues, agents, dispatch/overflow/SLA rules, queue sessions, metrics snapshots, and deterministic local/test seed queue.
- `och-esl/src/main/java/com/opencallhub/esl/domain/NormalizedEslEvent.java`: canonical ESL event DTO.
- `och-esl/src/main/java/com/opencallhub/esl/domain/EslEventType.java`: enum `CHANNEL_CREATE`, `CHANNEL_ANSWER`, `DTMF`, `CHANNEL_BRIDGE`, `CHANNEL_HANGUP`, `RECORD_START`, `RECORD_STOP`, `HEARTBEAT`, `CUSTOM`.
- `och-esl/src/main/java/com/opencallhub/esl/application/EslEventNormalizer.java`: maps raw FreeSWITCH headers to normalized events.
- `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`: wrappers for answer/play/collect digits/bridge/record/hangup.
- `och-call/src/main/java/com/opencallhub/call/domain/CallSession.java`: call aggregate.
- `och-call/src/main/java/com/opencallhub/call/domain/CallLeg.java`: call leg aggregate.
- `och-call/src/main/java/com/opencallhub/call/domain/CallEvent.java`: normalized call event record.
- `och-call/src/main/java/com/opencallhub/call/domain/CallState.java`: enum `NEW`, `RINGING`, `IVR`, `QUEUEING`, `BRIDGED`, `VOICEMAIL`, `ENDED`, `FAILED`.
- `och-call/src/main/java/com/opencallhub/call/application/CallStateMachine.java`: allowed transition validator.
- `och-call/src/main/java/com/opencallhub/call/application/CallEventIngestService.java`: idempotent ordered event ingestion.
- `och-call/src/main/java/com/opencallhub/call/application/InboundCallApplicationService.java`: creates inbound session and starts IVR.
- `och-call/src/main/java/com/opencallhub/call/api/CallController.java`: call query and local/test inbound simulation endpoints.
- `och-ivr/src/main/java/com/opencallhub/ivr/domain/IvrFlow.java`, `IvrFlowVersion.java`, `IvrNode.java`, `IvrEdge.java`, `IvrVariable.java`: IVR definition records.
- `och-ivr/src/main/java/com/opencallhub/ivr/domain/IvrNodeType.java`: enum `START`, `MENU`, `SCHEDULE_CHECK`, `TRANSFER_VOICEMAIL`, `PLAY`, `COLLECT_DIGITS`, `CONDITION`, `API_CALL`, `TRANSFER_AGENT`, `TRANSFER_SKILL_GROUP`, `HANGUP`.
- `och-ivr/src/main/java/com/opencallhub/ivr/domain/IvrExecution.java`, `IvrNodeExecution.java`, `IvrExecutionTraceEvent.java`: runtime trace model.
- `och-ivr/src/main/java/com/opencallhub/ivr/domain/VoicemailBox.java`, `VoicemailMessage.java`: voicemail model.
- `och-ivr/src/main/java/com/opencallhub/ivr/application/IvrRuntimeEngine.java`: node execution loop.
- `och-ivr/src/main/java/com/opencallhub/ivr/application/node/MenuNodeHandler.java`: DTMF menu branch behavior.
- `och-ivr/src/main/java/com/opencallhub/ivr/application/node/ScheduleCheckNodeHandler.java`: business-hours branch behavior.
- `och-ivr/src/main/java/com/opencallhub/ivr/application/node/TransferVoicemailNodeHandler.java`: voicemail recording behavior.
- `och-ivr/src/main/java/com/opencallhub/ivr/application/node/PlayNodeHandler.java`, `CollectDigitsNodeHandler.java`, `ConditionNodeHandler.java`, `ApiCallNodeHandler.java`, `TransferAgentNodeHandler.java`, `TransferSkillGroupNodeHandler.java`, `HangupNodeHandler.java`: remaining node handlers.
- `och-ivr/src/main/java/com/opencallhub/ivr/api/IvrFlowController.java`: `/api/ivr/flows/**` APIs.
- `och-ivr/src/main/java/com/opencallhub/ivr/api/IvrExecutionController.java`: `/api/ivr/test-runs` and `/api/ivr/executions/**` APIs.
- `och-ivr/src/main/java/com/opencallhub/ivr/api/VoicemailController.java`: `/api/ivr/voicemail/**` APIs.
- `och-acd/src/main/java/com/opencallhub/acd/domain/SkillGroup.java`, `AcdQueue.java`, `AgentStatus.java`, `AgentState.java`, `AcdQueueSession.java`, `DispatchStrategy.java`, `OverflowRule.java`, `SlaRule.java`, `QueueMetricSnapshot.java`: ACD domain records.
- `och-acd/src/main/java/com/opencallhub/acd/application/AgentStatusStateMachine.java`: allowed agent transitions.
- `och-acd/src/main/java/com/opencallhub/acd/application/QueueDispatchService.java`: assignment coordinator with Redis lock.
- `och-acd/src/main/java/com/opencallhub/acd/application/strategy/IdleFirstDispatchStrategy.java`, `RoundRobinDispatchStrategy.java`, `LeastAnsweredDispatchStrategy.java`, `SkillPriorityDispatchStrategy.java`, `WeightedDispatchStrategy.java`, `RecentlyMissedDispatchStrategy.java`: dispatch implementations.
- `och-acd/src/main/java/com/opencallhub/acd/application/QueueMonitorService.java`: metric aggregation and WebSocket publishing.
- `och-acd/src/main/java/com/opencallhub/acd/api/AcdConfigController.java`: skill group, queue, rules, and agent state APIs.
- `och-acd/src/main/java/com/opencallhub/acd/api/QueueMonitorController.java`: queue monitor APIs.

### Backend test files to create

- `och-esl/src/test/java/com/opencallhub/esl/application/EslEventNormalizerTest.java`: raw ESL headers normalize into canonical event fields.
- `och-esl/src/test/java/com/opencallhub/esl/application/PhoneControlCommandServiceTest.java`: command wrappers produce exact ESL commands and correlation IDs.
- `och-call/src/test/java/com/opencallhub/call/application/CallStateMachineTest.java`: call state transitions are accepted/rejected.
- `och-call/src/test/java/com/opencallhub/call/application/CallEventIngestServiceTest.java`: duplicate and out-of-order events are handled deterministically.
- `och-ivr/src/test/java/com/opencallhub/ivr/application/IvrRuntimeEngineTest.java`: menu, schedule, voicemail, ACD transfer, and trace behaviors.
- `och-ivr/src/test/java/com/opencallhub/ivr/application/IvrNodeHandlerTest.java`: play, collect digits, condition, API call, transfer agent, transfer skill group, and hangup node behavior.
- `och-acd/src/test/java/com/opencallhub/acd/application/AgentStatusStateMachineTest.java`: agent state transitions.
- `och-acd/src/test/java/com/opencallhub/acd/application/QueueDispatchServiceTest.java`: dispatch strategies, overflow, SLA, idempotent assignment, and concurrent assignment lock behavior.
- `och-acd/src/test/java/com/opencallhub/acd/application/QueueMonitorServiceTest.java`: queue metrics update and publish.
- `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`: verifies Phase 3 migration tables and deterministic seed rows are available through the API test context.
- `och-api/src/test/java/com/opencallhub/api/e2e/Phase3InboundFlowTest.java`: backend acceptance path with fake ESL and deterministic seed data.

### Backend files to modify

- `pom.xml`: add `och-esl`, `och-call`, `och-ivr`, and `och-acd` modules after `och-resource`.
- `och-api/pom.xml`: add dependencies on `och-esl`, `och-call`, `och-ivr`, and `och-acd`.
- `och-api/src/main/resources/application.yml`: include Phase 3 migration locations if Phase 1 Flyway scanning does not include module classpaths.
- `och-websocket/src/main/java/com/opencallhub/websocket/monitor/MonitorMessagePublisher.java`: expose queue metric publish method if Phase 1 only created the wrapper shell.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`: add Phase 3 permission constants for call, IVR, ACD, queue monitor, voicemail, and trace APIs.

### Frontend files to create

- `waihu-app/src/types/call.ts`: call session, leg, event, and event trace DTOs.
- `waihu-app/src/types/ivr.ts`: IVR flow, version, node, edge, variable, execution, node execution, trace event, voicemail DTOs.
- `waihu-app/src/types/acd.ts`: skill group, queue, agent status, rules, queue session, and metric DTOs.
- `waihu-app/src/services/callApi.ts`: typed client for `/api/call/**`.
- `waihu-app/src/services/ivrApi.ts`: typed client for `/api/ivr/**`.
- `waihu-app/src/services/acdApi.ts`: typed client for `/api/acd/**`.
- `waihu-app/src/stores/ivrDesigner.ts`: graph state, validation errors, version status, selected node.
- `waihu-app/src/stores/queueMonitor.ts`: real-time queue metrics from REST and WebSocket.
- `waihu-app/src/views/ivr/IvrFlowListView.vue`: flow list and version actions.
- `waihu-app/src/views/ivr/IvrDesignerView.vue`: LogicFlow canvas and node panel shell.
- `waihu-app/src/views/ivr/IvrTestRunView.vue`: local test run trigger and simulated DTMF input.
- `waihu-app/src/views/ivr/IvrExecutionTraceView.vue`: execution timeline and node variable snapshots.
- `waihu-app/src/views/ivr/VoicemailMessageView.vue`: voicemail messages and recording access checks.
- `waihu-app/src/components/ivr/MenuNodePanel.vue`, `ScheduleCheckNodePanel.vue`, `TransferVoicemailNodePanel.vue`, `PlayNodePanel.vue`, `CollectDigitsNodePanel.vue`, `ConditionNodePanel.vue`, `ApiCallNodePanel.vue`, `TransferAgentNodePanel.vue`, `TransferSkillGroupNodePanel.vue`, `HangupNodePanel.vue`: focused node config panels.
- `waihu-app/src/views/acd/SkillGroupView.vue`: skill group CRUD and agent assignment.
- `waihu-app/src/views/acd/QueueConfigView.vue`: queue strategy, capacity, prompt, service hours, overflow, and SLA config.
- `waihu-app/src/views/acd/AgentStatusView.vue`: current status snapshots and manual local/test status adjustment.
- `waihu-app/src/views/acd/QueueMonitorView.vue`: real-time queue monitor.

### Frontend files to modify

- `waihu-app/src/router/routes.ts`: add IVR and ACD route records with Phase 3 permission metadata.
- `waihu-app/src/components/menu/AppMenu.vue`: render Phase 3 menu entries from Phase 2 backend menu data.
- `waihu-app/src/services/wsClient.ts`: subscribe to `/ws/monitor` queue metric messages if Phase 1 wrapper requires a topic helper.

### Frontend test files to create

- `waihu-app/src/tests/ivr-designer-store.spec.ts`: graph validation and node panel selection.
- `waihu-app/src/tests/menu-node-panel.spec.ts`: DTMF branch configuration validation.
- `waihu-app/src/tests/queue-monitor-store.spec.ts`: WebSocket metric updates merge into store.
- `waihu-app/e2e/phase3-call-ivr-acd.spec.ts`: Playwright inbound acceptance path.

## Bite-Sized Tasks

### Task 1: Phase 3 Module Wiring and Migrations

**Owner:** Architect

**Files:**
- Modify: `pom.xml`
- Modify: `och-api/pom.xml`
- Create: `och-call/pom.xml`
- Create: `och-esl/pom.xml`
- Create: `och-ivr/pom.xml`
- Create: `och-acd/pom.xml`
- Create: `och-call/src/main/resources/db/migration/V4__call_core_schema.sql`
- Create: `och-ivr/src/main/resources/db/migration/V5__ivr_schema.sql`
- Create: `och-acd/src/main/resources/db/migration/V6__acd_schema.sql`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`

- [ ] **Step 1: Verify Phase 1 and Phase 2 gates before source changes**

Run from `e:/workspace/openCallHub`:

```bash
mvn -pl och-api,och-system,och-resource -am test
cd e:/workspace/openCallHub/waihu-app && npm run test
```

Expected output contains `BUILD SUCCESS` for Maven and a passing frontend test summary. If either command fails because Phase 1 or Phase 2 files are missing, stop and finish those phases first.

- [ ] **Step 2: Add Phase 3 Maven modules**

Modify the root `pom.xml` module list so it contains these entries after `och-resource`:

```xml
<module>och-esl</module>
<module>och-call</module>
<module>och-ivr</module>
<module>och-acd</module>
```

Create module POMs for `och-esl`, `och-call`, `och-ivr`, and `och-acd`. Each module uses the root parent, `jar` packaging, and dependencies on `och-common`, `och-security`, `och-infra`, Spring Web, Validation, and Spring Boot Test. `och-call`, `och-ivr`, and `och-acd` also depend on `och-esl`; `och-ivr` depends on `och-call`; `och-acd` depends on `och-call`; `och-api` depends on all four modules.

Run:

```bash
mvn -pl och-esl,och-call,och-ivr,och-acd -am test -DskipTests
```

Expected output contains `BUILD SUCCESS` and no `Could not find artifact` errors.

- [ ] **Step 3: Create call core migration**

Create `och-call/src/main/resources/db/migration/V4__call_core_schema.sql` with these tables and indexes:

```sql
CREATE TABLE call_session (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    direction VARCHAR(32) NOT NULL,
    caller VARCHAR(32) NOT NULL,
    callee VARCHAR(32) NOT NULL,
    current_state VARCHAR(32) NOT NULL,
    ivr_execution_id BIGINT,
    queue_session_id BIGINT,
    agent_id BIGINT,
    started_at DATETIME NOT NULL,
    answered_at DATETIME,
    ended_at DATETIME,
    end_reason VARCHAR(120),
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_call_session_call_id (tenant_id, call_id, deleted),
    KEY idx_call_session_state (tenant_id, current_state, started_at),
    KEY idx_call_session_trace (tenant_id, trace_id)
);

CREATE TABLE call_leg (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    leg_id VARCHAR(64) NOT NULL,
    uuid VARCHAR(64) NOT NULL,
    leg_role VARCHAR(32) NOT NULL,
    endpoint VARCHAR(120),
    state VARCHAR(32) NOT NULL,
    started_at DATETIME NOT NULL,
    answered_at DATETIME,
    ended_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_call_leg_uuid (tenant_id, uuid, deleted),
    KEY idx_call_leg_call (tenant_id, call_id, deleted)
);

CREATE TABLE call_event (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    event_id VARCHAR(96) NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    uuid VARCHAR(64),
    leg_id VARCHAR(64),
    event_type VARCHAR(64) NOT NULL,
    event_time DATETIME NOT NULL,
    sequence_no BIGINT NOT NULL,
    source VARCHAR(32) NOT NULL,
    normalized_payload JSON NOT NULL,
    raw_payload JSON,
    processing_status VARCHAR(32) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_call_event_id (tenant_id, event_id),
    UNIQUE KEY uk_call_event_idempotency (tenant_id, call_id, event_type, event_time),
    KEY idx_call_event_order (tenant_id, call_id, event_time, sequence_no)
);

CREATE TABLE call_event_dedup (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    idempotency_key VARCHAR(200) NOT NULL,
    first_event_id VARCHAR(96) NOT NULL,
    first_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_call_event_dedup_key (tenant_id, idempotency_key)
);
```

Append deterministic local/test seed data for `call_session` with `call_id='call-phase3-seed'`, `caller='13800000001'`, `callee='4008001000'`, `current_state='NEW'`, and `trace_id='trace-phase3-seed'` so query APIs have stable fixtures.

- [ ] **Step 4: Create IVR migration with a publishable test flow**

Create `och-ivr/src/main/resources/db/migration/V5__ivr_schema.sql` with tables named exactly `ivr_flow`, `ivr_flow_version`, `ivr_node`, `ivr_edge`, `ivr_variable`, `ivr_execution`, `ivr_node_execution`, `ivr_execution_trace`, `ivr_voicemail_box`, and `ivr_voicemail_message`. Seed flow `ivr-main` version `1` with these nodes and edges:

```text
start -> schedule-check-main
schedule-check-main.business -> menu-main
schedule-check-main.non_business -> voicemail-main
menu-main.digit_1 -> transfer-skill-support
menu-main.digit_2 -> transfer-voicemail-sales
menu-main.timeout -> hangup-timeout
transfer-skill-support -> hangup-after-transfer
transfer-voicemail-sales -> hangup-after-voicemail
voicemail-main -> hangup-after-voicemail
```

Expected result: the seed flow supports the acceptance path for DTMF `1`, non-business-hours voicemail, and trace replay from one published version.

- [ ] **Step 5: Create ACD migration with queue fixtures**

Create `och-acd/src/main/resources/db/migration/V6__acd_schema.sql` with tables named exactly `acd_skill_group`, `acd_skill_group_agent`, `acd_queue`, `acd_agent_status`, `acd_dispatch_rule`, `acd_overflow_rule`, `acd_sla_rule`, `acd_queue_session`, and `acd_queue_metric_snapshot`. Seed:

```text
skill group: support-main, id 10
queue: support-main-queue, id 20, strategy IDLE_FIRST, capacity 50
agent status: agentId 9, extension 8001, state AVAILABLE, skillGroupId 10
SLA rule: answer within 30 seconds
overflow rule: after 120 seconds route to voicemail box support-after-hours
```

- [ ] **Step 6: Create and validate migration smoke test**

Create `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`:

```java
package com.opencallhub.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MigrationSmokeTest {
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void phase3TablesAndSeedRowsExist() {
        assertThat(jdbcTemplate.queryForObject("select count(*) from call_session where call_id = 'call-phase3-seed'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from ivr_flow_version v join ivr_flow f on f.id = v.flow_id where f.flow_code = 'ivr-main' and v.version_no = 1", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from acd_skill_group where group_code = 'support-main'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from acd_queue where queue_code = 'support-main-queue'", Integer.class)).isEqualTo(1);
    }
}
```

Run:

```bash
mvn -pl och-api -am test -Dtest=MigrationSmokeTest
```

Expected output contains `BUILD SUCCESS`. The smoke test asserts the tables `call_session`, `ivr_flow_version`, `ivr_flow`, `acd_skill_group`, and `acd_queue` exist and seed rows `call-phase3-seed`, `ivr-main`, `support-main`, and `support-main-queue` are queryable.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add pom.xml och-api/pom.xml och-esl/pom.xml och-call/pom.xml och-ivr/pom.xml och-acd/pom.xml och-call/src/main/resources/db/migration/V4__call_core_schema.sql och-ivr/src/main/resources/db/migration/V5__ivr_schema.sql och-acd/src/main/resources/db/migration/V6__acd_schema.sql och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java
git commit -m "feat: add call ivr acd module schemas"
```

Expected output contains `feat: add call ivr acd module schemas`.

### Task 2: ESL Event Normalization and Phone Control Wrappers

**Owner:** Communications Expert

**Files:**
- Create: `och-esl/src/main/java/com/opencallhub/esl/domain/EslEventType.java`
- Create: `och-esl/src/main/java/com/opencallhub/esl/domain/NormalizedEslEvent.java`
- Create: `och-esl/src/main/java/com/opencallhub/esl/application/EslEventNormalizer.java`
- Create: `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`
- Test: `och-esl/src/test/java/com/opencallhub/esl/application/EslEventNormalizerTest.java`
- Test: `och-esl/src/test/java/com/opencallhub/esl/application/PhoneControlCommandServiceTest.java`

- [ ] **Step 1: Write ESL normalizer test**

Create `EslEventNormalizerTest`:

```java
package com.opencallhub.esl.application;

import com.opencallhub.esl.domain.EslEventType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EslEventNormalizerTest {
    @Test
    void normalizesDtmfEventWithCallIdentifiersAndRawHeaders() {
        EslEventNormalizer normalizer = new EslEventNormalizer();

        var event = normalizer.normalize(Map.of(
                "Event-Name", "DTMF",
                "Unique-ID", "uuid-a",
                "variable_call_id", "call-100",
                "variable_leg_id", "caller-leg",
                "DTMF-Digit", "1",
                "Event-Date-Timestamp", "1779148800000000"
        ));

        assertThat(event.eventType()).isEqualTo(EslEventType.DTMF);
        assertThat(event.callId()).isEqualTo("call-100");
        assertThat(event.uuid()).isEqualTo("uuid-a");
        assertThat(event.legId()).isEqualTo("caller-leg");
        assertThat(event.digit()).isEqualTo("1");
        assertThat(event.eventTime()).isEqualTo(Instant.parse("2026-05-19T00:00:00Z"));
        assertThat(event.rawHeaders()).containsEntry("Event-Name", "DTMF");
    }
}
```

- [ ] **Step 2: Implement normalized ESL event model**

Create:

```java
package com.opencallhub.esl.domain;

public enum EslEventType {
    CHANNEL_CREATE, CHANNEL_ANSWER, DTMF, CHANNEL_BRIDGE, CHANNEL_HANGUP, RECORD_START, RECORD_STOP, HEARTBEAT, CUSTOM
}
```

```java
package com.opencallhub.esl.domain;

import java.time.Instant;
import java.util.Map;

public record NormalizedEslEvent(
        String eventId,
        EslEventType eventType,
        String callId,
        String uuid,
        String legId,
        String digit,
        Instant eventTime,
        Map<String, String> normalizedFields,
        Map<String, String> rawHeaders
) {
}
```

Implement `EslEventNormalizer.normalize(Map<String,String>)` so `Event-Date-Timestamp` microseconds convert to `Instant`, `variable_call_id` and `variable_leg_id` are retained, and `eventId` is `callId + ':' + eventType + ':' + eventTime`.

- [ ] **Step 3: Write phone command wrapper test**

Create `PhoneControlCommandServiceTest`:

```java
package com.opencallhub.esl.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneControlCommandServiceTest {
    @Test
    void buildsPlayCollectBridgeRecordAndHangupCommands() {
        PhoneControlCommandService service = PhoneControlCommandService.inMemoryForTest();

        assertThat(service.answer("uuid-a").command()).isEqualTo("uuid_answer uuid-a");
        assertThat(service.play("uuid-a", "storage://prompt/welcome.wav").command()).isEqualTo("uuid_broadcast uuid-a storage://prompt/welcome.wav aleg");
        assertThat(service.collectDigits("uuid-a", 1, 5, 3000).command()).isEqualTo("play_and_get_digits 1 5 3 3000 # silence_stream://250 phrase:invalid digits ");
        assertThat(service.bridge("uuid-a", "user/8001").command()).isEqualTo("uuid_bridge uuid-a user/8001");
        assertThat(service.recordVoicemail("uuid-a", "storage://voicemail/call-100.wav", 120).command()).isEqualTo("uuid_record uuid-a start storage://voicemail/call-100.wav 120");
        assertThat(service.hangup("uuid-a", "NORMAL_CLEARING").command()).isEqualTo("uuid_kill uuid-a NORMAL_CLEARING");
    }
}
```

- [ ] **Step 4: Implement command wrappers**

Create `PhoneControlCommandService` returning `PhoneControlCommandResult(correlationId, uuid, command, acceptedAt)`. The production adapter sends commands to FreeSWITCH; the in-memory test implementation records commands and returns deterministic `correlationId` values starting with `cmd-`.

- [ ] **Step 5: Run ESL tests**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventNormalizerTest,PhoneControlCommandServiceTest
```

Expected output contains `BUILD SUCCESS` and both test classes pass.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-esl/src/main/java och-esl/src/test/java
git commit -m "feat: normalize esl events and commands"
```

Expected output contains `feat: normalize esl events and commands`.

### Task 3: Call State Machine, Idempotency, and Event Ordering

**Owner:** Backend Expert

**Files:**
- Create: call domain/application/API files listed in File Structure
- Test: `och-call/src/test/java/com/opencallhub/call/application/CallStateMachineTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/CallEventIngestServiceTest.java`

- [ ] **Step 1: Write call state machine test**

Create `CallStateMachineTest`:

```java
package com.opencallhub.call.application;

import com.opencallhub.call.domain.CallState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallStateMachineTest {
    @Test
    void inboundCallCanMoveFromNewToIvrToQueueToBridgeToEnd() {
        CallStateMachine machine = new CallStateMachine();

        assertThat(machine.transition(CallState.NEW, "CHANNEL_CREATE")).isEqualTo(CallState.RINGING);
        assertThat(machine.transition(CallState.RINGING, "START_IVR")).isEqualTo(CallState.IVR);
        assertThat(machine.transition(CallState.IVR, "TRANSFER_SKILL_GROUP")).isEqualTo(CallState.QUEUEING);
        assertThat(machine.transition(CallState.QUEUEING, "AGENT_ASSIGNED")).isEqualTo(CallState.BRIDGED);
        assertThat(machine.transition(CallState.BRIDGED, "CHANNEL_HANGUP")).isEqualTo(CallState.ENDED);
    }

    @Test
    void endedCallRejectsFurtherBusinessTransitions() {
        CallStateMachine machine = new CallStateMachine();
        assertThatThrownBy(() -> machine.transition(CallState.ENDED, "START_IVR"))
                .hasMessageContaining("Invalid call state transition ENDED -> START_IVR");
    }
}
```

- [ ] **Step 2: Implement call domain and state machine**

Create `CallState` enum and `CallStateMachine.transition(CallState current, String event)` with this transition table:

```text
NEW + CHANNEL_CREATE -> RINGING
RINGING + START_IVR -> IVR
RINGING + CHANNEL_HANGUP -> ENDED
IVR + TRANSFER_SKILL_GROUP -> QUEUEING
IVR + TRANSFER_VOICEMAIL -> VOICEMAIL
IVR + HANGUP -> ENDED
QUEUEING + AGENT_ASSIGNED -> BRIDGED
QUEUEING + QUEUE_OVERFLOW_VOICEMAIL -> VOICEMAIL
QUEUEING + CHANNEL_HANGUP -> ENDED
BRIDGED + CHANNEL_HANGUP -> ENDED
VOICEMAIL + RECORD_STOP -> ENDED
NEW/RINGING/IVR/QUEUEING/BRIDGED/VOICEMAIL + FAILURE -> FAILED
```

Expected result: invalid transitions throw `IllegalStateException` with current state and event name.

- [ ] **Step 3: Write idempotency and event ordering test**

Create `CallEventIngestServiceTest`:

```java
package com.opencallhub.call.application;

import com.opencallhub.esl.domain.EslEventType;
import com.opencallhub.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CallEventIngestServiceTest {
    @Test
    void duplicateEventsAreIgnoredAndOutOfOrderEventsAreStoredInEventTimeOrder() {
        CallEventIngestService service = CallEventIngestService.inMemoryForTest();
        var answer = new NormalizedEslEvent("evt-answer", EslEventType.CHANNEL_ANSWER, "call-100", "uuid-a", "caller-leg", null, Instant.parse("2026-05-19T00:00:05Z"), Map.of(), Map.of());
        var create = new NormalizedEslEvent("evt-create", EslEventType.CHANNEL_CREATE, "call-100", "uuid-a", "caller-leg", null, Instant.parse("2026-05-19T00:00:01Z"), Map.of(), Map.of());

        service.ingest(answer);
        service.ingest(create);
        service.ingest(answer);

        assertThat(service.eventsForCall("call-100")).extracting(event -> event.eventId()).containsExactly("evt-create", "evt-answer");
        assertThat(service.duplicateCount()).isEqualTo(1);
    }
}
```

- [ ] **Step 4: Implement event ingestion service**

Implement `CallEventIngestService.ingest(NormalizedEslEvent)` so it computes idempotency key `callId + ':' + eventType + ':' + eventTime`, ignores duplicates by `call_event_dedup`, stores raw/normalized payloads, and returns events sorted by `event_time` then `sequence_no`. Database implementation inserts into `call_event_dedup` and `call_event` in one transaction.

- [ ] **Step 5: Implement inbound call APIs**

Create `CallController` endpoints:

```text
GET  /api/call/sessions
GET  /api/call/sessions/{callId}
GET  /api/call/sessions/{callId}/events
POST /api/call/test-fixtures/inbound-call
```

`POST /api/call/test-fixtures/inbound-call` is active only for `local` and `test` profiles and accepts:

```json
{"caller":"13800000001","callee":"4008001000","businessTime":"2026-05-19T10:00:00+08:00","dtmf":"1"}
```

Expected response data contains `callId`, `traceId`, `ivrExecutionId`, and current state `QUEUEING` or `BRIDGED` depending on ACD assignment availability.

- [ ] **Step 6: Run call tests**

Run:

```bash
mvn -pl och-call -am test -Dtest=CallStateMachineTest,CallEventIngestServiceTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add och-call/src/main/java och-call/src/test/java
git commit -m "feat: add call state and event ingestion"
```

Expected output contains `feat: add call state and event ingestion`.

### Task 4: IVR Models, Runtime Engine, and Node Handlers

**Owner:** Backend Expert

**Files:**
- Create: IVR domain/application/API files listed in File Structure
- Test: `och-ivr/src/test/java/com/opencallhub/ivr/application/IvrRuntimeEngineTest.java`
- Test: `och-ivr/src/test/java/com/opencallhub/ivr/application/IvrNodeHandlerTest.java`

- [ ] **Step 1: Write IVR runtime menu/schedule/voicemail test**

Create `IvrRuntimeEngineTest`:

```java
package com.opencallhub.ivr.application;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IvrRuntimeEngineTest {
    @Test
    void menuDigitOneRoutesBusinessHourCallToSkillGroupWithTrace() {
        IvrRuntimeEngine engine = IvrRuntimeEngine.inMemoryWithSeedFlow();

        var result = engine.testRun("ivr-main", "call-100", OffsetDateTime.parse("2026-05-19T10:00:00+08:00"), Map.of("dtmf", "1"));

        assertThat(result.finalAction()).isEqualTo("TRANSFER_SKILL_GROUP");
        assertThat(result.targetSkillGroupCode()).isEqualTo("support-main");
        assertThat(result.trace()).extracting(trace -> trace.nodeCode()).contains("schedule-check-main", "menu-main", "transfer-skill-support");
    }

    @Test
    void nonBusinessHoursRoutesToVoicemail() {
        IvrRuntimeEngine engine = IvrRuntimeEngine.inMemoryWithSeedFlow();

        var result = engine.testRun("ivr-main", "call-101", OffsetDateTime.parse("2026-05-19T23:00:00+08:00"), Map.of("dtmf", "1"));

        assertThat(result.finalAction()).isEqualTo("TRANSFER_VOICEMAIL");
        assertThat(result.voicemailBoxCode()).isEqualTo("support-after-hours");
    }
}
```

- [ ] **Step 2: Implement IVR domain model**

Create records matching the File Structure section. `IvrNode.configJson` stores node configuration. `IvrEdge.conditionKey` uses exact keys `business`, `non_business`, `digit_1`, `digit_2`, `timeout`, `default`, and `success`. `IvrFlowVersion.status` is `DRAFT`, `PUBLISHED`, or `ARCHIVED`; only one version per flow can be `PUBLISHED`.

- [ ] **Step 3: Implement node handlers**

Implement these handler outputs:

```text
PlayNodeHandler -> command PLAY and edge success
CollectDigitsNodeHandler -> command COLLECT_DIGITS and variable collectedDigits
MenuNodeHandler -> reads variable dtmf, routes digit_N, timeout, or default
ScheduleCheckNodeHandler -> business when local time is within configured weekday 09:00-18:00, non_business otherwise
ConditionNodeHandler -> compares variables with EQ, NE, GT, LT, CONTAINS
ApiCallNodeHandler -> invokes configured HTTP endpoint with 3 second timeout and stores status/body variables
TransferAgentNodeHandler -> action TRANSFER_AGENT with targetAgentId
TransferSkillGroupNodeHandler -> action TRANSFER_SKILL_GROUP with skillGroupCode
TransferVoicemailNodeHandler -> action TRANSFER_VOICEMAIL with voicemailBoxCode
HangupNodeHandler -> action HANGUP with cause NORMAL_CLEARING
```

- [ ] **Step 4: Write node handler coverage test**

Create `IvrNodeHandlerTest` with one test per handler. Use deterministic fixtures:

```java
assertThat(new MenuNodeHandler().nextEdge(Map.of("dtmf", "2"))).isEqualTo("digit_2");
assertThat(new ScheduleCheckNodeHandler().nextEdge(OffsetDateTime.parse("2026-05-19T23:00:00+08:00"))).isEqualTo("non_business");
assertThat(new HangupNodeHandler().handle().finalAction()).isEqualTo("HANGUP");
```

Expected initial result: tests fail before handlers exist and pass after the handlers implement the outputs listed above.

- [ ] **Step 5: Implement REST APIs for flows and executions**

Create endpoints:

```text
GET    /api/ivr/flows
POST   /api/ivr/flows
GET    /api/ivr/flows/{flowId}/versions
POST   /api/ivr/flows/{flowId}/versions
PUT    /api/ivr/versions/{versionId}/graph
POST   /api/ivr/versions/{versionId}/publish
POST   /api/ivr/test-runs
GET    /api/ivr/executions/{executionId}
GET    /api/ivr/executions/{executionId}/trace
GET    /api/ivr/voicemail/boxes
GET    /api/ivr/voicemail/messages
PUT    /api/ivr/voicemail/messages/{messageId}/status
```

`POST /api/ivr/test-runs` accepts `flowCode`, `callId`, `businessTime`, and `variables`. Expected response includes `executionId`, `finalAction`, `targetSkillGroupCode`, `voicemailBoxCode`, and ordered `trace`.

- [ ] **Step 6: Run IVR tests**

Run:

```bash
mvn -pl och-ivr -am test -Dtest=IvrRuntimeEngineTest,IvrNodeHandlerTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add och-ivr/src/main/java och-ivr/src/test/java
git commit -m "feat: add ivr runtime and node handlers"
```

Expected output contains `feat: add ivr runtime and node handlers`.

### Task 5: ACD State Machine, Dispatch, Overflow, SLA, and Monitor

**Owner:** Architect

**Files:**
- Create: ACD domain/application/API files listed in File Structure
- Test: `och-acd/src/test/java/com/opencallhub/acd/application/AgentStatusStateMachineTest.java`
- Test: `och-acd/src/test/java/com/opencallhub/acd/application/QueueDispatchServiceTest.java`
- Test: `och-acd/src/test/java/com/opencallhub/acd/application/QueueMonitorServiceTest.java`

- [ ] **Step 1: Write agent status machine test**

Create `AgentStatusStateMachineTest`:

```java
package com.opencallhub.acd.application;

import com.opencallhub.acd.domain.AgentState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentStatusStateMachineTest {
    @Test
    void availableAgentCanBeReservedThenBusyThenAfterCallThenAvailable() {
        AgentStatusStateMachine machine = new AgentStatusStateMachine();

        assertThat(machine.transition(AgentState.AVAILABLE, "RESERVE_FOR_CALL")).isEqualTo(AgentState.RESERVED);
        assertThat(machine.transition(AgentState.RESERVED, "BRIDGE_CONNECTED")).isEqualTo(AgentState.BUSY);
        assertThat(machine.transition(AgentState.BUSY, "CALL_ENDED")).isEqualTo(AgentState.AFTER_CALL);
        assertThat(machine.transition(AgentState.AFTER_CALL, "AFTER_CALL_DONE")).isEqualTo(AgentState.AVAILABLE);
    }

    @Test
    void offlineAgentCannotBeReserved() {
        assertThatThrownBy(() -> new AgentStatusStateMachine().transition(AgentState.OFFLINE, "RESERVE_FOR_CALL"))
                .hasMessageContaining("Invalid agent state transition OFFLINE -> RESERVE_FOR_CALL");
    }
}
```

- [ ] **Step 2: Implement agent states**

Create `AgentState` enum with `OFFLINE`, `AVAILABLE`, `RESERVED`, `BUSY`, `AFTER_CALL`, `BREAK`, and `UNAVAILABLE`. Implement transition table:

```text
OFFLINE + GO_ONLINE -> AVAILABLE
AVAILABLE + RESERVE_FOR_CALL -> RESERVED
RESERVED + BRIDGE_CONNECTED -> BUSY
RESERVED + RESERVATION_TIMEOUT -> AVAILABLE
BUSY + CALL_ENDED -> AFTER_CALL
AFTER_CALL + AFTER_CALL_DONE -> AVAILABLE
AVAILABLE + START_BREAK -> BREAK
BREAK + END_BREAK -> AVAILABLE
AVAILABLE + GO_OFFLINE -> OFFLINE
UNAVAILABLE + RECOVER -> AVAILABLE
```

- [ ] **Step 3: Write dispatch strategy and overflow test**

Create `QueueDispatchServiceTest`:

```java
package com.opencallhub.acd.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueueDispatchServiceTest {
    @Test
    void idleFirstAssignsAvailableAgentAndIsIdempotentForSameQueueSession() {
        QueueDispatchService service = QueueDispatchService.inMemoryWithAgents("IDLE_FIRST");

        var first = service.dispatch("queue-session-100", "support-main");
        var second = service.dispatch("queue-session-100", "support-main");

        assertThat(first.assigned()).isTrue();
        assertThat(first.agentId()).isEqualTo(9L);
        assertThat(second.agentId()).isEqualTo(first.agentId());
        assertThat(service.assignmentCount()).isEqualTo(1);
    }

    @Test
    void fullQueueUsesOverflowVoicemailRule() {
        QueueDispatchService service = QueueDispatchService.inMemoryFullQueue();

        var result = service.enqueueOrOverflow("call-200", "support-main");

        assertThat(result.action()).isEqualTo("TRANSFER_VOICEMAIL");
        assertThat(result.voicemailBoxCode()).isEqualTo("support-after-hours");
    }
}
```

- [ ] **Step 4: Implement dispatch strategies**

Implement strategy outputs:

```text
IDLE_FIRST -> available agent with oldest lastStateChangedAt
ROUND_ROBIN -> next available agent after queue.lastAssignedAgentId
LEAST_ANSWERED -> available agent with lowest answeredCount
SKILL_PRIORITY -> available agent with highest skillLevel for skillGroupId
WEIGHTED -> available agent with highest configured weight
RECENTLY_MISSED -> exclude agents with lastMissedAt inside queue.recentMissedCooldownSeconds, then idle first
```

`QueueDispatchService.dispatch(queueSessionId, skillGroupCode)` must use a Redis lock key `acd:dispatch:{queueSessionId}` and assignment idempotency key `queueSessionId + ':ASSIGN_AGENT'`.

- [ ] **Step 5: Implement queue monitor test and service**

Create `QueueMonitorServiceTest`:

```java
package com.opencallhub.acd.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueueMonitorServiceTest {
    @Test
    void queueMetricsCountWaitingAvailableAndSlaBreaches() {
        QueueMonitorService service = QueueMonitorService.inMemoryFixture();

        var metric = service.snapshot("support-main");

        assertThat(metric.waitingCalls()).isEqualTo(2);
        assertThat(metric.availableAgents()).isEqualTo(1);
        assertThat(metric.slaBreachedCalls()).isEqualTo(1);
        assertThat(metric.averageWaitSeconds()).isEqualTo(45);
    }
}
```

Implement `QueueMonitorService.snapshot(skillGroupCode)` and publish WebSocket message:

```json
{"topic":"queue.metric.updated","data":{"skillGroupCode":"support-main","waitingCalls":2,"availableAgents":1,"slaBreachedCalls":1,"averageWaitSeconds":45}}
```

- [ ] **Step 6: Implement ACD APIs**

Create endpoints:

```text
GET    /api/acd/skill-groups
POST   /api/acd/skill-groups
PUT    /api/acd/skill-groups/{id}
PUT    /api/acd/skill-groups/{id}/agents
GET    /api/acd/queues
POST   /api/acd/queues
PUT    /api/acd/queues/{id}
PUT    /api/acd/queues/{id}/dispatch-rule
PUT    /api/acd/queues/{id}/overflow-rule
PUT    /api/acd/queues/{id}/sla-rule
GET    /api/acd/agents/status
PUT    /api/acd/agents/{agentId}/status
GET    /api/acd/monitor/queues
GET    /api/acd/monitor/queues/{skillGroupCode}
```

- [ ] **Step 7: Run ACD tests**

Run:

```bash
mvn -pl och-acd -am test -Dtest=AgentStatusStateMachineTest,QueueDispatchServiceTest,QueueMonitorServiceTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add och-acd/src/main/java och-acd/src/test/java och-websocket/src/main/java
git commit -m "feat: add acd dispatch and monitor"
```

Expected output contains `feat: add acd dispatch and monitor`.

### Task 6: Frontend IVR Designer, Node Panels, Test Run, and Trace Viewer

**Owner:** Frontend Expert

**Files:**
- Create: frontend IVR files listed in File Structure
- Modify: `waihu-app/src/router/routes.ts`
- Test: `waihu-app/src/tests/ivr-designer-store.spec.ts`
- Test: `waihu-app/src/tests/menu-node-panel.spec.ts`

- [ ] **Step 1: Write IVR designer store test**

Create `waihu-app/src/tests/ivr-designer-store.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it } from 'vitest'
import { useIvrDesignerStore } from '@/stores/ivrDesigner'

describe('ivr designer store', () => {
  it('validates start menu and voicemail paths before publish', () => {
    setActivePinia(createPinia())
    const store = useIvrDesignerStore()
    store.loadGraph({
      nodes: [
        { code: 'start', type: 'START', config: {} },
        { code: 'menu-main', type: 'MENU', config: { promptAssetCode: 'welcome-main', options: [{ digit: '1', label: 'Support' }] } },
        { code: 'voicemail-main', type: 'TRANSFER_VOICEMAIL', config: { voicemailBoxCode: 'support-after-hours' } },
      ],
      edges: [
        { source: 'start', target: 'menu-main', conditionKey: 'success' },
        { source: 'menu-main', target: 'voicemail-main', conditionKey: 'digit_1' },
      ],
    })

    expect(store.validateForPublish()).toEqual([])
    store.selectNode('menu-main')
    expect(store.selectedNode?.type).toBe('MENU')
  })
})
```

- [ ] **Step 2: Write menu node panel test**

Create `waihu-app/src/tests/menu-node-panel.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MenuNodePanel from '@/components/ivr/MenuNodePanel.vue'

describe('MenuNodePanel', () => {
  it('emits menu config with digit branch and retry settings', async () => {
    const wrapper = mount(MenuNodePanel, { props: { modelValue: { promptAssetCode: '', maxRetries: 2, timeoutMs: 3000, options: [] } } })

    await wrapper.get('[data-test="prompt-asset-code"]').setValue('welcome-main')
    await wrapper.get('[data-test="add-digit-option"]').trigger('click')
    await wrapper.get('[data-test="digit-0"]').setValue('1')
    await wrapper.get('[data-test="label-0"]').setValue('Support')

    const emitted = wrapper.emitted('update:modelValue')?.at(-1)?.[0] as any
    expect(emitted.promptAssetCode).toBe('welcome-main')
    expect(emitted.options[0]).toEqual({ digit: '1', label: 'Support' })
  })
})
```

- [ ] **Step 3: Implement IVR DTOs, API client, and store**

Create `waihu-app/src/types/ivr.ts`, `waihu-app/src/services/ivrApi.ts`, and `waihu-app/src/stores/ivrDesigner.ts`. `validateForPublish()` must return exact errors for missing start node, missing menu option target, missing voicemail box code, and missing schedule branches. With the graph in Step 1 it returns `[]`.

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- ivr-designer-store.spec.ts
```

Expected output contains a passing test summary for `ivr-designer-store.spec.ts`.

- [ ] **Step 4: Implement node panels**

Create all node panel files listed in File Structure. Each panel emits `update:modelValue` and uses stable `data-test` attributes for editable fields. Required fields:

```text
MenuNodePanel: promptAssetCode, maxRetries, timeoutMs, options digit/label
ScheduleCheckNodePanel: timezone, weekdays, startTime, endTime, holidayCalendarCode
TransferVoicemailNodePanel: voicemailBoxCode, maxRecordSeconds
PlayNodePanel: audioAssetCode, interruptible
CollectDigitsNodePanel: minDigits, maxDigits, timeoutMs, terminator
ConditionNodePanel: variableName, operator, compareValue
ApiCallNodePanel: method, url, timeoutMs, requestBodyTemplate
TransferAgentNodePanel: agentId
TransferSkillGroupNodePanel: skillGroupCode
HangupNodePanel: cause
```

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- menu-node-panel.spec.ts
```

Expected output contains a passing test summary for `menu-node-panel.spec.ts`.

- [ ] **Step 5: Implement designer and test-run pages**

Create `IvrFlowListView.vue`, `IvrDesignerView.vue`, `IvrTestRunView.vue`, `IvrExecutionTraceView.vue`, and `VoicemailMessageView.vue`. `IvrDesignerView.vue` uses LogicFlow to render node types and opens the matching node panel when a node is selected. `IvrTestRunView.vue` posts to `/api/ivr/test-runs` and displays final action plus ordered trace rows. `IvrExecutionTraceView.vue` shows node code, node type, enteredAt, exitedAt, result, variables, and error message.

- [ ] **Step 6: Add IVR routes**

Modify `waihu-app/src/router/routes.ts` with route paths:

```text
/ivr/flows
/ivr/flows/:flowId/designer
/ivr/test-runs
/ivr/executions/:executionId/trace
/ivr/voicemail/messages
```

Use permission codes `ivr:flow:view`, `ivr:flow:edit`, `ivr:test-run:view`, `ivr:trace:view`, and `ivr:voicemail:view`.

- [ ] **Step 7: Run frontend IVR tests and build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- ivr-designer-store.spec.ts menu-node-panel.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains passing Vitest summaries and frontend build output containing `built in`.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/ivr.ts waihu-app/src/services/ivrApi.ts waihu-app/src/stores/ivrDesigner.ts waihu-app/src/views/ivr waihu-app/src/components/ivr waihu-app/src/router/routes.ts waihu-app/src/tests/ivr-designer-store.spec.ts waihu-app/src/tests/menu-node-panel.spec.ts
git commit -m "feat: add ivr designer and trace frontend"
```

Expected output contains `feat: add ivr designer and trace frontend`.

### Task 7: Frontend ACD Config and Queue Monitor

**Owner:** Frontend Expert

**Files:**
- Create: frontend ACD files listed in File Structure
- Modify: `waihu-app/src/router/routes.ts`
- Modify: `waihu-app/src/services/wsClient.ts`
- Test: `waihu-app/src/tests/queue-monitor-store.spec.ts`

- [ ] **Step 1: Write queue monitor store test**

Create `waihu-app/src/tests/queue-monitor-store.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it } from 'vitest'
import { useQueueMonitorStore } from '@/stores/queueMonitor'

describe('queue monitor store', () => {
  it('merges websocket queue metric updates by skill group code', () => {
    setActivePinia(createPinia())
    const store = useQueueMonitorStore()

    store.applyMetricUpdate({ skillGroupCode: 'support-main', waitingCalls: 2, availableAgents: 1, slaBreachedCalls: 1, averageWaitSeconds: 45 })
    store.applyMetricUpdate({ skillGroupCode: 'support-main', waitingCalls: 1, availableAgents: 0, slaBreachedCalls: 0, averageWaitSeconds: 12 })

    expect(store.metricsBySkillGroup['support-main']).toMatchObject({ waitingCalls: 1, availableAgents: 0, averageWaitSeconds: 12 })
  })
})
```

- [ ] **Step 2: Implement ACD DTOs, API client, and monitor store**

Create `waihu-app/src/types/acd.ts`, `waihu-app/src/services/acdApi.ts`, and `waihu-app/src/stores/queueMonitor.ts`. The store must expose `loadInitialMetrics()`, `applyMetricUpdate(metric)`, and `subscribe()` using `/ws/monitor` topic `queue.metric.updated`.

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- queue-monitor-store.spec.ts
```

Expected output contains a passing test summary.

- [ ] **Step 3: Implement ACD config pages**

Create:

```text
waihu-app/src/views/acd/SkillGroupView.vue
waihu-app/src/views/acd/QueueConfigView.vue
waihu-app/src/views/acd/AgentStatusView.vue
```

Fields:

```text
SkillGroupView: groupCode, groupName, serviceTimezone, assignedAgents
QueueConfigView: queueCode, queueName, skillGroupCode, capacity, dispatchStrategy, promptAssetCode, serviceHours, overflowAfterSeconds, overflowVoicemailBoxCode, slaAnswerSeconds
AgentStatusView: agentId, displayName, extensionNo, skillGroupCodes, state, lastStateChangedAt
```

- [ ] **Step 4: Implement queue monitor page**

Create `waihu-app/src/views/acd/QueueMonitorView.vue`. It shows cards/table columns `skillGroupCode`, `waitingCalls`, `availableAgents`, `slaBreachedCalls`, `averageWaitSeconds`, and `lastUpdatedAt`. It must update from `queueMonitorStore.metricsBySkillGroup` without page refresh.

- [ ] **Step 5: Add ACD routes**

Modify `waihu-app/src/router/routes.ts` with route paths:

```text
/acd/skill-groups
/acd/queues
/acd/agents/status
/acd/monitor
```

Use permission codes `acd:skill-group:view`, `acd:queue:view`, `acd:agent-status:view`, and `acd:monitor:view`.

- [ ] **Step 6: Run frontend ACD tests and build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- queue-monitor-store.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains a passing Vitest summary and build output containing `built in`.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/acd.ts waihu-app/src/services/acdApi.ts waihu-app/src/stores/queueMonitor.ts waihu-app/src/views/acd waihu-app/src/router/routes.ts waihu-app/src/services/wsClient.ts waihu-app/src/tests/queue-monitor-store.spec.ts
git commit -m "feat: add acd config and monitor frontend"
```

Expected output contains `feat: add acd config and monitor frontend`.

### Task 8: Security and Access Control for Call Data, Traces, and Recordings

**Owner:** Security Expert

**Files:**
- Modify: `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/security/CallDataAccessService.java`
- Create: `och-ivr/src/main/java/com/opencallhub/ivr/application/security/RecordingAccessService.java`
- Create: `och-call/src/test/java/com/opencallhub/call/application/security/CallDataAccessServiceTest.java`
- Create: `och-ivr/src/test/java/com/opencallhub/ivr/application/security/RecordingAccessServiceTest.java`

- [ ] **Step 1: Write call data access test**

Create `CallDataAccessServiceTest`:

```java
package com.opencallhub.call.application.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallDataAccessServiceTest {
    @Test
    void supervisorWithSkillGroupScopeCanViewQueueCallButUnrelatedAgentCannot() {
        CallDataAccessService service = CallDataAccessService.inMemoryForTest();

        assertThat(service.canViewCall(100L, "SUPERVISOR", "support-main", "call-100")).isTrue();
        assertThat(service.canViewCall(9L, "AGENT", "sales-main", "call-100")).isFalse();
    }
}
```

- [ ] **Step 2: Write recording access test**

Create `RecordingAccessServiceTest`:

```java
package com.opencallhub.ivr.application.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordingAccessServiceTest {
    @Test
    void voicemailRecordingRequiresPermissionAndAuditReason() {
        RecordingAccessService service = RecordingAccessService.inMemoryForTest();

        assertThat(service.canAccessVoicemailRecording(1L, "ivr:voicemail:recording:play", "quality review")).isTrue();
        assertThat(service.canAccessVoicemailRecording(1L, "ivr:voicemail:view", "quality review")).isFalse();
        assertThat(service.canAccessVoicemailRecording(1L, "ivr:voicemail:recording:play", "")).isFalse();
    }
}
```

- [ ] **Step 3: Implement permission constants and services**

Add constants:

```text
call:session:view
call:event:view
ivr:flow:view
ivr:flow:edit
ivr:test-run:view
ivr:trace:view
ivr:voicemail:view
ivr:voicemail:recording:play
acd:skill-group:view
acd:queue:view
acd:agent-status:view
acd:monitor:view
```

`CallDataAccessService` checks role, data scope, skill group membership, and assigned agent. `RecordingAccessService` requires `ivr:voicemail:recording:play`, non-blank reason, and writes operation audit record with target type `VOICEMAIL_RECORDING` before returning a playable URL.

- [ ] **Step 4: Run security tests**

Run:

```bash
mvn -pl och-call,och-ivr -am test -Dtest=CallDataAccessServiceTest,RecordingAccessServiceTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 5: Commit checkpoint**

Run:

```bash
git add och-security/src/main/java och-call/src/main/java och-call/src/test/java och-ivr/src/main/java och-ivr/src/test/java
git commit -m "feat: secure call trace and recording access"
```

Expected output contains `feat: secure call trace and recording access`.

### Task 9: Backend Inbound E2E and Failure Simulation

**Owner:** QA Expert

**Files:**
- Create: `och-api/src/test/java/com/opencallhub/api/e2e/Phase3InboundFlowTest.java`
- Create: `och-api/src/test/java/com/opencallhub/api/e2e/Phase3FailureSimulationTest.java`

- [ ] **Step 1: Write backend inbound E2E test**

Create `Phase3InboundFlowTest`:

```java
package com.opencallhub.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Phase3InboundFlowTest {
    @Autowired MockMvc mockMvc;

    @Test
    void inboundCallRoutesThroughIvrMenuToAcdAndAssignsAgent() throws Exception {
        mockMvc.perform(post("/api/call/test-fixtures/inbound-call")
                .contentType("application/json")
                .content("{\"caller\":\"13800000001\",\"callee\":\"4008001000\",\"businessTime\":\"2026-05-19T10:00:00+08:00\",\"dtmf\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalAction").value("TRANSFER_SKILL_GROUP"))
                .andExpect(jsonPath("$.data.skillGroupCode").value("support-main"))
                .andExpect(jsonPath("$.data.assignedAgentId").value(9))
                .andExpect(jsonPath("$.data.trace[0].nodeCode").value("schedule-check-main"));
    }

    @Test
    void inboundCallRoutesNonBusinessHoursToVoicemail() throws Exception {
        mockMvc.perform(post("/api/call/test-fixtures/inbound-call")
                .contentType("application/json")
                .content("{\"caller\":\"13800000001\",\"callee\":\"4008001000\",\"businessTime\":\"2026-05-19T23:00:00+08:00\",\"dtmf\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalAction").value("TRANSFER_VOICEMAIL"))
                .andExpect(jsonPath("$.data.voicemailBoxCode").value("support-after-hours"));
    }
}
```

- [ ] **Step 2: Write failure simulation test**

Create `Phase3FailureSimulationTest` with assertions:

```java
mockMvc.perform(post("/api/call/test-fixtures/inbound-call")
        .contentType("application/json")
        .content("{\"caller\":\"13800000001\",\"callee\":\"4008001000\",\"businessTime\":\"2026-05-19T10:00:00+08:00\",\"dtmf\":\"9\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.finalAction").value("HANGUP"))
        .andExpect(jsonPath("$.data.trace[?(@.nodeCode == 'menu-main')]").exists());
```

Add tests for duplicate DTMF event and out-of-order hangup event. Expected assertions: duplicate DTMF creates one menu node execution, and out-of-order hangup ends the call without creating a second queue assignment.

- [ ] **Step 3: Run backend Phase 3 E2E tests**

Run:

```bash
mvn -pl och-api -am test -Dtest=Phase3InboundFlowTest,Phase3FailureSimulationTest
```

Expected output contains `BUILD SUCCESS` and Surefire reports show both E2E classes passed.

- [ ] **Step 4: Commit checkpoint**

Run:

```bash
git add och-api/src/test/java/com/opencallhub/api/e2e/Phase3InboundFlowTest.java och-api/src/test/java/com/opencallhub/api/e2e/Phase3FailureSimulationTest.java
git commit -m "test: add phase 3 inbound flow regression"
```

Expected output contains `test: add phase 3 inbound flow regression`.

### Task 10: Frontend E2E, Final Verification, and Acceptance Evidence

**Owner:** QA Expert

**Files:**
- Create: `waihu-app/e2e/phase3-call-ivr-acd.spec.ts`
- Verify: backend/frontend commands listed below

- [ ] **Step 1: Write Playwright Phase 3 E2E**

Create `waihu-app/e2e/phase3-call-ivr-acd.spec.ts`:

```ts
import { expect, test } from '@playwright/test'

test('phase 3 inbound IVR ACD acceptance path', async ({ page, request }) => {
  const inbound = await request.post('/api/call/test-fixtures/inbound-call', {
    data: { caller: '13800000001', callee: '4008001000', businessTime: '2026-05-19T10:00:00+08:00', dtmf: '1' },
  })
  expect(inbound.ok()).toBeTruthy()
  const body = await inbound.json()
  expect(body.data.finalAction).toBe('TRANSFER_SKILL_GROUP')
  expect(body.data.skillGroupCode).toBe('support-main')
  expect(body.data.assignedAgentId).toBe(9)

  await page.goto(`/ivr/executions/${body.data.ivrExecutionId}/trace`)
  await expect(page.getByText('schedule-check-main')).toBeVisible()
  await expect(page.getByText('menu-main')).toBeVisible()
  await expect(page.getByText('transfer-skill-support')).toBeVisible()

  await page.goto('/acd/monitor')
  await expect(page.getByText('support-main')).toBeVisible()
  await expect(page.getByText('Available Agents')).toBeVisible()

  const voicemail = await request.post('/api/call/test-fixtures/inbound-call', {
    data: { caller: '13800000002', callee: '4008001000', businessTime: '2026-05-19T23:00:00+08:00', dtmf: '1' },
  })
  const voicemailBody = await voicemail.json()
  expect(voicemailBody.data.finalAction).toBe('TRANSFER_VOICEMAIL')
  expect(voicemailBody.data.voicemailBoxCode).toBe('support-after-hours')
})
```

- [ ] **Step 2: Run backend full Phase 3 tests**

Run:

```bash
mvn -pl och-api,och-esl,och-call,och-ivr,och-acd -am test
```

Expected output contains `BUILD SUCCESS`. The Maven test summary or Surefire XML reports show these classes passed: `EslEventNormalizerTest`, `PhoneControlCommandServiceTest`, `CallStateMachineTest`, `CallEventIngestServiceTest`, `IvrRuntimeEngineTest`, `IvrNodeHandlerTest`, `AgentStatusStateMachineTest`, `QueueDispatchServiceTest`, `QueueMonitorServiceTest`, `Phase3InboundFlowTest`, and `Phase3FailureSimulationTest`.

- [ ] **Step 3: Run frontend unit tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- ivr-designer-store.spec.ts menu-node-panel.spec.ts queue-monitor-store.spec.ts
```

Expected output contains passing summaries for all three test files.

- [ ] **Step 4: Run frontend build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains `built in` and exits with code 0.

- [ ] **Step 5: Run Playwright Phase 3 E2E**

Start backend:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Use the Phase 1 Playwright config style in `waihu-app/playwright.config.ts`: `baseURL` is `http://127.0.0.1:5173` and `webServer.command` starts Vite with `npm run dev -- --host 127.0.0.1 --port 5173`. The Vite dev proxy must forward `/api` and `/ws` to the backend local profile at `http://127.0.0.1:8080`, so `request.post('/api/...')` reaches Spring Boot while `page.goto('/ivr/...')` loads the Vue router route from the frontend dev server.

In another terminal run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase3-call-ivr-acd.spec.ts
```

Expected output shows one passing Playwright spec named `phase 3 inbound IVR ACD acceptance path`. The browser opens `http://127.0.0.1:5173/ivr/executions/{ivrExecutionId}/trace`, renders the IVR trace rows, then opens `http://127.0.0.1:5173/acd/monitor` and renders the `support-main` queue monitor.

- [ ] **Step 6: Record manual acceptance evidence**

Capture command output or screenshots proving:

```text
Inbound call simulation with DTMF 1 returns skillGroupCode support-main and assignedAgentId 9.
Inbound call simulation at 23:00 +08:00 returns voicemailBoxCode support-after-hours.
IVR execution trace page shows schedule-check-main, menu-main, and transfer-skill-support in order.
ACD monitor page shows support-main queue metrics and updates after a new inbound simulation without browser refresh.
Voicemail message page requires voicemail recording permission before recording playback URL is shown.
```

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add waihu-app/e2e/phase3-call-ivr-acd.spec.ts
git commit -m "test: add phase 3 call ivr acd e2e"
```

Expected output contains `test: add phase 3 call ivr acd e2e`.

## Acceptance Gate

Phase 3 is accepted only when an inbound call can enter IVR, menu DTMF routes to the correct branch, non-business hours can route to voicemail, business hours can enter ACD, ACD assigns an available agent by configured strategy, IVR execution trace is viewable, and queue metrics update in real time.

## Final Phase 3 Verification Commands

Run these after all tasks and commit checkpoints are complete:

```bash
mvn -pl och-api,och-esl,och-call,och-ivr,och-acd -am test
cd e:/workspace/openCallHub/waihu-app && npm run test -- ivr-designer-store.spec.ts menu-node-panel.spec.ts queue-monitor-store.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase3-call-ivr-acd.spec.ts
```

Expected results:

```text
Backend Maven output contains BUILD SUCCESS.
Frontend unit test output contains passing summaries for IVR designer, menu node panel, and queue monitor store tests.
Frontend build exits with code 0 and prints built in.
Playwright report shows one passing spec named phase 3 inbound IVR ACD acceptance path.
```

## Self-Review

Spec coverage:
- `och-call`, `och-esl` boundary, call session, call leg, call event, call state machine, ESL event normalization, and phone control command wrappers are covered by Tasks 1, 2, 3, 8, 9, and 10.
- IVR flow/version/node/edge/variable model, IVR runtime engine, menu, schedule check, transfer voicemail, play, collect digits, condition, API call, transfer agent, transfer skill group, hangup, voicemail boxes/messages, IVR test run, and execution trace are covered by Tasks 1, 4, 6, 8, 9, and 10.
- Skill groups, queues, agent status machine, dispatch strategies, overflow rules, SLA rules, and queue monitor are covered by Tasks 1, 5, 7, 9, and 10.
- State machine tests, idempotency tests, event ordering tests, exact files, commands, expected results, and commit checkpoints are present in the task list.

Placeholder scan:
- The plan contains no deferred implementation placeholders and every task includes concrete paths, commands, expected results, or code snippets.

Type consistency:
- Shared identifiers use `traceId`, `callId`, `uuid`, `legId`, `agentId`, `ivrExecutionId`, `queueSessionId`, and `eventId` consistently across backend tests, APIs, frontend DTOs, and E2E tests.
