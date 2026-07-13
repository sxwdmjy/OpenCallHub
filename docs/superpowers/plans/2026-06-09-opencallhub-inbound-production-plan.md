# openCallHub Inbound Production Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the full production-grade inbound domain for openCallHub, covering call core, ESL, IVR, ACD, agent workspace, records, reporting, permissions, WebSocket, Mock ESL regression, and real FreeSWITCH / ESL acceptance.

**Architecture:** The implementation is a modular monolith with clear future microservice boundaries. `och-api` exposes REST facade APIs; `och-call` owns call facts and phone-bar commands; `och-esl` owns FreeSWITCH communication; `och-ivr` owns flow definitions and runtime; `och-acd` owns queueing and dispatch; `och-websocket` owns fanout; `och-customer`, `och-report`, and `och-security` provide the minimal inbound-domain customer, reporting, permission, masking, and audit support required by the production acceptance spec.

**Tech Stack:** Java 17, Spring Boot 3.5.3, Maven multi-module, MyBatis-Plus, MySQL, Redis locks, RabbitMQ/outbox abstraction, WebSocket, FreeSWITCH ESL, JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, Vue 3, TypeScript, Vite, Pinia, Axios, Ant Design Vue, LogicFlow, Vitest, Playwright.

---

## Scope Check

This specification covers multiple cooperating subsystems. It must not be implemented as one uncontrolled patch. Execute this plan in the task order below. Each task creates a working, testable slice and has its own validation command. Do not start frontend pages until the matching backend API contract exists. Do not start real FreeSWITCH acceptance until Mock ESL tests pass.

Reference spec:

- `docs/superpowers/specs/2026-06-09-opencallhub-inbound-production-design.md`

Current design decisions:

- Complete Phase 3 + Phase 4 inbound production acceptance shape.
- New domain tables replace old call / IVR / ACD / agent inbound tables.
- Old inbound-related API and page entries are replaced, not kept as long-term compatibility paths.
- Dual-channel validation: Mock / Fake ESL automated regression and real FreeSWITCH / ESL acceptance.
- Java package style remains `com.och`.

## File Structure

### Backend schema and module wiring

- Modify: `pom.xml` — ensure `och-esl`, `och-call`, `och-ivr`, `och-acd`, `och-customer`, `och-report`, `och-websocket`, `och-security`, and `och-api` are wired in the correct reactor order.
- Modify: `och-api/pom.xml` — depend on inbound-domain modules needed by the REST facade.
- Create: `och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql` — call session, leg, event, dedup, command, call record, recording record, service record.
- Create: `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql` — flow, version, node, edge, variable, execution, node execution, trace, voicemail.
- Create: `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql` — skill group, queue, agent relation, agent status, dispatch rule, overflow rule, SLA rule, queue session, metrics.
- Create: `och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql` — phone bar session, agent command, screen pop, after-call work.
- Create: `och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql` — customer profile, customer phone, inbound association seed data.
- Create: `och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql` — inbound daily, queue daily, agent daily report tables.
- Create: `och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java` — migration and seed smoke test.

### ESL and call core

- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/EslEventType.java` — normalized event enum.
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/NormalizedEslEvent.java` — normalized event payload.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventNormalizer.java` — raw header to normalized event mapper.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandService.java` — answer, playback, collect digits, bridge, hold, unhold, mute, unmute, transfer, record, hangup commands.
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallState.java` — call state enum.
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallStateMachine.java` — call transition validator.
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallEventIngestService.java` — idempotent event ingestion.
- Create: `och-call/src/main/java/com/och/call/application/InboundCallApplicationService.java` — inbound call session creation and IVR start port.
- Modify/Create: `och-call/src/main/java/com/och/call/api/CallController.java` — session, event, leg, fixture APIs.

### IVR

- Modify/Create: `och-ivr/src/main/java/com/och/ivr/domain/IvrNodeType.java` — full node enum.
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandler.java` — handler interface.
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandlerResult.java` — handler result with action and variables.
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/node/IvrNodeHandlerFactory.java` — node handler factory.
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/application/IvrRuntimeEngine.java` — runtime execution loop.
- Create: `och-ivr/src/main/java/com/och/ivr/application/IvrPublishService.java` — validate, publish, rollback versions.
- Modify/Create: `och-ivr/src/main/java/com/och/ivr/api/IvrFlowController.java` — flow and version APIs.
- Create: `och-ivr/src/main/java/com/och/ivr/api/IvrExecutionController.java` — test run and trace APIs.
- Create: `och-ivr/src/main/java/com/och/ivr/api/VoicemailController.java` — voicemail APIs.

### ACD

- Modify/Create: `och-acd/src/main/java/com/och/acd/domain/AgentState.java` — agent state enum.
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/AgentStateMachine.java` — agent transition validator.
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/QueueDispatchService.java` — enqueue and dispatch coordinator.
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/strategy/DispatchStrategy.java` — strategy interface.
- Modify/Create: `och-acd/src/main/java/com/och/acd/application/strategy/DispatchStrategyFactory.java` — strategy lookup.
- Create: `och-acd/src/main/java/com/och/acd/application/strategy/WeightedStrategy.java` — weighted dispatch.
- Create: `och-acd/src/main/java/com/och/acd/application/strategy/RecentlyMissedStrategy.java` — recently missed dispatch.
- Create: `och-acd/src/main/java/com/och/acd/application/QueueMonitorService.java` — metrics and monitor events.
- Modify/Create: `och-acd/src/main/java/com/och/acd/api/AcdConfigController.java` — skill group, queue, rule, status APIs.
- Create: `och-acd/src/main/java/com/och/acd/api/QueueMonitorController.java` — monitor APIs.

### Agent workspace, records, reporting, security

