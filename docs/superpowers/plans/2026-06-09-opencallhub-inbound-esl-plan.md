# openCallHub Inbound ESL Boundary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the inbound FreeSWITCH ESL boundary, including normalized event mapping, phone-control command wrappers, command result contracts, and a local/test Mock ESL event channel.

**Architecture:** `och-esl` owns only communication concerns. It preserves raw FreeSWITCH headers, maps them into `NormalizedEslEvent`, publishes application events for downstream modules, and exposes command wrappers used by call, IVR, and agent workspace services. Business decisions such as IVR routing, ACD assignment, and agent availability remain outside `och-esl`.

**Tech Stack:** Java 17, Spring Boot, Lombok, JUnit 5, AssertJ, Mockito, Spring ApplicationEventPublisher, FreeSWITCH ESL client abstraction already present in the module.

---

## Scope Check

This plan implements Task 2 from `docs/superpowers/plans/2026-06-09-opencallhub-inbound-production-plan.md`.

It does not implement call state updates, IVR runtime, ACD dispatch, phone-bar persistence, WebSocket fanout, or frontend pages. Those are handled by later plans.

Reference documents:

- Spec: `docs/superpowers/specs/2026-06-09-opencallhub-inbound-production-design.md`
- Master plan: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-production-plan.md`
- Schema plan: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-schema-plan.md`

## File Structure

- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/EslEventType.java`
  - Canonical event enum used by downstream modules.
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/NormalizedEslEvent.java`
  - Standard event object with callId, uuid, legId, caller, callee, DTMF, hangup cause, event time, raw headers, and channel variables.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventNormalizer.java`
  - Converts raw FreeSWITCH headers to `NormalizedEslEvent`.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventListener.java`
  - Accepts raw headers from real ESL client and publishes normalized application events.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslCommandClient.java`
  - Port interface for sending commands to FreeSWITCH.
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandService.java`
  - Command wrappers for answer, playback, collect digits, bridge, hold, unhold, mute, unmute, blind transfer, consult transfer, record, and hangup.
- Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandResult.java`
  - Command result DTO.
- Create: `och-esl/src/main/java/com/och/esl/application/MockEslEventService.java`
  - Local/test helper for publishing deterministic normalized ESL events.
- Test: `och-esl/src/test/java/com/och/esl/application/EslEventNormalizerTest.java`
- Test: `och-esl/src/test/java/com/och/esl/application/PhoneControlCommandServiceTest.java`
- Test: `och-esl/src/test/java/com/och/esl/application/MockEslEventServiceTest.java`

## Task 1: Normalize FreeSWITCH Events

**Files:**
- Test: `och-esl/src/test/java/com/och/esl/application/EslEventNormalizerTest.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/EslEventType.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/domain/NormalizedEslEvent.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventNormalizer.java`

- [ ] **Step 1: Write failing normalizer test**

Create `och-esl/src/test/java/com/och/esl/application/EslEventNormalizerTest.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
        assertThat(event.getEventTime()).isNotNull();
    }

    @Test
    void normalizesHangupCauseAndKeepsUnknownEventAsCustom() {
        EslEventNormalizer normalizer = new EslEventNormalizer();

        NormalizedEslEvent event = normalizer.normalize(Map.of(
                "Event-Name", "CHANNEL_HANGUP_COMPLETE",
                "Unique-ID", "uuid-b",
                "variable_call_id", "call-200",
                "Hangup-Cause", "NORMAL_CLEARING"
        ));

        assertThat(event.getEventTypeEnum()).isEqualTo(EslEventType.CHANNEL_HANGUP);
        assertThat(event.getHangupCause()).isEqualTo("NORMAL_CLEARING");

        NormalizedEslEvent custom = normalizer.normalize(Map.of(
                "Event-Name", "SOME_VENDOR_EVENT",
                "Unique-ID", "uuid-c"
        ));

        assertThat(custom.getEventTypeEnum()).isEqualTo(EslEventType.CUSTOM);
        assertThat(custom.getFreeswitchUuid()).isEqualTo("uuid-c");
    }

    @Test
    void usesCurrentTimeWhenTimestampIsMissing() {
        EslEventNormalizer normalizer = new EslEventNormalizer();

        NormalizedEslEvent event = normalizer.normalize(Map.of(
                "Event-Name", "CHANNEL_CREATE",
                "Unique-ID", "uuid-d",
                "variable_call_id", "call-300"
        ));

        assertThat(event.getEventTime()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
    }
}
```

- [ ] **Step 2: Run the normalizer test and verify failure**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventNormalizerTest
```

