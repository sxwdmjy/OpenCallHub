# openCallHub Inbound Call Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement inbound call facts, call state transitions, idempotent ESL event ingestion, inbound call fixture APIs, and query APIs for sessions, legs, and events.

**Architecture:** `och-call` is the source of truth for call state. `och-esl` publishes normalized events, `och-call` stores immutable event facts and updates call projections through a state machine, while `och-api` exposes facade endpoints for local/test fixtures and production queries. This plan keeps IVR and ACD business routing outside call event ingestion; those are implemented in later plans.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, Lombok, JUnit 5, AssertJ, Mockito, Spring Boot Test, MockMvc, MySQL-compatible schema from the inbound schema plan.

---

## Scope Check

This plan implements Task 3 from `docs/superpowers/plans/2026-06-09-opencallhub-inbound-production-plan.md`.

It assumes the schema plan and ESL boundary plan are completed. It does not implement IVR runtime, ACD dispatch, phone-bar commands, WebSocket fanout, frontend pages, reports, or real FreeSWITCH acceptance.

Reference documents:

- Spec: `docs/superpowers/specs/2026-06-09-opencallhub-inbound-production-design.md`
- Schema plan: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-schema-plan.md`
- ESL plan: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-esl-plan.md`

## File Structure

- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallState.java`
  - Enum for call lifecycle states.
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallSession.java`
  - MyBatis-Plus entity for `call_session`.
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallLeg.java`
  - MyBatis-Plus entity for `call_leg`.
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallEvent.java`
  - MyBatis-Plus entity for `call_event`.
- Create: `och-call/src/main/java/com/och/call/domain/CallCommand.java`
  - MyBatis-Plus entity for `call_command`.
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallSessionMapper.java`
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallLegMapper.java`
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallEventMapper.java`
- Create: `och-call/src/main/java/com/och/call/mapper/CallCommandMapper.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallStateMachine.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallEventIngestService.java`
- Create: `och-call/src/main/java/com/och/call/application/InboundCallApplicationService.java`
- Modify/Create: `och-call/src/main/java/com/och/call/api/CallController.java`
- Test: `och-call/src/test/java/com/och/call/application/CallStateMachineTest.java`
- Test: `och-call/src/test/java/com/och/call/application/CallEventIngestServiceTest.java`
- Test: `och-call/src/test/java/com/och/call/application/InboundCallApplicationServiceTest.java`
- Test: `och-call/src/test/java/com/och/call/api/CallControllerTest.java`

## Task 1: Call State Machine

**Files:**
- Test: `och-call/src/test/java/com/och/call/application/CallStateMachineTest.java`
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallState.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallStateMachine.java`

- [ ] **Step 1: Write failing state machine test**

Create `och-call/src/test/java/com/och/call/application/CallStateMachineTest.java`:

```java
package com.och.call.application;

import com.och.call.domain.CallState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallStateMachineTest {

    private final CallStateMachine machine = new CallStateMachine();

    @Test
    void allowsInboundHappyPathTransitions() {
        assertThat(machine.isValidTransition(CallState.NEW, CallState.RINGING)).isTrue();
        assertThat(machine.isValidTransition(CallState.RINGING, CallState.IVR)).isTrue();
        assertThat(machine.isValidTransition(CallState.IVR, CallState.QUEUEING)).isTrue();
        assertThat(machine.isValidTransition(CallState.QUEUEING, CallState.ASSIGNED)).isTrue();
        assertThat(machine.isValidTransition(CallState.ASSIGNED, CallState.BRIDGED)).isTrue();
        assertThat(machine.isValidTransition(CallState.BRIDGED, CallState.HOLD)).isTrue();
        assertThat(machine.isValidTransition(CallState.HOLD, CallState.BRIDGED)).isTrue();
        assertThat(machine.isValidTransition(CallState.BRIDGED, CallState.AFTER_CALL)).isTrue();
        assertThat(machine.isValidTransition(CallState.AFTER_CALL, CallState.ENDED)).isTrue();
    }

    @Test
    void allowsVoicemailAndFailurePaths() {
        assertThat(machine.isValidTransition(CallState.IVR, CallState.VOICEMAIL)).isTrue();
        assertThat(machine.isValidTransition(CallState.VOICEMAIL, CallState.ENDED)).isTrue();
        assertThat(machine.isValidTransition(CallState.IVR, CallState.FAILED)).isTrue();
        assertThat(machine.isValidTransition(CallState.QUEUEING, CallState.FAILED)).isTrue();
    }

    @Test
    void rejectsTerminalStateRollback() {
        assertThat(machine.isValidTransition(CallState.ENDED, CallState.BRIDGED)).isFalse();
        assertThat(machine.isValidTransition(CallState.FAILED, CallState.RINGING)).isFalse();
        assertThatThrownBy(() -> machine.validate(CallState.ENDED, CallState.BRIDGED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid call state transition");
    }
}
```