- Modify/Create: `och-call/src/main/java/com/och/call/application/PhoneBarApplicationService.java` — phone-bar command orchestration.
- Create: `och-call/src/main/java/com/och/call/application/ScreenPopApplicationService.java` — screen-pop payload builder.
- Modify/Create: `och-call/src/main/java/com/och/call/application/AfterCallWorkService.java` — after-call submission.
- Create: `och-call/src/main/java/com/och/call/application/RecordingAccessPolicy.java` — recording play/download authorization.
- Modify/Create: `och-call/src/main/java/com/och/call/api/PhoneBarController.java` — phone-bar APIs.
- Create: `och-call/src/main/java/com/och/call/api/AgentRecordController.java` — agent records APIs.
- Create: `och-customer/src/main/java/com/och/customer/application/CustomerLookupService.java` — phone lookup and masking.
- Create: `och-customer/src/main/java/com/och/customer/api/CustomerLookupController.java` — customer lookup and association APIs.
- Create: `och-report/src/main/java/com/och/report/application/InboundReportAggregationService.java` — inbound report aggregation.
- Create: `och-report/src/main/java/com/och/report/api/InboundReportController.java` — inbound report APIs.
- Modify/Create: `och-websocket/src/main/java/com/och/websocket/domain/WsMsgPayload.java` — typed WebSocket envelope.
- Create: `och-websocket/src/main/java/com/och/websocket/publisher/AgentMessagePublisher.java` — `/ws/agent` fanout.
- Create: `och-websocket/src/main/java/com/och/websocket/publisher/MonitorMessagePublisher.java` — `/ws/monitor` fanout.
- Modify: `och-security/src/main/java/com/och/security/permission/PermissionCodes.java` — inbound permission constants if this file exists; otherwise create the nearest existing permission constants class following current project style.

### Frontend

- Create: `waihu-app/src/types/call.ts` — call DTOs.
- Create: `waihu-app/src/types/ivr.ts` — IVR DTOs.
- Create: `waihu-app/src/types/acd.ts` — ACD DTOs.
- Create: `waihu-app/src/types/agent.ts` — agent workspace DTOs.
- Create: `waihu-app/src/types/records.ts` — record DTOs.
- Create: `waihu-app/src/types/report.ts` — report DTOs.
- Create: `waihu-app/src/apis/inbound/call.ts` — call API adapter.
- Create: `waihu-app/src/apis/inbound/ivr.ts` — IVR API adapter.
- Create: `waihu-app/src/apis/inbound/acd.ts` — ACD API adapter.
- Create: `waihu-app/src/apis/inbound/agent.ts` — agent API adapter.
- Create: `waihu-app/src/apis/inbound/records.ts` — records API adapter.
- Create: `waihu-app/src/apis/inbound/report.ts` — report API adapter.
- Create: `waihu-app/src/store/modules/agentWorkspace.ts` — agent workspace store.
- Create/Modify: `waihu-app/src/store/modules/ivr.ts` — IVR designer store.
- Create: `waihu-app/src/store/modules/queueMonitor.ts` — queue monitor store.
- Create: `waihu-app/src/views/Inbound/IvrFlowList/index.vue` — IVR list.
- Create: `waihu-app/src/views/Inbound/IvrDesigner/index.vue` — IVR designer.
- Create: `waihu-app/src/views/Inbound/IvrTrace/index.vue` — IVR trace.
- Create: `waihu-app/src/views/Inbound/Voicemail/index.vue` — voicemail.
- Create: `waihu-app/src/views/Inbound/AcdSkillGroup/index.vue` — skill group.
- Create: `waihu-app/src/views/Inbound/AcdQueue/index.vue` — queue config.
- Create: `waihu-app/src/views/Inbound/QueueMonitor/index.vue` — queue monitor.
- Create: `waihu-app/src/views/Inbound/AgentWorkspace/index.vue` — agent workspace shell.
- Create: `waihu-app/src/views/Inbound/Records/index.vue` — calls, recordings, services tabs.
- Create: `waihu-app/src/views/Inbound/Reports/index.vue` — inbound reports.
- Modify: existing router/menu files discovered in Task 9 to replace old inbound entries.

---

## Task 1: Schema Reset and Seed Data

**Files:**
- Create: `och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql`
- Create: `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql`
- Create: `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql`
- Create: `och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql`
- Create: `och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql`
- Create: `och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql`
- Test: `och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java`

- [ ] **Step 1: Write migration smoke test first**

Create `och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java`:

```java
package com.och.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class InboundMigrationSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inboundSchemaAndSeedRowsExist() {
        assertThat(count("call_session")).isGreaterThanOrEqualTo(0);
        assertThat(count("call_leg")).isGreaterThanOrEqualTo(0);
        assertThat(count("call_event")).isGreaterThanOrEqualTo(0);
        assertThat(count("ivr_flow")).isGreaterThanOrEqualTo(1);
        assertThat(count("ivr_flow_version")).isGreaterThanOrEqualTo(1);
        assertThat(count("ivr_node")).isGreaterThanOrEqualTo(5);
        assertThat(count("ivr_edge")).isGreaterThanOrEqualTo(4);
        assertThat(count("acd_skill_group")).isGreaterThanOrEqualTo(1);
        assertThat(count("acd_queue")).isGreaterThanOrEqualTo(1);
        assertThat(count("acd_agent_status")).isGreaterThanOrEqualTo(1);
        assertThat(count("agent_phone_bar_session")).isGreaterThanOrEqualTo(0);
        assertThat(count("customer_profile")).isGreaterThanOrEqualTo(1);
        assertThat(count("report_inbound_daily")).isGreaterThanOrEqualTo(0);
    }

    private Integer count(String tableName) {
        return jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    }
}
```

- [ ] **Step 2: Run smoke test and verify it fails before migrations exist**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected: `BUILD FAILURE` with SQL table-not-found error for `call_session` or another inbound table.

