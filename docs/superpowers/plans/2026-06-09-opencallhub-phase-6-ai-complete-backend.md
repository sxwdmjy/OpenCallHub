# openCallHub Phase 6 AI Complete Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the complete Phase 6 backend AI contact-center capability for openCallHub, using `res_ai_plugin` as the only plugin configuration source and completing ASR/TTS/LLM/Workflow invocation, robot sessions, summaries, tags, cost logs, plugin logs, AI quality results, safety controls, and tests.

**Architecture:** `och-resource` owns AI plugin resource CRUD, masking, audit, and delete protection. `och-ai` reads enabled resource plugin configurations by plugin code, routes calls through typed adapters, persists conversation traces, records costs, writes safe plugin-call summaries, and exposes backend APIs. The implementation stays backend-only: no frontend pages, no `och-quality` template/review workflow, no ESL command ownership.

**Tech Stack:** Java 17, Spring Boot 3.5, Maven multi-module, MyBatis-Plus, MySQL/Flyway-style SQL migration files, JUnit 5, Mockito, Spring `RestTemplate` and `MockRestServiceServer`.

---

## Scope Check

The spec covers one cohesive backend subsystem: Phase 6 AI contact-center backend. It touches `och-ai` heavily and reads `och-resource` plugin configuration, but it does not implement frontend UI, FreeSWITCH ESL control, outbound scheduling, `och-quality` templates, or report aggregation.

## File Structure

### Modify existing files

- `och-ai/pom.xml` — add dependency on `och-resource` so `och-ai` can read `res_ai_plugin` through the existing mapper/domain.
- `och-ai/src/main/resources/db/migration/V12__ai_robot_schema.sql` — add `ai_quality_result` table and add missing indexes/columns needed by complete Phase 6.
- `och-ai/src/main/java/com/och/ai/application/AiPluginInvokeService.java` — replace simulated ASR/TTS/LLM responses with resource-driven adapter invocation.
- `och-ai/src/main/java/com/och/ai/api/AiPluginController.java` — align request methods with full plugin invocation request objects.
- `och-ai/src/main/java/com/och/ai/application/RobotFlowApplicationService.java` — support edit, disable, version list/detail, and published plugin snapshot fields.
- `och-ai/src/main/java/com/och/ai/api/RobotFlowController.java` — expose edit, disable, version list/detail endpoints.
- `och-ai/src/main/java/com/och/ai/application/RobotFlowEngine.java` — load published version plugin codes, update summary/tags/slots, and integrate cost/log helpers.
- `och-ai/src/main/java/com/och/ai/api/RobotSessionController.java` — add detail and trace query endpoints.
- `och-ai/src/main/java/com/och/ai/application/adapter/DifyAdapter.java` — read endpoint and key from resource plugin config.
- `och-ai/src/main/java/com/och/ai/application/adapter/RAGFlowAdapter.java` — read endpoint, key, knowledge base config from resource plugin config.
- `och-ai/src/main/java/com/och/ai/application/adapter/LangFlowAdapter.java` — read endpoint, key, flow id config from resource plugin config.
- `och-ai/src/main/java/com/och/ai/application/adapter/PluginAdapterFactory.java` — select ASR/TTS/Bot adapters by typed plugin config.

### Create new files

- `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfig.java` — internal immutable plugin config snapshot.
- `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfigService.java` — reads and validates `res_ai_plugin` records.
- `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginType.java` — canonical plugin type constants for `ASR`, `TTS`, `LLM`, `WORKFLOW`.
- `och-ai/src/main/java/com/och/ai/application/plugin/PluginRequestSummary.java` — safe request summary builder.
- `och-ai/src/main/java/com/och/ai/application/plugin/PluginResponseSummary.java` — safe response summary builder.
- `och-ai/src/main/java/com/och/ai/application/plugin/PluginCallLogger.java` — writes `plugin_call_log` without sensitive text.
- `och-ai/src/main/java/com/och/ai/application/adapter/AsrAdapter.java` — ASR adapter contract.
- `och-ai/src/main/java/com/och/ai/application/adapter/TtsAdapter.java` — TTS adapter contract.
- `och-ai/src/main/java/com/och/ai/application/adapter/HttpAsrAdapter.java` — generic HTTP ASR adapter.
- `och-ai/src/main/java/com/och/ai/application/adapter/HttpTtsAdapter.java` — generic HTTP TTS adapter.
- `och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeRequest.java` — ASR invocation request.
- `och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeResult.java` — ASR invocation result.
- `och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeRequest.java` — TTS invocation request.
- `och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeResult.java` — TTS invocation result.
- `och-ai/src/main/java/com/och/ai/application/dto/BotInvokeRequest.java` — LLM/Workflow invocation request.
- `och-ai/src/main/java/com/och/ai/application/dto/BotInvokeResult.java` — LLM/Workflow invocation result.
- `och-ai/src/main/java/com/och/ai/domain/AiQualityResult.java` — AI quality result entity.
- `och-ai/src/main/java/com/och/ai/mapper/AiQualityResultMapper.java` — MyBatis-Plus mapper.
- `och-ai/src/main/java/com/och/ai/service/IAiQualityResultService.java` — quality result service interface.
- `och-ai/src/main/java/com/och/ai/service/impl/AiQualityResultServiceImpl.java` — quality result persistence service.
- `och-ai/src/main/java/com/och/ai/application/AiQualityResultApplicationService.java` — application service for AI quality output.
- `och-ai/src/main/java/com/och/ai/api/AiQualityResultController.java` — quality result API.