Expected result before implementation: `BUILD FAILURE` because the enum, fields, or mapping behavior is missing.

- [ ] **Step 3: Implement `EslEventType`**

Modify or create `och-esl/src/main/java/com/och/esl/domain/EslEventType.java`:

```java
package com.och.esl.domain;

public enum EslEventType {
    CHANNEL_CREATE,
    CHANNEL_ANSWER,
    CHANNEL_PROGRESS,
    CHANNEL_PROGRESS_MEDIA,
    CHANNEL_BRIDGE,
    CHANNEL_UNBRIDGE,
    CHANNEL_HANGUP,
    CHANNEL_DESTROY,
    CHANNEL_HOLD,
    CHANNEL_UNHOLD,
    DTMF,
    RECORD_START,
    RECORD_STOP,
    HEARTBEAT,
    CUSTOM
}
```

- [ ] **Step 4: Implement `NormalizedEslEvent`**

Modify or create `och-esl/src/main/java/com/och/esl/domain/NormalizedEslEvent.java`:

```java
package com.och.esl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedEslEvent {

    private EslEventType eventTypeEnum;
    private String eventType;
    private String callId;
    private String freeswitchUuid;
    private String legId;
    private String callerNumber;
    private String calledNumber;
    private String dtmfDigit;
    private String hangupCause;
    private LocalDateTime eventTime;
    private Map<String, String> rawHeaders;
    private Map<String, String> channelVars;

    public EslEventType getEventTypeEnum() {
        if (eventTypeEnum != null) {
            return eventTypeEnum;
        }
        if (eventType == null || eventType.isBlank()) {
            return EslEventType.CUSTOM;
        }
        try {
            return EslEventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            return EslEventType.CUSTOM;
        }
    }

    public EslEventType getEventType() {
        return getEventTypeEnum();
    }

    public String getDtmfDigit() {
        return dtmfDigit;
    }
}
```

- [ ] **Step 5: Implement `EslEventNormalizer`**