- [ ] **Step 3: Create call core migration**

Create `och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS call_session (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    direction VARCHAR(32) NOT NULL,
    caller_number VARCHAR(32),
    called_number VARCHAR(32),
    current_state VARCHAR(32) NOT NULL,
    ivr_execution_id BIGINT,
    queue_session_id BIGINT,
    agent_id BIGINT,
    started_at DATETIME NOT NULL,
    answered_at DATETIME,
    ended_at DATETIME,
    end_reason VARCHAR(128),
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_call_session_call_id (tenant_id, call_id, deleted),
    KEY idx_call_session_state (tenant_id, current_state, started_at),
    KEY idx_call_session_agent (tenant_id, agent_id, started_at),
    KEY idx_call_session_trace (tenant_id, trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫会话表';

CREATE TABLE IF NOT EXISTS call_leg (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    leg_id VARCHAR(64) NOT NULL,
    uuid VARCHAR(64) NOT NULL,
    leg_role VARCHAR(32) NOT NULL,
    endpoint VARCHAR(128),
    bridge_uuid VARCHAR(64),
    current_state VARCHAR(32) NOT NULL,
    started_at DATETIME NOT NULL,
    answered_at DATETIME,
    ended_at DATETIME,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_call_leg_uuid (tenant_id, uuid, deleted),
    KEY idx_call_leg_call (tenant_id, call_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫通话腿表';

CREATE TABLE IF NOT EXISTS call_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    event_id VARCHAR(96) NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    uuid VARCHAR(64),
    leg_id VARCHAR(64),
    event_type VARCHAR(64) NOT NULL,
    event_time DATETIME NOT NULL,
    sequence_no BIGINT NOT NULL DEFAULT 0,
    source VARCHAR(32) NOT NULL,
    normalized_payload JSON,
    raw_payload JSON,
    processing_status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_call_event_id (tenant_id, event_id),
    UNIQUE KEY uk_call_event_dedup (tenant_id, call_id, event_type, event_time, uuid),
    KEY idx_call_event_order (tenant_id, call_id, event_time, sequence_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫事件表';

CREATE TABLE IF NOT EXISTS call_event_dedup (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    dedup_key VARCHAR(240) NOT NULL,
    first_event_id VARCHAR(96) NOT NULL,
    first_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_call_event_dedup_key (tenant_id, dedup_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='呼叫事件幂等表';

CREATE TABLE IF NOT EXISTS call_command (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    command_id VARCHAR(96) NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    agent_id BIGINT,
    command_type VARCHAR(64) NOT NULL,
    command_status VARCHAR(32) NOT NULL,
    target VARCHAR(128),
    esl_correlation_id VARCHAR(96),
    failure_reason VARCHAR(512),
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_call_command_id (tenant_id, command_id),
    KEY idx_call_command_call (tenant_id, call_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电话控制命令表';

CREATE TABLE IF NOT EXISTS call_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    caller_number VARCHAR(32),
    called_number VARCHAR(32),
    agent_id BIGINT,
    queue_id BIGINT,
    answered_at DATETIME,
    ended_at DATETIME,
    talk_seconds INT DEFAULT 0,
    end_reason VARCHAR(128),
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_call_record_call (tenant_id, call_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通话记录表';

CREATE TABLE IF NOT EXISTS recording_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    storage_key VARCHAR(256) NOT NULL,
    duration_seconds INT DEFAULT 0,
    record_state VARCHAR(32) NOT NULL,
    permission_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    KEY idx_recording_call (tenant_id, call_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录音记录表';

CREATE TABLE IF NOT EXISTS service_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    agent_id BIGINT NOT NULL,
    customer_id BIGINT,
    disposition VARCHAR(64) NOT NULL,
    summary VARCHAR(1000),
    next_follow_time DATETIME,
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    KEY idx_service_record_call (tenant_id, call_id, deleted),
    KEY idx_service_record_agent (tenant_id, agent_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务记录表';
```

- [ ] **Step 4: Create IVR migration and seed flow**

Create `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql` with `ivr_flow`, `ivr_flow_version`, `ivr_node`, `ivr_edge`, `ivr_variable`, `ivr_execution`, `ivr_node_execution`, `ivr_execution_trace`, `ivr_voicemail_box`, and `ivr_voicemail_message`. Include seed rows:

```sql
INSERT INTO ivr_flow (id, tenant_id, flow_code, flow_name, published_version_id, enabled, created_at, status, deleted)
VALUES (100, 1, '4008001000', '默认呼入IVR', 1001, 1, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO ivr_flow_version (id, tenant_id, flow_id, version_no, version_state, created_at, status, deleted)
VALUES (1001, 1, 100, 1, 'PUBLISHED', CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO ivr_node (id, tenant_id, version_id, node_id, node_type, node_name, config_json, pos_x, pos_y, created_at, status, deleted)
VALUES
(101, 1, 1001, 'start', 'START', '开始', '{}', 100, 100, CURRENT_TIMESTAMP, 'ENABLED', 0),
(102, 1, 1001, 'schedule-main', 'SCHEDULE_CHECK', '服务时间判断', '{"workStart":"09:00","workEnd":"18:00"}', 300, 100, CURRENT_TIMESTAMP, 'ENABLED', 0),
(103, 1, 1001, 'menu-main', 'MENU', '主菜单', '{"retryTimes":3,"timeoutSeconds":5}', 500, 100, CURRENT_TIMESTAMP, 'ENABLED', 0),
(104, 1, 1001, 'transfer-support', 'TRANSFER_SKILL_GROUP', '转客服技能组', '{"skillGroupId":10}', 700, 60, CURRENT_TIMESTAMP, 'ENABLED', 0),
(105, 1, 1001, 'voicemail-main', 'TRANSFER_VOICEMAIL', '转语音信箱', '{"voicemailBoxId":10}', 700, 160, CURRENT_TIMESTAMP, 'ENABLED', 0),
(106, 1, 1001, 'hangup', 'HANGUP', '挂机', '{}', 900, 100, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO ivr_edge (id, tenant_id, version_id, edge_id, source_node_id, target_node_id, condition_expr, label, created_at, status, deleted)
VALUES
(201, 1, 1001, 'e-start-schedule', 'start', 'schedule-main', '', '开始', CURRENT_TIMESTAMP, 'ENABLED', 0),
(202, 1, 1001, 'e-schedule-work', 'schedule-main', 'menu-main', 'WORK_TIME', '工作时间', CURRENT_TIMESTAMP, 'ENABLED', 0),
(203, 1, 1001, 'e-schedule-nonwork', 'schedule-main', 'voicemail-main', 'NON_WORK_TIME', '非工作时间', CURRENT_TIMESTAMP, 'ENABLED', 0),
(204, 1, 1001, 'e-menu-1', 'menu-main', 'transfer-support', '1', '按1转客服', CURRENT_TIMESTAMP, 'ENABLED', 0),
(205, 1, 1001, 'e-menu-default', 'menu-main', 'voicemail-main', '', '默认语音信箱', CURRENT_TIMESTAMP, 'ENABLED', 0),
(206, 1, 1001, 'e-queue-hangup', 'transfer-support', 'hangup', '', '转接后挂机', CURRENT_TIMESTAMP, 'ENABLED', 0),
(207, 1, 1001, 'e-voicemail-hangup', 'voicemail-main', 'hangup', '', '留言后挂机', CURRENT_TIMESTAMP, 'ENABLED', 0);
```

- [ ] **Step 5: Create ACD migration and seed queue**

Create `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql` with ACD tables from the spec. Include seed rows:

```sql
INSERT INTO acd_skill_group (id, tenant_id, group_code, group_name, default_queue_id, dispatch_strategy, enabled, created_at, status, deleted)
VALUES (10, 1, 'support-main', '客服支持技能组', 20, 'IDLE_FIRST', 1, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO acd_queue (id, tenant_id, queue_code, queue_name, skill_group_id, capacity, sla_seconds, overflow_target_type, overflow_target_id, created_at, status, deleted)
VALUES (20, 1, 'support-main-queue', '客服支持队列', 10, 50, 30, 'VOICEMAIL', 10, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO acd_skill_group_agent (id, tenant_id, skill_group_id, agent_id, skill_level, weight, priority, created_at, status, deleted)
VALUES (30, 1, 10, 9, 5, 100, 1, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO acd_agent_status (id, tenant_id, agent_id, extension_no, current_state, current_calls, max_concurrency, last_state_changed_at, created_at, status, deleted)
VALUES (40, 1, 9, '8001', 'AVAILABLE', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ENABLED', 0);
```

- [ ] **Step 6: Create agent workspace, customer, and report migrations**

Create `V13`, `V14`, and `V15` with the tables listed in the File Structure section. Seed one customer with phone `13800000001` and one empty report date row for the test profile.

- [ ] **Step 7: Run migration smoke test**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected: `BUILD SUCCESS` and test output includes `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 8: Commit schema checkpoint**

```bash
git add och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java
git commit -m "feat: add inbound domain schema"
```

## Task 2: ESL Boundary and Mock Event Channel

**Files:**
- Test: `och-esl/src/test/java/com/och/esl/application/EslEventNormalizerTest.java`
- Test: `och-esl/src/test/java/com/och/esl/application/PhoneControlCommandServiceTest.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/EslEventType.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/NormalizedEslEvent.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventNormalizer.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandService.java`
- Create: `och-esl/src/main/java/com/och/esl/application/MockEslEventService.java`

- [ ] **Step 1: Write normalizer test**

Create `och-esl/src/test/java/com/och/esl/application/EslEventNormalizerTest.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EslEventNormalizerTest {

    @Test
    void normalizesDtmfHeadersWithCallIdentifiers() {
        EslEventNormalizer normalizer = new EslEventNormalizer();

        NormalizedEslEvent event = normalizer.normalize(Map.of(
                "Event-Name", "DTMF",
                "Unique-ID", "uuid-a",
                "variable_call_id", "call-100",
                "variable_leg_id", "caller-leg",
                "Caller-Caller-ID-Number", "13800000001",
                "Caller-Destination-Number", "4008001000",
                "DTMF-Digit", "1",
                "Event-Date-Timestamp", "1779148800000000"
        ));

        assertThat(event.getEventTypeEnum()).isEqualTo(EslEventType.DTMF);
        assertThat(event.getCallId()).isEqualTo("call-100");
        assertThat(event.getFreeswitchUuid()).isEqualTo("uuid-a");
        assertThat(event.getLegId()).isEqualTo("caller-leg");
        assertThat(event.getCallerNumber()).isEqualTo("13800000001");
        assertThat(event.getCalledNumber()).isEqualTo("4008001000");
        assertThat(event.getDtmfDigit()).isEqualTo("1");
        assertThat(event.getRawHeaders()).containsEntry("Event-Name", "DTMF");
    }
}
```

- [ ] **Step 2: Run normalizer test and verify failure**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventNormalizerTest
```

Expected before implementation: `BUILD FAILURE` because fields or methods are missing.

- [ ] **Step 3: Implement event enum and normalized event**

Implement `EslEventType` with values `CHANNEL_CREATE`, `CHANNEL_ANSWER`, `DTMF`, `CHANNEL_BRIDGE`, `CHANNEL_HANGUP`, `CHANNEL_DESTROY`, `CHANNEL_HOLD`, `CHANNEL_UNHOLD`, `RECORD_START`, `RECORD_STOP`, `HEARTBEAT`, `CUSTOM`.