### Test files

- `och-ai/src/test/java/com/och/ai/application/plugin/AiPluginConfigServiceTest.java`
- `och-ai/src/test/java/com/och/ai/application/plugin/PluginCallLoggerTest.java`
- `och-ai/src/test/java/com/och/ai/application/adapter/HttpAsrAdapterTest.java`
- `och-ai/src/test/java/com/och/ai/application/adapter/HttpTtsAdapterTest.java`
- `och-ai/src/test/java/com/och/ai/application/adapter/DifyAdapterTest.java`
- `och-ai/src/test/java/com/och/ai/application/RobotFlowApplicationServiceTest.java`
- `och-ai/src/test/java/com/och/ai/application/RobotFlowEngineTest.java`
- `och-ai/src/test/java/com/och/ai/application/AiQualityResultApplicationServiceTest.java`

---

## Task 1: Resource-Driven Plugin Configuration

**Files:**
- Modify: `och-ai/pom.xml`
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfig.java`
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfigService.java`
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/AiPluginType.java`
- Test: `och-ai/src/test/java/com/och/ai/application/plugin/AiPluginConfigServiceTest.java`

- [ ] **Step 1: Write failing config service tests**

Create `AiPluginConfigServiceTest.java` with these tests:

```java
package com.och.ai.application.plugin;