Modify or create `och-esl/src/main/java/com/och/esl/application/EslEventNormalizer.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EslEventNormalizer {

    public NormalizedEslEvent normalize(Map<String, String> headers) {
        Map<String, String> safeHeaders = headers == null ? Map.of() : new LinkedHashMap<>(headers);
        EslEventType eventType = mapEventType(safeHeaders.get("Event-Name"));
        Map<String, String> channelVars = extractChannelVars(safeHeaders);

        return NormalizedEslEvent.builder()
                .eventTypeEnum(eventType)
                .eventType(eventType.name())
                .callId(firstNonBlank(safeHeaders.get("variable_call_id"), safeHeaders.get("Call-ID")))
                .freeswitchUuid(safeHeaders.get("Unique-ID"))
                .legId(safeHeaders.get("variable_leg_id"))
                .callerNumber(firstNonBlank(safeHeaders.get("Caller-Caller-ID-Number"), safeHeaders.get("Caller-ANI")))
                .calledNumber(firstNonBlank(safeHeaders.get("Caller-Destination-Number"), safeHeaders.get("variable_destination_number")))
                .dtmfDigit(safeHeaders.get("DTMF-Digit"))
                .hangupCause(firstNonBlank(safeHeaders.get("Hangup-Cause"), safeHeaders.get("variable_hangup_cause")))
                .eventTime(parseEventTime(safeHeaders.get("Event-Date-Timestamp")))
                .rawHeaders(safeHeaders)
                .channelVars(channelVars)
                .build();
    }

    private EslEventType mapEventType(String rawEventName) {
        if (rawEventName == null || rawEventName.isBlank()) {
            return EslEventType.CUSTOM;
        }
        return switch (rawEventName) {
            case "CHANNEL_CREATE" -> EslEventType.CHANNEL_CREATE;
            case "CHANNEL_ANSWER" -> EslEventType.CHANNEL_ANSWER;
            case "CHANNEL_PROGRESS" -> EslEventType.CHANNEL_PROGRESS;
            case "CHANNEL_PROGRESS_MEDIA" -> EslEventType.CHANNEL_PROGRESS_MEDIA;
            case "CHANNEL_BRIDGE" -> EslEventType.CHANNEL_BRIDGE;
            case "CHANNEL_UNBRIDGE" -> EslEventType.CHANNEL_UNBRIDGE;
            case "CHANNEL_HANGUP", "CHANNEL_HANGUP_COMPLETE" -> EslEventType.CHANNEL_HANGUP;
            case "CHANNEL_DESTROY" -> EslEventType.CHANNEL_DESTROY;
            case "CHANNEL_HOLD" -> EslEventType.CHANNEL_HOLD;
            case "CHANNEL_UNHOLD" -> EslEventType.CHANNEL_UNHOLD;
            case "DTMF" -> EslEventType.DTMF;
            case "RECORD_START" -> EslEventType.RECORD_START;
            case "RECORD_STOP" -> EslEventType.RECORD_STOP;
            case "HEARTBEAT" -> EslEventType.HEARTBEAT;
            default -> EslEventType.CUSTOM;
        };
    }

    private LocalDateTime parseEventTime(String timestampMicros) {
        if (timestampMicros == null || timestampMicros.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            long micros = Long.parseLong(timestampMicros);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(micros / 1000), ZoneId.systemDefault());
        } catch (NumberFormatException e) {
            return LocalDateTime.now();
        }
    }

    private Map<String, String> extractChannelVars(Map<String, String> headers) {
        Map<String, String> channelVars = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (key != null && key.startsWith("variable_")) {
                channelVars.put(key.substring("variable_".length()), value);
            }
        });
        return channelVars;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
```

- [ ] **Step 6: Run normalizer test and verify pass**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventNormalizerTest
```

Expected result: `BUILD SUCCESS`, `Tests run: 3, Failures: 0, Errors: 0`.

## Task 2: Implement Phone Control Command Wrappers

**Files:**
- Test: `och-esl/src/test/java/com/och/esl/application/PhoneControlCommandServiceTest.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslCommandClient.java`
- Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandResult.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandService.java`

- [ ] **Step 1: Write command service test**

Create `och-esl/src/test/java/com/och/esl/application/PhoneControlCommandServiceTest.java`:

```java
package com.och.esl.application;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneControlCommandServiceTest {

    private final CapturingClient client = new CapturingClient();
    private final PhoneControlCommandService service = new PhoneControlCommandService(client);

    @Test
    void sendsAnswerHoldTransferRecordAndHangupCommands() {
        PhoneControlCommandResult answer = service.answer("uuid-a");
        PhoneControlCommandResult hold = service.hold("uuid-a");
        PhoneControlCommandResult unhold = service.unhold("uuid-a");
        PhoneControlCommandResult blindTransfer = service.blindTransfer("uuid-a", "8002");
        PhoneControlCommandResult consultTransfer = service.consultTransfer("uuid-a", "8003");
        PhoneControlCommandResult record = service.record("uuid-a", "/recordings/call-a.wav");
        PhoneControlCommandResult hangup = service.hangup("uuid-a", "NORMAL_CLEARING");

        assertThat(answer.isAccepted()).isTrue();
        assertThat(hold.getCommandType()).isEqualTo("HOLD");
        assertThat(unhold.getCommandType()).isEqualTo("UNHOLD");
        assertThat(blindTransfer.getRawCommand()).contains("uuid_transfer uuid-a 8002 XML default");
        assertThat(consultTransfer.getRawCommand()).contains("originate user/8003");
        assertThat(record.getRawCommand()).contains("uuid_record uuid-a start /recordings/call-a.wav");
        assertThat(hangup.getRawCommand()).contains("uuid_kill uuid-a NORMAL_CLEARING");
        assertThat(client.commands).hasSize(7);
        assertThat(client.commands.get(0)).contains("uuid_answer uuid-a");
    }

    static class CapturingClient implements EslCommandClient {
        private final List<String> commands = new ArrayList<>();

        @Override
        public String sendApiCommand(String command) {
            commands.add(command);
            return "+OK";
        }
    }
}
```