Implement `NormalizedEslEvent` as a Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor` class with fields used by the test plus `Map<String, String> channelVars` and `LocalDateTime eventTime`.

- [ ] **Step 4: Implement normalizer**

Implement `EslEventNormalizer.normalize(Map<String, String> headers)` so it maps the headers from Step 1 and parses event name safely. Unknown names map to `CUSTOM`.

- [ ] **Step 5: Run normalizer test and verify pass**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventNormalizerTest
```

Expected: `BUILD SUCCESS` and `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 6: Write phone control command test**

Create `PhoneControlCommandServiceTest` that asserts answer, hold, unhold, blind transfer, consult transfer, record, and hangup commands call a fake command client with exact command names and include a correlation ID.

- [ ] **Step 7: Implement command service**

Implement the minimal command service methods required by Step 6. Each method returns a command result with `commandId`, `commandType`, `accepted`, and `rawCommand`.

- [ ] **Step 8: Add Mock ESL event service**

Create `MockEslEventService` with methods `publishInboundCreate`, `publishDtmf`, `publishAnswer`, `publishBridge`, `publishHangup`. Guard it with local/test profile in configuration or constructor usage so it is not active in production profile.

- [ ] **Step 9: Run ESL tests**

Run:

```bash
mvn -pl och-esl test
```

Expected: all `och-esl` tests pass.

- [ ] **Step 10: Commit ESL checkpoint**

```bash
git add och-esl/src/main/java/com/och/esl och-esl/src/test/java/com/och/esl
git commit -m "feat: add inbound esl boundary"
```

## Task 3: Call Core State and Event Ingestion

**Files:**
- Test: `och-call/src/test/java/com/och/call/application/CallStateMachineTest.java`
- Test: `och-call/src/test/java/com/och/call/application/CallEventIngestServiceTest.java`
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallState.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallStateMachine.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallEventIngestService.java`
- Create: `och-call/src/main/java/com/och/call/application/InboundCallApplicationService.java`
- Modify/Create: `och-call/src/main/java/com/och/call/api/CallController.java`

- [ ] **Step 1: Write call state machine test**

Create `CallStateMachineTest` with tests for:

```java
assertThat(machine.isValidTransition(CallState.NEW, CallState.RINGING)).isTrue();
assertThat(machine.isValidTransition(CallState.RINGING, CallState.IVR)).isTrue();
assertThat(machine.isValidTransition(CallState.IVR, CallState.QUEUEING)).isTrue();
assertThat(machine.isValidTransition(CallState.QUEUEING, CallState.ASSIGNED)).isTrue();
assertThat(machine.isValidTransition(CallState.ASSIGNED, CallState.BRIDGED)).isTrue();
assertThat(machine.isValidTransition(CallState.BRIDGED, CallState.HOLD)).isTrue();
assertThat(machine.isValidTransition(CallState.HOLD, CallState.BRIDGED)).isTrue();
assertThat(machine.isValidTransition(CallState.BRIDGED, CallState.AFTER_CALL)).isTrue();
assertThat(machine.isValidTransition(CallState.AFTER_CALL, CallState.ENDED)).isTrue();
assertThat(machine.isValidTransition(CallState.ENDED, CallState.BRIDGED)).isFalse();
```

- [ ] **Step 2: Run state machine test and verify failure**

Run:

```bash
mvn -pl och-call test -Dtest=CallStateMachineTest
```

Expected before implementation: failure for missing states or transition rules.

- [ ] **Step 3: Implement call state enum and state machine**

Implement `CallState` with `NEW`, `RINGING`, `IVR`, `QUEUEING`, `ASSIGNED`, `BRIDGED`, `HOLD`, `VOICEMAIL`, `AFTER_CALL`, `ENDED`, `FAILED`. Implement `isTerminal()` for `ENDED` and `FAILED`.

Implement `CallStateMachine` with the transitions from Step 1 plus `IVR -> VOICEMAIL`, `VOICEMAIL -> ENDED`, and failure transitions from active states to `FAILED`.

- [ ] **Step 4: Run state machine test and verify pass**

Run:

```bash
mvn -pl och-call test -Dtest=CallStateMachineTest
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Write event ingestion test**

Create `CallEventIngestServiceTest` with fake mappers or Mockito. Test that duplicate `callId + eventType + eventTime + uuid` is processed once, and an event after `ENDED` does not move the call back to `BRIDGED`.

- [ ] **Step 6: Implement idempotent event ingestion**

Implement `CallEventIngestService` so it creates sessions from create events, updates state from normalized events, ignores duplicate dedup keys, and refuses non-terminal transitions after terminal states.

- [ ] **Step 7: Add inbound call application service and controller fixture APIs**

Implement `InboundCallApplicationService.startInbound(callId, callerNumber, calledNumber, traceId)` and expose local/test fixture endpoints from `CallController` for inbound call and event injection.

- [ ] **Step 8: Run call tests**

Run:

```bash
mvn -pl och-call test
```

Expected: all `och-call` tests pass.

- [ ] **Step 9: Commit call core checkpoint**

```bash
git add och-call/src/main/java/com/och/call och-call/src/test/java/com/och/call
git commit -m "feat: add inbound call core"
```

## Task 4: IVR Flow Runtime, Publishing, and Trace

**Files:**
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrNodeHandlerTest.java`
- Test: `och-ivr/src/test/java/com/och/ivr/application/IvrRuntimeEngineTest.java`
- Create/Modify: IVR domain, mapper, service, API files listed in File Structure.

- [ ] **Step 1: Write node handler tests**

Create tests that verify:

1. `MenuNodeHandler` maps DTMF `1` to `transfer-support`.
2. `ScheduleCheckNodeHandler` maps `09:00-18:00` working time to menu and non-working time to voicemail.
3. `QueueNodeHandler` parses `{"skillGroupId":10}` into result variable `skillGroupId=10`.
4. `VoicemailNodeHandler` returns action `VOICEMAIL`.
5. `HangupNodeHandler` returns action `HANGUP`.

