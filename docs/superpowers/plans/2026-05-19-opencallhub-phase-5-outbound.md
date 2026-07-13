# openCallHub Phase 5 Outbound Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Phase 5 outbound center so preview, predictive, and robot outbound tasks can import customers, enforce compliance filters, dial safely, stream progress, and aggregate results.

**Architecture:** Phase 5 builds on Phase 1 foundations, Phase 2 resources and permissions, Phase 3 call/ESL boundaries, and Phase 4 agent workspace. Backend work creates `och-call-task` as the owner of outbound task state, customer rows, filter decisions, retry scheduling, concurrency leasing, predictive tuning, robot handoff boundaries, WebSocket progress events, and result statistics; `och-call` and `och-esl` remain responsible for accepted dialing commands and final call events. Frontend work adds an outbound center under `waihu-app/src/views/outbound` with a task wizard, preview dialing UI, predictive monitor, robot session list, import error review, and statistics pages.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Web MVC, Spring Scheduling, Flyway, MySQL, Redis locks from Phase 1, RabbitMQ/outbox abstraction from Phase 1, FreeSWITCH ESL dialing ports from Phase 3, Phase 4 agent status and phone-bar APIs, JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, Vue 3, TypeScript, Vite, Pinia, Axios, Ant Design Vue, Vitest, Playwright.

---

## Agentic Worker Note

This is planning-only guidance. Execute it only after Phase 1, Phase 2, Phase 3, and Phase 4 acceptance gates pass. Use an isolated worktree at execution time, keep source changes scoped to Phase 5, and do not implement Phase 6 AI voice internals; robot outbound must call the explicit stubbed AI handoff boundary defined in Task 6 until Phase 6 replaces it. Commit commands below are execution checkpoints for implementers; this planning task does not run them.

## Goal

Phase 5 implements:

- Outbound task model for `PREVIEW`, `PREDICTIVE`, and `ROBOT` modes.
- Customer list import with row validation, duplicate detection, DNC filtering, blacklist filtering, and task data-scope isolation.
- Outbound time window validation and retry rules for busy, no-answer, failed, abandoned, and robot-handoff outcomes.
- Preview dialing where an agent claims one customer, reviews it, and manually dials through the Phase 4 phone-bar path.
- Predictive dialing where a scheduler calculates pacing from agent availability, connect rate, average talk seconds, trunk capacity, number-pool capacity, and abandonment thresholds.
- Robot dialing orchestration where scheduled calls create robot sessions and invoke a Phase 5 `RobotOutboundHandoffClient` stub if Phase 6 is not present.
- Number pool and trunk concurrency leases that prevent over-dialing and release capacity on final call events.
- Duplicate dialing prevention with idempotency key `taskId + customerId + attemptNo` and Redis lock key `outbound:dial:{taskId}:{customerId}:{attemptNo}`.
- Task progress WebSocket messages over `/ws/task` and outbound result statistics.
- Backend, frontend, security, concurrency, rate tuning, and three-mode outbound E2E tests.

## Architecture

### Backend boundaries

- `och-call-task` owns outbound task lifecycle, task customer lifecycle, scheduling, filters, retry decisions, dialing idempotency, predictive pacing, robot session records, WebSocket progress payloads, and statistics projections.
- `och-call` owns call session and call event truth. Phase 5 adds outbound call metadata linkage by `taskId`, `taskCustomerId`, `attemptNo`, `dialMode`, and `dialId` while leaving Phase 3 and Phase 4 call control semantics intact.
- `och-esl` owns outbound originate command formatting and dispatch. It accepts a dialing request from `och-call-task` and returns command acceptance; final result still comes from call events.
- `och-resource` remains owner of phone numbers, number pools, trunks, gateways, and concurrency configuration from Phase 2. Phase 5 reads resource capacity and writes only short-lived capacity leases in `och-call-task`.
- `och-compliance` is introduced narrowly for Phase 5 DNC and blacklist checks. Later compliance exports, masking, and audit expansion remain outside Phase 5.
- `och-websocket` owns authenticated `/ws/task` fanout. Messages are filtered by task ownership, supervisor scope, and data permission.
- `och-ai` is not implemented in Phase 5. `RobotOutboundHandoffClient` exposes the stable boundary `startRobotSession(RobotOutboundHandoffRequest request)` and the Phase 5 stub returns `ACCEPTED_STUB` with `handoffMode = "PHASE5_STUB"`.

### State machines

Outbound task state machine:

```text
DRAFT + SUBMIT -> READY
READY + START -> RUNNING
RUNNING + PAUSE -> PAUSED
PAUSED + RESUME -> RUNNING
RUNNING + COMPLETE_WHEN_NO_PENDING_CUSTOMERS -> COMPLETED
RUNNING + CANCEL -> CANCELLED
PAUSED + CANCEL -> CANCELLED
RUNNING + FAIL_FATAL -> FAILED
```

Outbound customer state machine:

```text
IMPORTED + FILTER_PASS -> READY
IMPORTED + FILTER_BLOCK -> BLOCKED
READY + CLAIM_PREVIEW -> CLAIMED
CLAIMED + RELEASE -> READY
CLAIMED + START_DIAL -> DIALING
READY + SCHEDULE_PREDICTIVE -> SCHEDULED
READY + SCHEDULE_ROBOT -> SCHEDULED
SCHEDULED + START_DIAL -> DIALING
DIALING + CONNECTED -> CONNECTED
DIALING + NO_ANSWER -> RETRY_WAIT
DIALING + BUSY -> RETRY_WAIT
DIALING + FAILED -> RETRY_WAIT
DIALING + DNC_DETECTED -> BLOCKED
DIALING + BLACKLIST_DETECTED -> BLOCKED
CONNECTED + AGENT_BRIDGED -> FINISHED
CONNECTED + ROBOT_HANDOFF_ACCEPTED -> ROBOT_ACTIVE
ROBOT_ACTIVE + ROBOT_COMPLETED -> FINISHED
CONNECTED + ABANDONED -> ABANDONED
RETRY_WAIT + RETRY_DUE -> READY
RETRY_WAIT + RETRY_EXHAUSTED -> FINISHED
```

Dial attempt state machine:

```text
CREATED + COMMAND_ACCEPTED -> DIALING
DIALING + CHANNEL_ANSWER -> CONNECTED
DIALING + CHANNEL_HANGUP_NO_ANSWER -> NO_ANSWER
DIALING + CHANNEL_HANGUP_BUSY -> BUSY
DIALING + CHANNEL_HANGUP_FAILED -> FAILED
CONNECTED + BRIDGE_AGENT -> AGENT_BRIDGED
CONNECTED + ROBOT_HANDOFF_ACCEPTED -> ROBOT_HANDOFF
CONNECTED + HANGUP_BEFORE_BRIDGE -> ABANDONED
ANY_NON_FINAL + TIMEOUT -> FAILED
```

### Role owners

- Product Expert: outbound task creation wizard and metrics
- Architect: outbound state machine and task boundaries
- Backend Expert: task scheduling, locks, idempotency, retry rules
- Communications Expert: outbound dialing, bridge, failure handling
- AI Voice Expert: robot outbound handoff to AI session
- Frontend Expert: task wizard, preview UI, predictive monitor, robot session list
- QA Expert: three-mode outbound E2E
- Security Expert: DNC, blacklist, task data scope

## Tech Stack