- [ ] **Step 2: Run command test and verify failure**

Run:

```bash
mvn -pl och-esl test -Dtest=PhoneControlCommandServiceTest
```

Expected result before implementation: `BUILD FAILURE` because command result, constructor, or methods are missing.

- [ ] **Step 3: Implement `EslCommandClient` port**

Modify or create `och-esl/src/main/java/com/och/esl/application/EslCommandClient.java`:

```java
package com.och.esl.application;

public interface EslCommandClient {
    String sendApiCommand(String command);
}
```

- [ ] **Step 4: Implement `PhoneControlCommandResult`**

Create `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandResult.java`:

```java
package com.och.esl.application;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhoneControlCommandResult {
    String commandId;
    String commandType;
    boolean accepted;
    String rawCommand;
    String rawResponse;
}
```

- [ ] **Step 5: Implement `PhoneControlCommandService`**

Modify or create `och-esl/src/main/java/com/och/esl/application/PhoneControlCommandService.java`:

```java
package com.och.esl.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneControlCommandService {

    private final EslCommandClient commandClient;

    public PhoneControlCommandResult answer(String uuid) {
        return send("ANSWER", "uuid_answer " + uuid);
    }

    public PhoneControlCommandResult hold(String uuid) {
        return send("HOLD", "uuid_hold " + uuid);
    }

    public PhoneControlCommandResult unhold(String uuid) {
        return send("UNHOLD", "uuid_hold off " + uuid);
    }

    public PhoneControlCommandResult mute(String uuid) {
        return send("MUTE", "uuid_audio " + uuid + " start read mute");
    }

    public PhoneControlCommandResult unmute(String uuid) {
        return send("UNMUTE", "uuid_audio " + uuid + " stop read mute");
    }

    public PhoneControlCommandResult playback(String uuid, String filePath) {
        return send("PLAYBACK", "uuid_broadcast " + uuid + " " + filePath + " aleg");
    }

    public PhoneControlCommandResult collectDigits(String uuid, int minDigits, int maxDigits, int timeoutMillis) {
        return send("COLLECT_DIGITS", "uuid_recv_dtmf " + uuid + " " + minDigits + " " + maxDigits + " " + timeoutMillis);
    }

    public PhoneControlCommandResult bridge(String callerUuid, String agentEndpoint) {
        return send("BRIDGE", "uuid_bridge " + callerUuid + " " + agentEndpoint);
    }

    public PhoneControlCommandResult blindTransfer(String uuid, String targetExtension) {
        return send("BLIND_TRANSFER", "uuid_transfer " + uuid + " " + targetExtension + " XML default");
    }

    public PhoneControlCommandResult consultTransfer(String uuid, String targetExtension) {
        return send("CONSULT_TRANSFER", "originate user/" + targetExtension + " &park() {consult_source_uuid=" + uuid + "}");
    }

    public PhoneControlCommandResult record(String uuid, String filePath) {
        return send("RECORD", "uuid_record " + uuid + " start " + filePath);
    }

    public PhoneControlCommandResult stopRecord(String uuid, String filePath) {
        return send("STOP_RECORD", "uuid_record " + uuid + " stop " + filePath);
    }

    public PhoneControlCommandResult hangup(String uuid, String cause) {
        String safeCause = cause == null || cause.isBlank() ? "NORMAL_CLEARING" : cause;
        return send("HANGUP", "uuid_kill " + uuid + " " + safeCause);
    }

    private PhoneControlCommandResult send(String commandType, String rawCommand) {
        String response = commandClient.sendApiCommand(rawCommand);
        return PhoneControlCommandResult.builder()
                .commandId(UUID.randomUUID().toString())
                .commandType(commandType)
                .accepted(response != null && response.startsWith("+OK"))
                .rawCommand(rawCommand)
                .rawResponse(response)
                .build();
    }
}
```