- [ ] **Step 2: Run state machine test and verify failure**

Run:

```bash
mvn -pl och-call test -Dtest=CallStateMachineTest
```

Expected result before implementation: `BUILD FAILURE` because states or transitions are missing.

- [ ] **Step 3: Implement `CallState`**

Modify or create `och-call/src/main/java/com/och/call/domain/CallState.java`:

```java
package com.och.call.domain;

public enum CallState {
    NEW("新建"),
    RINGING("振铃中"),
    IVR("IVR处理中"),
    QUEUEING("排队中"),
    ASSIGNED("已分配坐席"),
    BRIDGED("通话中"),
    HOLD("保持中"),
    VOICEMAIL("语音信箱"),
    AFTER_CALL("后处理"),
    ENDED("已结束"),
    FAILED("失败");

    private final String label;

    CallState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTerminal() {
        return this == ENDED || this == FAILED;
    }
}
```

- [ ] **Step 4: Implement `CallStateMachine`**

Modify or create `och-call/src/main/java/com/och/call/application/CallStateMachine.java`:

```java
package com.och.call.application;

import com.och.call.domain.CallState;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class CallStateMachine {

    private static final Map<CallState, Set<CallState>> TRANSITIONS = Map.ofEntries(
            Map.entry(CallState.NEW, EnumSet.of(CallState.RINGING, CallState.IVR, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.RINGING, EnumSet.of(CallState.IVR, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.IVR, EnumSet.of(CallState.QUEUEING, CallState.VOICEMAIL, CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.QUEUEING, EnumSet.of(CallState.ASSIGNED, CallState.VOICEMAIL, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.ASSIGNED, EnumSet.of(CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.BRIDGED, EnumSet.of(CallState.HOLD, CallState.AFTER_CALL, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.HOLD, EnumSet.of(CallState.BRIDGED, CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.VOICEMAIL, EnumSet.of(CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.AFTER_CALL, EnumSet.of(CallState.ENDED, CallState.FAILED)),
            Map.entry(CallState.ENDED, EnumSet.noneOf(CallState.class)),
            Map.entry(CallState.FAILED, EnumSet.noneOf(CallState.class))
    );

    public boolean isValidTransition(CallState from, CallState to) {
        Set<CallState> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public void validate(CallState from, CallState to) {
        if (!isValidTransition(from, to)) {
            throw new IllegalStateException("Invalid call state transition: " + from + " -> " + to);
        }
    }
}
```

- [ ] **Step 5: Run state machine test and verify pass**

Run:

```bash
mvn -pl och-call test -Dtest=CallStateMachineTest
```

Expected result: `BUILD SUCCESS`, `Tests run: 3, Failures: 0, Errors: 0`.

## Task 2: Call Domain Entities and Mappers

**Files:**
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallSession.java`
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallLeg.java`
- Modify/Create: `och-call/src/main/java/com/och/call/domain/CallEvent.java`
- Create: `och-call/src/main/java/com/och/call/domain/CallCommand.java`
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallSessionMapper.java`
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallLegMapper.java`
- Modify/Create: `och-call/src/main/java/com/och/call/mapper/CallEventMapper.java`
- Create: `och-call/src/main/java/com/och/call/mapper/CallCommandMapper.java`

- [ ] **Step 1: Implement `CallSession` entity**

Implement fields aligned with `call_session`: `id`, `tenantId`, `callId`, `direction`, `callerNumber`, `calledNumber`, `currentState`, `ivrExecutionId`, `queueSessionId`, `agentId`, `startedAt`, `answeredAt`, `endedAt`, `endReason`, `traceId`, `createdBy`, `createdAt`, `updatedBy`, `updatedAt`, `deleted`, `status`, `remark`. If older code uses `state`, keep a computed or compatibility getter only if needed by existing callers; new code must use `currentState`.

- [ ] **Step 2: Implement `CallLeg`, `CallEvent`, and `CallCommand` entities**

Use MyBatis-Plus `@TableName`, `@TableId(type = IdType.AUTO)`, Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Keep JSON columns as `String` until a project-wide JSON type handler is introduced.

- [ ] **Step 3: Implement mapper interfaces**

Each mapper extends `BaseMapper<T>` and uses `@Mapper`:

```java
@Mapper
public interface CallSessionMapper extends BaseMapper<CallSession> {
}
```

Repeat for `CallLegMapper`, `CallEventMapper`, and `CallCommandMapper`.

- [ ] **Step 4: Compile call module**

Run:

```bash
mvn -pl och-call test -DskipTests
```

Expected result: `BUILD SUCCESS`.

## Task 3: Event Ingestion Service

**Files:**
- Test: `och-call/src/test/java/com/och/call/application/CallEventIngestServiceTest.java`
- Modify/Create: `och-call/src/main/java/com/och/call/application/CallEventIngestService.java`