- Backend runtime: Java 17 and Spring Boot from Phase 1.
- Backend persistence: Flyway SQL migrations and MySQL tables with common `id`, `tenant_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `status`, and `remark` columns where applicable.
- Backend events: Phase 1 RabbitMQ/outbox abstraction with `eventId`, idempotent consumers, retry queue, and dead-letter queue behavior.
- Backend locking: Redis locks from Phase 1 using keys `outbound:claim:{taskId}:{customerId}`, `outbound:dial:{taskId}:{customerId}:{attemptNo}`, `outbound:number-pool:{poolId}`, and `outbound:trunk:{trunkId}`.
- Backend scheduling: Spring Scheduling for local execution, with lock protection so multiple nodes do not run the same task slice.
- Communications: Phase 3 `PhoneControlCommandService` and outbound originate wrapper added in this phase.
- Agent bridge: Phase 4 agent availability and phone-bar state.
- Frontend runtime: Vue 3, TypeScript, Vue Router, Pinia, Axios, Ant Design Vue, and Phase 1 layout/pattern components.
- Frontend tests: Vitest for stores/components/API adapters and Playwright for Phase 5 outbound acceptance paths.

## File Structure

### Backend files to create

- `och-call-task/pom.xml`: Maven module definition for outbound task domain.
- `och-call-task/src/main/resources/db/migration/V10__outbound_task_schema.sql`: outbound task, customer, attempt, filter, retry, concurrency lease, predictive stat, robot session, progress event, and result statistics schema.
- `och-compliance/pom.xml`: narrow DNC and blacklist module for Phase 5.
- `och-compliance/src/main/resources/db/migration/V11__dnc_blacklist_schema.sql`: DNC and blacklist tables with seed records.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTask.java`: task aggregate.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTaskMode.java`: enum `PREVIEW`, `PREDICTIVE`, `ROBOT`.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTaskState.java`: task state enum.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundCustomer.java`: imported customer row aggregate.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundCustomerState.java`: customer state enum.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundDialAttempt.java`: dial attempt aggregate with idempotency key.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/RetryRule.java`: retry configuration value object.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTimeWindow.java`: allowed dialing window value object.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/PredictiveDialingPolicy.java`: target abandon rate, min/max pacing, and tuning limits.
- `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundStatistics.java`: result statistic projection.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTaskStateMachine.java`: validates task transitions.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundCustomerStateMachine.java`: validates customer transitions.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTaskApplicationService.java`: create, submit, start, pause, resume, cancel, and query tasks.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/CustomerImportService.java`: parses CSV rows and creates customer rows plus import errors.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundFilterService.java`: applies DNC, blacklist, duplicate, and data-scope filters.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTimeWindowService.java`: validates local dialing windows.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/RetryRuleService.java`: computes next retry time and exhaustion.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/PreviewDialingService.java`: claim, release, and dial preview customers.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/PredictiveDialingScheduler.java`: scheduled predictive dialing loop.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/PredictiveTuningService.java`: pacing calculation and abandonment protection.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/RobotOutboundScheduler.java`: scheduled robot dialing loop.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/RobotOutboundHandoffClient.java`: Phase 5 AI handoff boundary interface.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/StubRobotOutboundHandoffClient.java`: deterministic stub until Phase 6.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundDialingService.java`: shared dialing orchestration and duplicate prevention.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundConcurrencyLeaseService.java`: number-pool and trunk lease acquire/release.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundCallEventConsumer.java`: reconciles call events into attempts and customers.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/TaskProgressPublisher.java`: emits `/ws/task` messages.
- `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundStatisticsService.java`: aggregates task and daily outbound stats.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/OutboundTaskController.java`: `/api/outbound/tasks/**` APIs.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/OutboundImportController.java`: `/api/outbound/tasks/{taskId}/customers/import` APIs.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/PreviewDialingController.java`: `/api/outbound/preview/**` APIs.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/PredictiveDialingController.java`: `/api/outbound/predictive/**` APIs.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/RobotOutboundController.java`: `/api/outbound/robot/**` APIs.
- `och-call-task/src/main/java/com/opencallhub/calltask/api/OutboundStatisticsController.java`: `/api/outbound/statistics/**` APIs.
- `och-compliance/src/main/java/com/opencallhub/compliance/application/DncBlacklistService.java`: DNC and blacklist lookup service.
- `och-compliance/src/main/java/com/opencallhub/compliance/api/DncBlacklistController.java`: admin APIs for Phase 5 DNC and blacklist records.
- `och-websocket/src/main/java/com/opencallhub/websocket/task/TaskProgressMessage.java`: typed task message envelope.
- `och-websocket/src/main/java/com/opencallhub/websocket/task/TaskProgressMessagePublisher.java`: authenticated `/ws/task` publisher.
- `och-api/src/main/java/com/opencallhub/api/testfixture/OutboundPhase5TestFixtureController.java`: `local` and `test` profile endpoints for deterministic preview, predictive, and robot E2E setup.

### Backend files to modify

- `pom.xml`: add `och-call-task` and `och-compliance` after `och-call` and before `och-ai`.
- `och-api/pom.xml`: add dependencies on `och-call-task` and `och-compliance`.
- `och-api/src/main/resources/application.yml`: include module migration locations if classpath scanning does not include them.
- `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`: add outbound originate wrapper with caller number, callee number, trunk gateway, SIP headers, and `dialId` metadata.
- `och-call/src/main/java/com/opencallhub/call/domain/CallSession.java`: add outbound metadata fields if Phase 3 call session does not already have them.
- `och-call/src/main/java/com/opencallhub/call/application/CallSessionService.java`: create outbound call session from accepted outbound dial request.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`: add outbound permission constants.
- `och-websocket/src/main/java/com/opencallhub/websocket/config/WebSocketConfig.java`: register `/ws/task` if Phase 1 only registered generic infrastructure.

### Backend test files to create

- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundTaskStateMachineTest.java`: task and customer state transitions.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/CustomerImportServiceTest.java`: CSV import, invalid row reporting, duplicate phone reporting.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundFilterServiceTest.java`: DNC, blacklist, duplicate, and data-scope blocks.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundTimeWindowServiceTest.java`: allowed and blocked dialing times.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/RetryRuleServiceTest.java`: retry intervals and exhaustion.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/PreviewDialingServiceTest.java`: claim, release, duplicate claim prevention, and preview dial idempotency.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundDialingServiceDuplicateTest.java`: duplicate dialing prevention by idempotency key.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundConcurrencyLeaseServiceTest.java`: number-pool and trunk capacity enforcement.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/PredictiveTuningServiceTest.java`: rate tuning and abandonment protection.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/PredictiveDialingSchedulerTest.java`: predictive scheduler respects agent and trunk capacity.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/RobotOutboundSchedulerTest.java`: robot scheduling creates sessions and calls handoff boundary.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/TaskProgressPublisherTest.java`: task progress message payload and data-scope filtering.
- `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundStatisticsServiceTest.java`: result aggregation.
- `och-compliance/src/test/java/com/opencallhub/compliance/application/DncBlacklistServiceTest.java`: DNC and blacklist matching.
- `och-esl/src/test/java/com/opencallhub/esl/application/OutboundOriginateCommandTest.java`: originate command formatting.
- `och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundFlowTest.java`: backend E2E for preview, predictive, and robot flows.
- `och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundSecurityTest.java`: data-scope, DNC, blacklist, and WebSocket visibility regression.

### Frontend files to create

- `waihu-app/src/types/outbound.ts`: outbound task, customer, attempt, import, predictive, robot, WebSocket, and statistics DTOs.
- `waihu-app/src/services/outboundApi.ts`: typed REST client for outbound APIs.
- `waihu-app/src/stores/outboundTask.ts`: task list, detail, wizard draft, import result, progress, and statistics state.
- `waihu-app/src/stores/previewDialing.ts`: preview claim, customer detail, dial command, and result state.
- `waihu-app/src/stores/predictiveMonitor.ts`: pacing, abandon rate, concurrency, and live task progress state.
- `waihu-app/src/stores/robotOutbound.ts`: robot task sessions and handoff status state.
- `waihu-app/src/views/outbound/OutboundTaskListView.vue`: task list and status actions.
- `waihu-app/src/views/outbound/OutboundTaskWizardView.vue`: creation wizard for task mode, import, filters, retry, time windows, resources, and review.
- `waihu-app/src/views/outbound/PreviewDialingView.vue`: agent claim and manual dial page.
- `waihu-app/src/views/outbound/PredictiveMonitorView.vue`: predictive live monitor and tuning display.
- `waihu-app/src/views/outbound/RobotOutboundSessionsView.vue`: robot session list and outcome display.
- `waihu-app/src/views/outbound/OutboundStatisticsView.vue`: task and daily result statistics.
- `waihu-app/src/components/outbound/OutboundModeStep.vue`: mode selection step.
- `waihu-app/src/components/outbound/CustomerImportStep.vue`: CSV upload and import result step.
- `waihu-app/src/components/outbound/FilterRuleStep.vue`: DNC, blacklist, and duplicate filter configuration step.
- `waihu-app/src/components/outbound/TimeWindowRetryStep.vue`: time window and retry rule step.
- `waihu-app/src/components/outbound/ResourceConcurrencyStep.vue`: number pool, trunk, and concurrency step.
- `waihu-app/src/components/outbound/PreviewClaimPanel.vue`: preview customer card and claim actions.
- `waihu-app/src/components/outbound/PredictiveTuningPanel.vue`: pacing, connect rate, abandon threshold, and capacity cards.
- `waihu-app/src/components/outbound/RobotSessionList.vue`: robot handoff session table.
- `waihu-app/src/components/outbound/TaskProgressTimeline.vue`: WebSocket-driven progress event timeline.

### Frontend files to modify

- `waihu-app/src/router/routes.ts`: add outbound routes and permission metadata.
- `waihu-app/src/services/wsClient.ts`: add `/ws/task` subscription helper if the Phase 1 wrapper only has generic connection functions.
- `waihu-app/src/components/menu/AppMenu.vue`: render outbound center entries from Phase 2 menu metadata.

### Frontend test files to create

- `waihu-app/src/tests/outbound-task-store.spec.ts`: task wizard and progress state.
- `waihu-app/src/tests/outbound-task-wizard.spec.ts`: wizard mode, filter, time window, retry, and resource validation rendering.
- `waihu-app/src/tests/preview-dialing.spec.ts`: preview claim and manual dial behavior.
- `waihu-app/src/tests/predictive-monitor.spec.ts`: predictive metrics and abandonment warning display.
- `waihu-app/src/tests/robot-session-list.spec.ts`: robot handoff session rendering.
- `waihu-app/e2e/phase5-outbound.spec.ts`: Playwright three-mode outbound acceptance path.

## Bite-Sized Tasks

### Task 1: Module Wiring, Schema, and State Machines

**Owner:** Backend Expert

**Files:**
- Modify: `pom.xml`
- Modify: `och-api/pom.xml`
- Create: `och-call-task/pom.xml`
- Create: `och-compliance/pom.xml`
- Create: `och-call-task/src/main/resources/db/migration/V10__outbound_task_schema.sql`
- Create: `och-compliance/src/main/resources/db/migration/V11__dnc_blacklist_schema.sql`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTaskMode.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundTaskState.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/domain/OutboundCustomerState.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTaskStateMachine.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundCustomerStateMachine.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundTaskStateMachineTest.java`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`

- [ ] **Step 1: Verify Phase 1 through Phase 4 gates before source changes**

Run from `e:/workspace/openCallHub`:

```bash
mvn -pl och-api,och-system,och-resource,och-esl,och-call,och-ivr,och-acd,och-customer,och-report,och-websocket -am test
cd e:/workspace/openCallHub/waihu-app && npm run test
```

Expected output contains `BUILD SUCCESS` for Maven and a passing frontend test summary. If either command fails because Phase 1, Phase 2, Phase 3, or Phase 4 files are missing, stop and finish those phases before starting this plan.

- [ ] **Step 2: Add Phase 5 Maven modules**

Modify the root `pom.xml` module list so it contains these entries after `och-call`:

```xml
<module>och-call-task</module>
<module>och-compliance</module>
```

Create `och-call-task/pom.xml`:

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
    <artifactId>och-call-task</artifactId>
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
            <artifactId>och-resource</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-call</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-esl</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-compliance</artifactId>
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

Create `och-compliance/pom.xml`:

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
    <artifactId>och-compliance</artifactId>
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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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
    <artifactId>och-call-task</artifactId>
</dependency>
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-compliance</artifactId>
</dependency>
```

Run:

```bash
mvn -pl och-call-task,och-compliance -am test -DskipTests
```

Expected output contains `BUILD SUCCESS` and no `Could not find artifact` errors.

- [ ] **Step 3: Create outbound task schema**

Create `och-call-task/src/main/resources/db/migration/V10__outbound_task_schema.sql`:

```sql
CREATE TABLE outbound_task (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_name VARCHAR(120) NOT NULL,
    task_mode VARCHAR(32) NOT NULL,
    task_state VARCHAR(32) NOT NULL,
    customer_pool_id BIGINT,
    number_pool_id BIGINT NOT NULL,
    trunk_id BIGINT NOT NULL,
    skill_group_id BIGINT,
    robot_id BIGINT,
    max_concurrency INT NOT NULL DEFAULT 1,
    predictive_min_pacing DECIMAL(6,2) NOT NULL DEFAULT 1.00,
    predictive_max_pacing DECIMAL(6,2) NOT NULL DEFAULT 3.00,
    predictive_target_abandon_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0300,
    allowed_weekdays VARCHAR(32) NOT NULL DEFAULT '1,2,3,4,5',
    daily_start_time VARCHAR(8) NOT NULL DEFAULT '09:00:00',
    daily_end_time VARCHAR(8) NOT NULL DEFAULT '18:00:00',
    retry_busy_minutes INT NOT NULL DEFAULT 30,
    retry_no_answer_minutes INT NOT NULL DEFAULT 60,
    retry_failure_minutes INT NOT NULL DEFAULT 120,
    max_attempts INT NOT NULL DEFAULT 3,
    started_at DATETIME,
    ended_at DATETIME,
    trace_id VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_outbound_task_state (tenant_id, task_state, task_mode, deleted),
    KEY idx_outbound_task_created_by (tenant_id, created_by, created_at)
);

CREATE TABLE outbound_task_customer (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    customer_id BIGINT,
    customer_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    masked_phone VARCHAR(32) NOT NULL,
    customer_state VARCHAR(32) NOT NULL,
    claimed_by_agent_id BIGINT,
    claimed_at DATETIME,
    attempt_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME,
    last_attempt_id BIGINT,
    filter_block_reason VARCHAR(120),
    external_row_no INT,
    raw_payload JSON,
    trace_id VARCHAR(64),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_outbound_task_phone (tenant_id, task_id, phone_number, deleted),
    KEY idx_outbound_customer_state_retry (tenant_id, task_id, customer_state, next_retry_at),
    KEY idx_outbound_customer_claimed (tenant_id, task_id, claimed_by_agent_id, customer_state)
);

CREATE TABLE outbound_dial_attempt (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    task_customer_id BIGINT NOT NULL,
    attempt_no INT NOT NULL,
    dial_id VARCHAR(96) NOT NULL,
    idempotency_key VARCHAR(160) NOT NULL,
    dial_mode VARCHAR(32) NOT NULL,
    caller_number VARCHAR(32) NOT NULL,
    callee_number VARCHAR(32) NOT NULL,
    trunk_id BIGINT NOT NULL,
    number_pool_id BIGINT NOT NULL,
    attempt_state VARCHAR(32) NOT NULL,
    call_id VARCHAR(64),
    fs_uuid VARCHAR(96),
    failure_reason VARCHAR(300),
    started_at DATETIME NOT NULL,
    connected_at DATETIME,
    ended_at DATETIME,
    trace_id VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_outbound_attempt_idempotency (tenant_id, idempotency_key),
    UNIQUE KEY uk_outbound_attempt_dial_id (tenant_id, dial_id),
    KEY idx_outbound_attempt_task (tenant_id, task_id, started_at),
    KEY idx_outbound_attempt_customer (tenant_id, task_customer_id, attempt_no)
);

CREATE TABLE outbound_import_error (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    row_no INT NOT NULL,
    phone_number VARCHAR(32),
    error_code VARCHAR(64) NOT NULL,
    error_message VARCHAR(300) NOT NULL,
    raw_payload JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_outbound_import_error_task (tenant_id, task_id, row_no)
);

CREATE TABLE outbound_filter_decision (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    task_customer_id BIGINT NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    decision VARCHAR(32) NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    reason_message VARCHAR(300) NOT NULL,
    decided_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trace_id VARCHAR(64) NOT NULL,
    KEY idx_outbound_filter_task (tenant_id, task_id, decision),
    KEY idx_outbound_filter_customer (tenant_id, task_customer_id)
);

CREATE TABLE outbound_concurrency_lease (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    lease_key VARCHAR(160) NOT NULL,
    task_id BIGINT NOT NULL,
    dial_id VARCHAR(96) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_id BIGINT NOT NULL,
    acquired_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    released_at DATETIME,
    lease_state VARCHAR(32) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_outbound_lease_key (tenant_id, lease_key),
    KEY idx_outbound_lease_resource (tenant_id, resource_type, resource_id, lease_state, expires_at)
);

CREATE TABLE outbound_predictive_stat (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    stat_minute DATETIME NOT NULL,
    available_agents INT NOT NULL DEFAULT 0,
    active_dials INT NOT NULL DEFAULT 0,
    connected_calls INT NOT NULL DEFAULT 0,
    abandoned_calls INT NOT NULL DEFAULT 0,
    connect_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
    abandon_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
    avg_talk_seconds DECIMAL(10,2) NOT NULL DEFAULT 0,
    pacing_ratio DECIMAL(6,2) NOT NULL DEFAULT 1.00,
    recommended_dials INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_outbound_predictive_stat (tenant_id, task_id, stat_minute)
);

CREATE TABLE outbound_robot_session (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    task_customer_id BIGINT NOT NULL,
    attempt_id BIGINT NOT NULL,
    call_id VARCHAR(64),
    robot_id BIGINT NOT NULL,
    ai_session_id VARCHAR(96),
    handoff_status VARCHAR(32) NOT NULL,
    handoff_mode VARCHAR(32) NOT NULL,
    handoff_failure_reason VARCHAR(300),
    started_at DATETIME NOT NULL,
    ended_at DATETIME,
    trace_id VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_outbound_robot_attempt (tenant_id, attempt_id),
    KEY idx_outbound_robot_task (tenant_id, task_id, handoff_status, started_at)
);

CREATE TABLE outbound_task_progress_event (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    event_payload JSON NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_outbound_progress_task (tenant_id, task_id, created_at)
);

CREATE TABLE outbound_task_statistics (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    task_id BIGINT NOT NULL,
    imported_count INT NOT NULL DEFAULT 0,
    blocked_count INT NOT NULL DEFAULT 0,
    dialed_count INT NOT NULL DEFAULT 0,
    connected_count INT NOT NULL DEFAULT 0,
    no_answer_count INT NOT NULL DEFAULT 0,
    busy_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    abandoned_count INT NOT NULL DEFAULT 0,
    robot_completed_count INT NOT NULL DEFAULT 0,
    finished_count INT NOT NULL DEFAULT 0,
    connect_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
    abandonment_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0000,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_outbound_task_statistics (tenant_id, task_id)
);

INSERT INTO outbound_task (id, tenant_id, task_name, task_mode, task_state, number_pool_id, trunk_id, skill_group_id, robot_id, max_concurrency, created_by, updated_by, trace_id)
VALUES
(5001, 1, 'Phase 5 Preview Fixture', 'PREVIEW', 'READY', 101, 201, 301, NULL, 1, 1, 1, 'trace-phase5-preview'),
(5002, 1, 'Phase 5 Predictive Fixture', 'PREDICTIVE', 'READY', 101, 201, 301, NULL, 5, 1, 1, 'trace-phase5-predictive'),
(5003, 1, 'Phase 5 Robot Fixture', 'ROBOT', 'READY', 101, 201, NULL, 901, 3, 1, 1, 'trace-phase5-robot');
```

- [ ] **Step 4: Create DNC and blacklist schema**

Create `och-compliance/src/main/resources/db/migration/V11__dnc_blacklist_schema.sql`:

```sql
CREATE TABLE compliance_dnc_number (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    phone_number VARCHAR(32) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    effective_at DATETIME NOT NULL,
    expires_at DATETIME,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dnc_phone (tenant_id, phone_number, deleted),
    KEY idx_dnc_status (tenant_id, status, effective_at, expires_at)
);

CREATE TABLE compliance_blacklist_number (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    phone_number VARCHAR(32) NOT NULL,
    reason VARCHAR(200) NOT NULL,
    source VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_blacklist_phone (tenant_id, phone_number, deleted),
    KEY idx_blacklist_status (tenant_id, status, source)
);

INSERT INTO compliance_dnc_number (id, tenant_id, phone_number, reason, effective_at, status, created_by, updated_by)
VALUES (9001, 1, '13900000001', 'Customer opted out during Phase 5 fixture', '2026-05-19 00:00:00', 'ENABLED', 1, 1);

INSERT INTO compliance_blacklist_number (id, tenant_id, phone_number, reason, source, status, created_by, updated_by)
VALUES (9101, 1, '13900000002', 'Fraud complaint fixture', 'MANUAL', 'ENABLED', 1, 1);
```

- [ ] **Step 5: Write state machine tests**

Create `OutboundTaskStateMachineTest`:

```java
package com.opencallhub.calltask.application;

import com.opencallhub.calltask.domain.OutboundCustomerState;
import com.opencallhub.calltask.domain.OutboundTaskState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundTaskStateMachineTest {
    @Test
    void taskTransitionsFromDraftToCompletedThroughRunning() {
        OutboundTaskStateMachine task = new OutboundTaskStateMachine();

        assertThat(task.transition(OutboundTaskState.DRAFT, "SUBMIT")).isEqualTo(OutboundTaskState.READY);
        assertThat(task.transition(OutboundTaskState.READY, "START")).isEqualTo(OutboundTaskState.RUNNING);
        assertThat(task.transition(OutboundTaskState.RUNNING, "PAUSE")).isEqualTo(OutboundTaskState.PAUSED);
        assertThat(task.transition(OutboundTaskState.PAUSED, "RESUME")).isEqualTo(OutboundTaskState.RUNNING);
        assertThat(task.transition(OutboundTaskState.RUNNING, "COMPLETE_WHEN_NO_PENDING_CUSTOMERS")).isEqualTo(OutboundTaskState.COMPLETED);
    }

    @Test
    void customerTransitionsBlockFilteredNumbersAndFinishRetries() {
        OutboundCustomerStateMachine customer = new OutboundCustomerStateMachine();

        assertThat(customer.transition(OutboundCustomerState.IMPORTED, "FILTER_BLOCK")).isEqualTo(OutboundCustomerState.BLOCKED);
        assertThat(customer.transition(OutboundCustomerState.READY, "CLAIM_PREVIEW")).isEqualTo(OutboundCustomerState.CLAIMED);
        assertThat(customer.transition(OutboundCustomerState.CLAIMED, "START_DIAL")).isEqualTo(OutboundCustomerState.DIALING);
        assertThat(customer.transition(OutboundCustomerState.DIALING, "NO_ANSWER")).isEqualTo(OutboundCustomerState.RETRY_WAIT);
        assertThat(customer.transition(OutboundCustomerState.RETRY_WAIT, "RETRY_EXHAUSTED")).isEqualTo(OutboundCustomerState.FINISHED);
    }

    @Test
    void invalidTaskTransitionIsRejected() {
        OutboundTaskStateMachine task = new OutboundTaskStateMachine();

        assertThatThrownBy(() -> task.transition(OutboundTaskState.DRAFT, "START"))
                .hasMessageContaining("Invalid outbound task transition DRAFT -> START");
    }
}
```

- [ ] **Step 6: Implement enums and state machines**

Create these enums exactly:

```java
public enum OutboundTaskMode { PREVIEW, PREDICTIVE, ROBOT }
public enum OutboundTaskState { DRAFT, READY, RUNNING, PAUSED, COMPLETED, CANCELLED, FAILED }
public enum OutboundCustomerState { IMPORTED, READY, BLOCKED, CLAIMED, SCHEDULED, DIALING, CONNECTED, ROBOT_ACTIVE, RETRY_WAIT, ABANDONED, FINISHED }
```

`OutboundTaskStateMachine.transition` and `OutboundCustomerStateMachine.transition` must implement the transition tables in this plan and throw `IllegalStateException` with message `Invalid outbound task transition {state} -> {event}` or `Invalid outbound customer transition {state} -> {event}` for rejected transitions.

- [ ] **Step 7: Extend migration smoke test**

Add assertions to `och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java`:

```java
assertThat(jdbcTemplate.queryForObject("select count(*) from outbound_task where id in (5001, 5002, 5003)", Integer.class)).isEqualTo(3);
assertThat(jdbcTemplate.queryForObject("select count(*) from compliance_dnc_number where phone_number = '13900000001'", Integer.class)).isEqualTo(1);
assertThat(jdbcTemplate.queryForObject("select count(*) from compliance_blacklist_number where phone_number = '13900000002'", Integer.class)).isEqualTo(1);
assertThat(jdbcTemplate.queryForObject("select count(*) from information_schema.tables where table_name = 'outbound_dial_attempt'", Integer.class)).isGreaterThanOrEqualTo(1);
```

Run:

```bash
mvn -pl och-api,och-call-task -am test -Dtest=MigrationSmokeTest,OutboundTaskStateMachineTest
```

Expected output contains `BUILD SUCCESS` and proves Phase 5 tables plus seed rows are queryable.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add pom.xml och-api/pom.xml och-call-task/pom.xml och-compliance/pom.xml och-call-task/src/main/resources/db/migration/V10__outbound_task_schema.sql och-compliance/src/main/resources/db/migration/V11__dnc_blacklist_schema.sql och-call-task/src/main/java/com/opencallhub/calltask/domain och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTaskStateMachine.java och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundCustomerStateMachine.java och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundTaskStateMachineTest.java och-api/src/test/java/com/opencallhub/api/e2e/MigrationSmokeTest.java
git commit -m "feat: add outbound task foundation"
```

Expected output contains `feat: add outbound task foundation`.

### Task 2: Customer Import, Filters, Time Windows, and Retry Rules

**Owner:** Backend Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/CustomerImportService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundFilterService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundTimeWindowService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/RetryRuleService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/api/OutboundImportController.java`
- Create: `och-compliance/src/main/java/com/opencallhub/compliance/application/DncBlacklistService.java`
- Create: `och-compliance/src/main/java/com/opencallhub/compliance/api/DncBlacklistController.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/CustomerImportServiceTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundFilterServiceTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundTimeWindowServiceTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/RetryRuleServiceTest.java`
- Test: `och-compliance/src/test/java/com/opencallhub/compliance/application/DncBlacklistServiceTest.java`

- [ ] **Step 1: Write CSV import test**

Create `CustomerImportServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerImportServiceTest {
    @Test
    void importsValidRowsAndReportsInvalidAndDuplicateRows() {
        CustomerImportService service = CustomerImportService.inMemoryFixture();
        String csv = "customerName,phoneNumber,customerId\n" +
                "Alice,13800000001,1001\n" +
                "NoPhone,,1002\n" +
                "Duplicate,13800000001,1003\n" +
                "Bob,13800000002,1004\n";

        var result = service.importCsv(5001L, csv, 7L, "trace-import-1");

        assertThat(result.importedCount()).isEqualTo(2);
        assertThat(result.errorCount()).isEqualTo(2);
        assertThat(result.errors()).extracting("errorCode").containsExactly("PHONE_REQUIRED", "DUPLICATE_IN_FILE");
        assertThat(result.customers()).extracting("phoneNumber").containsExactly("13800000001", "13800000002");
    }
}
```

- [ ] **Step 2: Implement import service and API**

`CustomerImportService.importCsv(taskId, csvContent, operatorId, traceId)` must parse these columns exactly:

```text
customerName,phoneNumber,customerId
```

Validation rules:

```text
customerName blank -> CUSTOMER_NAME_REQUIRED
phoneNumber blank -> PHONE_REQUIRED
phoneNumber not matching ^1[3-9][0-9]{9}$ -> PHONE_INVALID
same phoneNumber appears twice in one file -> DUPLICATE_IN_FILE
same phoneNumber already exists for taskId -> DUPLICATE_IN_TASK
```

Create `POST /api/outbound/tasks/{taskId}/customers/import` accepting multipart file field `file`. Response data must contain `importedCount`, `errorCount`, `customers`, and `errors`.

- [ ] **Step 3: Write DNC and blacklist service test**

Create `DncBlacklistServiceTest`:

```java
package com.opencallhub.compliance.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DncBlacklistServiceTest {
    @Test
    void detectsEnabledDncAndBlacklistNumbers() {
        DncBlacklistService service = DncBlacklistService.inMemoryFixture();

        assertThat(service.check("13900000001").dnc()).isTrue();
        assertThat(service.check("13900000001").reasonCode()).isEqualTo("DNC");
        assertThat(service.check("13900000002").blacklisted()).isTrue();
        assertThat(service.check("13900000002").reasonCode()).isEqualTo("BLACKLIST");
        assertThat(service.check("13800000001").allowed()).isTrue();
    }
}
```

- [ ] **Step 4: Write outbound filter test**

Create `OutboundFilterServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundFilterServiceTest {
    @Test
    void blocksDncBlacklistDuplicateAndOutOfScopeCustomers() {
        OutboundFilterService service = OutboundFilterService.inMemoryFixture();

        var decisions = service.applyFilters(5001L, 7L, "trace-filter-1");

        assertThat(decisions).extracting("phoneNumber", "decision", "reasonCode").contains(
                org.assertj.core.groups.Tuple.tuple("13900000001", "BLOCK", "DNC"),
                org.assertj.core.groups.Tuple.tuple("13900000002", "BLOCK", "BLACKLIST"),
                org.assertj.core.groups.Tuple.tuple("13800000001", "PASS", "ELIGIBLE"),
                org.assertj.core.groups.Tuple.tuple("13800000003", "BLOCK", "DATA_SCOPE_DENIED")
        );
    }
}
```

- [ ] **Step 5: Implement DNC, blacklist, duplicate, and data-scope filters**

`OutboundFilterService.applyFilters(taskId, operatorId, traceId)` must write one `outbound_filter_decision` per customer and update `outbound_task_customer.customer_state`:

```text
DNC -> BLOCKED with filter_block_reason DNC
BLACKLIST -> BLOCKED with filter_block_reason BLACKLIST
DUPLICATE_IN_TASK -> BLOCKED with filter_block_reason DUPLICATE_IN_TASK
DATA_SCOPE_DENIED -> BLOCKED with filter_block_reason DATA_SCOPE_DENIED
ELIGIBLE -> READY
```

Data-scope rule for Phase 5: an operator can use a customer only when `customer_pool_id` is in the operator's permitted customer pools from Phase 2 or when `created_by = operatorId` for imported rows without a customer pool. The service must not reveal full phone numbers in decision query responses unless permission `outbound:phone:full` is present.

- [ ] **Step 6: Write time window test**

Create `OutboundTimeWindowServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundTimeWindowServiceTest {
    @Test
    void allowsWeekdayBusinessHoursAndBlocksOutsideWindow() {
        OutboundTimeWindowService service = new OutboundTimeWindowService();
        var window = service.window("1,2,3,4,5", "09:00:00", "18:00:00");

        assertThat(service.canDial(window, LocalDateTime.parse("2026-05-19T10:15:00"))).isTrue();
        assertThat(service.canDial(window, LocalDateTime.parse("2026-05-19T08:59:59"))).isFalse();
        assertThat(service.canDial(window, LocalDateTime.parse("2026-05-19T18:00:01"))).isFalse();
        assertThat(service.canDial(window, LocalDateTime.parse("2026-05-23T10:15:00"))).isFalse();
    }
}
```

- [ ] **Step 7: Implement time window validation**

`OutboundTimeWindowService.canDial(window, now)` must use ISO weekdays `1` through `7`, inclusive start time, inclusive exact end time only at `18:00:00`, and reject blank weekday or blank time values by throwing `IllegalArgumentException` with message `Outbound time window requires weekdays, start time, and end time`.

- [ ] **Step 8: Write retry rule test**

Create `RetryRuleServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RetryRuleServiceTest {
    @Test
    void computesRetryTimeAndExhaustionByResult() {
        RetryRuleService service = new RetryRuleService();
        var base = LocalDateTime.parse("2026-05-19T10:00:00");
        var rule = service.rule(30, 60, 120, 3);

        assertThat(service.nextRetryAt(rule, "BUSY", 1, base)).isEqualTo(LocalDateTime.parse("2026-05-19T10:30:00"));
        assertThat(service.nextRetryAt(rule, "NO_ANSWER", 2, base)).isEqualTo(LocalDateTime.parse("2026-05-19T11:00:00"));
        assertThat(service.nextRetryAt(rule, "FAILED", 2, base)).isEqualTo(LocalDateTime.parse("2026-05-19T12:00:00"));
        assertThat(service.isExhausted(rule, 3)).isTrue();
        assertThat(service.isExhausted(rule, 2)).isFalse();
    }
}
```

- [ ] **Step 9: Implement retry rules**

`RetryRuleService.nextRetryAt` maps results exactly:

```text
BUSY -> retry_busy_minutes
NO_ANSWER -> retry_no_answer_minutes
FAILED -> retry_failure_minutes
ABANDONED -> retry_failure_minutes
```

`RetryRuleService.isExhausted(rule, attemptNo)` returns `true` when `attemptNo >= maxAttempts`.

- [ ] **Step 10: Run import and filter tests**

Run:

```bash
mvn -pl och-call-task,och-compliance -am test -Dtest=CustomerImportServiceTest,OutboundFilterServiceTest,OutboundTimeWindowServiceTest,RetryRuleServiceTest,DncBlacklistServiceTest
```

Expected output contains `BUILD SUCCESS` and all five test classes pass.

- [ ] **Step 11: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java och-compliance/src/main/java och-compliance/src/test/java
git commit -m "feat: add outbound import filters and retry rules"
```

Expected output contains `feat: add outbound import filters and retry rules`.

### Task 3: Preview Dialing, Idempotency, and Duplicate Dial Prevention

**Owner:** Backend Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/PreviewDialingService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundDialingService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundCallEventConsumer.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/api/PreviewDialingController.java`
- Modify: `och-esl/src/main/java/com/opencallhub/esl/application/PhoneControlCommandService.java`
- Modify: `och-call/src/main/java/com/opencallhub/call/application/CallSessionService.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/PreviewDialingServiceTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundDialingServiceDuplicateTest.java`
- Test: `och-esl/src/test/java/com/opencallhub/esl/application/OutboundOriginateCommandTest.java`

- [ ] **Step 1: Write preview service test**

Create `PreviewDialingServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PreviewDialingServiceTest {
    @Test
    void agentClaimsReleasesAndDialsOneCustomer() {
        PreviewDialingService service = PreviewDialingService.inMemoryFixture();

        var claim = service.claimNext(5001L, 9L, "trace-preview-1");
        assertThat(claim.customerId()).isEqualTo(8001L);
        assertThat(claim.customerState()).isEqualTo("CLAIMED");
        assertThat(claim.maskedPhone()).isEqualTo("138****0001");

        service.releaseClaim(5001L, 8001L, 9L, "trace-preview-2");
        var secondClaim = service.claimNext(5001L, 9L, "trace-preview-3");
        assertThat(secondClaim.customerId()).isEqualTo(8001L);

        var dial = service.dialClaimedCustomer(5001L, 8001L, 9L, "trace-preview-4");
        assertThat(dial.dialMode()).isEqualTo("PREVIEW");
        assertThat(dial.commandStatus()).isEqualTo("ACCEPTED");
        assertThat(dial.idempotencyKey()).isEqualTo("5001:8001:1");
    }

    @Test
    void secondAgentCannotClaimAlreadyClaimedCustomer() {
        PreviewDialingService service = PreviewDialingService.inMemoryFixture();
        service.claimNext(5001L, 9L, "trace-preview-5");

        assertThatThrownBy(() -> service.claimCustomer(5001L, 8001L, 10L, "trace-preview-6"))
                .hasMessageContaining("Outbound customer 8001 is already claimed by agent 9");
    }
}
```

- [ ] **Step 2: Implement preview claim and API**

Create `PreviewDialingController` endpoints:

```text
POST /api/outbound/preview/tasks/{taskId}/claim-next
POST /api/outbound/preview/tasks/{taskId}/customers/{taskCustomerId}/claim
POST /api/outbound/preview/tasks/{taskId}/customers/{taskCustomerId}/release
POST /api/outbound/preview/tasks/{taskId}/customers/{taskCustomerId}/dial
```

Claim rules:

```text
Only task_mode PREVIEW can use preview APIs.
Only READY customers can be claimed.
Claim uses Redis lock outbound:claim:{taskId}:{taskCustomerId} with 30 second lease.
A customer claimed by one agent cannot be claimed by another agent.
The response returns masked phone unless permission outbound:phone:full exists.
```

- [ ] **Step 3: Write outbound originate command test**

Create `OutboundOriginateCommandTest`:

```java
package com.opencallhub.esl.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundOriginateCommandTest {
    @Test
    void formatsOriginateWithTaskAndDialMetadata() {
        PhoneControlCommandService service = PhoneControlCommandService.inMemoryForTest();

        var command = service.outboundOriginate("13800000001", "4008001000", "gw-main", "dial-5001-8001-1", "task-5001", "trace-dial-1");

        assertThat(command.command()).isEqualTo("originate {origination_caller_id_number=4008001000,sip_h_X-OCH-Dial-Id=dial-5001-8001-1,sip_h_X-OCH-Task-Id=task-5001,sip_h_X-OCH-Trace-Id=trace-dial-1}sofia/gateway/gw-main/13800000001 &park()");
    }
}
```

- [ ] **Step 4: Implement outbound originate wrapper**

Add method to `PhoneControlCommandService`:

```java
PhoneControlCommandResult outboundOriginate(String calleeNumber, String callerNumber, String gatewayName, String dialId, String taskHeader, String traceId);
```

The command string must match the test exactly. Production dispatch must persist outbound call metadata before dispatch so duplicate ESL responses or delayed events can be reconciled by `dialId` and `callId`.

- [ ] **Step 5: Write duplicate dialing prevention test**

Create `OutboundDialingServiceDuplicateTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundDialingServiceDuplicateTest {
    @Test
    void concurrentDialRequestsCreateOneAttemptForSameTaskCustomerAndAttemptNo() throws Exception {
        OutboundDialingService service = OutboundDialingService.inMemoryFixture();
        var pool = Executors.newFixedThreadPool(8);

        for (int i = 0; i < 8; i++) {
            pool.submit(() -> service.dial(5001L, 8001L, 1, "PREVIEW", 9L, "trace-duplicate"));
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(service.attemptCount("5001:8001:1")).isEqualTo(1);
        assertThat(service.commandDispatchCount("5001:8001:1")).isEqualTo(1);
    }
}
```

- [ ] **Step 6: Implement shared dialing idempotency**

`OutboundDialingService.dial(taskId, taskCustomerId, attemptNo, dialMode, operatorId, traceId)` must:

```text
Build idempotencyKey as {taskId}:{taskCustomerId}:{attemptNo}.
Acquire Redis lock outbound:dial:{taskId}:{taskCustomerId}:{attemptNo} for 15 seconds.
If outbound_dial_attempt already exists for idempotencyKey, return the existing attempt without dispatching a second command.
Validate task state RUNNING or READY for manual preview start.
Validate customer state CLAIMED for PREVIEW or SCHEDULED for PREDICTIVE/ROBOT.
Validate DNC, blacklist, time window, number-pool lease, and trunk lease immediately before dispatch.
Create outbound_dial_attempt with state CREATED.
Call PhoneControlCommandService.outboundOriginate.
Update attempt state to DIALING when command is accepted.
Publish task progress event DIAL_STARTED.
Release lock in finally block while keeping resource leases until final call event or timeout.
```

- [ ] **Step 7: Implement call event reconciliation**

`OutboundCallEventConsumer` maps normalized call events:

```text
CHANNEL_ANSWER -> attempt CONNECTED and customer CONNECTED
BRIDGE_AGENT -> attempt AGENT_BRIDGED and customer FINISHED
CHANNEL_HANGUP with cause USER_BUSY -> attempt BUSY and retry decision
CHANNEL_HANGUP with cause NO_ANSWER -> attempt NO_ANSWER and retry decision
CHANNEL_HANGUP with cause NORMAL_CLEARING before bridge in PREDICTIVE -> attempt ABANDONED and customer ABANDONED
CHANNEL_HANGUP with other non-success cause -> attempt FAILED and retry decision
```

Every final event releases number-pool and trunk leases and publishes task progress event `DIAL_RESULT`.

- [ ] **Step 8: Run preview and idempotency tests**

Run:

```bash
mvn -pl och-call-task,och-esl,och-call -am test -Dtest=PreviewDialingServiceTest,OutboundDialingServiceDuplicateTest,OutboundOriginateCommandTest
```

Expected output contains `BUILD SUCCESS` and proves preview claim, exact originate command formatting, and duplicate dialing prevention.

- [ ] **Step 9: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java och-esl/src/main/java och-esl/src/test/java och-call/src/main/java
git commit -m "feat: add preview outbound dialing"
```

Expected output contains `feat: add preview outbound dialing`.

### Task 4: Number Pool and Trunk Concurrency Leases

**Owner:** Backend Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundConcurrencyLeaseService.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundConcurrencyLeaseServiceTest.java`

- [ ] **Step 1: Write concurrency lease test**

Create `OutboundConcurrencyLeaseServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundConcurrencyLeaseServiceTest {
    @Test
    void enforcesNumberPoolAndTrunkCapacityAndReleasesOnFinalEvent() {
        OutboundConcurrencyLeaseService service = OutboundConcurrencyLeaseService.inMemoryFixture(2, 1);

        var first = service.acquire(5002L, "dial-1", 101L, 201L, "trace-lease-1");
        assertThat(first.acquired()).isTrue();

        assertThatThrownBy(() -> service.acquire(5002L, "dial-2", 101L, 201L, "trace-lease-2"))
                .hasMessageContaining("Trunk 201 outbound concurrency limit reached");

        service.release("dial-1", "trace-release-1");
        var second = service.acquire(5002L, "dial-2", 101L, 201L, "trace-lease-3");
        assertThat(second.acquired()).isTrue();
    }

    @Test
    void expiredLeasesDoNotCountAgainstCapacity() {
        OutboundConcurrencyLeaseService service = OutboundConcurrencyLeaseService.inMemoryFixture(1, 1);
        service.acquireWithSecondsToLive(5002L, "dial-expired", 101L, 201L, -1, "trace-lease-4");

        var active = service.acquire(5002L, "dial-active", 101L, 201L, "trace-lease-5");

        assertThat(active.acquired()).isTrue();
        assertThat(service.activeLeaseCount("TRUNK", 201L)).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Implement lease service**

`OutboundConcurrencyLeaseService.acquire(taskId, dialId, numberPoolId, trunkId, traceId)` must:

```text
Read number pool capacity from Phase 2 resource config.
Read trunk outbound concurrency from Phase 2 trunk config.
Acquire Redis lock outbound:number-pool:{numberPoolId} then outbound:trunk:{trunkId}, always in that order.
Ignore leases where expires_at < current time or lease_state = RELEASED.
Reject when active number-pool leases >= number pool outbound capacity.
Reject when active trunk leases >= trunk outbound concurrency.
Insert two ACTIVE rows into outbound_concurrency_lease, one for NUMBER_POOL and one for TRUNK.
Set expires_at to acquired_at + 10 minutes.
Return both lease ids.
```

`release(dialId, traceId)` sets both rows to `RELEASED` and writes `released_at`.

- [ ] **Step 3: Add lease calls to shared dialing service**

Modify `OutboundDialingService.dial` so capacity leasing happens after time-window and compliance validation and before outbound originate dispatch. If originate dispatch fails, release both leases immediately and mark attempt `FAILED` with failure reason `ORIGINATE_COMMAND_REJECTED`.

- [ ] **Step 4: Run concurrency tests**

Run:

```bash
mvn -pl och-call-task -am test -Dtest=OutboundConcurrencyLeaseServiceTest,OutboundDialingServiceDuplicateTest
```

Expected output contains `BUILD SUCCESS` and proves capacity enforcement still preserves dial idempotency.

- [ ] **Step 5: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java
git commit -m "feat: enforce outbound resource concurrency"
```

Expected output contains `feat: enforce outbound resource concurrency`.

### Task 5: Predictive Dialing Scheduler, Rate Tuning, and Abandonment Protection

**Owner:** Backend Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/domain/PredictiveDialingPolicy.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/PredictiveTuningService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/PredictiveDialingScheduler.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/api/PredictiveDialingController.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/PredictiveTuningServiceTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/PredictiveDialingSchedulerTest.java`

- [ ] **Step 1: Write predictive tuning test**

Create `PredictiveTuningServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictiveTuningServiceTest {
    @Test
    void increasesPacingWhenSafeAndReducesWhenAbandonmentExceedsThreshold() {
        PredictiveTuningService service = new PredictiveTuningService();
        var policy = service.policy(1.00, 3.00, 0.0300);

        var safe = service.tune(policy, 10, 2, 0.45, 180, 0.0100, 1.00);
        assertThat(safe.pacingRatio()).isEqualByComparingTo("1.20");
        assertThat(safe.recommendedDials()).isEqualTo(10);
        assertThat(safe.protectionActive()).isFalse();

        var risky = service.tune(policy, 10, 8, 0.45, 180, 0.0500, 2.00);
        assertThat(risky.pacingRatio()).isEqualByComparingTo("1.00");
        assertThat(risky.recommendedDials()).isEqualTo(2);
        assertThat(risky.protectionActive()).isTrue();
    }
}
```

- [ ] **Step 2: Implement predictive tuning algorithm**

`PredictiveTuningService.tune(policy, availableAgents, activeDials, connectRate, avgTalkSeconds, currentAbandonRate, currentPacing)` must calculate exactly:

```text
If currentAbandonRate > targetAbandonRate:
  nextPacing = max(minPacing, currentPacing - 1.00)
  recommendedDials = max(0, availableAgents - activeDials)
  protectionActive = true
Else:
  nextPacing = min(maxPacing, currentPacing + 0.20)
  rawNeeded = ceil(availableAgents * nextPacing)
  recommendedDials = max(0, rawNeeded - activeDials)
  protectionActive = false
If connectRate < 0.10, cap recommendedDials at availableAgents.
If availableAgents = 0, recommendedDials = 0 and nextPacing = minPacing.
```

Persist every tuning result in `outbound_predictive_stat` with minute-level bucket `stat_minute`.

- [ ] **Step 3: Write predictive scheduler test**

Create `PredictiveDialingSchedulerTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictiveDialingSchedulerTest {
    @Test
    void schedulerDialsOnlyRecommendedCountWithinConcurrencyAndTimeWindow() {
        PredictiveDialingScheduler scheduler = PredictiveDialingScheduler.inMemoryFixture();

        var result = scheduler.runOnce(5002L, "2026-05-19T10:00:00", "trace-predictive-1");

        assertThat(result.taskId()).isEqualTo(5002L);
        assertThat(result.recommendedDials()).isEqualTo(3);
        assertThat(result.dispatchedDials()).isEqualTo(3);
        assertThat(result.skippedBecauseCapacity()).isEqualTo(0);
        assertThat(result.skippedBecauseTimeWindow()).isEqualTo(0);
    }

    @Test
    void schedulerStopsWhenAbandonmentProtectionIsActive() {
        PredictiveDialingScheduler scheduler = PredictiveDialingScheduler.inMemoryFixtureWithAbandonRate(0.0600);

        var result = scheduler.runOnce(5002L, "2026-05-19T10:01:00", "trace-predictive-2");

        assertThat(result.protectionActive()).isTrue();
        assertThat(result.dispatchedDials()).isLessThanOrEqualTo(result.availableAgents());
    }
}
```

- [ ] **Step 4: Implement predictive scheduler and APIs**

`PredictiveDialingScheduler.runOnce(taskId, nowIso, traceId)` must:

```text
Load task and ensure task_mode = PREDICTIVE.
Skip if task_state is not RUNNING.
Validate outbound time window.
Calculate available agents from Phase 4 agent availability by task skill_group_id.
Calculate active outbound dials from non-final attempts.
Calculate connect rate, abandonment rate, and average talk seconds from attempts in the last 30 minutes.
Call PredictiveTuningService.tune.
Select READY or RETRY_WAIT customers due for retry ordered by next_retry_at, id.
Mark selected rows SCHEDULED.
Call OutboundDialingService.dial for each selected row using dialMode PREDICTIVE.
Publish task progress event PREDICTIVE_TICK.
Return counts.
```

Create endpoints:

```text
POST /api/outbound/predictive/tasks/{taskId}/run-once
GET  /api/outbound/predictive/tasks/{taskId}/stats/latest
GET  /api/outbound/predictive/tasks/{taskId}/stats/timeline
```

- [ ] **Step 5: Run predictive tests**

Run:

```bash
mvn -pl och-call-task -am test -Dtest=PredictiveTuningServiceTest,PredictiveDialingSchedulerTest,OutboundConcurrencyLeaseServiceTest
```

Expected output contains `BUILD SUCCESS` and proves rate tuning, abandonment protection, and capacity enforcement.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java
git commit -m "feat: add predictive outbound scheduler"
```

Expected output contains `feat: add predictive outbound scheduler`.

### Task 6: Robot Outbound Scheduling and AI Handoff Boundary

**Owner:** AI Voice Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/RobotOutboundScheduler.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/RobotOutboundHandoffClient.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/StubRobotOutboundHandoffClient.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/api/RobotOutboundController.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/RobotOutboundSchedulerTest.java`

- [ ] **Step 1: Write robot scheduler test**

Create `RobotOutboundSchedulerTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RobotOutboundSchedulerTest {
    @Test
    void robotSchedulerCreatesSessionAndUsesPhase5StubHandoff() {
        RobotOutboundScheduler scheduler = RobotOutboundScheduler.inMemoryFixture();

        var result = scheduler.runOnce(5003L, "2026-05-19T10:00:00", "trace-robot-1");

        assertThat(result.taskId()).isEqualTo(5003L);
        assertThat(result.scheduledCalls()).isEqualTo(2);
        assertThat(result.robotSessions()).hasSize(2);
        assertThat(result.robotSessions()).allSatisfy(session -> {
            assertThat(session.handoffStatus()).isEqualTo("ACCEPTED_STUB");
            assertThat(session.handoffMode()).isEqualTo("PHASE5_STUB");
        });
    }
}
```

- [ ] **Step 2: Define robot handoff boundary**

Create `RobotOutboundHandoffClient`:

```java
package com.opencallhub.calltask.application;

public interface RobotOutboundHandoffClient {
    RobotOutboundHandoffResponse startRobotSession(RobotOutboundHandoffRequest request);

    record RobotOutboundHandoffRequest(
            Long tenantId,
            Long taskId,
            Long taskCustomerId,
            Long attemptId,
            String callId,
            Long robotId,
            String phoneNumber,
            String traceId
    ) {}

    record RobotOutboundHandoffResponse(
            String aiSessionId,
            String handoffStatus,
            String handoffMode,
            String failureReason
    ) {}
}
```

Create `StubRobotOutboundHandoffClient` returning:

```java
new RobotOutboundHandoffResponse("stub-ai-session-" + request.attemptId(), "ACCEPTED_STUB", "PHASE5_STUB", null)
```

- [ ] **Step 3: Implement robot scheduler**

`RobotOutboundScheduler.runOnce(taskId, nowIso, traceId)` must:

```text
Load task and ensure task_mode = ROBOT.
Skip if task_state is not RUNNING.
Validate outbound time window.
Select READY or RETRY_WAIT customers due for retry up to task.max_concurrency.
Mark selected rows SCHEDULED.
Call OutboundDialingService.dial with dialMode ROBOT.
On CHANNEL_ANSWER from OutboundCallEventConsumer, create outbound_robot_session.
Call RobotOutboundHandoffClient.startRobotSession.
Update outbound_robot_session handoff_status and ai_session_id from response.
Set customer state ROBOT_ACTIVE for ACCEPTED_STUB or ACCEPTED.
Publish task progress events ROBOT_DIAL_STARTED and ROBOT_HANDOFF_ACCEPTED.
```

Create endpoints:

```text
POST /api/outbound/robot/tasks/{taskId}/run-once
GET  /api/outbound/robot/tasks/{taskId}/sessions
GET  /api/outbound/robot/sessions/{sessionId}
```

- [ ] **Step 4: Run robot scheduling tests**

Run:

```bash
mvn -pl och-call-task -am test -Dtest=RobotOutboundSchedulerTest,OutboundDialingServiceDuplicateTest
```

Expected output contains `BUILD SUCCESS` and proves robot scheduling uses the stable AI handoff boundary without requiring Phase 6.

- [ ] **Step 5: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java
git commit -m "feat: add robot outbound scheduling boundary"
```

Expected output contains `feat: add robot outbound scheduling boundary`.

### Task 7: Task Progress WebSocket and Result Statistics

**Owner:** Backend Expert

**Files:**
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/TaskProgressPublisher.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundStatisticsService.java`
- Create: `och-call-task/src/main/java/com/opencallhub/calltask/api/OutboundStatisticsController.java`
- Create: `och-websocket/src/main/java/com/opencallhub/websocket/task/TaskProgressMessage.java`
- Create: `och-websocket/src/main/java/com/opencallhub/websocket/task/TaskProgressMessagePublisher.java`
- Modify: `och-websocket/src/main/java/com/opencallhub/websocket/config/WebSocketConfig.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/TaskProgressPublisherTest.java`
- Test: `och-call-task/src/test/java/com/opencallhub/calltask/application/OutboundStatisticsServiceTest.java`

- [ ] **Step 1: Write task progress publisher test**

Create `TaskProgressPublisherTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskProgressPublisherTest {
    @Test
    void publishesTaskProgressOnlyToPermittedSubscribers() {
        TaskProgressPublisher publisher = TaskProgressPublisher.inMemoryFixture();

        publisher.publish(5002L, "DIAL_STARTED", "{\"dialId\":\"dial-1\"}", "trace-ws-1");

        assertThat(publisher.messagesForUser(7L)).hasSize(1);
        assertThat(publisher.messagesForUser(99L)).isEmpty();
        assertThat(publisher.messagesForUser(7L).get(0).topic()).isEqualTo("outbound.task.progress");
        assertThat(publisher.messagesForUser(7L).get(0).taskId()).isEqualTo(5002L);
    }
}
```

- [ ] **Step 2: Implement `/ws/task` message envelope**

Create `TaskProgressMessage`:

```java
public record TaskProgressMessage(
        String messageId,
        Long taskId,
        String topic,
        String eventType,
        Object data,
        String traceId,
        Instant occurredAt
) {}
```

Supported event types:

```text
TASK_CREATED
TASK_STARTED
CUSTOMERS_IMPORTED
FILTER_COMPLETED
CUSTOMER_CLAIMED
DIAL_STARTED
DIAL_RESULT
PREDICTIVE_TICK
ROBOT_DIAL_STARTED
ROBOT_HANDOFF_ACCEPTED
STATISTICS_UPDATED
TASK_COMPLETED
TASK_FAILED
```

`TaskProgressPublisher.publish` must insert `outbound_task_progress_event`, publish to `/ws/task`, and filter subscribers using Phase 2 task data scope.

- [ ] **Step 3: Write statistics test**

Create `OutboundStatisticsServiceTest`:

```java
package com.opencallhub.calltask.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundStatisticsServiceTest {
    @Test
    void aggregatesTaskResultStatistics() {
        OutboundStatisticsService service = OutboundStatisticsService.inMemoryFixture();

        var stats = service.recalculateTask(5002L, "trace-stats-1");

        assertThat(stats.importedCount()).isEqualTo(10);
        assertThat(stats.blockedCount()).isEqualTo(2);
        assertThat(stats.dialedCount()).isEqualTo(6);
        assertThat(stats.connectedCount()).isEqualTo(3);
        assertThat(stats.abandonedCount()).isEqualTo(1);
        assertThat(stats.connectRate()).isEqualByComparingTo("0.5000");
        assertThat(stats.abandonmentRate()).isEqualByComparingTo("0.3333");
    }
}
```

- [ ] **Step 4: Implement statistics aggregation and APIs**

`OutboundStatisticsService.recalculateTask(taskId, traceId)` must compute:

```text
importedCount = count outbound_task_customer where task_id = taskId and deleted = 0
blockedCount = count customer_state BLOCKED
finishedCount = count customer_state FINISHED plus ABANDONED
Dialed metrics from outbound_dial_attempt final states
connectRate = connectedCount / dialedCount rounded to 4 decimals, 0.0000 when dialedCount = 0
abandonmentRate = abandonedCount / connectedCount rounded to 4 decimals, 0.0000 when connectedCount = 0
```

Create endpoints:

```text
POST /api/outbound/statistics/tasks/{taskId}/recalculate
GET  /api/outbound/statistics/tasks/{taskId}
GET  /api/outbound/statistics/daily?startDate=2026-05-19&endDate=2026-05-19
GET  /api/outbound/tasks/{taskId}/progress-events
```

- [ ] **Step 5: Run WebSocket and statistics tests**

Run:

```bash
mvn -pl och-call-task,och-websocket -am test -Dtest=TaskProgressPublisherTest,OutboundStatisticsServiceTest
```

Expected output contains `BUILD SUCCESS` and proves real-time task progress and result aggregation.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-call-task/src/main/java och-call-task/src/test/java och-websocket/src/main/java
git commit -m "feat: add outbound progress and statistics"
```

Expected output contains `feat: add outbound progress and statistics`.

### Task 8: Frontend Task Wizard, Preview UI, Predictive Monitor, Robot Session List, and Statistics

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/types/outbound.ts`
- Create: `waihu-app/src/services/outboundApi.ts`
- Create: `waihu-app/src/stores/outboundTask.ts`
- Create: `waihu-app/src/stores/previewDialing.ts`
- Create: `waihu-app/src/stores/predictiveMonitor.ts`
- Create: `waihu-app/src/stores/robotOutbound.ts`
- Create: `waihu-app/src/views/outbound/OutboundTaskListView.vue`
- Create: `waihu-app/src/views/outbound/OutboundTaskWizardView.vue`
- Create: `waihu-app/src/views/outbound/PreviewDialingView.vue`
- Create: `waihu-app/src/views/outbound/PredictiveMonitorView.vue`
- Create: `waihu-app/src/views/outbound/RobotOutboundSessionsView.vue`
- Create: `waihu-app/src/views/outbound/OutboundStatisticsView.vue`
- Create: `waihu-app/src/components/outbound/OutboundModeStep.vue`
- Create: `waihu-app/src/components/outbound/CustomerImportStep.vue`
- Create: `waihu-app/src/components/outbound/FilterRuleStep.vue`
- Create: `waihu-app/src/components/outbound/TimeWindowRetryStep.vue`
- Create: `waihu-app/src/components/outbound/ResourceConcurrencyStep.vue`
- Create: `waihu-app/src/components/outbound/PreviewClaimPanel.vue`
- Create: `waihu-app/src/components/outbound/PredictiveTuningPanel.vue`
- Create: `waihu-app/src/components/outbound/RobotSessionList.vue`
- Create: `waihu-app/src/components/outbound/TaskProgressTimeline.vue`
- Modify: `waihu-app/src/router/routes.ts`
- Modify: `waihu-app/src/services/wsClient.ts`
- Test: `waihu-app/src/tests/outbound-task-store.spec.ts`
- Test: `waihu-app/src/tests/outbound-task-wizard.spec.ts`
- Test: `waihu-app/src/tests/preview-dialing.spec.ts`
- Test: `waihu-app/src/tests/predictive-monitor.spec.ts`
- Test: `waihu-app/src/tests/robot-session-list.spec.ts`

- [ ] **Step 1: Write outbound task store test**

Create `waihu-app/src/tests/outbound-task-store.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it } from 'vitest'
import { useOutboundTaskStore } from '@/stores/outboundTask'

describe('outbound task store', () => {
  it('tracks wizard draft, import result, progress events, and statistics', () => {
    setActivePinia(createPinia())
    const store = useOutboundTaskStore()

    store.setWizardMode('PREDICTIVE')
    store.setConcurrency({ numberPoolId: 101, trunkId: 201, maxConcurrency: 5 })
    store.applyImportResult({ importedCount: 2, errorCount: 1, errors: [{ rowNo: 3, errorCode: 'PHONE_INVALID', errorMessage: 'Invalid phone' }] })
    store.applyProgressEvent({ taskId: 5002, eventType: 'PREDICTIVE_TICK', data: { recommendedDials: 3, pacingRatio: '1.20' } })
    store.applyStatistics({ taskId: 5002, dialedCount: 6, connectedCount: 3, abandonmentRate: '0.3333' })

    expect(store.wizard.mode).toBe('PREDICTIVE')
    expect(store.importResult?.errorCount).toBe(1)
    expect(store.progressEvents[0].eventType).toBe('PREDICTIVE_TICK')
    expect(store.statistics?.connectedCount).toBe(3)
  })
})
```

- [ ] **Step 2: Implement outbound types, API client, and stores**

Create `waihu-app/src/types/outbound.ts` with these core types:

```ts
export type OutboundTaskMode = 'PREVIEW' | 'PREDICTIVE' | 'ROBOT'
export type OutboundTaskState = 'DRAFT' | 'READY' | 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'CANCELLED' | 'FAILED'
export type OutboundCustomerState = 'IMPORTED' | 'READY' | 'BLOCKED' | 'CLAIMED' | 'SCHEDULED' | 'DIALING' | 'CONNECTED' | 'ROBOT_ACTIVE' | 'RETRY_WAIT' | 'ABANDONED' | 'FINISHED'

export interface OutboundTaskWizardDraft {
  mode: OutboundTaskMode
  taskName: string
  numberPoolId?: number
  trunkId?: number
  skillGroupId?: number
  robotId?: number
  maxConcurrency: number
  weekdays: string[]
  dailyStartTime: string
  dailyEndTime: string
  maxAttempts: number
}

export interface OutboundProgressEvent {
  taskId: number
  eventType: string
  data: Record<string, unknown>
}
```

`outboundApi.ts` must wrap all endpoints created in Tasks 2, 3, 5, 6, and 7. Stores must not contain raw Axios calls.

- [ ] **Step 3: Write task wizard component test**

Create `waihu-app/src/tests/outbound-task-wizard.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import OutboundModeStep from '@/components/outbound/OutboundModeStep.vue'
import TimeWindowRetryStep from '@/components/outbound/TimeWindowRetryStep.vue'

describe('outbound task wizard', () => {
  it('renders three modes and validates time window plus retry settings', async () => {
    const mode = mount(OutboundModeStep, { props: { modelValue: 'PREVIEW' } })
    expect(mode.text()).toContain('Preview')
    expect(mode.text()).toContain('Predictive')
    expect(mode.text()).toContain('Robot')

    const retry = mount(TimeWindowRetryStep, {
      props: {
        modelValue: { weekdays: ['1', '2', '3', '4', '5'], dailyStartTime: '09:00:00', dailyEndTime: '18:00:00', maxAttempts: 3 }
      }
    })
    expect(retry.text()).toContain('09:00:00')
    expect(retry.text()).toContain('18:00:00')
    expect(retry.text()).toContain('3')
  })
})
```

- [ ] **Step 4: Implement wizard pages and route entries**

Add routes:

```ts
{
  path: '/outbound/tasks',
  name: 'OutboundTaskList',
  component: () => import('@/views/outbound/OutboundTaskListView.vue'),
  meta: { permission: 'outbound:task:view' }
},
{
  path: '/outbound/tasks/new',
  name: 'OutboundTaskWizard',
  component: () => import('@/views/outbound/OutboundTaskWizardView.vue'),
  meta: { permission: 'outbound:task:create' }
},
{
  path: '/outbound/preview/:taskId',
  name: 'PreviewDialing',
  component: () => import('@/views/outbound/PreviewDialingView.vue'),
  meta: { permission: 'outbound:preview:dial' }
},
{
  path: '/outbound/predictive/:taskId',
  name: 'PredictiveMonitor',
  component: () => import('@/views/outbound/PredictiveMonitorView.vue'),
  meta: { permission: 'outbound:predictive:monitor' }
},
{
  path: '/outbound/robot/:taskId/sessions',
  name: 'RobotOutboundSessions',
  component: () => import('@/views/outbound/RobotOutboundSessionsView.vue'),
  meta: { permission: 'outbound:robot:view' }
},
{
  path: '/outbound/statistics/:taskId',
  name: 'OutboundStatistics',
  component: () => import('@/views/outbound/OutboundStatisticsView.vue'),
  meta: { permission: 'outbound:statistics:view' }
}
```

Wizard step order:

```text
1 mode and metrics objective
2 customer CSV import
3 DNC, blacklist, duplicate filter review
4 time window and retry rules
5 number pool, trunk, concurrency, skill group or robot selection
6 review and submit
```

- [ ] **Step 5: Write preview dialing component test**

Create `waihu-app/src/tests/preview-dialing.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import PreviewClaimPanel from '@/components/outbound/PreviewClaimPanel.vue'

describe('preview dialing', () => {
  it('shows claimed customer and emits dial action', async () => {
    const wrapper = mount(PreviewClaimPanel, {
      props: { customer: { taskCustomerId: 8001, customerName: 'Alice', maskedPhone: '138****0001', customerState: 'CLAIMED' } }
    })
    wrapper.vm.$emit = vi.fn()

    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('138****0001')
    await wrapper.find('[data-test="preview-dial"]').trigger('click')
    expect(wrapper.emitted('dial')).toBeTruthy()
  })
})
```

- [ ] **Step 6: Implement preview page**

`PreviewDialingView.vue` must:

```text
Load task summary by taskId.
Call claim-next when the agent clicks Claim customer.
Display customerName, maskedPhone, attemptCount, filter status, and previous attempt result.
Call preview dial API when the agent clicks Dial.
Show commandStatus ACCEPTED and dialId.
Append `/ws/task` events to TaskProgressTimeline.
Disable Dial unless customerState is CLAIMED.
```

- [ ] **Step 7: Write predictive monitor test**

Create `waihu-app/src/tests/predictive-monitor.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PredictiveTuningPanel from '@/components/outbound/PredictiveTuningPanel.vue'

describe('predictive monitor', () => {
  it('shows pacing and abandonment protection warning', () => {
    const wrapper = mount(PredictiveTuningPanel, {
      props: { stat: { pacingRatio: '1.00', recommendedDials: 2, availableAgents: 10, abandonRate: '0.0500', targetAbandonRate: '0.0300', protectionActive: true } }
    })

    expect(wrapper.text()).toContain('1.00')
    expect(wrapper.text()).toContain('2')
    expect(wrapper.text()).toContain('Abandonment protection active')
  })
})
```

- [ ] **Step 8: Implement predictive monitor page**

`PredictiveMonitorView.vue` must:

```text
Load latest predictive stat and timeline.
Display availableAgents, activeDials, connectRate, abandonRate, targetAbandonRate, pacingRatio, recommendedDials, and protectionActive.
Use red warning styling when abandonRate > targetAbandonRate.
Provide Run once button calling POST /api/outbound/predictive/tasks/{taskId}/run-once for local and test profiles.
Stream `/ws/task` PREDICTIVE_TICK and DIAL_RESULT events into TaskProgressTimeline.
```

- [ ] **Step 9: Write robot session list test**

Create `waihu-app/src/tests/robot-session-list.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import RobotSessionList from '@/components/outbound/RobotSessionList.vue'

describe('robot session list', () => {
  it('renders phase 5 stub handoff status', () => {
    const wrapper = mount(RobotSessionList, {
      props: { sessions: [{ sessionId: 6001, customerName: 'Robot Alice', maskedPhone: '138****0005', handoffStatus: 'ACCEPTED_STUB', handoffMode: 'PHASE5_STUB' }] }
    })

    expect(wrapper.text()).toContain('Robot Alice')
    expect(wrapper.text()).toContain('ACCEPTED_STUB')
    expect(wrapper.text()).toContain('PHASE5_STUB')
  })
})
```

- [ ] **Step 10: Implement robot session and statistics pages**

`RobotOutboundSessionsView.vue` must display `sessionId`, `customerName`, `maskedPhone`, `callId`, `aiSessionId`, `handoffStatus`, `handoffMode`, `startedAt`, and `endedAt`.

`OutboundStatisticsView.vue` must display `importedCount`, `blockedCount`, `dialedCount`, `connectedCount`, `noAnswerCount`, `busyCount`, `failedCount`, `abandonedCount`, `robotCompletedCount`, `connectRate`, and `abandonmentRate`.

- [ ] **Step 11: Run frontend unit tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- outbound-task-store outbound-task-wizard preview-dialing predictive-monitor robot-session-list
```

Expected output contains a passing Vitest summary for all five outbound test files.

- [ ] **Step 12: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/outbound.ts waihu-app/src/services/outboundApi.ts waihu-app/src/stores/outboundTask.ts waihu-app/src/stores/previewDialing.ts waihu-app/src/stores/predictiveMonitor.ts waihu-app/src/stores/robotOutbound.ts waihu-app/src/views/outbound waihu-app/src/components/outbound waihu-app/src/router/routes.ts waihu-app/src/services/wsClient.ts waihu-app/src/tests/outbound-task-store.spec.ts waihu-app/src/tests/outbound-task-wizard.spec.ts waihu-app/src/tests/preview-dialing.spec.ts waihu-app/src/tests/predictive-monitor.spec.ts waihu-app/src/tests/robot-session-list.spec.ts
git commit -m "feat: add outbound center frontend"
```

Expected output contains `feat: add outbound center frontend`.

### Task 9: Security, Permissions, Audit, and Data Scope

**Owner:** Security Expert

**Files:**
- Modify: `och-security/src/main/java/com/opencallhub/security/permission/PermissionCodes.java`
- Modify: `och-call-task/src/main/java/com/opencallhub/calltask/application/OutboundFilterService.java`
- Modify: `och-call-task/src/main/java/com/opencallhub/calltask/application/PreviewDialingService.java`
- Modify: `och-call-task/src/main/java/com/opencallhub/calltask/application/TaskProgressPublisher.java`
- Modify: `och-compliance/src/main/java/com/opencallhub/compliance/api/DncBlacklistController.java`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundSecurityTest.java`

- [ ] **Step 1: Add outbound permission constants**

Add these constants to `PermissionCodes`:

```java
public static final String OUTBOUND_TASK_VIEW = "outbound:task:view";
public static final String OUTBOUND_TASK_CREATE = "outbound:task:create";
public static final String OUTBOUND_TASK_START = "outbound:task:start";
public static final String OUTBOUND_PREVIEW_DIAL = "outbound:preview:dial";
public static final String OUTBOUND_PREDICTIVE_MONITOR = "outbound:predictive:monitor";
public static final String OUTBOUND_ROBOT_VIEW = "outbound:robot:view";
public static final String OUTBOUND_STATISTICS_VIEW = "outbound:statistics:view";
public static final String OUTBOUND_PHONE_FULL = "outbound:phone:full";
public static final String COMPLIANCE_DNC_MANAGE = "compliance:dnc:manage";
public static final String COMPLIANCE_BLACKLIST_MANAGE = "compliance:blacklist:manage";
```

- [ ] **Step 2: Write security E2E test**

Create `Phase5OutboundSecurityTest`:

```java
package com.opencallhub.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Phase5OutboundSecurityTest {
    @Autowired MockMvc mockMvc;

    @Test
    void taskDataScopeMasksPhonesAndBlocksUnauthorizedTaskProgress() throws Exception {
        mockMvc.perform(get("/api/outbound/tasks/5001").header("X-Test-User", "agent-limited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customers[0].maskedPhone").value("138****0001"))
                .andExpect(jsonPath("$.data.customers[0].phoneNumber").doesNotExist());

        mockMvc.perform(get("/api/outbound/tasks/5002/progress-events").header("X-Test-User", "agent-limited"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dncAndBlacklistManagementRequireCompliancePermissions() throws Exception {
        mockMvc.perform(post("/api/compliance/dnc").header("X-Test-User", "agent-limited")
                        .contentType("application/json")
                        .content("{\"phoneNumber\":\"13900000003\",\"reason\":\"opt out\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/compliance/dnc").header("X-Test-User", "compliance-admin")
                        .contentType("application/json")
                        .content("{\"phoneNumber\":\"13900000003\",\"reason\":\"opt out\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phoneNumber").value("13900000003"));
    }
}
```

- [ ] **Step 3: Enforce task data scope and masking**

Apply these rules in all outbound query and WebSocket paths:

```text
Task creator can see the task.
Supervisor can see tasks in permitted departments and skill groups from Phase 2 data scope.
Agent can see preview tasks assigned to their skill group and only their claimed customers.
Robot session list requires outbound:robot:view.
Full phone number appears only with outbound:phone:full.
DNC and blacklist mutations require compliance:dnc:manage or compliance:blacklist:manage.
Every task mutation writes audit action with taskId, operatorId, traceId, beforeState, afterState, and reason.
```

- [ ] **Step 4: Run security tests**

Run:

```bash
mvn -pl och-api,och-call-task,och-compliance -am test -Dtest=Phase5OutboundSecurityTest,OutboundFilterServiceTest,TaskProgressPublisherTest
```

Expected output contains `BUILD SUCCESS` and proves DNC, blacklist, task data scope, masking, and WebSocket scope rules.

- [ ] **Step 5: Commit checkpoint**

Run:

```bash
git add och-security/src/main/java och-call-task/src/main/java och-compliance/src/main/java och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundSecurityTest.java
git commit -m "feat: secure outbound task data scope"
```

Expected output contains `feat: secure outbound task data scope`.

### Task 10: Three-Mode Outbound E2E and Acceptance Gate

**Owner:** QA Expert

**Files:**
- Create: `och-api/src/main/java/com/opencallhub/api/testfixture/OutboundPhase5TestFixtureController.java`
- Test: `och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundFlowTest.java`
- Test: `waihu-app/e2e/phase5-outbound.spec.ts`

- [ ] **Step 1: Create deterministic E2E fixture controller**

Create `OutboundPhase5TestFixtureController` under `local` and `test` profiles with endpoints:

```text
POST /api/outbound/test-fixtures/reset-phase5
POST /api/outbound/test-fixtures/seed-preview-task
POST /api/outbound/test-fixtures/seed-predictive-task
POST /api/outbound/test-fixtures/seed-robot-task
POST /api/outbound/test-fixtures/simulate-call-event
```

`reset-phase5` must clear only rows with ids in the Phase 5 fixture ranges `5001-5999`, `8001-8999`, `9001-9199`, and robot session ids `6001-6999`. It must not delete user-created rows outside those ranges.

- [ ] **Step 2: Write backend three-mode E2E test**

Create `Phase5OutboundFlowTest`:

```java
package com.opencallhub.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Phase5OutboundFlowTest {
    @Autowired MockMvc mockMvc;

    @Test
    void previewPredictiveAndRobotOutboundCompleteAcceptancePath() throws Exception {
        mockMvc.perform(post("/api/outbound/test-fixtures/reset-phase5"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/outbound/test-fixtures/seed-preview-task"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/outbound/preview/tasks/5001/claim-next").header("X-Test-Agent-Id", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerState").value("CLAIMED"));
        mockMvc.perform(post("/api/outbound/preview/tasks/5001/customers/8001/dial").header("X-Test-Agent-Id", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commandStatus").value("ACCEPTED"));

        mockMvc.perform(post("/api/outbound/test-fixtures/seed-predictive-task"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/outbound/predictive/tasks/5002/run-once"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendedDials").value(3));

        mockMvc.perform(post("/api/outbound/test-fixtures/seed-robot-task"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/outbound/robot/tasks/5003/run-once"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduledCalls").value(2));
        mockMvc.perform(get("/api/outbound/robot/tasks/5003/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].handoffStatus").value("ACCEPTED_STUB"));

        mockMvc.perform(post("/api/outbound/statistics/tasks/5002/recalculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dialedCount").isNumber());
    }
}
```

- [ ] **Step 3: Write Playwright three-mode E2E test**

Create `waihu-app/e2e/phase5-outbound.spec.ts`:

```ts
import { expect, test } from '@playwright/test'

test('phase 5 outbound preview predictive and robot paths', async ({ page, request }) => {
  await request.post('/api/outbound/test-fixtures/reset-phase5')
  await request.post('/api/outbound/test-fixtures/seed-preview-task')
  await page.goto('/outbound/preview/5001')
  await page.getByRole('button', { name: 'Claim customer' }).click()
  await expect(page.getByText('138****0001')).toBeVisible()
  await page.getByRole('button', { name: 'Dial' }).click()
  await expect(page.getByText('ACCEPTED')).toBeVisible()

  await request.post('/api/outbound/test-fixtures/seed-predictive-task')
  await page.goto('/outbound/predictive/5002')
  await page.getByRole('button', { name: 'Run once' }).click()
  await expect(page.getByText('Recommended dials')).toBeVisible()
  await expect(page.getByText('Abandonment')).toBeVisible()

  await request.post('/api/outbound/test-fixtures/seed-robot-task')
  await page.goto('/outbound/robot/5003/sessions')
  await page.getByRole('button', { name: 'Run robot scheduler once' }).click()
  await expect(page.getByText('ACCEPTED_STUB')).toBeVisible()
  await expect(page.getByText('PHASE5_STUB')).toBeVisible()

  await page.goto('/outbound/statistics/5002')
  await expect(page.getByText('Connect rate')).toBeVisible()
  await expect(page.getByText('Abandonment rate')).toBeVisible()
})
```

- [ ] **Step 4: Run backend E2E and all Phase 5 backend tests**

Run:

```bash
mvn -pl och-api,och-call-task,och-compliance,och-esl,och-websocket -am test -Dtest=Phase5OutboundFlowTest,Phase5OutboundSecurityTest,OutboundTaskStateMachineTest,CustomerImportServiceTest,OutboundFilterServiceTest,OutboundTimeWindowServiceTest,RetryRuleServiceTest,PreviewDialingServiceTest,OutboundDialingServiceDuplicateTest,OutboundConcurrencyLeaseServiceTest,PredictiveTuningServiceTest,PredictiveDialingSchedulerTest,RobotOutboundSchedulerTest,TaskProgressPublisherTest,OutboundStatisticsServiceTest,DncBlacklistServiceTest,OutboundOriginateCommandTest
```

Expected output contains `BUILD SUCCESS` and all named test classes pass.

- [ ] **Step 5: Run frontend tests and build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- outbound-task-store outbound-task-wizard preview-dialing predictive-monitor robot-session-list && npm run build
```

Expected output contains a passing Vitest summary and a successful Vite production build.

- [ ] **Step 6: Run Playwright outbound E2E**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase5-outbound.spec.ts
```

Expected output contains one passing Playwright spec named `phase 5 outbound preview predictive and robot paths`.

- [ ] **Step 7: Verify acceptance gate**

Acceptance gate:

```markdown
Phase 5 is accepted only when preview outbound can claim and dial a customer, predictive outbound can auto-tune while respecting abandonment thresholds, robot outbound can schedule calls, DNC/blacklist/time-window checks are enforced, task progress updates in real time, and outbound results are aggregated.
```

Evidence commands and expected proof:

```bash
mvn -pl och-api,och-call-task,och-compliance,och-esl,och-websocket -am test -Dtest=Phase5OutboundFlowTest,Phase5OutboundSecurityTest
```

Expected proof: `Phase5OutboundFlowTest` demonstrates preview claim and dial, predictive tuning run, robot scheduling, and result aggregation; `Phase5OutboundSecurityTest` demonstrates DNC/blacklist and data-scope enforcement.

```bash
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase5-outbound.spec.ts
```

Expected proof: Playwright observes preview UI dial acceptance, predictive monitor, robot stub handoff, and statistics page.

- [ ] **Step 8: Final commit checkpoint**

Run:

```bash
git add och-api/src/main/java/com/opencallhub/api/testfixture/OutboundPhase5TestFixtureController.java och-api/src/test/java/com/opencallhub/api/e2e/Phase5OutboundFlowTest.java waihu-app/e2e/phase5-outbound.spec.ts
git commit -m "test: add outbound three mode acceptance coverage"
```

Expected output contains `test: add outbound three mode acceptance coverage`.

## Phase 5 Self-Review Checklist

Spec coverage:

- Outbound task model is covered in Task 1.
- Preview dialing is covered in Task 3 and Task 8.
- Predictive dialing, tuning, and abandonment protection are covered in Task 5 and Task 8.
- Robot dialing task orchestration and the Phase 5 stub handoff boundary are covered in Task 6 and Task 8.
- Customer list import is covered in Task 2 and Task 8.
- DNC filtering, blacklist filtering, task data scope, and security permissions are covered in Task 2 and Task 9.
- Outbound time window validation and retry rules are covered in Task 2.
- Number pool and trunk concurrency are covered in Task 4.
- Task progress WebSocket and result statistics are covered in Task 7 and Task 8.
- Duplicate dialing prevention, concurrency, rate tuning, and three-mode E2E tests are covered in Tasks 3, 4, 5, and 10.

Placeholder scan:

- No deferred placeholders are intentionally present. The robot AI path uses the concrete `RobotOutboundHandoffClient` boundary and `StubRobotOutboundHandoffClient` response until Phase 6 supplies the production implementation.

Type consistency:

- Shared identifiers are consistently named `traceId`, `callId`, `uuid`, `legId`, `agentId`, `taskId`, `taskCustomerId`, `attemptNo`, `dialId`, `eventId`, `preview`, `predictive`, and `robot`.