- [ ] **Step 6: Run command test and verify pass**

Run:

```bash
mvn -pl och-esl test -Dtest=PhoneControlCommandServiceTest
```

Expected result: `BUILD SUCCESS`, `Tests run: 1, Failures: 0, Errors: 0`.

## Task 3: Add Mock ESL Event Channel

**Files:**
- Test: `och-esl/src/test/java/com/och/esl/application/MockEslEventServiceTest.java`
- Create: `och-esl/src/main/java/com/och/esl/application/MockEslEventService.java`

- [ ] **Step 1: Write Mock ESL event test**

Create `och-esl/src/test/java/com/och/esl/application/MockEslEventServiceTest.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockEslEventServiceTest {

    private final CapturingPublisher publisher = new CapturingPublisher();
    private final MockEslEventService service = new MockEslEventService(publisher);

    @Test
    void publishesDeterministicInboundDtmfAndHangupEvents() {
        service.publishInboundCreate("call-1", "uuid-1", "13800000001", "4008001000");
        service.publishDtmf("call-1", "uuid-1", "1");
        service.publishHangup("call-1", "uuid-1", "NORMAL_CLEARING");

        assertThat(publisher.events).hasSize(3);
        assertThat(publisher.events.get(0).getEventTypeEnum()).isEqualTo(EslEventType.CHANNEL_CREATE);
        assertThat(publisher.events.get(1).getEventTypeEnum()).isEqualTo(EslEventType.DTMF);
        assertThat(publisher.events.get(1).getDtmfDigit()).isEqualTo("1");
        assertThat(publisher.events.get(2).getHangupCause()).isEqualTo("NORMAL_CLEARING");
    }

    static class CapturingPublisher implements ApplicationEventPublisher {
        private final List<NormalizedEslEvent> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            events.add((NormalizedEslEvent) event);
        }
    }
}
```

- [ ] **Step 2: Run Mock ESL test and verify failure**

Run:

```bash
mvn -pl och-esl test -Dtest=MockEslEventServiceTest
```

Expected result before implementation: `BUILD FAILURE` because `MockEslEventService` is missing.

- [ ] **Step 3: Implement Mock ESL event service**

Create `och-esl/src/main/java/com/och/esl/application/MockEslEventService.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.EslEventType;
import com.och.esl.domain.NormalizedEslEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Profile({"local", "test"})
@RequiredArgsConstructor
public class MockEslEventService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishInboundCreate(String callId, String uuid, String callerNumber, String calledNumber) {
        publish(EslEventType.CHANNEL_CREATE, callId, uuid, null, callerNumber, calledNumber, null, null);
    }

    public void publishAnswer(String callId, String uuid) {
        publish(EslEventType.CHANNEL_ANSWER, callId, uuid, null, null, null, null, null);
    }

    public void publishBridge(String callId, String uuid, String legId) {
        publish(EslEventType.CHANNEL_BRIDGE, callId, uuid, legId, null, null, null, null);
    }

    public void publishDtmf(String callId, String uuid, String digit) {
        publish(EslEventType.DTMF, callId, uuid, null, null, null, digit, null);
    }

    public void publishHangup(String callId, String uuid, String hangupCause) {
        publish(EslEventType.CHANNEL_HANGUP, callId, uuid, null, null, null, null, hangupCause);
    }

    private void publish(EslEventType eventType, String callId, String uuid, String legId,
                         String callerNumber, String calledNumber, String digit, String hangupCause) {
        eventPublisher.publishEvent(NormalizedEslEvent.builder()
                .eventTypeEnum(eventType)
                .eventType(eventType.name())
                .callId(callId)
                .freeswitchUuid(uuid)
                .legId(legId)
                .callerNumber(callerNumber)
                .calledNumber(calledNumber)
                .dtmfDigit(digit)
                .hangupCause(hangupCause)
                .eventTime(LocalDateTime.now())
                .rawHeaders(Map.of("mock", "true"))
                .channelVars(Map.of("call_id", callId))
                .build());
    }
}
```