- [ ] **Step 1: Write event ingestion tests**

Create tests with Mockito mappers. Cover:

1. A `CHANNEL_CREATE` event creates a new `call_session` with state `RINGING`.
2. Duplicate event with same dedup key is ignored.
3. `CHANNEL_HANGUP` moves active call to `ENDED`.
4. A later `CHANNEL_BRIDGE` after `ENDED` is ignored.
5. `DTMF` is stored as event but does not directly change call state.

- [ ] **Step 2: Run ingestion tests and verify failure**

Run:

```bash
mvn -pl och-call test -Dtest=CallEventIngestServiceTest
```

Expected result before implementation: `BUILD FAILURE` or failing assertions.

- [ ] **Step 3: Implement ingestion mapping**

Implement `CallEventIngestService` with:

- `@EventListener public void onNormalizedEslEvent(NormalizedEslEvent event)`
- `public void ingest(NormalizedEslEvent event)`
- `private CallState mapEventToState(EslEventType type)`
- `private String buildDedupKey(NormalizedEslEvent event)`

State mapping:

```text
CHANNEL_CREATE / CHANNEL_PROGRESS / CHANNEL_PROGRESS_MEDIA -> RINGING
CHANNEL_ANSWER / CHANNEL_BRIDGE -> BRIDGED
CHANNEL_HOLD -> HOLD
CHANNEL_UNHOLD / CHANNEL_UNBRIDGE -> BRIDGED
CHANNEL_HANGUP / CHANNEL_DESTROY -> ENDED
DTMF / RECORD_START / RECORD_STOP / HEARTBEAT / CUSTOM -> no direct state change
```

- [ ] **Step 4: Implement terminal-state protection**

If current state is `ENDED` or `FAILED`, ignore any non-terminal event state. Still store the event row for traceability.

- [ ] **Step 5: Run ingestion tests and verify pass**

Run:

```bash
mvn -pl och-call test -Dtest=CallEventIngestServiceTest
```

Expected result: tests pass.

## Task 4: Inbound Call Fixture and Query APIs

**Files:**
- Test: `och-call/src/test/java/com/och/call/application/InboundCallApplicationServiceTest.java`
- Test: `och-call/src/test/java/com/och/call/api/CallControllerTest.java`
- Create: `och-call/src/main/java/com/och/call/application/InboundCallApplicationService.java`
- Modify/Create: `och-call/src/main/java/com/och/call/api/CallController.java`

- [ ] **Step 1: Write inbound service test**

Test `startInbound` creates a `call_session` with `callId`, `callerNumber`, `calledNumber`, `direction=INBOUND`, `currentState=NEW`, and provided `traceId`.

- [ ] **Step 2: Implement inbound application service**

Create `InboundCallApplicationService.startInbound(String callId, String callerNumber, String calledNumber, String traceId)`. If the callId already exists, return the existing session instead of inserting a duplicate.

- [ ] **Step 3: Write controller test**

Use MockMvc to verify:

1. `GET /api/call/sessions` returns a paged response.
2. `GET /api/call/sessions/{callId}` returns one session.
3. `GET /api/call/sessions/{callId}/events` returns events.
4. `POST /api/call/test-fixtures/inbound-call` is available only for local/test profile.

- [ ] **Step 4: Implement controller APIs**

Implement query endpoints and local/test fixture endpoints. Controller delegates to application services and mappers; it does not manipulate state directly.

- [ ] **Step 5: Run controller tests**

Run:

```bash
mvn -pl och-call test -Dtest=InboundCallApplicationServiceTest,CallControllerTest
```

Expected result: tests pass.

## Task 5: Full Call Core Verification

**Files:**
- All files in this plan.

- [ ] **Step 1: Run call module tests**

Run:

```bash
mvn -pl och-call test
```

Expected result: `BUILD SUCCESS` and all call tests pass.

- [ ] **Step 2: Compile API dependency chain**

Run:

```bash
mvn -pl och-api -am test -DskipTests
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 3: Commit call core checkpoint**

Commit only after user approval to commit:

```bash
git add och-call/src/main/java/com/och/call och-call/src/test/java/com/och/call
git commit -m "feat: add inbound call core"
```

## Self-Review

### Spec coverage

This plan covers the call core storage and behavior: sessions, legs, events, commands, call state machine, idempotent event ingestion, terminal state protection, and local/test inbound fixtures. It does not cover IVR, ACD, phone-bar persistence, or reporting.

### Placeholder scan

No placeholder terms are used. Each test, file, command, and expected result is specified.

### Type consistency

State names match the spec and schema: `NEW`, `RINGING`, `IVR`, `QUEUEING`, `ASSIGNED`, `BRIDGED`, `HOLD`, `VOICEMAIL`, `AFTER_CALL`, `ENDED`, `FAILED`. Identifiers are consistent: `callId`, `uuid`, `legId`, `eventId`, `traceId`, `agentId`.
