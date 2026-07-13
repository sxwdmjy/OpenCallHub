# openCallHub Phase 4 Agent Workspace and Inbound Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Phase 4 agent workspace and inbound closure so an agent can go online, receive a screen pop, control the call, finish after-call work, and view linked call, recording, and service records.

**Architecture:** Phase 4 builds directly on Phase 1 foundations, Phase 2 permissions/resources, and Phase 3 call/IVR/ACD modules. Backend work extends `och-acd`, `och-call`, `och-esl`, `och-websocket`, `och-customer`, `och-report`, and `och-api` with agent status persistence, phone-bar command APIs, agent WebSocket events, after-call work, records, simple customer association, recording metadata, service records, and inbound aggregation. Frontend work creates an immersive Vue 3 agent workspace under `waihu-app/src/views/agent` with a phone bar, screen pop, customer card, call controls, transfer panels, after-call form, record drawers, and high-frequency WebSocket-driven updates.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Web MVC, Flyway, MySQL, Redis locks from Phase 1, RabbitMQ/outbox abstraction from Phase 1, FreeSWITCH ESL command ports from Phase 3, JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, Vue 3, TypeScript, Vite, Pinia, Axios, Ant Design Vue, Vitest, Playwright.

---

## Agentic Worker Note

This is planning-only guidance. Execute it only after Phase 1, Phase 2, and Phase 3 acceptance gates pass. Use an isolated worktree at execution time, keep source changes scoped to Phase 4, and do not mix outbound, AI robot, ticket, knowledge, quality, compliance, or production-hardening implementation into these checkpoints. Commit commands below are execution checkpoints for implementers; this planning task does not run them.

## Goal

Phase 4 implements:

- Agent status state machine and state persistence for phone-bar availability and ACD availability.
- Phone bar APIs for online/offline/break/answer/hangup/hold/unhold/mute/unmute/blind transfer/consult transfer.
- WebSocket agent events for status changes, screen pop, ringing, bridge, hold, mute, transfer, hangup, and after-call transitions.
- Screen pop, customer card, call controls, transfer panels, and after-call work inside an immersive agent workspace.
- Call record, recording record, service record, and simple customer association for inbound calls.
- Inbound report aggregation for service volume, answered calls, missed calls, average ring seconds, average talk seconds, average after-call seconds, hold count, transfer count, and agent availability.
- Backend, frontend, security, and E2E tests proving the accepted inbound journey.

## Architecture

### Backend boundaries

- `och-acd` remains the owner of agent availability. Phase 4 extends the Phase 3 `AgentStatusStateMachine` so `AVAILABLE` drives ACD eligibility, `RESERVED` and `BUSY` block new assignment, `AFTER_CALL` blocks assignment until after-call submission, and `OFFLINE`/`BREAK` block assignment.
- `och-call` owns phone-bar session state, call controls, call record, recording record, service record, customer association, and inbound report event publication. It records command acceptance immediately and waits for Phase 3 ESL/call events for final truth.
- `och-esl` owns command formatting and FreeSWITCH command dispatch for answer, hangup, hold, unhold, mute, unmute, bridge, blind transfer, and consult transfer. It does not persist business records.
- `och-websocket` owns `/ws/agent` authenticated fanout. Agent messages are filtered by `agentId`, assigned call, supervisor scope, and permission code.
- `och-customer` is introduced as a narrow Phase 4 customer lookup/association module only: customer profile, customer phone, and inbound association. Full customer pool/import/workflow features remain outside Phase 4.
- `och-report` is introduced with only inbound daily aggregation and query APIs required by Phase 4. Broader reporting remains outside Phase 4.

### Frontend boundaries

- `waihu-app/src/views/agent/AgentWorkspaceView.vue` is the immersive workspace route and owns page layout composition only.
- `waihu-app/src/components/agent/PhoneBar.vue` owns status display and high-frequency call controls.
- `waihu-app/src/components/agent/ScreenPopPanel.vue` owns inbound alert, caller/callee metadata, queue, IVR trace summary, and answer action.
- `waihu-app/src/components/agent/CustomerCard.vue` owns customer lookup, simple association, and masked customer fields.
- `waihu-app/src/components/agent/CallControlPanel.vue` owns hold/unhold/mute/unmute/hangup and visible command state.
- `waihu-app/src/components/agent/TransferPanel.vue` owns blind transfer and consult transfer inputs/actions.
- `waihu-app/src/components/agent/AfterCallWorkPanel.vue` owns disposition, service summary, customer association, and after-call submission.
- `waihu-app/src/components/agent/CallRecordDrawer.vue`, `RecordingRecordDrawer.vue`, and `ServiceRecordDrawer.vue` show post-call records without navigating away from the workspace.
- `waihu-app/src/stores/agentWorkspace.ts` is the single Pinia store receiving `/ws/agent` events and merging REST command results.

### Role owners

- Product Expert: agent journey and after-call workflow
- Frontend Expert: immersive workspace and high-frequency interactions
- Backend Expert: phone bar API and state persistence
- Communications Expert: bridge, hold, transfer, hangup event correctness
- QA Expert: inbound E2E regression
- Security Expert: customer and recording visibility

## Tech Stack