- [ ] **Step 4: Run Mock ESL test and verify pass**

Run:

```bash
mvn -pl och-esl test -Dtest=MockEslEventServiceTest
```

Expected result: `BUILD SUCCESS`, `Tests run: 1, Failures: 0, Errors: 0`.

## Task 4: Wire Real ESL Listener to Normalizer

**Files:**
- Test: `och-esl/src/test/java/com/och/esl/application/EslEventListenerTest.java`
- Modify/Create: `och-esl/src/main/java/com/och/esl/application/EslEventListener.java`

- [ ] **Step 1: Write listener test**

Create `och-esl/src/test/java/com/och/esl/application/EslEventListenerTest.java`:

```java
package com.och.esl.application;

import com.och.esl.domain.NormalizedEslEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EslEventListenerTest {

    private final CapturingPublisher publisher = new CapturingPublisher();
    private final EslEventListener listener = new EslEventListener(new EslEventNormalizer(), publisher);

    @Test
    void normalizesAndPublishesRawHeaders() {
        listener.onRawEvent(Map.of(
                "Event-Name", "DTMF",
                "Unique-ID", "uuid-a",
                "variable_call_id", "call-100",
                "DTMF-Digit", "1"
        ));

        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.get(0).getCallId()).isEqualTo("call-100");
        assertThat(publisher.events.get(0).getDtmfDigit()).isEqualTo("1");
    }

    static class CapturingPublisher implements ApplicationEventPublisher {
        private final List<NormalizedEslEvent> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            events.add((NormalizedEslEvent) event);
        }
    }
}
```

- [ ] **Step 2: Implement listener method**

Modify or create `EslEventListener` with constructor dependencies `EslEventNormalizer` and `ApplicationEventPublisher`, and method:

```java
public void onRawEvent(Map<String, String> headers) {
    NormalizedEslEvent event = normalizer.normalize(headers);
    eventPublisher.publishEvent(event);
}
```

Keep any existing real ESL subscription code, but route raw headers through this method.

- [ ] **Step 3: Run listener test**

Run:

```bash
mvn -pl och-esl test -Dtest=EslEventListenerTest
```

Expected result: `BUILD SUCCESS`.

## Task 5: Run Full ESL Verification

**Files:**
- All files in this plan.

- [ ] **Step 1: Run all ESL tests**

Run:

```bash
mvn -pl och-esl test
```

Expected result: `BUILD SUCCESS` and all ESL tests pass.

- [ ] **Step 2: Compile API dependency chain**

Run:

```bash
mvn -pl och-api -am test -DskipTests
```

Expected result: `BUILD SUCCESS`. This ensures downstream modules still compile with the new ESL contracts.

- [ ] **Step 3: Commit ESL checkpoint**

Commit only after user approval to commit:

```bash
git add och-esl/src/main/java/com/och/esl och-esl/src/test/java/com/och/esl
git commit -m "feat: add inbound esl boundary"
```

## Self-Review

### Spec coverage

This plan covers the communication boundary from the production inbound spec: normalized ESL events, raw header preservation, command wrappers, command result contracts, and Mock ESL automated regression. It intentionally does not implement call-state consumption or phone-bar persistence.

### Placeholder scan

No placeholder terms are used. Every test, file path, command, expected result, and code skeleton needed for this subsystem is specified.

### Type consistency

`EslEventType`, `NormalizedEslEvent`, `EslEventNormalizer`, `PhoneControlCommandService`, `PhoneControlCommandResult`, `EslCommandClient`, and `MockEslEventService` use consistent names and package paths under `com.och.esl`.