- [ ] **Step 2: Run handler tests and verify failure**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrNodeHandlerTest
```

Expected before implementation: missing handler behavior fails.

- [ ] **Step 3: Implement node handlers and factory**

Implement handlers with no external side effects. Media commands are represented as action results and are sent by the runtime or facade service, not inside the pure handler tests.

- [ ] **Step 4: Run handler tests and verify pass**

Run:

```bash
mvn -pl och-ivr test -Dtest=IvrNodeHandlerTest
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Write runtime engine test**

Create `IvrRuntimeEngineTest` that uses seed-like nodes and edges in memory. It verifies start -> schedule -> menu -> transfer skill group, and schedule -> voicemail for non-work time. It also verifies a trace event is written for each node transition.

- [ ] **Step 6: Implement runtime engine and execution trace service**

Implement `IvrRuntimeEngine` to load active version nodes, execute until waiting input or terminal action, record `ivr_execution`, `ivr_node_execution`, and `ivr_execution_trace`, and return a result object to the facade.

- [ ] **Step 7: Implement publish service and APIs**

Implement validate, publish, rollback, test run, execution trace, and voicemail APIs. Validation returns explicit errors for no start node, isolated node, missing menu default, missing `skillGroupId`, missing voicemail box, and missing fallback path.

- [ ] **Step 8: Run IVR tests**

Run:

```bash
mvn -pl och-ivr test
```

Expected: all `och-ivr` tests pass.

- [ ] **Step 9: Commit IVR checkpoint**

```bash
git add och-ivr/src/main/java/com/och/ivr och-ivr/src/test/java/com/och/ivr
git commit -m "feat: add inbound ivr runtime"
```

## Task 5: ACD Dispatch, Queue Monitor, and Agent State

**Files:**
- Test: `och-acd/src/test/java/com/och/acd/application/AgentStateMachineTest.java`
- Test: `och-acd/src/test/java/com/och/acd/application/QueueDispatchServiceTest.java`
- Test: `och-acd/src/test/java/com/och/acd/application/QueueMonitorServiceTest.java`
- Modify/Create: ACD files listed in File Structure.

- [ ] **Step 1: Write agent state machine test**

Test transitions `OFFLINE -> ONLINE -> AVAILABLE -> RESERVED -> BUSY -> AFTER_CALL -> AVAILABLE`, `AVAILABLE -> BREAK -> AVAILABLE`, and invalid `OFFLINE -> BUSY`.

- [ ] **Step 2: Implement agent state machine**

Implement `AgentState` and `AgentStateMachine` with Chinese comments explaining why `RESERVED` blocks duplicate assignment before bridge confirmation.

- [ ] **Step 3: Write dispatch strategy tests**

Test idle first, round robin, least calls, skill priority, weighted, and recently missed selection with deterministic agent lists.

- [ ] **Step 4: Implement dispatch strategies**

Implement each strategy as a separate focused class. `DispatchStrategyFactory` maps strategy code to implementation and fails with a business exception for unknown codes.

- [ ] **Step 5: Write queue dispatch service test**

Test enqueue creates a queue session, dispatch reserves one available agent, duplicate callId does not create a second waiting session, and no available agent leaves session in `WAITING`.

- [ ] **Step 6: Implement queue dispatch service**

Implement enqueue, tryDispatch, reserve, bridge confirmation, release to after-call, overflow, and queue stats. Use transaction boundaries and a Redis lock or database uniqueness for callId idempotency.

- [ ] **Step 7: Write queue monitor test**

Test waiting count, available agent count, average wait seconds, max wait seconds, SLA warning flag, and WebSocket monitor payload.

- [ ] **Step 8: Implement queue monitor service and APIs**

Implement `/api/acd/queues/{queueId}/monitor` and monitor event publication.

- [ ] **Step 9: Run ACD tests**

Run:

```bash
mvn -pl och-acd test
```

Expected: all `och-acd` tests pass.

- [ ] **Step 10: Commit ACD checkpoint**

```bash
git add och-acd/src/main/java/com/och/acd och-acd/src/test/java/com/och/acd
git commit -m "feat: add inbound acd dispatch"
```

## Task 6: Agent Workspace Backend and Recording Security

**Files:**
- Test: `och-call/src/test/java/com/och/call/application/PhoneBarApplicationServiceTest.java`
- Test: `och-call/src/test/java/com/och/call/application/AfterCallWorkServiceTest.java`
- Test: `och-call/src/test/java/com/och/call/application/RecordingAccessPolicyTest.java`
- Modify/Create: `PhoneBarApplicationService`, `ScreenPopApplicationService`, `AfterCallWorkService`, `RecordingAccessPolicy`, `PhoneBarController`, `AgentRecordController`.
- Create/Modify: customer lookup files.

- [ ] **Step 1: Write phone bar tests**

Test online, offline, break start/end, answer, hold, unhold, blind transfer, consult transfer, hangup, duplicate command idempotency, and invalid state command rejection.

- [ ] **Step 2: Implement phone bar service and controller**

Implement command acceptance records first, then command dispatch to `PhoneControlCommandService`. HTTP success returns accepted command state, not final call state.

- [ ] **Step 3: Write screen pop and customer lookup tests**

Test screen pop contains caller, called number, IVR trace summary, queue name, wait seconds, masked customer phone, and customer id when found by phone.

- [ ] **Step 4: Implement screen pop and customer lookup**

Implement lookup by phone, masking rules, and screen-pop payload builder. Do not include real recording URI or unauthorized customer fields.

- [ ] **Step 5: Write after-call tests**

Test after-call submit creates service record, associates customer, moves agent to available, and rejects submit when call is not in `AFTER_CALL` state.

- [ ] **Step 6: Implement after-call service**

Implement after-call validation, service record creation, customer association, agent state release, and audit event.