- Backend runtime: Java 17 and Spring Boot from Phase 1.
- Backend persistence: Flyway SQL migrations and MySQL tables with common `id`, `tenant_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `status`, and `remark` columns where applicable.
- Backend communication: Phase 3 `PhoneControlCommandService`, normalized ESL events, call state machine, and ACD agent state machine.
- Backend events: Phase 1 RabbitMQ/outbox abstraction with `eventId`, idempotent consumers, retry queue, and dead-letter queue behavior.
- Backend concurrency: Redis locks from Phase 1 for phone-bar command serialization using lock key `phonebar:call:{callId}`.
- Frontend runtime: Vue 3, TypeScript, Vue Router, Pinia, Axios, Ant Design Vue, and Phase 1 layout/pattern components.
- Frontend tests: Vitest for stores/components/API adapters and Playwright for the Phase 4 inbound acceptance path.

## File Structure

### Backend files to create

- `och-call/src/main/resources/db/migration/V7__agent_inbound_schema.sql`: phone-bar session, agent call command, call record, recording record, service record, and inbound customer association schema.
- `och-customer/pom.xml`: narrow customer module for Phase 4 lookup and association.
- `och-customer/src/main/resources/db/migration/V8__customer_phase4_schema.sql`: `customer_profile` and `customer_phone` fixtures for screen pop/customer card.
- `och-report/pom.xml`: narrow report module for Phase 4 inbound aggregation.
- `och-report/src/main/resources/db/migration/V9__inbound_report_schema.sql`: `report_inbound_daily` and `report_agent_inbound_daily` aggregation tables.
- `och-acd/src/main/java/com/opencallhub/acd/application/AgentAvailabilityService.java`: persists status transitions and exposes ACD eligibility.
- `och-call/src/main/java/com/opencallhub/call/domain/PhoneBarSession.java`: current agent/call phone-bar state.
- `och-call/src/main/java/com/opencallhub/call/domain/PhoneBarCommand.java`: accepted command record.
- `och-call/src/main/java/com/opencallhub/call/domain/CallControlState.java`: enum `IDLE`, `RINGING`, `TALKING`, `HELD`, `MUTED`, `TRANSFERRING`, `AFTER_CALL`, `ENDED`.
- `och-call/src/main/java/com/opencallhub/call/domain/CallRecord.java`: post-call record projection.
- `och-call/src/main/java/com/opencallhub/call/domain/RecordingRecord.java`: recording metadata projection with storage URI hidden from unauthorized users.
- `och-call/src/main/java/com/opencallhub/call/domain/ServiceRecord.java`: after-call service record.
- `och-call/src/main/java/com/opencallhub/call/application/PhoneBarApplicationService.java`: status and command orchestration.
- `och-call/src/main/java/com/opencallhub/call/application/AgentEventApplicationService.java`: converts call/ACD events into agent WebSocket messages.
- `och-call/src/main/java/com/opencallhub/call/application/ScreenPopApplicationService.java`: builds screen-pop payload from call, IVR, ACD, and customer modules.
- `och-call/src/main/java/com/opencallhub/call/application/AfterCallWorkService.java`: validates and persists after-call work.
- `och-call/src/main/java/com/opencallhub/call/application/CallRecordService.java`: creates and queries call records.
- `och-call/src/main/java/com/opencallhub/call/application/RecordingRecordService.java`: creates and queries recording records.
- `och-call/src/main/java/com/opencallhub/call/application/ServiceRecordService.java`: creates and queries service records.
- `och-call/src/main/java/com/opencallhub/call/api/PhoneBarController.java`: `/api/agent/phone-bar/**` APIs.
- `och-call/src/main/java/com/opencallhub/call/api/AgentRecordController.java`: `/api/agent/records/**` APIs.
- `och-customer/src/main/java/com/opencallhub/customer/domain/CustomerProfile.java`: Phase 4 customer profile record.
- `och-customer/src/main/java/com/opencallhub/customer/domain/CustomerPhone.java`: Phase 4 customer phone record.
- `och-customer/src/main/java/com/opencallhub/customer/application/CustomerLookupService.java`: lookup by phone with masking and data scope.
- `och-customer/src/main/java/com/opencallhub/customer/application/CustomerAssociationService.java`: associates a call/service record with a customer.
- `och-customer/src/main/java/com/opencallhub/customer/api/CustomerLookupController.java`: `/api/customer/lookup/by-phone` and `/api/customer/associations` APIs.
- `och-report/src/main/java/com/opencallhub/report/application/InboundReportAggregationService.java`: daily inbound aggregation job and direct recalculation.
- `och-report/src/main/java/com/opencallhub/report/api/InboundReportController.java`: `/api/report/inbound/daily` and `/api/report/inbound/agents/daily` APIs.
- `och-websocket/src/main/java/com/opencallhub/websocket/agent/AgentMessagePublisher.java`: publishes authenticated `/ws/agent` messages.
- `och-websocket/src/main/java/com/opencallhub/websocket/agent/AgentWebSocketMessage.java`: typed message envelope.
- `och-api/src/main/java/com/opencallhub/api/testfixture/InboundCallTestFixtureController.java`: `local` and `test` profile controller for `POST /api/call/test-fixtures/inbound-call` used by Phase 4 backend and Playwright E2E tests.

### Backend test files to create

- `och-acd/src/test/java/com/opencallhub/acd/application/AgentAvailabilityServiceTest.java`: agent state transitions and ACD eligibility.
- `och-call/src/test/java/com/opencallhub/call/application/PhoneBarApplicationServiceTest.java`: online/offline and phone command acceptance.
- `och-call/src/test/java/com/opencallhub/call/application/AgentEventApplicationServiceTest.java`: screen pop and call-state WebSocket messages.
- `och-call/src/test/java/com/opencallhub/call/application/AfterCallWorkServiceTest.java`: after-call validation, service record creation, and state release.
- `och-call/src/test/java/com/opencallhub/call/application/CallRecordServiceTest.java`: call, recording, and service records are linked by `callId`.
- `och-call/src/test/java/com/opencallhub/call/api/PhoneBarControllerTest.java`: MockMvc contract tests for phone-bar APIs.
- `och-customer/src/test/java/com/opencallhub/customer/application/CustomerLookupServiceTest.java`: masked lookup and association behavior.
- `och-report/src/test/java/com/opencallhub/report/application/InboundReportAggregationServiceTest.java`: inbound aggregation metrics.
- `och-api/src/test/java/com/opencallhub/api/e2e/Phase4AgentInboundFlowTest.java`: backend E2E for online, screen pop, answer, hold, transfer, hangup, after-call work, records.
- `och-api/src/test/java/com/opencallhub/api/e2e/Phase4SecurityAccessTest.java`: customer and recording visibility regression.
- `och-api/src/test/java/com/opencallhub/api/testfixture/InboundCallTestFixtureControllerTest.java`: test-fixture endpoint contract for creating a deterministic inbound call assigned to agent 9.

### Backend files to modify

- `pom.xml`: add `och-customer` and `och-report` after `och-acd`.
- `och-api/pom.xml`: add dependencies on `och-customer` and `och-report`.
- `och-api/src/main/resources/application.yml`: include Phase 4 migration locations if module classpath scanning does not include them.
- `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`: add hold, unhold, mute, unmute, blind transfer, consult transfer, and consult complete wrappers.
- `och-acd/src/main/java/com/opencallhub/acd/application/AgentStatusStateMachine.java`: add Phase 4 events `GO_ONLINE`, `GO_OFFLINE`, `START_BREAK`, `END_BREAK`, `ANSWER_CALL`, `CALL_HELD`, `CALL_UNHELD`, `CALL_ENDED`, `AFTER_CALL_SUBMITTED` if absent from Phase 3.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`: add Phase 4 permission constants.
- `och-websocket/src/main/java/com/opencallhub/websocket/config/WebSocketConfig.java`: register `/ws/agent` if Phase 1 only registered wrapper infrastructure.

### Frontend files to create

- `waihu-app/src/types/agent.ts`: phone-bar state, commands, WebSocket events, after-call work, and agent workspace DTOs.
- `waihu-app/src/types/customer.ts`: Phase 4 customer card and association DTOs.
- `waihu-app/src/types/records.ts`: call record, recording record, and service record DTOs.
- `waihu-app/src/types/report.ts`: inbound report DTOs.
- `waihu-app/src/services/agentApi.ts`: typed client for `/api/agent/phone-bar/**` and `/api/agent/records/**`.
- `waihu-app/src/services/customerApi.ts`: typed client for `/api/customer/lookup/by-phone` and associations.
- `waihu-app/src/services/reportApi.ts`: typed client for inbound report APIs.
- `waihu-app/src/stores/agentWorkspace.ts`: phone state, active call, screen pop, customer, command pending state, transfer state, after-call state, and record drawers.
- `waihu-app/src/views/agent/AgentWorkspaceView.vue`: immersive agent workspace page.
- `waihu-app/src/components/agent/PhoneBar.vue`: online/offline/break and active call state display.
- `waihu-app/src/components/agent/ScreenPopPanel.vue`: inbound screen pop with answer action.
- `waihu-app/src/components/agent/CustomerCard.vue`: customer identity, masked phone, association selector.
- `waihu-app/src/components/agent/CallControlPanel.vue`: answer/hangup/hold/unhold/mute/unmute controls.
- `waihu-app/src/components/agent/TransferPanel.vue`: blind transfer and consult transfer controls.
- `waihu-app/src/components/agent/AfterCallWorkPanel.vue`: disposition, summary, next action, and submit button.
- `waihu-app/src/components/agent/CallRecordDrawer.vue`: call record detail.
- `waihu-app/src/components/agent/RecordingRecordDrawer.vue`: recording record detail with permission-aware play/download state.
- `waihu-app/src/components/agent/ServiceRecordDrawer.vue`: service record detail.
- `waihu-app/src/views/report/InboundReportView.vue`: Phase 4 inbound report page.

### Frontend files to modify

- `waihu-app/src/router/routes.ts`: add `/agent/workspace` and `/report/inbound` route records with Phase 4 permission metadata.
- `waihu-app/src/services/wsClient.ts`: add `/ws/agent` subscription helper if the Phase 1 wrapper only has generic connection functions.
- `waihu-app/src/components/menu/AppMenu.vue`: render agent workspace and inbound report entries from Phase 2 menu metadata.

### Frontend test files to create

- `waihu-app/src/tests/agent-workspace-store.spec.ts`: WebSocket event merge and command state.
- `waihu-app/src/tests/phone-bar.spec.ts`: phone bar status and control rendering.
- `waihu-app/src/tests/screen-pop-panel.spec.ts`: screen pop answer behavior.
- `waihu-app/src/tests/after-call-work-panel.spec.ts`: after-call validation and submit payload.
- `waihu-app/src/tests/customer-card.spec.ts`: masked customer and association behavior.
- `waihu-app/e2e/phase4-agent-inbound.spec.ts`: Playwright inbound agent acceptance path.

## Bite-Sized Tasks

### Task 1: Phase 4 Module Wiring and Migrations

**Owner:** Backend Expert

**Files:**
- Modify: `pom.xml`
- Modify: `och-api/pom.xml`
- Create: `och-customer/pom.xml`
- Create: `och-report/pom.xml`
- Create: `och-call/src/main/resources/db/migration/V7__agent_inbound_schema.sql`
- Create: `och-customer/src/main/resources/db/migration/V8__customer_phase4_schema.sql`
- Create: `och-report/src/main/resources/db/migration/V9__inbound_report_schema.sql`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`

- [ ] **Step 1: Verify Phase 1 through Phase 3 gates before source changes**

Run from `e:/workspace/openCallHub`:

```bash
mvn -pl och-api,och-system,och-resource,och-esl,och-call,och-ivr,och-acd -am test
cd e:/workspace/openCallHub/waihu-app && npm run test
```

Expected output contains `BUILD SUCCESS` for Maven and a passing frontend test summary. If either command fails because Phase 1, Phase 2, or Phase 3 files are missing, stop and finish those phases before starting this plan.

- [ ] **Step 2: Add Phase 4 Maven modules**

Modify the root `pom.xml` module list so it contains these entries after `och-acd`:

```xml
<module>och-customer</module>
<module>och-report</module>
```

Create `och-customer/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.opencallhub</groupId>
        <artifactId>opencallhub</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>och-customer</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-infra</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

Create `och-report/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.opencallhub</groupId>
        <artifactId>opencallhub</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>och-report</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-infra</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-call</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-acd</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

Modify `och-api/pom.xml` so it depends on both modules:

```xml
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-customer</artifactId>
</dependency>
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-report</artifactId>
</dependency>
```

Run:

```bash
mvn -pl och-customer,och-report -am test -DskipTests
```

Expected output contains `BUILD SUCCESS` and no `Could not find artifact` errors.

- [ ] **Step 3: Create agent inbound schema**

Create `och-call/src/main/resources/db/migration/V7__agent_inbound_schema.sql`:

```sql
CREATE TABLE agent_phone_bar_session (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    agent_id BIGINT NOT NULL,
    extension_no VARCHAR(32) NOT NULL,
    current_call_id VARCHAR(64),
    agent_state VARCHAR(32) NOT NULL,
    control_state VARCHAR(32) NOT NULL,
    last_state_changed_at DATETIME NOT NULL,
    trace_id VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_agent_phone_bar_agent (tenant_id, agent_id, deleted),
    KEY idx_agent_phone_bar_state (tenant_id, agent_state, control_state)
);

CREATE TABLE agent_call_command (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    command_id VARCHAR(96) NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    agent_id BIGINT NOT NULL,
    command_type VARCHAR(64) NOT NULL,
    target_agent_id BIGINT,
    target_extension_no VARCHAR(32),
    command_status VARCHAR(32) NOT NULL,
    esl_correlation_id VARCHAR(96),
    failure_reason VARCHAR(300),
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agent_call_command_id (tenant_id, command_id),
    KEY idx_agent_call_command_call (tenant_id, call_id, created_at)
);

CREATE TABLE call_record (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    direction VARCHAR(32) NOT NULL,
    caller VARCHAR(32) NOT NULL,
    callee VARCHAR(32) NOT NULL,
    agent_id BIGINT,
    extension_no VARCHAR(32),
    customer_id BIGINT,
    queue_session_id BIGINT,
    started_at DATETIME NOT NULL,
    answered_at DATETIME,
    ended_at DATETIME,
    ring_seconds INT NOT NULL DEFAULT 0,
    talk_seconds INT NOT NULL DEFAULT 0,
    hold_seconds INT NOT NULL DEFAULT 0,
    hold_count INT NOT NULL DEFAULT 0,
    transfer_count INT NOT NULL DEFAULT 0,
    end_reason VARCHAR(120),
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_call_record_call_id (tenant_id, call_id, deleted),
    KEY idx_call_record_agent_time (tenant_id, agent_id, started_at),
    KEY idx_call_record_customer_time (tenant_id, customer_id, started_at)
);

CREATE TABLE recording_record (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    agent_id BIGINT,
    recording_type VARCHAR(32) NOT NULL,
    storage_uri VARCHAR(500) NOT NULL,
    duration_seconds INT NOT NULL DEFAULT 0,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    recording_status VARCHAR(32) NOT NULL,
    started_at DATETIME,
    ended_at DATETIME,
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_recording_call (tenant_id, call_id, deleted),
    KEY idx_recording_agent_time (tenant_id, agent_id, started_at)
);

CREATE TABLE service_record (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    agent_id BIGINT NOT NULL,
    customer_id BIGINT,
    disposition_code VARCHAR(64) NOT NULL,
    service_summary VARCHAR(1000) NOT NULL,
    next_action VARCHAR(300),
    after_call_started_at DATETIME NOT NULL,
    after_call_submitted_at DATETIME NOT NULL,
    after_call_seconds INT NOT NULL DEFAULT 0,
    trace_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_service_record_call_agent (tenant_id, call_id, agent_id, deleted),
    KEY idx_service_record_customer_time (tenant_id, customer_id, created_at)
);

CREATE TABLE call_customer_association (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    customer_id BIGINT NOT NULL,
    association_source VARCHAR(32) NOT NULL,
    associated_by BIGINT NOT NULL,
    associated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trace_id VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_call_customer (tenant_id, call_id, customer_id),
    KEY idx_call_customer_customer (tenant_id, customer_id, associated_at)
);

INSERT INTO agent_phone_bar_session (id, tenant_id, agent_id, extension_no, current_call_id, agent_state, control_state, last_state_changed_at, trace_id, created_by, updated_by)
VALUES (1, 1, 9, '8001', NULL, 'OFFLINE', 'IDLE', '2026-05-19 09:00:00', 'trace-phase4-agent-seed', 1, 1);
```

- [ ] **Step 4: Create customer Phase 4 schema**

Create `och-customer/src/main/resources/db/migration/V8__customer_phase4_schema.sql`:

```sql
CREATE TABLE customer_profile (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    customer_name VARCHAR(100) NOT NULL,
    customer_level VARCHAR(32) NOT NULL DEFAULT 'STANDARD',
    owner_dept_id BIGINT,
    owner_agent_id BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_customer_owner_agent (tenant_id, owner_agent_id, deleted),
    KEY idx_customer_owner_dept (tenant_id, owner_dept_id, deleted)
);

CREATE TABLE customer_phone (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    customer_id BIGINT NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    phone_type VARCHAR(32) NOT NULL DEFAULT 'MOBILE',
    is_primary TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customer_phone (tenant_id, phone_number, deleted),
    KEY idx_customer_phone_customer (tenant_id, customer_id, deleted)
);

INSERT INTO customer_profile (id, tenant_id, customer_name, customer_level, owner_dept_id, owner_agent_id, status, remark, created_by, updated_by)
VALUES (1001, 1, 'Li Hua', 'VIP', 21, 9, 'ENABLED', 'Phase 4 screen pop fixture', 1, 1);

INSERT INTO customer_phone (id, tenant_id, customer_id, phone_number, phone_type, is_primary, status, created_by, updated_by)
VALUES (10001, 1, 1001, '13800000001', 'MOBILE', 1, 'ENABLED', 1, 1);
```

- [ ] **Step 5: Create inbound report schema**

Create `och-report/src/main/resources/db/migration/V9__inbound_report_schema.sql`:

```sql
CREATE TABLE report_inbound_daily (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    report_date DATE NOT NULL,
    total_calls INT NOT NULL DEFAULT 0,
    answered_calls INT NOT NULL DEFAULT 0,
    missed_calls INT NOT NULL DEFAULT 0,
    transferred_calls INT NOT NULL DEFAULT 0,
    avg_ring_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    avg_talk_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    avg_after_call_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    hold_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_inbound_daily (tenant_id, report_date)
);

CREATE TABLE report_agent_inbound_daily (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    report_date DATE NOT NULL,
    agent_id BIGINT NOT NULL,
    answered_calls INT NOT NULL DEFAULT 0,
    transferred_calls INT NOT NULL DEFAULT 0,
    avg_talk_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    avg_after_call_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    available_seconds INT NOT NULL DEFAULT 0,
    busy_seconds INT NOT NULL DEFAULT 0,
    after_call_seconds INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_agent_inbound_daily (tenant_id, report_date, agent_id)
);
```

- [ ] **Step 6: Extend migration smoke test**

Add assertions to `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`:

```java
assertThat(jdbcTemplate.queryForObject("select count(*) from agent_phone_bar_session where agent_id = 9 and extension_no = '8001'", Integer.class)).isEqualTo(1);
assertThat(jdbcTemplate.queryForObject("select count(*) from customer_profile where id = 1001", Integer.class)).isEqualTo(1);
assertThat(jdbcTemplate.queryForObject("select count(*) from customer_phone where phone_number = '13800000001'", Integer.class)).isEqualTo(1);
assertThat(jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name = 'report_inbound_daily'", Integer.class)).isGreaterThanOrEqualTo(1);
```

Run:

```bash
mvn -pl och-api -am test -Dtest=MigrationSmokeTest
```

Expected output contains `BUILD SUCCESS` and proves Phase 4 tables plus seed rows are queryable.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add pom.xml och-api/pom.xml och-customer/pom.xml och-report/pom.xml och-call/src/main/resources/db/migration/V7__agent_inbound_schema.sql och-customer/src/main/resources/db/migration/V8__customer_phase4_schema.sql och-report/src/main/resources/db/migration/V9__inbound_report_schema.sql och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java
git commit -m "feat: add agent inbound schemas"
```

Expected output contains `feat: add agent inbound schemas`.

### Task 2: Backend Agent Status, Phone-Bar APIs, and WebSocket Events

**Owner:** Backend Expert

**Files:**
- Modify: `och-acd/src/main/java/com/opencallhub/acd/application/AgentStatusStateMachine.java`
- Create: `och-acd/src/main/java/com/opencallhub/acd/application/AgentAvailabilityService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/PhoneBarApplicationService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/AgentEventApplicationService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/api/PhoneBarController.java`
- Create: `och-websocket/src/main/java/com/opencallhub/websocket/agent/AgentMessagePublisher.java`
- Test: `och-acd/src/test/java/com/opencallhub/acd/application/AgentAvailabilityServiceTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/PhoneBarApplicationServiceTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/AgentEventApplicationServiceTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/api/PhoneBarControllerTest.java`

- [ ] **Step 1: Write agent availability test**

Create `AgentAvailabilityServiceTest`:

```java
package com.opencallhub.acd.application;

import com.opencallhub.acd.domain.AgentState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentAvailabilityServiceTest {
    @Test
    void onlineAgentBecomesAvailableForAcdAndAfterCallBlocksAvailability() {
        AgentAvailabilityService service = AgentAvailabilityService.inMemoryForTest(9L, AgentState.OFFLINE);

        service.transition(9L, "GO_ONLINE");
        assertThat(service.currentState(9L)).isEqualTo(AgentState.AVAILABLE);
        assertThat(service.isAvailableForAcd(9L)).isTrue();

        service.transition(9L, "RESERVE_FOR_CALL");
        service.transition(9L, "BRIDGE_CONNECTED");
        service.transition(9L, "CALL_ENDED");
        assertThat(service.currentState(9L)).isEqualTo(AgentState.AFTER_CALL);
        assertThat(service.isAvailableForAcd(9L)).isFalse();

        service.transition(9L, "AFTER_CALL_SUBMITTED");
        assertThat(service.currentState(9L)).isEqualTo(AgentState.AVAILABLE);
    }

    @Test
    void offlineAgentCannotAnswerCall() {
        AgentAvailabilityService service = AgentAvailabilityService.inMemoryForTest(9L, AgentState.OFFLINE);

        assertThatThrownBy(() -> service.transition(9L, "ANSWER_CALL"))
                .hasMessageContaining("Invalid agent state transition OFFLINE -> ANSWER_CALL");
    }
}
```

- [ ] **Step 2: Implement agent availability service**

Extend `AgentStatusStateMachine` with this transition table:

```text
OFFLINE + GO_ONLINE -> AVAILABLE
AVAILABLE + GO_OFFLINE -> OFFLINE
AVAILABLE + START_BREAK -> BREAK
BREAK + END_BREAK -> AVAILABLE
AVAILABLE + RESERVE_FOR_CALL -> RESERVED
RESERVED + ANSWER_CALL -> BUSY
RESERVED + BRIDGE_CONNECTED -> BUSY
BUSY + CALL_HELD -> BUSY
BUSY + CALL_UNHELD -> BUSY
BUSY + CALL_ENDED -> AFTER_CALL
AFTER_CALL + AFTER_CALL_SUBMITTED -> AVAILABLE
AVAILABLE + GO_UNAVAILABLE -> UNAVAILABLE
UNAVAILABLE + RECOVER -> AVAILABLE
```

`AgentAvailabilityService.isAvailableForAcd(agentId)` returns `true` only for `AVAILABLE`. The database-backed implementation updates `acd_agent_status` and `agent_phone_bar_session` in one transaction.

- [ ] **Step 3: Write phone-bar application test**

Create `PhoneBarApplicationServiceTest`:

```java
package com.opencallhub.call.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneBarApplicationServiceTest {
    @Test
    void agentCanGoOnlineReceiveScreenPopAnswerHoldAndHangup() {
        PhoneBarApplicationService service = PhoneBarApplicationService.inMemoryFixture();

        assertThat(service.goOnline(9L, "8001").agentState()).isEqualTo("AVAILABLE");
        var pop = service.createScreenPop("call-400", 9L, "13800000001", "4008001000", "support-main");
        assertThat(pop.topic()).isEqualTo("agent.screen-pop");
        assertThat(pop.data().get("callId")).isEqualTo("call-400");

        assertThat(service.answer(9L, "call-400").commandType()).isEqualTo("ANSWER");
        assertThat(service.hold(9L, "call-400").commandType()).isEqualTo("HOLD");
        assertThat(service.hangup(9L, "call-400").commandType()).isEqualTo("HANGUP");
        assertThat(service.currentSession(9L).controlState()).isEqualTo("AFTER_CALL");
    }
}
```

- [ ] **Step 4: Implement phone-bar APIs**

Create `PhoneBarController` endpoints:

```text
GET    /api/agent/phone-bar/session
POST   /api/agent/phone-bar/online
POST   /api/agent/phone-bar/offline
POST   /api/agent/phone-bar/break/start
POST   /api/agent/phone-bar/break/end
POST   /api/agent/phone-bar/calls/{callId}/answer
POST   /api/agent/phone-bar/calls/{callId}/hangup
POST   /api/agent/phone-bar/calls/{callId}/hold
POST   /api/agent/phone-bar/calls/{callId}/unhold
POST   /api/agent/phone-bar/calls/{callId}/mute
POST   /api/agent/phone-bar/calls/{callId}/unmute
POST   /api/agent/phone-bar/calls/{callId}/transfer/blind
POST   /api/agent/phone-bar/calls/{callId}/transfer/consult/start
POST   /api/agent/phone-bar/calls/{callId}/transfer/consult/complete
POST   /api/agent/phone-bar/calls/{callId}/transfer/consult/cancel
```

Command response contract:

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "commandId": "cmd-call-400-answer-9",
    "callId": "call-400",
    "agentId": 9,
    "commandType": "ANSWER",
    "commandStatus": "ACCEPTED",
    "controlState": "TALKING"
  },
  "traceId": "trace-phonebar-1"
}
```

- [ ] **Step 5: Write WebSocket event service test**

Create `AgentEventApplicationServiceTest`:

```java
package com.opencallhub.call.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentEventApplicationServiceTest {
    @Test
    void publishesScreenPopBridgeHoldTransferAndHangupEventsToAssignedAgent() {
        AgentEventApplicationService service = AgentEventApplicationService.inMemoryForTest();

        service.screenPop(9L, "call-400", "13800000001", "4008001000", "support-main");
        service.callStateChanged(9L, "call-400", "BRIDGED");
        service.callStateChanged(9L, "call-400", "HELD");
        service.transferStarted(9L, "call-400", "8002", "CONSULT");
        service.callStateChanged(9L, "call-400", "ENDED");

        assertThat(service.publishedTopics()).containsExactly(
                "agent.screen-pop",
                "agent.call-bridged",
                "agent.call-held",
                "agent.transfer-started",
                "agent.call-ended"
        );
        assertThat(service.messagesForAgent(9L)).allMatch(message -> message.agentId().equals(9L));
    }
}
```

- [ ] **Step 6: Implement `/ws/agent` publishing**

Create `AgentWebSocketMessage` envelope:

```java
public record AgentWebSocketMessage(
        String messageId,
        Long agentId,
        String topic,
        String callId,
        Object data,
        String traceId,
        Instant occurredAt
) {}
```

Supported topics:

```text
agent.status-changed
agent.screen-pop
agent.call-ringing
agent.call-answered
agent.call-bridged
agent.call-held
agent.call-unheld
agent.call-muted
agent.call-unmuted
agent.transfer-started
agent.transfer-completed
agent.transfer-cancelled
agent.call-ended
agent.after-call-started
agent.after-call-submitted
agent.record-created
```

- [ ] **Step 7: Write and run MockMvc phone-bar API tests**

Create `PhoneBarControllerTest` with assertions:

```java
mockMvc.perform(post("/api/agent/phone-bar/online")
        .contentType("application/json")
        .content("{\"agentId\":9,\"extensionNo\":\"8001\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.agentState").value("AVAILABLE"));

mockMvc.perform(post("/api/agent/phone-bar/calls/call-400/answer")
        .contentType("application/json")
        .content("{\"agentId\":9}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.commandType").value("ANSWER"))
        .andExpect(jsonPath("$.data.commandStatus").value("ACCEPTED"));
```

Run:

```bash
mvn -pl och-acd,och-call -am test -Dtest=AgentAvailabilityServiceTest,PhoneBarApplicationServiceTest,AgentEventApplicationServiceTest,PhoneBarControllerTest
```

Expected output contains `BUILD SUCCESS` and all four test classes pass.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add och-acd/src/main/java och-acd/src/test/java och-call/src/main/java och-call/src/test/java och-websocket/src/main/java
git commit -m "feat: add agent phone bar backend"
```

Expected output contains `feat: add agent phone bar backend`.

### Task 3: Communications Command Correctness for Hold, Transfer, and Hangup

**Owner:** Communications Expert

**Files:**
- Modify: `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`
- Test: `och-esl/src/test/java/com/opencallhub/esl/application/PhoneControlCommandServiceTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/PhoneBarCommandEventReconciliationTest.java`

- [ ] **Step 1: Extend phone command wrapper test**

Add assertions to `PhoneControlCommandServiceTest`:

```java
assertThat(service.hold("uuid-a").command()).isEqualTo("uuid_hold uuid-a on");
assertThat(service.unhold("uuid-a").command()).isEqualTo("uuid_hold uuid-a off");
assertThat(service.mute("uuid-a").command()).isEqualTo("uuid_audio uuid-a start mute");
assertThat(service.unmute("uuid-a").command()).isEqualTo("uuid_audio uuid-a stop mute");
assertThat(service.blindTransfer("uuid-a", "8002").command()).isEqualTo("uuid_transfer uuid-a 8002 XML default");
assertThat(service.consultTransferStart("uuid-a", "8002").command()).isEqualTo("originate user/8002 &park()");
assertThat(service.consultTransferComplete("uuid-a", "uuid-consult").command()).isEqualTo("uuid_bridge uuid-a uuid-consult");
assertThat(service.consultTransferCancel("uuid-consult").command()).isEqualTo("uuid_kill uuid-consult NORMAL_CLEARING");
```

- [ ] **Step 2: Implement command wrappers**

Add methods to `PhoneControlCommandService`:

```java
PhoneControlCommandResult hold(String uuid);
PhoneControlCommandResult unhold(String uuid);
PhoneControlCommandResult mute(String uuid);
PhoneControlCommandResult unmute(String uuid);
PhoneControlCommandResult blindTransfer(String uuid, String targetExtensionNo);
PhoneControlCommandResult consultTransferStart(String uuid, String targetExtensionNo);
PhoneControlCommandResult consultTransferComplete(String originalUuid, String consultUuid);
PhoneControlCommandResult consultTransferCancel(String consultUuid);
```

Production command dispatch must attach `callId`, `agentId`, `commandId`, and `traceId` as command metadata when the FreeSWITCH adapter supports headers. If metadata headers are unavailable, persist metadata in `agent_call_command` before dispatch and reconcile by `uuid` plus command time window.

- [ ] **Step 3: Write event reconciliation test**

Create `PhoneBarCommandEventReconciliationTest`:

```java
package com.opencallhub.call.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneBarCommandEventReconciliationTest {
    @Test
    void hangupEventMovesAcceptedHangupCommandToSucceededAndStartsAfterCallWork() {
        PhoneBarApplicationService service = PhoneBarApplicationService.inMemoryFixture();
        service.goOnline(9L, "8001");
        service.createScreenPop("call-400", 9L, "13800000001", "4008001000", "support-main");
        var command = service.hangup(9L, "call-400");

        service.onEslEvent("call-400", "CHANNEL_HANGUP", "uuid-a");

        assertThat(service.commandStatus(command.commandId())).isEqualTo("SUCCEEDED");
        assertThat(service.currentSession(9L).controlState()).isEqualTo("AFTER_CALL");
        assertThat(service.currentSession(9L).agentState()).isEqualTo("AFTER_CALL");
    }
}
```

- [ ] **Step 4: Implement reconciliation**

`PhoneBarApplicationService.onEslEvent(callId, eventType, uuid)` updates command statuses as follows:

```text
CHANNEL_ANSWER after ANSWER -> SUCCEEDED and controlState TALKING
CHANNEL_HOLD after HOLD -> SUCCEEDED and controlState HELD
CHANNEL_UNHOLD after UNHOLD -> SUCCEEDED and controlState TALKING
CHANNEL_BRIDGE after BLIND_TRANSFER or CONSULT_TRANSFER_COMPLETE -> SUCCEEDED and controlState TALKING
CHANNEL_HANGUP after HANGUP -> SUCCEEDED and controlState AFTER_CALL
CHANNEL_HANGUP without accepted HANGUP -> controlState AFTER_CALL and endReason REMOTE_HANGUP
```

- [ ] **Step 5: Run communications tests**

Run:

```bash
mvn -pl och-esl,och-call -am test -Dtest=PhoneControlCommandServiceTest,PhoneBarCommandEventReconciliationTest
```

Expected output contains `BUILD SUCCESS` and proves command strings plus hangup reconciliation are deterministic.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-esl/src/main/java och-esl/src/test/java och-call/src/main/java och-call/src/test/java
git commit -m "feat: add inbound call control commands"
```

Expected output contains `feat: add inbound call control commands`.

### Task 4: Records, After-Call Work, Customer Association, and Report Aggregation

**Owner:** Backend Expert

**Files:**
- Create: `och-call/src/main/java/com/opencallhub/call/application/AfterCallWorkService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/CallRecordService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/RecordingRecordService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/ServiceRecordService.java`
- Create: `och-call/src/main/java/com/opencallhub/call/api/AgentRecordController.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/domain/CustomerProfile.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/domain/CustomerPhone.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/application/CustomerLookupService.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/application/CustomerAssociationService.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/api/CustomerLookupController.java`
- Create: `och-report/src/main/java/com/opencallhub/report/application/InboundReportAggregationService.java`
- Create: `och-report/src/main/java/com/opencallhub/report/api/InboundReportController.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/AfterCallWorkServiceTest.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/CallRecordServiceTest.java`
- Test: `och-customer/src/test/java/com/opencallhub/customer/application/CustomerLookupServiceTest.java`
- Test: `och-report/src/test/java/com/opencallhub/report/application/InboundReportAggregationServiceTest.java`

- [ ] **Step 1: Write after-call work test**

Create `AfterCallWorkServiceTest`:

```java
package com.opencallhub.call.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfterCallWorkServiceTest {
    @Test
    void submitAfterCallCreatesServiceRecordAndReleasesAgentAvailability() {
        AfterCallWorkService service = AfterCallWorkService.inMemoryFixture();

        var record = service.submit(9L, "call-400", 1001L, "RESOLVED", "Customer confirmed delivery address", "No follow-up required");

        assertThat(record.serviceRecordId()).isEqualTo(7001L);
        assertThat(record.callId()).isEqualTo("call-400");
        assertThat(record.customerId()).isEqualTo(1001L);
        assertThat(record.dispositionCode()).isEqualTo("RESOLVED");
        assertThat(service.agentState(9L)).isEqualTo("AVAILABLE");
    }

    @Test
    void dispositionAndSummaryAreRequired() {
        AfterCallWorkService service = AfterCallWorkService.inMemoryFixture();

        assertThatThrownBy(() -> service.submit(9L, "call-400", 1001L, "", "", ""))
                .hasMessageContaining("dispositionCode and serviceSummary are required");
    }
}
```

- [ ] **Step 2: Write record linkage test**

Create `CallRecordServiceTest`:

```java
package com.opencallhub.call.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallRecordServiceTest {
    @Test
    void callRecordingAndServiceRecordsAreLinkedByCallId() {
        CallRecordService service = CallRecordService.inMemoryFixture();

        service.createCallRecord("call-400", 9L, 1001L, "13800000001", "4008001000", 12, 180, 15, 1, 1);
        service.createRecordingRecord("call-400", 9L, "storage://recordings/call-400.wav", 180, 4096);
        service.createServiceRecord("call-400", 9L, 1001L, "RESOLVED", "Customer confirmed delivery address", 25);

        var bundle = service.recordBundle("call-400", 9L);
        assertThat(bundle.callRecord().callId()).isEqualTo("call-400");
        assertThat(bundle.recordingRecords()).hasSize(1);
        assertThat(bundle.serviceRecord().customerId()).isEqualTo(1001L);
    }
}
```

- [ ] **Step 3: Implement record APIs**

Create `AgentRecordController` endpoints:

```text
GET  /api/agent/records/calls
GET  /api/agent/records/calls/{callId}
GET  /api/agent/records/calls/{callId}/recordings
GET  /api/agent/records/calls/{callId}/service-record
POST /api/agent/records/calls/{callId}/after-call-work
```

`POST /api/agent/records/calls/{callId}/after-call-work` accepts:

```json
{
  "agentId": 9,
  "customerId": 1001,
  "dispositionCode": "RESOLVED",
  "serviceSummary": "Customer confirmed delivery address",
  "nextAction": "No follow-up required"
}
```

Expected response data contains `serviceRecordId`, `callId`, `agentId`, `customerId`, `afterCallSeconds`, and `agentState` equal to `AVAILABLE`.

- [ ] **Step 4: Write customer lookup test**

Create `CustomerLookupServiceTest`:

```java
package com.opencallhub.customer.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerLookupServiceTest {
    @Test
    void lookupByPhoneReturnsMaskedCustomerAndAssociationCanBeSaved() {
        CustomerLookupService lookup = CustomerLookupService.inMemoryFixture();
        CustomerAssociationService association = CustomerAssociationService.inMemoryFixture();

        var customer = lookup.lookupByPhone("13800000001", 9L);
        assertThat(customer.customerId()).isEqualTo(1001L);
        assertThat(customer.maskedPhone()).isEqualTo("138****0001");

        var saved = association.associate("call-400", 1001L, 9L, "MANUAL", "trace-customer-1");
        assertThat(saved.callId()).isEqualTo("call-400");
        assertThat(saved.customerId()).isEqualTo(1001L);
    }
}
```

- [ ] **Step 5: Implement customer APIs**

Create endpoints:

```text
GET  /api/customer/lookup/by-phone?phone=13800000001
POST /api/customer/associations
```

`GET /api/customer/lookup/by-phone` returns masked phone and customer summary:

```json
{
  "customerId": 1001,
  "customerName": "Li Hua",
  "customerLevel": "VIP",
  "maskedPhone": "138****0001",
  "ownerAgentId": 9
}
```

- [ ] **Step 6: Write inbound report aggregation test**

Create `InboundReportAggregationServiceTest`:

```java
package com.opencallhub.report.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InboundReportAggregationServiceTest {
    @Test
    void aggregatesInboundDailyMetricsFromCallAndServiceRecords() {
        InboundReportAggregationService service = InboundReportAggregationService.inMemoryFixture();

        var report = service.recalculate(LocalDate.parse("2026-05-19"));

        assertThat(report.totalCalls()).isEqualTo(2);
        assertThat(report.answeredCalls()).isEqualTo(1);
        assertThat(report.missedCalls()).isEqualTo(1);
        assertThat(report.transferredCalls()).isEqualTo(1);
        assertThat(report.avgRingSeconds()).isEqualByComparingTo("12.00");
        assertThat(report.avgTalkSeconds()).isEqualByComparingTo("180.00");
        assertThat(report.avgAfterCallSeconds()).isEqualByComparingTo("25.00");
        assertThat(report.holdCount()).isEqualTo(1);
    }
}
```

- [ ] **Step 7: Implement inbound report APIs**

Create endpoints:

```text
POST /api/report/inbound/recalculate?date=2026-05-19
GET  /api/report/inbound/daily?startDate=2026-05-19&endDate=2026-05-19
GET  /api/report/inbound/agents/daily?date=2026-05-19
```

Expected daily report response contains fields `reportDate`, `totalCalls`, `answeredCalls`, `missedCalls`, `transferredCalls`, `avgRingSeconds`, `avgTalkSeconds`, `avgAfterCallSeconds`, and `holdCount`.

- [ ] **Step 8: Run backend records/report tests**

Run:

```bash
mvn -pl och-call,och-customer,och-report -am test -Dtest=AfterCallWorkServiceTest,CallRecordServiceTest,CustomerLookupServiceTest,InboundReportAggregationServiceTest
```

Expected output contains `BUILD SUCCESS` and all four test classes pass.

- [ ] **Step 9: Commit checkpoint**

Run:

```bash
git add och-call/src/main/java och-call/src/test/java och-customer/src/main/java och-customer/src/test/java och-report/src/main/java och-report/src/test/java
git commit -m "feat: add inbound records and reports"
```

Expected output contains `feat: add inbound records and reports`.

### Task 5: Frontend Agent Workspace Store, Phone Bar, Screen Pop, Customer Card, and Call Controls

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/types/agent.ts`
- Create: `waihu-app/src/types/customer.ts`
- Create: `waihu-app/src/services/agentApi.ts`
- Create: `waihu-app/src/services/customerApi.ts`
- Create: `waihu-app/src/stores/agentWorkspace.ts`
- Create: `waihu-app/src/views/agent/AgentWorkspaceView.vue`
- Create: `waihu-app/src/components/agent/PhoneBar.vue`
- Create: `waihu-app/src/components/agent/ScreenPopPanel.vue`
- Create: `waihu-app/src/components/agent/CustomerCard.vue`
- Create: `waihu-app/src/components/agent/CallControlPanel.vue`
- Create: `waihu-app/src/components/agent/TransferPanel.vue`
- Modify: `waihu-app/src/router/routes.ts`
- Test: `waihu-app/src/tests/agent-workspace-store.spec.ts`
- Test: `waihu-app/src/tests/phone-bar.spec.ts`
- Test: `waihu-app/src/tests/screen-pop-panel.spec.ts`
- Test: `waihu-app/src/tests/customer-card.spec.ts`

- [ ] **Step 1: Write agent workspace store test**

Create `waihu-app/src/tests/agent-workspace-store.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it } from 'vitest'
import { useAgentWorkspaceStore } from '@/stores/agentWorkspace'

describe('agent workspace store', () => {
  it('merges screen pop answer hold transfer hangup and after-call events', () => {
    setActivePinia(createPinia())
    const store = useAgentWorkspaceStore()

    store.applyAgentEvent({ topic: 'agent.status-changed', callId: null, data: { agentState: 'AVAILABLE', controlState: 'IDLE' } })
    store.applyAgentEvent({ topic: 'agent.screen-pop', callId: 'call-400', data: { caller: '13800000001', callee: '4008001000', skillGroupCode: 'support-main', customerId: 1001 } })
    store.applyCommandAccepted({ commandType: 'ANSWER', commandStatus: 'ACCEPTED', callId: 'call-400', controlState: 'TALKING' })
    store.applyAgentEvent({ topic: 'agent.call-held', callId: 'call-400', data: { controlState: 'HELD' } })
    store.applyAgentEvent({ topic: 'agent.transfer-started', callId: 'call-400', data: { transferType: 'CONSULT', targetExtensionNo: '8002' } })
    store.applyAgentEvent({ topic: 'agent.call-ended', callId: 'call-400', data: { controlState: 'AFTER_CALL', agentState: 'AFTER_CALL' } })

    expect(store.agentState).toBe('AFTER_CALL')
    expect(store.controlState).toBe('AFTER_CALL')
    expect(store.activeCall?.callId).toBe('call-400')
    expect(store.transfer.targetExtensionNo).toBe('8002')
  })
})
```

- [ ] **Step 2: Implement DTOs, API clients, and store**

Create `agent.ts` types:

```ts
export type AgentState = 'OFFLINE' | 'AVAILABLE' | 'RESERVED' | 'BUSY' | 'AFTER_CALL' | 'BREAK' | 'UNAVAILABLE'
export type CallControlState = 'IDLE' | 'RINGING' | 'TALKING' | 'HELD' | 'MUTED' | 'TRANSFERRING' | 'AFTER_CALL' | 'ENDED'
export interface AgentCallSummary { callId: string; caller: string; callee: string; skillGroupCode: string; customerId?: number }
export interface AgentCommandResult { commandId: string; callId: string; agentId: number; commandType: string; commandStatus: 'ACCEPTED' | 'SUCCEEDED' | 'FAILED'; controlState: CallControlState }
export interface AgentWsEvent { topic: string; callId: string | null; data: Record<string, unknown> }
```

`useAgentWorkspaceStore` exposes `goOnline`, `goOffline`, `startBreak`, `endBreak`, `answer`, `hangup`, `hold`, `unhold`, `mute`, `unmute`, `blindTransfer`, `consultTransferStart`, `consultTransferComplete`, `consultTransferCancel`, `applyAgentEvent`, and `applyCommandAccepted`.

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- agent-workspace-store.spec.ts
```

Expected output contains a passing Vitest summary for `agent-workspace-store.spec.ts`.

- [ ] **Step 3: Write phone bar and screen pop component tests**

Create `phone-bar.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PhoneBar from '@/components/agent/PhoneBar.vue'

describe('PhoneBar', () => {
  it('shows status and emits online offline break actions', async () => {
    const wrapper = mount(PhoneBar, { props: { agentState: 'OFFLINE', controlState: 'IDLE', extensionNo: '8001', pending: false } })
    await wrapper.get('[data-test="go-online"]').trigger('click')
    expect(wrapper.emitted('go-online')).toHaveLength(1)
    expect(wrapper.text()).toContain('8001')
  })
})
```

Create `screen-pop-panel.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ScreenPopPanel from '@/components/agent/ScreenPopPanel.vue'

describe('ScreenPopPanel', () => {
  it('renders caller queue and emits answer', async () => {
    const wrapper = mount(ScreenPopPanel, { props: { screenPop: { callId: 'call-400', caller: '13800000001', callee: '4008001000', skillGroupCode: 'support-main', customerId: 1001 } } })
    expect(wrapper.text()).toContain('13800000001')
    expect(wrapper.text()).toContain('support-main')
    await wrapper.get('[data-test="answer-call"]').trigger('click')
    expect(wrapper.emitted('answer')).toEqual([[ 'call-400' ]])
  })
})
```

- [ ] **Step 4: Implement phone bar and screen pop components**

`PhoneBar.vue` renders `agentState`, `controlState`, `extensionNo`, and buttons with `data-test` values `go-online`, `go-offline`, `start-break`, and `end-break`. `ScreenPopPanel.vue` renders caller, callee, skill group, customer id, IVR summary, and an answer button with `data-test="answer-call"`.

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- phone-bar.spec.ts screen-pop-panel.spec.ts
```

Expected output contains passing Vitest summaries for both files.

- [ ] **Step 5: Write and implement customer card test**

Create `customer-card.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import CustomerCard from '@/components/agent/CustomerCard.vue'

describe('CustomerCard', () => {
  it('renders masked customer and emits association', async () => {
    const wrapper = mount(CustomerCard, { props: { customer: { customerId: 1001, customerName: 'Li Hua', customerLevel: 'VIP', maskedPhone: '138****0001', ownerAgentId: 9 }, callId: 'call-400' } })
    expect(wrapper.text()).toContain('Li Hua')
    expect(wrapper.text()).toContain('138****0001')
    await wrapper.get('[data-test="associate-customer"]').trigger('click')
    expect(wrapper.emitted('associate')).toEqual([[{ callId: 'call-400', customerId: 1001 }]])
  })
})
```

Implement `CustomerCard.vue` with `data-test="associate-customer"` and no raw phone field.

- [ ] **Step 6: Implement call controls and transfer panel**

`CallControlPanel.vue` buttons and emitted events:

```text
[data-test="answer"] -> answer
[data-test="hangup"] -> hangup
[data-test="hold"] -> hold
[data-test="unhold"] -> unhold
[data-test="mute"] -> mute
[data-test="unmute"] -> unmute
```

`TransferPanel.vue` fields and events:

```text
[data-test="target-extension"] input targetExtensionNo
[data-test="blind-transfer"] -> blind-transfer
[data-test="consult-start"] -> consult-start
[data-test="consult-complete"] -> consult-complete
[data-test="consult-cancel"] -> consult-cancel
```

- [ ] **Step 7: Implement immersive workspace page and routes**

Create `AgentWorkspaceView.vue` with layout zones:

```text
top: PhoneBar
left: ScreenPopPanel and active call timeline
center: CustomerCard and service history summary
right: TransferPanel and knowledge placeholder-free quick notes section labeled "Service notes"
bottom: CallControlPanel and AfterCallWorkPanel slot area
```

Modify `waihu-app/src/router/routes.ts`:

```text
/agent/workspace -> agent:workspace:view
```

The route title is `Agent Workspace`.

- [ ] **Step 8: Run frontend workspace tests and build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- agent-workspace-store.spec.ts phone-bar.spec.ts screen-pop-panel.spec.ts customer-card.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains passing Vitest summaries and frontend build output containing `built in`.

- [ ] **Step 9: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/agent.ts waihu-app/src/types/customer.ts waihu-app/src/services/agentApi.ts waihu-app/src/services/customerApi.ts waihu-app/src/stores/agentWorkspace.ts waihu-app/src/views/agent/AgentWorkspaceView.vue waihu-app/src/components/agent/PhoneBar.vue waihu-app/src/components/agent/ScreenPopPanel.vue waihu-app/src/components/agent/CustomerCard.vue waihu-app/src/components/agent/CallControlPanel.vue waihu-app/src/components/agent/TransferPanel.vue waihu-app/src/router/routes.ts waihu-app/src/tests/agent-workspace-store.spec.ts waihu-app/src/tests/phone-bar.spec.ts waihu-app/src/tests/screen-pop-panel.spec.ts waihu-app/src/tests/customer-card.spec.ts
git commit -m "feat: add agent workspace phone bar frontend"
```

Expected output contains `feat: add agent workspace phone bar frontend`.

### Task 6: Frontend After-Call Work, Record Drawers, and Inbound Reports

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/types/records.ts`
- Create: `waihu-app/src/types/report.ts`
- Create: `waihu-app/src/services/reportApi.ts`
- Create: `waihu-app/src/components/agent/AfterCallWorkPanel.vue`
- Create: `waihu-app/src/components/agent/CallRecordDrawer.vue`
- Create: `waihu-app/src/components/agent/RecordingRecordDrawer.vue`
- Create: `waihu-app/src/components/agent/ServiceRecordDrawer.vue`
- Create: `waihu-app/src/views/report/InboundReportView.vue`
- Modify: `waihu-app/src/views/agent/AgentWorkspaceView.vue`
- Modify: `waihu-app/src/router/routes.ts`
- Test: `waihu-app/src/tests/after-call-work-panel.spec.ts`

- [ ] **Step 1: Write after-call panel test**

Create `after-call-work-panel.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AfterCallWorkPanel from '@/components/agent/AfterCallWorkPanel.vue'

describe('AfterCallWorkPanel', () => {
  it('requires disposition and summary before submit', async () => {
    const wrapper = mount(AfterCallWorkPanel, { props: { callId: 'call-400', customerId: 1001, submitting: false } })
    await wrapper.get('[data-test="submit-after-call"]').trigger('click')
    expect(wrapper.text()).toContain('Disposition and summary are required')

    await wrapper.get('[data-test="disposition-code"]').setValue('RESOLVED')
    await wrapper.get('[data-test="service-summary"]').setValue('Customer confirmed delivery address')
    await wrapper.get('[data-test="next-action"]').setValue('No follow-up required')
    await wrapper.get('[data-test="submit-after-call"]').trigger('click')

    expect(wrapper.emitted('submit')?.[0][0]).toMatchObject({ callId: 'call-400', customerId: 1001, dispositionCode: 'RESOLVED' })
  })
})
```

- [ ] **Step 2: Implement after-call panel**

`AfterCallWorkPanel.vue` uses fields:

```text
[data-test="disposition-code"] select values RESOLVED, CALLBACK_REQUIRED, ESCALATED, WRONG_NUMBER, NO_ACTION
[data-test="service-summary"] textarea max length 1000
[data-test="next-action"] input max length 300
[data-test="submit-after-call"] submit button
```

It displays exact validation text `Disposition and summary are required` when either required field is blank.

- [ ] **Step 3: Implement record types and drawers**

Create `records.ts` with:

```ts
export interface CallRecord { callId: string; caller: string; callee: string; agentId: number; customerId?: number; ringSeconds: number; talkSeconds: number; holdSeconds: number; transferCount: number; endReason?: string }
export interface RecordingRecord { recordingId: number; callId: string; durationSeconds: number; recordingStatus: string; canPlay: boolean; playUrl?: string }
export interface ServiceRecord { serviceRecordId: number; callId: string; agentId: number; customerId?: number; dispositionCode: string; serviceSummary: string; nextAction?: string; afterCallSeconds: number }
```

`RecordingRecordDrawer.vue` never renders `storageUri`; it renders play button only when `canPlay` is true and `playUrl` is present.

- [ ] **Step 4: Implement inbound report page**

Create `report.ts` and `InboundReportView.vue`. The report page calls `/api/report/inbound/daily` and renders columns:

```text
reportDate
totalCalls
answeredCalls
missedCalls
transferredCalls
avgRingSeconds
avgTalkSeconds
avgAfterCallSeconds
holdCount
```

Route:

```text
/report/inbound -> report:inbound:view
```

- [ ] **Step 5: Integrate after-call and drawers into workspace**

Modify `AgentWorkspaceView.vue` so `AfterCallWorkPanel` appears when `controlState === 'AFTER_CALL'`. On successful submit, it refreshes call, recording, and service records and opens `ServiceRecordDrawer.vue`.

- [ ] **Step 6: Run frontend records/report tests and build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- after-call-work-panel.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains a passing Vitest summary and build output containing `built in`.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/records.ts waihu-app/src/types/report.ts waihu-app/src/services/reportApi.ts waihu-app/src/components/agent/AfterCallWorkPanel.vue waihu-app/src/components/agent/CallRecordDrawer.vue waihu-app/src/components/agent/RecordingRecordDrawer.vue waihu-app/src/components/agent/ServiceRecordDrawer.vue waihu-app/src/views/report/InboundReportView.vue waihu-app/src/views/agent/AgentWorkspaceView.vue waihu-app/src/router/routes.ts waihu-app/src/tests/after-call-work-panel.spec.ts
git commit -m "feat: add after-call records and inbound report frontend"
```

Expected output contains `feat: add after-call records and inbound report frontend`.

### Task 7: Security for Customer and Recording Visibility

**Owner:** Security Expert

**Files:**
- Modify: `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`
- Create: `och-call/src/main/java/com/opencallhub/call/application/security/RecordingVisibilityService.java`
- Create: `och-customer/src/main/java/com/opencallhub/customer/application/security/CustomerVisibilityService.java`
- Test: `och-call/src/test/java/com/opencallhub/call/application/security/RecordingVisibilityServiceTest.java`
- Test: `och-customer/src/test/java/com/opencallhub/customer/application/security/CustomerVisibilityServiceTest.java`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/Phase4SecurityAccessTest.java`

- [ ] **Step 1: Add Phase 4 permission constants**

Add constants:

```text
agent:workspace:view
agent:phonebar:status
agent:phonebar:control
agent:phonebar:transfer
agent:after-call:submit
agent:record:view
agent:recording:view
agent:recording:play
customer:lookup:view
customer:association:create
report:inbound:view
```

- [ ] **Step 2: Write recording visibility test**

Create `RecordingVisibilityServiceTest`:

```java
package com.opencallhub.call.application.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordingVisibilityServiceTest {
    @Test
    void recordingPlayRequiresPermissionAssignedAgentOrSupervisorScopeAndReason() {
        RecordingVisibilityService service = RecordingVisibilityService.inMemoryFixture();

        assertThat(service.canPlay(9L, "AGENT", "call-400", "agent:recording:play", "service replay")).isTrue();
        assertThat(service.canPlay(10L, "AGENT", "call-400", "agent:recording:play", "service replay")).isFalse();
        assertThat(service.canPlay(100L, "SUPERVISOR", "call-400", "agent:recording:play", "quality review")).isTrue();
        assertThat(service.canPlay(100L, "SUPERVISOR", "call-400", "agent:recording:view", "quality review")).isFalse();
        assertThat(service.canPlay(100L, "SUPERVISOR", "call-400", "agent:recording:play", "")).isFalse();
    }
}
```

- [ ] **Step 3: Write customer visibility test**

Create `CustomerVisibilityServiceTest`:

```java
package com.opencallhub.customer.application.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerVisibilityServiceTest {
    @Test
    void agentSeesOwnedCustomerWithMaskedPhoneAndCannotSeeUnownedCustomer() {
        CustomerVisibilityService service = CustomerVisibilityService.inMemoryFixture();

        assertThat(service.canViewCustomer(9L, "AGENT", 1001L)).isTrue();
        assertThat(service.maskPhone("13800000001", 9L, "AGENT")).isEqualTo("138****0001");
        assertThat(service.canViewCustomer(10L, "AGENT", 1001L)).isFalse();
        assertThat(service.canViewCustomer(100L, "SUPERVISOR", 1001L)).isTrue();
    }
}
```

- [ ] **Step 4: Implement visibility services and audit**

`RecordingVisibilityService.playUrl(recordingId, userId, permission, reason)` requires `agent:recording:play`, non-blank reason, and assigned-agent or supervisor data scope. It writes operation audit target type `RECORDING_RECORD` with action `PLAY` before returning a signed local/test URL.

`CustomerVisibilityService` applies:

```text
ADMIN -> can view all Phase 4 customers
SUPERVISOR -> can view customers in dept tree from data scope
AGENT -> can view customer when ownerAgentId equals current user or when the active call is assigned to current user
```

- [ ] **Step 5: Write API security E2E test**

Create `Phase4SecurityAccessTest` with assertions:

```java
mockMvc.perform(get("/api/customer/lookup/by-phone?phone=13800000001").header("X-Test-User", "agent01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.maskedPhone").value("138****0001"))
        .andExpect(jsonPath("$.data.rawPhone").doesNotExist());

mockMvc.perform(get("/api/agent/records/calls/call-400/recordings").header("X-Test-User", "agent01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].storageUri").doesNotExist());
```

Add an unauthorized user assertion expecting HTTP 403 for recording play without `agent:recording:play`.

- [ ] **Step 6: Run security tests**

Run:

```bash
mvn -pl och-api,och-call,och-customer -am test -Dtest=RecordingVisibilityServiceTest,CustomerVisibilityServiceTest,Phase4SecurityAccessTest
```

Expected output contains `BUILD SUCCESS` and proves raw phones and storage URIs are not exposed.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add och-security/src/main/java och-call/src/main/java och-call/src/test/java och-customer/src/main/java och-customer/src/test/java och-api/src/test/java/com/opencallhub/api/e2e/Phase4SecurityAccessTest.java
git commit -m "feat: secure customer and recording visibility"
```

Expected output contains `feat: secure customer and recording visibility`.

### Task 8: Backend and Frontend Inbound E2E Regression

**Owner:** QA Expert

**Files:**
- Create: `och-api/src/main/java/com/opencallhub/api/testfixture/InboundCallTestFixtureController.java`
- Create: `och-api/src/test/java/com/opencallhub/api/testfixture/InboundCallTestFixtureControllerTest.java`
- Create: `och-api/src/test/java/com/opencallhub/api/e2e/Phase4AgentInboundFlowTest.java`
- Create: `waihu-app/e2e/phase4-agent-inbound.spec.ts`

- [ ] **Step 1: Create backend inbound-call test fixture endpoint**

Create `och-api/src/main/java/com/opencallhub/api/testfixture/InboundCallTestFixtureController.java`:

```java
package com.opencallhub.api.testfixture;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/call/test-fixtures")
@Profile({"local", "test"})
class InboundCallTestFixtureController {
    @PostMapping("/inbound-call")
    ResponseEntity<Map<String, Object>> createInboundCall(@RequestBody InboundCallFixtureRequest request) {
        return ResponseEntity.ok(Map.of(
                "code", "0",
                "message", "success",
                "data", Map.of(
                        "callId", "call-400",
                        "caller", request.caller(),
                        "callee", request.callee(),
                        "businessTime", request.businessTime().toString(),
                        "dtmf", request.dtmf(),
                        "skillGroupCode", "support-main",
                        "assignedAgentId", 9,
                        "customerId", 1001,
                        "traceId", "trace-phase4-fixture-1"
                ),
                "traceId", "trace-phase4-fixture-1"
        ));
    }

    record InboundCallFixtureRequest(String caller, String callee, OffsetDateTime businessTime, String dtmf) {}
}
```

Create `och-api/src/test/java/com/opencallhub/api/testfixture/InboundCallTestFixtureControllerTest.java`:

```java
package com.opencallhub.api.testfixture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InboundCallTestFixtureControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void createsDeterministicInboundCallFixtureForPhase4E2e() throws Exception {
        mockMvc.perform(post("/api/call/test-fixtures/inbound-call")
                .contentType("application/json")
                .content("{\"caller\":\"13800000001\",\"callee\":\"4008001000\",\"businessTime\":\"2026-05-19T10:00:00+08:00\",\"dtmf\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.callId").value("call-400"))
                .andExpect(jsonPath("$.data.skillGroupCode").value("support-main"))
                .andExpect(jsonPath("$.data.assignedAgentId").value(9))
                .andExpect(jsonPath("$.data.customerId").value(1001));
    }
}
```

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundCallTestFixtureControllerTest
```

Expected output contains `BUILD SUCCESS` and proves `POST /api/call/test-fixtures/inbound-call` exists under the `test` profile. The same controller is available when the backend runs with `-Dspring-boot.run.profiles=local` for Playwright.

- [ ] **Step 2: Write backend Phase 4 E2E test**

Create `Phase4AgentInboundFlowTest`:

```java
package com.opencallhub.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase4AgentInboundFlowTest {
    @Autowired MockMvc mockMvc;

    @Test
    void agentInboundClosureCreatesRecordsAndReleasesAvailability() throws Exception {
        mockMvc.perform(post("/api/agent/phone-bar/online")
                .contentType("application/json")
                .content("{\"agentId\":9,\"extensionNo\":\"8001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentState").value("AVAILABLE"));

        mockMvc.perform(post("/api/call/test-fixtures/inbound-call")
                .contentType("application/json")
                .content("{\"caller\":\"13800000001\",\"callee\":\"4008001000\",\"businessTime\":\"2026-05-19T10:00:00+08:00\",\"dtmf\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedAgentId").value(9));

        mockMvc.perform(post("/api/agent/phone-bar/calls/call-400/answer").contentType("application/json").content("{\"agentId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commandType").value("ANSWER"));

        mockMvc.perform(post("/api/agent/phone-bar/calls/call-400/hold").contentType("application/json").content("{\"agentId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commandType").value("HOLD"));

        mockMvc.perform(post("/api/agent/phone-bar/calls/call-400/transfer/blind").contentType("application/json").content("{\"agentId\":9,\"targetExtensionNo\":\"8002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commandType").value("BLIND_TRANSFER"));

        mockMvc.perform(post("/api/agent/phone-bar/calls/call-400/hangup").contentType("application/json").content("{\"agentId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commandType").value("HANGUP"));

        mockMvc.perform(post("/api/agent/records/calls/call-400/after-call-work")
                .contentType("application/json")
                .content("{\"agentId\":9,\"customerId\":1001,\"dispositionCode\":\"RESOLVED\",\"serviceSummary\":\"Customer confirmed delivery address\",\"nextAction\":\"No follow-up required\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentState").value("AVAILABLE"));

        mockMvc.perform(get("/api/agent/records/calls/call-400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.callRecord.callId").value("call-400"))
                .andExpect(jsonPath("$.data.recordingRecords[0].recordingId").value(6001))
                .andExpect(jsonPath("$.data.recordingRecords[0].callId").value("call-400"))
                .andExpect(jsonPath("$.data.serviceRecord.serviceRecordId").value(7001))
                .andExpect(jsonPath("$.data.serviceRecord.dispositionCode").value("RESOLVED"));
    }
}
```

- [ ] **Step 3: Run backend Phase 4 E2E**

Run:

```bash
mvn -pl och-api -am test -Dtest=Phase4AgentInboundFlowTest
```

Expected output contains `BUILD SUCCESS` and the test proves the complete backend journey from online to records.

- [ ] **Step 4: Write Playwright Phase 4 E2E**

Create `waihu-app/e2e/phase4-agent-inbound.spec.ts`:

```ts
import { expect, test } from '@playwright/test'

test('phase 4 agent inbound acceptance path', async ({ page, request }) => {
  await page.goto('/agent/workspace')
  await page.getByTestId('go-online').click()

  const inbound = await request.post('/api/call/test-fixtures/inbound-call', {
    data: { caller: '13800000001', callee: '4008001000', businessTime: '2026-05-19T10:00:00+08:00', dtmf: '1' },
  })
  expect(inbound.ok()).toBeTruthy()

  await expect(page.getByText('13800000001')).toBeVisible()
  await expect(page.getByText('support-main')).toBeVisible()
  await page.getByTestId('answer-call').click()
  await page.getByTestId('hold').click()
  await page.getByTestId('unhold').click()
  await page.getByTestId('target-extension').fill('8002')
  await page.getByTestId('blind-transfer').click()
  await page.getByTestId('hangup').click()

  await page.getByTestId('disposition-code').selectOption('RESOLVED')
  await page.getByTestId('service-summary').fill('Customer confirmed delivery address')
  await page.getByTestId('next-action').fill('No follow-up required')
  await page.getByTestId('submit-after-call').click()

  await expect(page.getByText('Service Record')).toBeVisible()
  await expect(page.getByText('RESOLVED')).toBeVisible()
  await expect(page.getByText('Call Record')).toBeVisible()
  await expect(page.getByText('Recording Record')).toBeVisible()
})
```

- [ ] **Step 5: Run full backend verification**

Run:

```bash
mvn -pl och-api,och-esl,och-call,och-acd,och-customer,och-report -am test
```

Expected output contains `BUILD SUCCESS`. Surefire reports include `AgentAvailabilityServiceTest`, `PhoneBarApplicationServiceTest`, `AgentEventApplicationServiceTest`, `PhoneControlCommandServiceTest`, `AfterCallWorkServiceTest`, `CallRecordServiceTest`, `CustomerLookupServiceTest`, `InboundReportAggregationServiceTest`, `RecordingVisibilityServiceTest`, `CustomerVisibilityServiceTest`, `Phase4SecurityAccessTest`, and `Phase4AgentInboundFlowTest`.

- [ ] **Step 6: Run frontend unit tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- agent-workspace-store.spec.ts phone-bar.spec.ts screen-pop-panel.spec.ts customer-card.spec.ts after-call-work-panel.spec.ts
```

Expected output contains passing summaries for all five test files.

- [ ] **Step 7: Run frontend build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains `built in` and exits with code 0.

- [ ] **Step 8: Run Playwright Phase 4 E2E**

Start backend:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Use the Phase 1 Playwright config if it already defines `webServer` for `http://127.0.0.1:5173`. If that config is absent, start the frontend dev server in another terminal before running Playwright:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run dev -- --host 127.0.0.1 --port 5173
```

Then run Playwright in a third terminal:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase4-agent-inbound.spec.ts
```

Expected output shows Vite serving `http://127.0.0.1:5173` either from Phase 1 `webServer` or the explicit `npm run dev` command, and one passing Playwright spec named `phase 4 agent inbound acceptance path`. The browser opens `http://127.0.0.1:5173/agent/workspace`, drives online, screen pop, answer, hold, unhold, blind transfer, hangup, after-call submit, and sees call, recording, and service records.

- [ ] **Step 9: Record manual acceptance evidence**

Capture command output or screenshots proving:

```text
Agent 9 with extension 8001 goes online and ACD availability is true.
Inbound call from 13800000001 creates a screen pop with support-main and customer Li Hua.
Answer, hold, unhold, blind transfer, and hangup commands return ACCEPTED and reconcile to WebSocket state changes.
After-call work with disposition RESOLVED creates a service record and returns agent state AVAILABLE.
Call record, recording record, and service record are visible from the agent workspace.
Inbound daily report shows answeredCalls incremented and avgAfterCallSeconds populated.
Unauthorized user cannot see raw customer phone or recording storage URI.
```

- [ ] **Step 10: Commit checkpoint**

Run:

```bash
git add och-api/src/main/java/com/opencallhub/api/testfixture/InboundCallTestFixtureController.java och-api/src/test/java/com/opencallhub/api/testfixture/InboundCallTestFixtureControllerTest.java och-api/src/test/java/com/opencallhub/api/e2e/Phase4AgentInboundFlowTest.java waihu-app/e2e/phase4-agent-inbound.spec.ts
git commit -m "test: add phase 4 agent inbound e2e"
```

Expected output contains `test: add phase 4 agent inbound e2e`.

## Acceptance Gate

Phase 4 is accepted only when an agent can go online, receive screen pop, answer, hold, transfer, hang up, submit after-call work, and then see call record, recording record, and service record with agent state driving ACD availability.

## Final Phase 4 Verification Commands

Run these after all tasks and commit checkpoints are complete. If Phase 1 Playwright config already starts `http://127.0.0.1:5173`, run only the Maven, Vitest, build, and e2e commands. If it does not, start the frontend dev server in a separate terminal before the e2e command:

```bash
mvn -pl och-api,och-esl,och-call,och-acd,och-customer,och-report -am test
cd e:/workspace/openCallHub/waihu-app && npm run test -- agent-workspace-store.spec.ts phone-bar.spec.ts screen-pop-panel.spec.ts customer-card.spec.ts after-call-work-panel.spec.ts
cd e:/workspace/openCallHub/waihu-app && npm run build
cd e:/workspace/openCallHub/waihu-app && npm run dev -- --host 127.0.0.1 --port 5173
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase4-agent-inbound.spec.ts
```

Expected results:

```text
Backend Maven output contains BUILD SUCCESS.
Frontend unit test output contains passing summaries for agent workspace store, phone bar, screen pop panel, customer card, and after-call work panel tests.
Frontend build exits with code 0 and prints built in.
Playwright config starts or explicit frontend dev-server output serves http://127.0.0.1:5173, and the Playwright report shows one passing spec named phase 4 agent inbound acceptance path.
```

## Self-Review

Spec coverage:
- Agent status state machine and ACD availability are covered by Tasks 1, 2, 7, and 8.
- Phone bar APIs, WebSocket agent events, screen pop, and call controls for answer, hangup, hold, unhold, mute, unmute, blind transfer, and consult transfer are covered by Tasks 2, 3, 5, and 8.
- After-call work, call record, recording record, service record, simple customer association, and inbound report aggregation are covered by Tasks 4, 6, 7, and 8.
- Frontend immersive workspace, tests, exact files, commands, expected results, and commit checkpoints are present in the task list.

Placeholder scan:
- The plan contains no deferred implementation placeholders and every task includes concrete paths, commands, expected results, or code snippets.

Type consistency:
- Shared identifiers use `traceId`, `callId`, `uuid`, `legId`, `agentId`, `queueSessionId`, `customerId`, `recordingId`, `serviceRecordId`, `commandId`, and `eventId` consistently across backend tests, APIs, frontend DTOs, and E2E tests.