import com.och.common.error.BizException;
import com.och.resource.domain.AiPlugin;
import com.och.resource.mapper.AiPluginMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiPluginConfigServiceTest {

    private final AiPluginMapper aiPluginMapper = mock(AiPluginMapper.class);
    private final AiPluginConfigService service = new AiPluginConfigService(aiPluginMapper);

    @Test
    void shouldLoadEnabledPluginByCodeAndType() {
        AiPlugin plugin = new AiPlugin();
        plugin.setPluginName("Dify客服机器人");
        plugin.setPluginType("WORKFLOW");
        plugin.setProvider("DIFY");
        plugin.setEndpointUrl("https://dify.example/v1");
        plugin.setApiKey("raw-key");
        plugin.setApiSecret("raw-secret");
        plugin.setConfigJson("{\"responseMode\":\"blocking\"}");
        plugin.setEnabled(true);
        plugin.setStatus("ENABLED");
        when(aiPluginMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(plugin);

        AiPluginConfig config = service.getRequired("DIFY_MAIN", AiPluginType.WORKFLOW);

        assertThat(config.pluginCode()).isEqualTo("DIFY_MAIN");
        assertThat(config.pluginType()).isEqualTo(AiPluginType.WORKFLOW);
        assertThat(config.provider()).isEqualTo("DIFY");
        assertThat(config.endpointUrl()).isEqualTo("https://dify.example/v1");
        assertThat(config.apiKey()).isEqualTo("raw-key");
        assertThat(config.configJson()).contains("responseMode");
    }

    @Test
    void shouldRejectMissingPlugin() {
        when(aiPluginMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThatThrownBy(() -> service.getRequired("MISSING", AiPluginType.LLM))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI插件不存在");
    }

    @Test
    void shouldRejectDisabledPlugin() {
        AiPlugin plugin = new AiPlugin();
        plugin.setPluginName("停用插件");
        plugin.setPluginType("LLM");
        plugin.setEnabled(false);
        when(aiPluginMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(plugin);

        assertThatThrownBy(() -> service.getRequired("DISABLED", AiPluginType.LLM))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI插件已停用");
    }

    @Test
    void shouldRejectTypeMismatch() {
        AiPlugin plugin = new AiPlugin();
        plugin.setPluginName("语音插件");
        plugin.setPluginType("ASR");
        plugin.setEnabled(true);
        when(aiPluginMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(plugin);

        assertThatThrownBy(() -> service.getRequired("ASR_MAIN", AiPluginType.TTS))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI插件类型不匹配");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl och-ai -Dtest=AiPluginConfigServiceTest test
```

Expected: compilation fails because `AiPluginConfig`, `AiPluginConfigService`, and `AiPluginType` do not exist.

- [ ] **Step 3: Add `och-resource` dependency**

Modify `och-ai/pom.xml` dependencies:

```xml
<dependency>
    <groupId>com.och</groupId>
    <artifactId>och-resource</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 4: Implement config snapshot and service**

Create `AiPluginType.java`:

```java
package com.och.ai.application.plugin;

public enum AiPluginType {
    ASR,
    TTS,
    LLM,
    WORKFLOW;

    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value.trim());
    }
}
```

Create `AiPluginConfig.java`:

```java
package com.och.ai.application.plugin;

/**
 * AI 插件内部调用配置快照。
 * <p>该对象只在 och-ai 内部流转，接口层不得直接返回，避免密钥泄漏。</p>
 */
public record AiPluginConfig(
        String pluginCode,
        AiPluginType pluginType,
        String provider,
        String endpointUrl,
        String apiKey,
        String apiSecret,
        String configJson,
        Boolean enabled
) {
}
```

Create `AiPluginConfigService.java`:

```java
package com.och.ai.application.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.AiPlugin;
import com.och.resource.mapper.AiPluginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI 插件配置读取服务。
 * <p>只从资源中心读取插件配置，统一校验存在性、启用状态和插件类型。</p>
 */
@Service
@RequiredArgsConstructor
public class AiPluginConfigService {

    private final AiPluginMapper aiPluginMapper;

    public AiPluginConfig getRequired(String pluginCode, AiPluginType expectedType) {
        if (pluginCode == null || pluginCode.isBlank()) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "AI插件编码不能为空");
        }
        AiPlugin plugin = aiPluginMapper.selectOne(new LambdaQueryWrapper<AiPlugin>()
                .eq(AiPlugin::getPluginName, pluginCode)
                .last("LIMIT 1"));
        if (plugin == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "AI插件不存在: " + pluginCode);
        }
        if (!Boolean.TRUE.equals(plugin.getEnabled())) {
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED, "AI插件已停用: " + pluginCode);
        }
        if (expectedType != null && !expectedType.matches(plugin.getPluginType())) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR,
                    "AI插件类型不匹配: pluginCode=" + pluginCode + ", expected=" + expectedType + ", actual=" + plugin.getPluginType());
        }
        return new AiPluginConfig(pluginCode, AiPluginType.valueOf(plugin.getPluginType().trim().toUpperCase()),
                plugin.getProvider(), plugin.getEndpointUrl(), plugin.getApiKey(), plugin.getApiSecret(),
                plugin.getConfigJson(), plugin.getEnabled());
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
mvn -pl och-ai -Dtest=AiPluginConfigServiceTest test
```

Expected: 4 tests pass.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/pom.xml och-ai/src/main/java/com/och/ai/application/plugin/AiPluginType.java och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfig.java och-ai/src/main/java/com/och/ai/application/plugin/AiPluginConfigService.java och-ai/src/test/java/com/och/ai/application/plugin/AiPluginConfigServiceTest.java
git commit -m "feat: load ai plugin config from resource center"
```

---

## Task 2: Safe Plugin Call Logging

**Files:**
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/PluginRequestSummary.java`
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/PluginResponseSummary.java`
- Create: `och-ai/src/main/java/com/och/ai/application/plugin/PluginCallLogger.java`
- Test: `och-ai/src/test/java/com/och/ai/application/plugin/PluginCallLoggerTest.java`

- [ ] **Step 1: Write failing logger test**

Create `PluginCallLoggerTest.java`:

```java
package com.och.ai.application.plugin;

import com.och.ai.domain.PluginCallLog;
import com.och.ai.mapper.PluginCallLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PluginCallLoggerTest {

    private final PluginCallLogMapper mapper = mock(PluginCallLogMapper.class);
    private final PluginCallLogger logger = new PluginCallLogger(mapper);

    @Test
    void shouldPersistOnlySafeSummaries() {
        logger.recordSuccess("CALL-001", "SESSION-001", AiPluginType.WORKFLOW, "DIFY_MAIN", 2,
                PluginRequestSummary.ofText("secret prompt content", "DIFY"),
                PluginResponseSummary.ofText("supplier full response", "QUERY", 200),
                123, 0.02D);

        ArgumentCaptor<PluginCallLog> captor = ArgumentCaptor.forClass(PluginCallLog.class);
        verify(mapper).insert(captor.capture());
        PluginCallLog log = captor.getValue();

        assertThat(log.getPluginType()).isEqualTo("WORKFLOW");
        assertThat(log.getPluginCode()).isEqualTo("DIFY_MAIN");
        assertThat(log.getRequestData()).contains("textLength", "provider");
        assertThat(log.getRequestData()).doesNotContain("secret prompt content");
        assertThat(log.getResponseData()).contains("textLength", "intent", "statusCode");
        assertThat(log.getResponseData()).doesNotContain("supplier full response");
        assertThat(log.getSuccess()).isTrue();
    }

    @Test
    void shouldPersistFailureTypeWithoutSensitiveMessage() {
        logger.recordFailure("CALL-002", "SESSION-002", AiPluginType.ASR, "ASR_MAIN", 1,
                PluginRequestSummary.ofAudio("recording-file", "GENERIC"), "SocketTimeoutException", 3000);

        ArgumentCaptor<PluginCallLog> captor = ArgumentCaptor.forClass(PluginCallLog.class);
        verify(mapper).insert(captor.capture());
        PluginCallLog log = captor.getValue();

        assertThat(log.getSuccess()).isFalse();
        assertThat(log.getErrorMessage()).isEqualTo("SocketTimeoutException");
        assertThat(log.getRequestData()).contains("audioRefPresent");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl och-ai -Dtest=PluginCallLoggerTest test
```

Expected: compilation fails because logger and summary classes do not exist.

- [ ] **Step 3: Implement summaries and logger**

Create `PluginRequestSummary.java`:

```java
package com.och.ai.application.plugin;

public record PluginRequestSummary(String json) {

    public static PluginRequestSummary ofText(String text, String provider) {
        return new PluginRequestSummary("{\"textLength\":" + length(text) + ",\"provider\":\"" + safe(provider) + "\"}");
    }

    public static PluginRequestSummary ofAudio(String audioRef, String provider) {
        return new PluginRequestSummary("{\"audioRefPresent\":" + (audioRef != null && !audioRef.isBlank())
                + ",\"provider\":\"" + safe(provider) + "\"}");
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
```

Create `PluginResponseSummary.java`:

```java
package com.och.ai.application.plugin;

public record PluginResponseSummary(String json) {

    public static PluginResponseSummary ofText(String text, String intent, Integer statusCode) {
        return new PluginResponseSummary("{\"textLength\":" + (text == null ? 0 : text.length())
                + ",\"intent\":\"" + safe(intent) + "\",\"statusCode\":" + (statusCode == null ? 0 : statusCode) + "}");
    }

    public static PluginResponseSummary ofAudio(String audioUrl, Integer statusCode) {
        return new PluginResponseSummary("{\"audioUrlPresent\":" + (audioUrl != null && !audioUrl.isBlank())
                + ",\"statusCode\":" + (statusCode == null ? 0 : statusCode) + "}");
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
```

Create `PluginCallLogger.java`:

```java
package com.och.ai.application.plugin;

import com.och.ai.domain.PluginCallLog;
import com.och.ai.mapper.PluginCallLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PluginCallLogger {

    private final PluginCallLogMapper pluginCallLogMapper;

    public void recordSuccess(String callId, String sessionId, AiPluginType pluginType, String pluginCode, Integer turnNo,
                              PluginRequestSummary requestSummary, PluginResponseSummary responseSummary,
                              Integer latencyMs, Double cost) {
        PluginCallLog log = base(callId, sessionId, pluginType, pluginCode, turnNo, requestSummary);
        log.setResponseData(responseSummary == null ? "{}" : responseSummary.json());
        log.setLatencyMs(latencyMs == null ? 0 : latencyMs);
        log.setSuccess(true);
        log.setCost(cost == null ? 0D : cost);
        pluginCallLogMapper.insert(log);
    }

    public void recordFailure(String callId, String sessionId, AiPluginType pluginType, String pluginCode, Integer turnNo,
                              PluginRequestSummary requestSummary, String errorType, Integer latencyMs) {
        PluginCallLog log = base(callId, sessionId, pluginType, pluginCode, turnNo, requestSummary);
        log.setResponseData("{}");
        log.setLatencyMs(latencyMs == null ? 0 : latencyMs);
        log.setSuccess(false);
        log.setErrorMessage(errorType == null || errorType.isBlank() ? "UNKNOWN_ERROR" : errorType);
        log.setCost(0D);
        pluginCallLogMapper.insert(log);
    }

    private PluginCallLog base(String callId, String sessionId, AiPluginType pluginType, String pluginCode, Integer turnNo,
                               PluginRequestSummary requestSummary) {
        PluginCallLog log = new PluginCallLog();
        log.setCallId(callId);
        log.setSessionId(sessionId);
        log.setPluginType(pluginType.name());
        log.setPluginCode(pluginCode);
        log.setTurnNo(turnNo);
        log.setRequestData(requestSummary == null ? "{}" : requestSummary.json());
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl och-ai -Dtest=PluginCallLoggerTest test
```

Expected: 2 tests pass.

- [ ] **Step 5: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/plugin/PluginRequestSummary.java och-ai/src/main/java/com/och/ai/application/plugin/PluginResponseSummary.java och-ai/src/main/java/com/och/ai/application/plugin/PluginCallLogger.java och-ai/src/test/java/com/och/ai/application/plugin/PluginCallLoggerTest.java
git commit -m "feat: add safe ai plugin call logging"
```

---

## Task 3: Generic HTTP ASR and TTS Adapters

**Files:**
- Create: `och-ai/src/main/java/com/och/ai/application/adapter/AsrAdapter.java`
- Create: `och-ai/src/main/java/com/och/ai/application/adapter/TtsAdapter.java`
- Create: `och-ai/src/main/java/com/och/ai/application/adapter/HttpAsrAdapter.java`
- Create: `och-ai/src/main/java/com/och/ai/application/adapter/HttpTtsAdapter.java`
- Create: `och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeRequest.java`
- Create: `och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeResult.java`
- Create: `och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeRequest.java`
- Create: `och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeResult.java`
- Test: `och-ai/src/test/java/com/och/ai/application/adapter/HttpAsrAdapterTest.java`
- Test: `och-ai/src/test/java/com/och/ai/application/adapter/HttpTtsAdapterTest.java`

- [ ] **Step 1: Write failing ASR adapter test**

Create `HttpAsrAdapterTest.java`:

```java
package com.och.ai.application.adapter;

import com.och.ai.application.dto.AsrInvokeRequest;
import com.och.ai.application.dto.AsrInvokeResult;
import com.och.ai.application.plugin.AiPluginConfig;
import com.och.ai.application.plugin.AiPluginType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpAsrAdapterTest {

    @Test
    void shouldCallGenericAsrEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpAsrAdapter adapter = new HttpAsrAdapter(restTemplate);
        AiPluginConfig config = new AiPluginConfig("ASR_MAIN", AiPluginType.ASR, "GENERIC_HTTP",
                "https://asr.example/recognize", "raw-key", "raw-secret", "{}", true);
        server.expect(requestTo("https://asr.example/recognize"))
                .andExpect(header("Authorization", "Bearer raw-key"))
                .andRespond(withSuccess("{\"text\":\"您好\",\"isFinal\":true,\"confidence\":0.93,\"latencyMs\":120}", MediaType.APPLICATION_JSON));

        AsrInvokeResult result = adapter.invoke(new AsrInvokeRequest("https://file/call.wav", null, "zh-CN", "wav", 16000, "S1"), config);

        assertThat(result.text()).isEqualTo("您好");
        assertThat(result.isFinal()).isTrue();
        assertThat(result.confidence()).isEqualTo(0.93D);
        assertThat(result.latencyMs()).isEqualTo(120);
        server.verify();
    }
}
```

- [ ] **Step 2: Write failing TTS adapter test**

Create `HttpTtsAdapterTest.java`:

```java
package com.och.ai.application.adapter;

import com.och.ai.application.dto.TtsInvokeRequest;
import com.och.ai.application.dto.TtsInvokeResult;
import com.och.ai.application.plugin.AiPluginConfig;
import com.och.ai.application.plugin.AiPluginType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpTtsAdapterTest {

    @Test
    void shouldCallGenericTtsEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpTtsAdapter adapter = new HttpTtsAdapter(restTemplate);
        AiPluginConfig config = new AiPluginConfig("TTS_MAIN", AiPluginType.TTS, "GENERIC_HTTP",
                "https://tts.example/synthesize", "raw-key", "raw-secret", "{}", true);
        server.expect(requestTo("https://tts.example/synthesize"))
                .andExpect(header("Authorization", "Bearer raw-key"))
                .andRespond(withSuccess("{\"audioUrl\":\"https://file/tts.wav\",\"audioDataRef\":\"FILE-001\",\"latencyMs\":210}", MediaType.APPLICATION_JSON));

        TtsInvokeResult result = adapter.invoke(new TtsInvokeRequest("您好", "zhixiaomei", "wav", 1.0D, "S1"), config);

        assertThat(result.audioUrl()).isEqualTo("https://file/tts.wav");
        assertThat(result.audioDataRef()).isEqualTo("FILE-001");
        assertThat(result.latencyMs()).isEqualTo(210);
        server.verify();
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run:

```bash
mvn -pl och-ai -Dtest=HttpAsrAdapterTest,HttpTtsAdapterTest test
```

Expected: compilation fails because adapter contracts and DTOs do not exist.

- [ ] **Step 4: Implement DTOs and adapters**

Create DTO records:

```java
package com.och.ai.application.dto;

public record AsrInvokeRequest(String audioUrl, String audioDataRef, String language, String format, Integer sampleRate, String sessionId) {}
```

```java
package com.och.ai.application.dto;

public record AsrInvokeResult(String text, boolean isFinal, double confidence, int latencyMs) {}
```

```java
package com.och.ai.application.dto;

public record TtsInvokeRequest(String text, String voice, String format, Double speed, String sessionId) {}
```

```java
package com.och.ai.application.dto;

public record TtsInvokeResult(String audioUrl, String audioDataRef, int latencyMs) {}
```

Create `AsrAdapter.java`:

```java
package com.och.ai.application.adapter;

import com.och.ai.application.dto.AsrInvokeRequest;
import com.och.ai.application.dto.AsrInvokeResult;
import com.och.ai.application.plugin.AiPluginConfig;

public interface AsrAdapter {
    String getProvider();
    AsrInvokeResult invoke(AsrInvokeRequest request, AiPluginConfig config);
}
```

Create `TtsAdapter.java`:

```java
package com.och.ai.application.adapter;

import com.och.ai.application.dto.TtsInvokeRequest;
import com.och.ai.application.dto.TtsInvokeResult;
import com.och.ai.application.plugin.AiPluginConfig;

public interface TtsAdapter {
    String getProvider();
    TtsInvokeResult invoke(TtsInvokeRequest request, AiPluginConfig config);
}
```

Create HTTP adapters with constructor-injected `RestTemplate`, JSON map parsing, and Authorization header when `apiKey` is present.

- [ ] **Step 5: Run tests to verify they pass**

Run:

```bash
mvn -pl och-ai -Dtest=HttpAsrAdapterTest,HttpTtsAdapterTest test
```

Expected: 2 tests pass.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/adapter/AsrAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/TtsAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/HttpAsrAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/HttpTtsAdapter.java och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeRequest.java och-ai/src/main/java/com/och/ai/application/dto/AsrInvokeResult.java och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeRequest.java och-ai/src/main/java/com/och/ai/application/dto/TtsInvokeResult.java och-ai/src/test/java/com/och/ai/application/adapter/HttpAsrAdapterTest.java och-ai/src/test/java/com/och/ai/application/adapter/HttpTtsAdapterTest.java
git commit -m "feat: add generic http asr tts adapters"
```

---

## Task 4: Complete Plugin Invocation Service

**Files:**
- Modify: `och-ai/src/main/java/com/och/ai/application/AiPluginInvokeService.java`
- Modify: `och-ai/src/main/java/com/och/ai/api/AiPluginController.java`
- Create: `och-ai/src/test/java/com/och/ai/application/AiPluginInvokeServiceTest.java`

- [ ] **Step 1: Write failing invocation service test**

Create a test that mocks `AiPluginConfigService`, `AsrAdapter`, `TtsAdapter`, `BotAdapter`, `PluginCallLogger`, and `AiCostTracker`. Verify ASR/TTS/LLM all read resource config, call the selected adapter, and write safe logs.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl och-ai -Dtest=AiPluginInvokeServiceTest test
```

Expected: test fails because `AiPluginInvokeService` still returns simulated responses.

- [ ] **Step 3: Implement service orchestration**

Replace simulated ASR/TTS/LLM methods with orchestration that:

```java
AiPluginConfig config = aiPluginConfigService.getRequired(pluginCode, AiPluginType.ASR);
AsrAdapter adapter = pluginAdapterFactory.getAsrAdapter(config);
AsrInvokeResult result = adapter.invoke(request, config);
pluginCallLogger.recordSuccess(null, request.sessionId(), AiPluginType.ASR, pluginCode, null,
        PluginRequestSummary.ofAudio(request.audioUrl() != null ? request.audioUrl() : request.audioDataRef(), config.provider()),
        PluginResponseSummary.ofText(result.text(), "ASR", 200), result.latencyMs(), 0D);
return result;
```

Use the same structure for TTS and LLM/Workflow, with plugin type validation and failure logging.

- [ ] **Step 4: Update controller request methods**

`AiPluginController` should expose:

```java
@PostMapping("/plugins/asr/test")
public ApiResponse<AsrInvokeResult> asr(@RequestBody AsrInvokeRequest request,
                                        @RequestParam String pluginCode) {
    return success(aiPluginInvokeService.invokeAsr(request, pluginCode));
}
```

Add equivalent endpoints for TTS and LLM/Workflow.

- [ ] **Step 5: Run tests**

Run:

```bash
mvn -pl och-ai -Dtest=AiPluginInvokeServiceTest test
```

Expected: invocation service tests pass.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/AiPluginInvokeService.java och-ai/src/main/java/com/och/ai/api/AiPluginController.java och-ai/src/test/java/com/och/ai/application/AiPluginInvokeServiceTest.java
git commit -m "feat: route ai plugin calls through resource configs"
```

---

## Task 5: AI Quality Result Persistence

**Files:**
- Modify: `och-ai/src/main/resources/db/migration/V12__ai_robot_schema.sql`
- Create: `och-ai/src/main/java/com/och/ai/domain/AiQualityResult.java`
- Create: `och-ai/src/main/java/com/och/ai/mapper/AiQualityResultMapper.java`
- Create: `och-ai/src/main/java/com/och/ai/service/IAiQualityResultService.java`
- Create: `och-ai/src/main/java/com/och/ai/service/impl/AiQualityResultServiceImpl.java`
- Create: `och-ai/src/main/java/com/och/ai/application/AiQualityResultApplicationService.java`
- Create: `och-ai/src/main/java/com/och/ai/api/AiQualityResultController.java`
- Test: `och-ai/src/test/java/com/och/ai/application/AiQualityResultApplicationServiceTest.java`

- [ ] **Step 1: Write failing quality result test**

Create a test that saves a result for `CALL-001`, `SESSION-001`, score `92`, risk `LOW`, summary, tags, hit rules, and evidence. Verify mapper insert receives an entity with those values and no raw prompt field.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl och-ai -Dtest=AiQualityResultApplicationServiceTest test
```

Expected: compilation fails because quality result classes do not exist.

- [ ] **Step 3: Add SQL table**

Append to `V12__ai_robot_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS ai_quality_result (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    call_id           VARCHAR(64)  COMMENT '呼叫ID',
    session_id        VARCHAR(64)  COMMENT '机器人会话ID',
    conversation_id   BIGINT       COMMENT 'AI对话ID',
    score             INT          DEFAULT 0 COMMENT 'AI质检分数',
    risk_level        VARCHAR(32)  COMMENT '风险等级: LOW/MEDIUM/HIGH',
    summary           TEXT         COMMENT '质检摘要',
    tags              VARCHAR(512) COMMENT '标签，逗号分隔',
    hit_rules_json    TEXT         COMMENT '命中规则摘要JSON',
    evidence_json     TEXT         COMMENT '证据摘要JSON',
    status            VARCHAR(32)  DEFAULT 'GENERATED' COMMENT '状态',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_call (call_id),
    INDEX idx_session (session_id),
    INDEX idx_risk (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI质检结果';
```

- [ ] **Step 4: Implement entity, mapper, service, controller**

Create `AiQualityResult` with fields matching the SQL table, `@TableName("ai_quality_result")`, `@TableId(type = IdType.AUTO)`, `@TableField(fill = FieldFill.INSERT)` for `createdAt`, and `@TableField(fill = FieldFill.UPDATE)` for `updatedAt`.

Controller endpoints:

```java
@PostMapping
public ApiResponse<AiQualityResult> create(@RequestBody AiQualityResult result) {
    return success(aiQualityResultApplicationService.create(result));
}

@GetMapping("/session/{sessionId}")
public ApiResponse<List<AiQualityResult>> listBySession(@PathVariable String sessionId) {
    return success(aiQualityResultApplicationService.listBySession(sessionId));
}
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
mvn -pl och-ai -Dtest=AiQualityResultApplicationServiceTest test
```

Expected: quality result tests pass.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/resources/db/migration/V12__ai_robot_schema.sql och-ai/src/main/java/com/och/ai/domain/AiQualityResult.java och-ai/src/main/java/com/och/ai/mapper/AiQualityResultMapper.java och-ai/src/main/java/com/och/ai/service/IAiQualityResultService.java och-ai/src/main/java/com/och/ai/service/impl/AiQualityResultServiceImpl.java och-ai/src/main/java/com/och/ai/application/AiQualityResultApplicationService.java och-ai/src/main/java/com/och/ai/api/AiQualityResultController.java och-ai/src/test/java/com/och/ai/application/AiQualityResultApplicationServiceTest.java
git commit -m "feat: persist ai quality results"
```

---

## Task 6: Robot Flow Complete API

**Files:**
- Modify: `och-ai/src/main/java/com/och/ai/application/RobotFlowApplicationService.java`
- Modify: `och-ai/src/main/java/com/och/ai/api/RobotFlowController.java`
- Modify: `och-ai/src/test/java/com/och/ai/application/RobotFlowApplicationServiceTest.java`

- [ ] **Step 1: Add failing tests for edit, disable, version list, version detail**

Extend `RobotFlowApplicationServiceTest` with tests named:

```java
shouldEditRobotFlowDraft()
shouldDisableRobotFlow()
shouldListVersionsByFlowId()
shouldGetVersionDetail()
```

Each test should mock the mapper, call the service, and verify mapper update/select behavior.

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
mvn -pl och-ai -Dtest=RobotFlowApplicationServiceTest test
```

Expected: new tests fail because service methods do not exist.

- [ ] **Step 3: Implement service methods**

Add methods:

```java
@Transactional
public RobotFlow edit(Long id, RobotFlow flow) { ... }

@Transactional
public void disable(Long id) { ... }

public List<RobotFlowVersion> listVersions(Long flowId) { ... }

public RobotFlowVersion getVersion(Long versionId) { ... }
```

`disable` sets `enabled=false` and does not physically delete. `listVersions` orders by `versionNo desc`. `getVersion` throws `BizException(ErrorCode.NOT_FOUND, "机器人流程版本不存在: " + versionId)` when missing.

- [ ] **Step 4: Add controller endpoints**

Add:

```java
@PutMapping("/{id}")
public ApiResponse<RobotFlow> edit(@PathVariable Long id, @RequestBody RobotFlow flow) { ... }

@PostMapping("/{id}/disable")
public ApiResponse<Void> disable(@PathVariable Long id) { ... }

@GetMapping("/{id}/versions")
public ApiResponse<List<RobotFlowVersion>> versions(@PathVariable Long id) { ... }

@GetMapping("/versions/{versionId}")
public ApiResponse<RobotFlowVersion> version(@PathVariable Long versionId) { ... }
```

- [ ] **Step 5: Run tests**

Run:

```bash
mvn -pl och-ai -Dtest=RobotFlowApplicationServiceTest test
```

Expected: robot flow tests pass.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/RobotFlowApplicationService.java och-ai/src/main/java/com/och/ai/api/RobotFlowController.java och-ai/src/test/java/com/och/ai/application/RobotFlowApplicationServiceTest.java
git commit -m "feat: complete robot flow management api"
```

---

## Task 7: Robot Session Detail, Trace, Summary, Tags, Transfer Reason

**Files:**
- Modify: `och-ai/src/main/java/com/och/ai/application/RobotFlowEngine.java`
- Modify: `och-ai/src/main/java/com/och/ai/service/IRobotSessionService.java`
- Modify: `och-ai/src/main/java/com/och/ai/service/impl/RobotSessionServiceImpl.java`
- Modify: `och-ai/src/main/java/com/och/ai/api/RobotSessionController.java`
- Modify: `och-ai/src/test/java/com/och/ai/application/RobotFlowEngineTest.java`

- [ ] **Step 1: Write failing session trace tests**

Extend `RobotFlowEngineTest` with tests that verify:

- session detail returns `RobotSession` by session id.
- trace returns ordered `AiConversationTurn` list.
- turn result updates summary and tags when adapter response carries them.
- transfer reason is `intent` when `needTransfer=true`.

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
mvn -pl och-ai -Dtest=RobotFlowEngineTest test
```

Expected: new tests fail because methods and fields are missing.

- [ ] **Step 3: Extend bot result contract**

Add `summary`, `tags`, and `needTransfer` to the internal bot invocation result. Keep old constructor-compatible tests by adding a static factory or updating existing tests in the same task.

- [ ] **Step 4: Implement query methods**

Add methods:

```java
public RobotSession getSessionDetail(String sessionId)
public List<AiConversationTurn> getSessionTrace(String sessionId)
public String getTransferReason(String sessionId)
```

`getTransferReason` returns `currentIntent` for transferred sessions and `endReason` for ended sessions.

- [ ] **Step 5: Update controller endpoints**

Add:

```java
@GetMapping("/{sessionId}")
public ApiResponse<RobotSession> detail(@PathVariable String sessionId) { ... }

@GetMapping("/{sessionId}/trace")
public ApiResponse<List<AiConversationTurn>> trace(@PathVariable String sessionId) { ... }

@GetMapping("/{sessionId}/transfer-reason")
public ApiResponse<Map<String, String>> transferReason(@PathVariable String sessionId) { ... }
```

- [ ] **Step 6: Run tests**

Run:

```bash
mvn -pl och-ai -Dtest=RobotFlowEngineTest test
```

Expected: robot flow engine tests pass.

- [ ] **Step 7: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/RobotFlowEngine.java och-ai/src/main/java/com/och/ai/service/IRobotSessionService.java och-ai/src/main/java/com/och/ai/service/impl/RobotSessionServiceImpl.java och-ai/src/main/java/com/och/ai/api/RobotSessionController.java och-ai/src/test/java/com/och/ai/application/RobotFlowEngineTest.java
git commit -m "feat: expose robot session trace and transfer reason"
```

---

## Task 8: Provider Adapter Migration From Environment to Resource Config

**Files:**
- Modify: `och-ai/src/main/java/com/och/ai/application/adapter/BotAdapter.java`
- Modify: `och-ai/src/main/java/com/och/ai/application/adapter/DifyAdapter.java`
- Modify: `och-ai/src/main/java/com/och/ai/application/adapter/RAGFlowAdapter.java`
- Modify: `och-ai/src/main/java/com/och/ai/application/adapter/LangFlowAdapter.java`
- Test: `och-ai/src/test/java/com/och/ai/application/adapter/DifyAdapterTest.java`

- [ ] **Step 1: Write failing Dify adapter resource-config test**

Create `DifyAdapterTest` with `MockRestServiceServer`, a config object whose endpoint is `https://dify.local/v1`, and assert the adapter posts to `https://dify.local/v1/chat-messages` with `Authorization: Bearer raw-key`.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl och-ai -Dtest=DifyAdapterTest test
```

Expected: fails because `DifyAdapter` reads environment variables and does not accept `AiPluginConfig`.

- [ ] **Step 3: Change BotAdapter method signature**

Change:

```java
BotResponse process(String userInput, String context, String systemPrompt, Integer turnNo);
```

to:

```java
BotResponse process(String userInput, String context, String systemPrompt, Integer turnNo, AiPluginConfig config);
```

Update `InternalBotAdapter` to ignore config.

- [ ] **Step 4: Update provider adapters**

Dify:

- base URL from `config.endpointUrl()`.
- API key from `config.apiKey()`.
- response mode from `config.configJson()` with default `blocking`.

RAGFlow:

- base URL from `config.endpointUrl()`.
- API key from `config.apiKey()`.
- knowledge base id from `config.configJson()` field `knowledgeBaseId`.

LangFlow:

- base URL from `config.endpointUrl()`.
- API key from `config.apiKey()`.
- flow id from `config.configJson()` field `flowId`.

- [ ] **Step 5: Update RobotFlowEngine call site**

Load version plugin code, call `AiPluginConfigService.getRequired(code, AiPluginType.WORKFLOW)` or `LLM`, then pass config into adapter.

- [ ] **Step 6: Run adapter and engine tests**

Run:

```bash
mvn -pl och-ai -Dtest=DifyAdapterTest,PluginAdapterFactoryTest,RobotFlowEngineTest test
```

Expected: selected tests pass.

- [ ] **Step 7: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai/src/main/java/com/och/ai/application/adapter/BotAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/DifyAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/RAGFlowAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/LangFlowAdapter.java och-ai/src/main/java/com/och/ai/application/adapter/InternalBotAdapter.java och-ai/src/main/java/com/och/ai/application/RobotFlowEngine.java och-ai/src/test/java/com/och/ai/application/adapter/DifyAdapterTest.java
git commit -m "refactor: drive bot adapters from resource configs"
```

---

## Task 9: Full Validation and Residual Cleanup

**Files:**
- Review: all files changed in Tasks 1-8

- [ ] **Step 1: Run AI module tests**

Run:

```bash
mvn -pl och-ai test
```

Expected: all `och-ai` tests pass.

- [ ] **Step 2: Run API dependency compile**

Run:

```bash
mvn -pl och-api -am -DskipTests compile
```

Expected: all reactor modules through `och-api` build successfully.

- [ ] **Step 3: Run diff whitespace check**

Run:

```bash
git diff --check
```

Expected: no whitespace errors. Existing LF/CRLF warnings may appear and do not fail the check.

- [ ] **Step 4: Search old AI residuals**

Run:

```bash
git grep -n -E "DashScopeService|DeepSeekService|IntentInfo|IntentPattern|IntentVector|dashscope|deepseek" -- "*.java" "*.xml" "*.yml"
```

Expected: no matches.

- [ ] **Step 5: Independent verification**

Invoke a verification agent with the original user request, all changed files, the implementation approach, and this plan path.

Expected: verification verdict is PASS. If the verifier reports FAIL, fix the reported issue and rerun the verifier.

- [ ] **Step 6: Commit checkpoint**

If commit authorization is active, run:

```bash
git add och-ai och-api/src/main/resources/application.yml och-api/src/main/java/com/och/api/service/impl/CallFlowServiceImpl.java docs/superpowers/specs/2026-06-09-opencallhub-phase-6-ai-complete-design.md docs/superpowers/plans/2026-06-09-opencallhub-phase-6-ai-complete-backend.md
git commit -m "feat: complete phase 6 ai backend"
```

---

## Self-Review

### Spec coverage

- Resource-driven plugin config: Task 1.
- Safe plugin call summaries and logs: Task 2.
- Generic HTTP ASR/TTS: Task 3.
- Complete ASR/TTS/LLM/Workflow invocation service: Task 4.
- AI quality result persistence and API: Task 5.
- Robot flow create/edit/publish/disable/version APIs: Task 6.
- Robot session detail, trace, summary, tags, transfer reason: Task 7.
- Dify/RAGFlow/LangFlow resource-config migration: Task 8.
- Full validation and old AI residual cleanup: Task 9.

### 占位内容扫描

This plan does not use deferred markers. Each task lists exact files, commands, expected results, and concrete code shapes for the implementation.

### Type consistency

The plan uses these canonical types consistently:

- `AiPluginType` for `ASR`, `TTS`, `LLM`, `WORKFLOW`.
- `AiPluginConfig` for resource plugin snapshots.
- `AsrInvokeRequest` / `AsrInvokeResult` for ASR.
- `TtsInvokeRequest` / `TtsInvokeResult` for TTS.
- `PluginRequestSummary` / `PluginResponseSummary` / `PluginCallLogger` for safe logging.
- `AiQualityResult` for AI quality output persistence.