- [ ] **Step 7: Write recording access tests**

Test play token requires `record:recording:play`, download token requires `record:recording:download`, and each successful token generation writes an audit record.

- [ ] **Step 8: Implement recording access policy and record APIs**

Implement metadata-only list, play token, download token, and audit hooks.

- [ ] **Step 9: Run workspace backend tests**

Run:

```bash
mvn -pl och-call,och-customer -am test -Dtest=PhoneBarApplicationServiceTest,AfterCallWorkServiceTest,RecordingAccessPolicyTest
```

Expected: tests pass.

- [ ] **Step 10: Commit workspace backend checkpoint**

```bash
git add och-call/src/main/java/com/och/call och-call/src/test/java/com/och/call och-customer/src/main/java/com/och/customer och-customer/src/test/java/com/och/customer
git commit -m "feat: add inbound agent workspace backend"
```

## Task 7: WebSocket Fanout and Inbound Reporting

**Files:**
- Test: `och-websocket/src/test/java/com/och/websocket/WebSocketFanoutTest.java`
- Test: `och-report/src/test/java/com/och/report/application/InboundReportAggregationServiceTest.java`
- Create/Modify: WebSocket publisher files and report files listed in File Structure.

- [ ] **Step 1: Write WebSocket fanout test**

Test `/ws/agent` sends screen-pop only to assigned agent, `/ws/monitor` sends queue metric only to users authorized for the skill group, and sensitive payload fields are removed.

- [ ] **Step 2: Implement typed WebSocket envelope and publishers**

Implement `WsMsgPayload`, `AgentMessagePublisher`, `MonitorMessagePublisher`, `NoticeMessagePublisher`, and `TaskMessagePublisher`. Each publisher accepts a domain event and produces one typed message envelope.

- [ ] **Step 3: Write report aggregation test**

Test daily inbound metrics from call records and queue sessions: inbound count, answered count, missed count, average wait seconds, average talk seconds, average after-call seconds, transfer count, hold count, SLA rate.

- [ ] **Step 4: Implement inbound report aggregation**

Implement direct recalculation by date and scheduled aggregation. Recalculation writes `report_inbound_daily`, `report_queue_daily`, and `report_agent_inbound_daily`.

- [ ] **Step 5: Implement report APIs**

Implement daily, queue daily, agent daily, and recalculate APIs.

- [ ] **Step 6: Run WebSocket and report tests**

Run:

```bash
mvn -pl och-websocket,och-report -am test
```

Expected: tests pass.

- [ ] **Step 7: Commit WebSocket and report checkpoint**

```bash
git add och-websocket/src/main/java/com/och/websocket och-websocket/src/test/java/com/och/websocket och-report/src/main/java/com/och/report och-report/src/test/java/com/och/report
git commit -m "feat: add inbound websocket and reports"
```

## Task 8: API Facade and Backend End-to-End Tests

**Files:**
- Test: `och-api/src/test/java/com/och/api/e2e/Phase3InboundFlowTest.java`
- Test: `och-api/src/test/java/com/och/api/e2e/Phase4AgentInboundFlowTest.java`
- Test: `och-api/src/test/java/com/och/api/e2e/Phase4SecurityAccessTest.java`
- Modify/Create: `och-api/src/main/java/com/och/api/controller/**` inbound controllers.
- Modify/Create: `och-api/src/main/java/com/och/api/service/impl/CallFlowServiceImpl.java`.

- [ ] **Step 1: Write Phase 3 backend E2E test**

Test fixture creates inbound call `call-e2e-1`, injects DTMF `1`, expects IVR trace includes `menu-main`, call state becomes `QUEUEING` or `ASSIGNED`, and ACD queue session references skill group 10.

- [ ] **Step 2: Implement API facade for inbound start and DTMF fixture**

Implement call-flow facade so Mock ESL and real ESL events use the same ingestion path.

- [ ] **Step 3: Write Phase 4 agent flow E2E test**

Test agent 9 goes online, receives screen pop, answers, holds, unholds, hangs up, submits after-call work, and sees call record, recording metadata, and service record.

- [ ] **Step 4: Implement Phase 4 facade and controllers**

Wire phone-bar, screen-pop, after-call, records, customer association, WebSocket fanout, and report event publication.

- [ ] **Step 5: Write security E2E test**

Test unauthorized user cannot read another agent's screen pop, cannot view unauthorized skill group monitor, cannot play recording without permission, and cannot download recording without download permission.

- [ ] **Step 6: Implement permission checks and audit hooks**

Implement inbound permission constants, controller method guards, data range checks, masking, and audit records.

- [ ] **Step 7: Run API E2E tests**

Run:

```bash
mvn -pl och-api -am test -Dtest=Phase3InboundFlowTest,Phase4AgentInboundFlowTest,Phase4SecurityAccessTest
```

Expected: all three tests pass.

- [ ] **Step 8: Commit API facade checkpoint**

```bash
git add och-api/src/main/java/com/och/api och-api/src/test/java/com/och/api/e2e och-security/src/main/java/com/och/security
git commit -m "feat: add inbound api facade"
```

## Task 9: Frontend IVR, ACD, Agent Workspace, Records, and Reports

**Files:**
- Create/Modify: frontend files listed in File Structure.
- Test: `waihu-app/src/tests/agent-workspace-store.spec.ts`
- Test: `waihu-app/src/tests/ivr-designer-store.spec.ts`
- Test: `waihu-app/src/tests/queue-monitor-store.spec.ts`
- Test: `waihu-app/e2e/phase-inbound-production.spec.ts`

- [ ] **Step 1: Write frontend store tests**

Create store tests for `agentWorkspaceStore`, `ivrDesignerStore`, and `queueMonitorStore`. Tests must verify WebSocket message merge, command pending state, IVR validation errors, and queue metric updates.

- [ ] **Step 2: Implement types and API adapters**

Implement `waihu-app/src/types/*.ts` and `waihu-app/src/apis/inbound/*.ts` with exact REST paths from this plan.

- [ ] **Step 3: Implement stores**

Implement agent workspace, IVR designer, and queue monitor stores. Stores own state merging; Vue components only render and call actions.

- [ ] **Step 4: Run store tests**

Run:

```bash
cd waihu-app && npm run test -- agent-workspace-store ivr-designer-store queue-monitor-store
```

Expected: tests pass. If this project uses a different Vitest script name, inspect `waihu-app/package.json` and run the existing test script with these test files.

- [ ] **Step 5: Implement IVR pages**

Implement flow list, designer, trace, and voicemail pages. Designer must show validation errors returned by `/api/ivr/versions/{versionId}/validate`.

- [ ] **Step 6: Implement ACD pages**

Implement skill group, queue config, and queue monitor pages. Queue monitor subscribes to `/ws/monitor` and falls back to REST refresh.

- [ ] **Step 7: Implement agent workspace**

Implement phone bar, screen pop, customer card, call control panel, transfer panel, event timeline, after-call panel, and record drawers.

- [ ] **Step 8: Implement records and reports pages**

Implement calls, recordings, services, inbound daily, queue daily, and agent daily views. Recording play/download buttons must reflect permission fields.

- [ ] **Step 9: Replace old inbound menu routes**

Modify existing route and menu files so official navigation points to new `waihu-app/src/views/Inbound/**` pages instead of old call / IVR / ACD / agent entries.

- [ ] **Step 10: Write Playwright E2E**

Create `waihu-app/e2e/phase-inbound-production.spec.ts` with these scenarios: IVR publish/test run, ACD monitor, agent inbound flow, recording permission view.

- [ ] **Step 11: Run frontend build and tests**

Run:

```bash
cd waihu-app && npm run build
```

Expected: production build succeeds. Then run the existing frontend unit and E2E commands from `waihu-app/package.json`.

- [ ] **Step 12: Commit frontend checkpoint**

```bash
git add waihu-app/src/types waihu-app/src/apis/inbound waihu-app/src/store/modules waihu-app/src/views/Inbound waihu-app/src/tests waihu-app/e2e
git commit -m "feat: add inbound frontend workspace"
```

## Task 10: Real FreeSWITCH / ESL Acceptance and Final Gate

**Files:**
- Create: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-fs-acceptance.md`
- Modify: `och-api/src/main/resources/application.yml`
- Modify/Create: `och-esl/src/main/java/com/och/esl/config/EslConfigurationProperties.java`
- Test: backend and frontend commands from previous tasks.

- [ ] **Step 1: Create real FS acceptance checklist document**

Create `docs/superpowers/plans/2026-06-09-opencallhub-inbound-fs-acceptance.md` with the 12 checklist items from the spec: extension registration, inbound call, event receipt, DTMF route, ACD assignment, screen pop, answer, hold/unhold, transfer, hangup, recording, after-call, reports, recording permission.

- [ ] **Step 2: Add ESL runtime configuration**

Ensure `application.yml` has profile-based properties for real ESL host, port, password, timeout, mock enabled flag, recording directory, playback directory, WebSocket heartbeat, recording token TTL, SLA threshold, and masking rules.

- [ ] **Step 3: Run full backend verification**

Run:

```bash
mvn -pl och-api -am test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Run full frontend verification**

Run:

```bash
cd waihu-app && npm run build
```

Expected: build succeeds. Run unit and E2E scripts listed in `waihu-app/package.json` and record outputs in the acceptance document.

- [ ] **Step 5: Execute real FS acceptance checklist**

Follow the checklist document against a real FreeSWITCH / ESL environment. Record pass/fail, command output, callId, uuid, agentId, and screenshots or logs for each item.

- [ ] **Step 6: Fix defects found by acceptance**

For each failed checklist item, add or update a focused automated test first, reproduce the failure, implement the fix, run the test, and update the checklist evidence.

- [ ] **Step 7: Commit final acceptance checkpoint**

```bash
git add docs/superpowers/plans/2026-06-09-opencallhub-inbound-fs-acceptance.md och-api/src/main/resources/application.yml och-esl/src/main/java/com/och/esl/config/EslConfigurationProperties.java
git commit -m "test: add inbound fs acceptance evidence"
```

## Final Verification Commands

Run these before declaring implementation complete:

```bash
mvn -pl och-api -am test
cd waihu-app && npm run build
```

Also run the existing frontend unit and E2E scripts from `waihu-app/package.json`. The real FreeSWITCH / ESL checklist must be completed with evidence before production acceptance.

## Self-Review

### Spec coverage

- Communication and call core: covered by Tasks 2, 3, 8, 10.
- IVR flow, runtime, trace, voicemail: covered by Tasks 1, 4, 8, 9, 10.
- ACD queue, dispatch, monitor: covered by Tasks 1, 5, 8, 9, 10.
- Agent workspace, phone bar, after-call: covered by Tasks 1, 6, 8, 9, 10.
- Records, recording, customer association: covered by Tasks 1, 6, 8, 9, 10.
- Reports: covered by Tasks 1, 7, 9, 10.
- Permissions, masking, audit: covered by Tasks 6, 8, 9, 10.
- Mock ESL and real FS acceptance: covered by Tasks 2, 8, 10.

### Placeholder scan

This plan intentionally avoids placeholder terms and gives exact files, commands, expected outcomes, and acceptance gates. Implementation details that depend on current class availability are constrained to existing package patterns and concrete method responsibilities.

### Type consistency

REST paths, WebSocket channels, state names, permission codes, seed identifiers, `callId`, `uuid`, `legId`, `agentId`, `skillGroupId`, `queueId`, `traceId`, and `eventId` are consistent with the approved spec.
