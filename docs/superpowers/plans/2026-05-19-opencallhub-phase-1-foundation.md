# openCallHub Phase 1 Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the openCallHub Phase 1 foundation so the modular Spring Boot backend and Vue 3 frontend start locally with login/home shells, shared API contracts, traceability, permission/audit skeletons, RabbitMQ, Redis, file storage abstraction, and clean database migration.

**Architecture:** Keep openCallHub as a modular monolith with a root Maven aggregator, a Spring Boot API entry module, focused foundation modules, and interfaces that can become microservice boundaries later. Backend requests flow through trace, permission, audit, exception, response, Redis, RabbitMQ, storage, and migration foundations before domain modules add business behavior. Frontend starts as a Vue 3 console shell with route/menu/request/WebSocket/state/layout patterns that future product domains reuse.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Web MVC, Spring Security filter chain skeleton, springdoc-openapi, Flyway, MySQL, Redis, RabbitMQ, JUnit 5, Mockito, Spring Boot Test, Vue 3, TypeScript, Vite, Vue Router, Pinia, Axios, Vitest, Playwright, ESLint, Docker Compose.

---

## Agentic Worker Note

This plan is implementation guidance only. Run it in an isolated execution worktree at implementation time because the current working tree may contain unrelated changes. Do not mix unrelated changes into the commits listed below. Commit commands are included as checkpoints for the engineer executing the plan, but this planning task does not run them.

## Goal

Phase 1 establishes foundations only: Maven module foundation, unified response, unified exception handling, traceId, OpenAPI, permission interception skeleton, audit skeleton, RabbitMQ event abstraction, Redis wrapper, file storage abstraction, migration mechanism, Vue 3 app structure, route permission shell, menu shell, request wrapper, WebSocket wrapper, Pinia state conventions, layout shell, table/form/detail drawer patterns, login shell, and home shell.

## Architecture

### Backend layering

- `och-api`: Spring Boot application entrypoint, REST controllers for foundation checks, OpenAPI grouping, local environment configuration, and startup wiring.
- `och-common`: shared contracts, response envelope, trace context, exceptions, pagination primitives, and small reusable utilities.
- `och-security`: permission annotation, permission interceptor skeleton, audit annotation, audit capture skeleton, and security smoke endpoints used by tests.
- `och-infra`: Redis wrapper, RabbitMQ event abstraction, file storage abstraction, Flyway migration assets, dependency health checks, and local development configuration.
- Domain modules remain present but do not receive business features in Phase 1.

### Frontend layering

- `waihu-app`: Vue 3 console shell with routes, request/WebSocket wrappers, Pinia stores, layout components, and reusable page patterns.
- `src/router`: route metadata and permission guard shell.
- `src/stores`: typed Pinia stores for auth, permission, menu, and app runtime state.
- `src/services`: Axios API client and WebSocket client with traceId propagation.
- `src/layouts` and `src/components/patterns`: console layout plus table/form/detail drawer patterns.
- `src/views`: login and home shell pages.

### Role owners

- Architect: owns module boundaries, base layering, event abstraction, and cross-module dependency rules.
- Backend Expert: owns Spring Boot foundation, response, exceptions, traceId, OpenAPI, Redis wrapper, and RabbitMQ implementation.
- Frontend Expert: owns Vue app shell, routing, request wrapper, WebSocket wrapper, layout shell, table/form/detail drawer patterns, login shell, and home shell.
- DevOps Expert: owns local startup commands, Docker Compose dependencies, and environment variables.
- Security Expert: owns permission interception skeleton and audit skeleton.
- QA Expert: owns foundation test gates and acceptance verification.

## Tech Stack

- Backend runtime: Java 17 and Spring Boot.
- Build: Maven multi-module build from `pom.xml`.
- API documentation: springdoc-openapi exposed at `/swagger-ui/index.html` and `/v3/api-docs`.
- Database migration: Flyway migrations in `och-infra/src/main/resources/db/migration`.
- Messaging: RabbitMQ via a foundation event publisher and subscriber abstraction.
- Cache/session foundation: Redis via a typed wrapper around `StringRedisTemplate`.
- File storage: local implementation behind a storage interface, with MinIO-compatible configuration keys reserved in config.
- Frontend: Vue 3, TypeScript, Vite, Vue Router, Pinia, Axios, Vitest, Playwright.
- Local dependencies: MySQL, Redis, RabbitMQ using Docker Compose.

## File Structure

### Backend files to create

- `och-common/pom.xml`: Maven child module for shared contracts and utilities.
- `och-common/src/main/java/com/opencallhub/common/api/ApiResponse.java`: response envelope with `code`, `message`, `data`, and `traceId`.
- `och-common/src/main/java/com/opencallhub/common/api/PageResponse.java`: standard page payload.
- `och-common/src/main/java/com/opencallhub/common/error/ErrorCode.java`: canonical error codes for foundation layer.
- `och-common/src/main/java/com/opencallhub/common/error/BizException.java`: runtime business exception carrying `ErrorCode`.
- `och-common/src/main/java/com/opencallhub/common/trace/TraceContext.java`: thread-local traceId access.
- `och-common/src/main/java/com/opencallhub/common/trace/TraceIdGenerator.java`: UUID-based traceId generator.
- `och-common/src/main/java/com/opencallhub/common/trace/TraceIdFilter.java`: servlet filter that reads or creates `X-Trace-Id`.
- `och-common/src/main/java/com/opencallhub/common/web/GlobalExceptionHandler.java`: maps exceptions to `ApiResponse`.
- `och-common/src/main/java/com/opencallhub/common/web/ApiResponseAdvice.java`: wraps successful controller responses.
- `och-common/src/test/java/com/opencallhub/common/api/ApiResponseTest.java`: response contract unit tests.
- `och-common/src/test/java/com/opencallhub/common/trace/TraceIdFilterTest.java`: traceId filter tests.
- `och-common/src/test/java/com/opencallhub/common/web/GlobalExceptionHandlerTest.java`: exception mapping tests.
- `och-infra/pom.xml`: Maven child module for infrastructure foundations.
- `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCache.java`: typed Redis get/set/delete wrapper.
- `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCacheProperties.java`: Redis key prefix and TTL properties.
- `och-infra/src/main/java/com/opencallhub/infra/mq/DomainEvent.java`: common event contract with `eventId`, `eventType`, `occurredAt`, `traceId`, and `payload`.
- `och-infra/src/main/java/com/opencallhub/infra/mq/EventPublisher.java`: event publishing interface.
- `och-infra/src/main/java/com/opencallhub/infra/mq/EventSubscriber.java`: event subscriber interface.
- `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventProperties.java`: exchange, queue, routing key, retry, and dead-letter settings.
- `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventPublisher.java`: RabbitMQ publisher implementation.
- `och-infra/src/main/java/com/opencallhub/infra/mq/FoundationEventListener.java`: test event consumer that stores the last consumed event in memory for smoke checks.
- `och-infra/src/main/java/com/opencallhub/infra/file/FileStorage.java`: storage abstraction.
- `och-infra/src/main/java/com/opencallhub/infra/file/StoredFile.java`: stored-file metadata record.
- `och-infra/src/main/java/com/opencallhub/infra/file/LocalFileStorage.java`: local filesystem implementation.
- `och-infra/src/main/java/com/opencallhub/infra/file/FileStorageProperties.java`: local and object storage configuration properties.
- `och-infra/src/main/resources/db/migration/V1__foundation_schema.sql`: migration creating `foundation_migration_check` and `audit_event` tables.
- `och-infra/src/test/java/com/opencallhub/infra/redis/RedisCacheTest.java`: Redis wrapper contract tests with mocked template.
- `och-infra/src/test/java/com/opencallhub/infra/mq/RabbitEventPublisherTest.java`: Rabbit publisher unit tests.
- `och-infra/src/test/java/com/opencallhub/infra/file/LocalFileStorageTest.java`: local file storage tests.
- `och-security/src/main/java/com/opencallhub/security/permission/RequiresPermission.java`: permission annotation.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionContext.java`: current-user permission provider skeleton.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionInterceptor.java`: MVC interceptor enforcing annotation metadata.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionWebMvcConfigurer.java`: registers permission interceptor.
- `och-security/src/main/java/com/opencallhub/security/audit/Audited.java`: audit annotation.
- `och-security/src/main/java/com/opencallhub/security/audit/AuditEvent.java`: audit event data record.
- `och-security/src/main/java/com/opencallhub/security/audit/AuditSink.java`: audit sink interface.
- `och-security/src/main/java/com/opencallhub/security/audit/InMemoryAuditSink.java`: foundation audit sink for smoke tests.
- `och-security/src/main/java/com/opencallhub/security/audit/AuditAspect.java`: captures annotated method invocation metadata.
- `och-security/src/test/java/com/opencallhub/security/permission/PermissionInterceptorTest.java`: permission interception tests.
- `och-security/src/test/java/com/opencallhub/security/audit/AuditAspectTest.java`: audit capture tests.
- `och-api/src/main/java/com/opencallhub/api/OpenCallHubApplication.java`: Spring Boot main class if absent, or modified to scan new base packages.
- `och-api/src/main/java/com/opencallhub/api/config/OpenApiConfig.java`: OpenAPI title, version, and foundation group.
- `och-api/src/main/java/com/opencallhub/api/config/SecurityFoundationConfig.java`: permits shell endpoints while leaving security extension points visible.
- `och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java`: smoke endpoints for health, trace, Redis, RabbitMQ, file storage, and migration checks.
- `och-api/src/main/resources/application.yml`: shared backend config for server, Spring, OpenAPI, trace logging, Redis, RabbitMQ, Flyway, and file storage.
- `och-api/src/main/resources/application-local.yml`: local environment values reading env vars with safe defaults.
- `och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java`: controller contract tests.
- `docker/docker-compose.foundation.yml`: local MySQL, Redis, RabbitMQ dependencies.
- `.env.example`: documented environment variables for local foundation startup.

### Backend files to modify

- `pom.xml`: add `och-common`, `och-infra`, and any missing Phase 1 modules to the aggregator and dependency management.
- `och-api/pom.xml`: depend on `och-common`, `och-infra`, `och-security`, Spring Web, Validation, Security, Actuator, AMQP, Redis, Flyway, MySQL driver, springdoc, and test dependencies.
- `och-security/pom.xml`: depend on `och-common`, Spring Web MVC, Spring AOP, Spring Security, and test dependencies.

### Frontend files to create or replace

- `waihu-app/package.json`: scripts and dependencies for Vue 3 foundation.
- `waihu-app/vite.config.ts`: Vite config, Vitest config, path alias, and dev proxy to backend.
- `waihu-app/playwright.config.ts`: Playwright base URL and web server configuration for executable E2E tests.
- `waihu-app/tsconfig.json`: TypeScript settings.
- `waihu-app/index.html`: root HTML shell.
- `waihu-app/src/main.ts`: Vue app bootstrap with router and Pinia.
- `waihu-app/src/App.vue`: root router outlet.
- `waihu-app/src/router/index.ts`: routes and permission guard.
- `waihu-app/src/router/routes.ts`: route definitions for login, console layout, home, and pattern demo.
- `waihu-app/src/stores/auth.ts`: auth shell store.
- `waihu-app/src/stores/permission.ts`: permission shell store.
- `waihu-app/src/stores/menu.ts`: menu shell store.
- `waihu-app/src/stores/app.ts`: global app runtime store.
- `waihu-app/src/services/request.ts`: Axios wrapper returning unified response data and preserving `traceId`.
- `waihu-app/src/services/websocket.ts`: WebSocket wrapper with reconnect and typed message dispatch.
- `waihu-app/src/services/foundationApi.ts`: login shell, profile shell, and foundation smoke API methods.
- `waihu-app/src/layouts/ConsoleLayout.vue`: top/sidebar/content layout shell.
- `waihu-app/src/components/menu/AppMenu.vue`: grouped menu shell.
- `waihu-app/src/components/patterns/DataTablePattern.vue`: reusable table pattern.
- `waihu-app/src/components/patterns/FormPattern.vue`: reusable form pattern.
- `waihu-app/src/components/patterns/DetailDrawerPattern.vue`: reusable detail drawer pattern.
- `waihu-app/src/views/LoginView.vue`: login shell.
- `waihu-app/src/views/HomeView.vue`: home shell.
- `waihu-app/src/views/PatternDemoView.vue`: visual demo for table/form/detail drawer patterns.
- `waihu-app/src/styles/theme.css`: foundation console theme tokens.
- `waihu-app/src/styles/layout.css`: layout shell styles.
- `waihu-app/src/types/api.ts`: `ApiResponse`, `PageResponse`, and trace-aware request types.
- `waihu-app/src/types/menu.ts`: route and menu metadata types.
- `waihu-app/src/types/websocket.ts`: WebSocket message types.
- `waihu-app/src/tests/request.spec.ts`: request wrapper tests.
- `waihu-app/src/tests/router.spec.ts`: route permission shell tests.
- `waihu-app/src/tests/stores.spec.ts`: Pinia convention tests.
- `waihu-app/src/tests/websocket.spec.ts`: WebSocket wrapper tests.
- `waihu-app/e2e/login-home.spec.ts`: login shell to home shell E2E test.
- `waihu-app/playwright.config.ts`: Playwright base URL and web server configuration for the E2E test.

## Bite-Sized Tasks

### Task 1: Maven Module Foundation

**Owner:** Architect

**Files:**
- Modify: `pom.xml`
- Create: `och-common/pom.xml`
- Create: `och-infra/pom.xml`
- Modify: `och-api/pom.xml`
- Modify: `och-security/pom.xml`
- Test: Maven reactor output from `mvn -pl och-api -am test -DskipTests=false`

- [ ] **Step 1: Verify current module inventory**

Run from `e:/workspace/openCallHub`:

```bash
mvn -q help:evaluate -Dexpression=project.modules -DforceStdout
```

Expected output contains the existing module list and does not fail with `Non-readable POM`.

- [ ] **Step 2: Add missing foundation modules to the root Maven aggregator**

Modify `pom.xml` so the `<modules>` section contains these exact entries in this order:

```xml
<modules>
    <module>och-common</module>
    <module>och-infra</module>
    <module>och-security</module>
    <module>och-system</module>
    <module>och-file</module>
    <module>och-file-client</module>
    <module>och-api</module>
    <module>och-websocket</module>
    <module>och-call-task</module>
    <module>och-esl</module>
    <module>och-ivr</module>
    <module>och-ai</module>
</modules>
```

Expected result: root `pom.xml` includes `och-common` and `och-infra` before modules that depend on them.

- [ ] **Step 3: Create `och-common/pom.xml`**

Create `och-common/pom.xml` with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.opencallhub</groupId>
        <artifactId>openCallHub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>och-common</artifactId>
    <packaging>jar</packaging>

    <dependencies>
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

Expected result: Maven can resolve `och-common` as a reactor module.

- [ ] **Step 4: Create `och-infra/pom.xml`**

Create `och-infra/pom.xml` with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.opencallhub</groupId>
        <artifactId>openCallHub</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>och-infra</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

Expected result: Maven can resolve `och-infra` and its dependency on `och-common`.

- [ ] **Step 5: Update API and security module dependencies**

In `och-api/pom.xml`, add dependencies on `och-common`, `och-infra`, and `och-security`:

```xml
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-common</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-infra</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-security</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

In `och-security/pom.xml`, add dependencies on `och-common`, Spring AOP, Spring Security, and Spring Web MVC:

```xml
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-common</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Expected result: `och-api` compiles against the foundation modules.

- [ ] **Step 6: Run the Maven foundation build**

Run:

```bash
mvn -pl och-api -am test -DskipTests=false
```

Expected output contains `BUILD SUCCESS` or test compilation errors only for classes not yet created in later tasks. If compilation fails because `och-common` source folders do not exist, create `src/main/java` and `src/test/java` directories in `och-common` and rerun.

- [ ] **Step 7: Commit checkpoint**

Run only after Step 6 produces `BUILD SUCCESS` at this task boundary:

```bash
git add pom.xml och-common/pom.xml och-infra/pom.xml och-api/pom.xml och-security/pom.xml
git commit -m "build: add foundation Maven modules"
```

Expected output contains `[branch-name commit-hash] build: add foundation Maven modules`.

### Task 2: Unified Response, Exceptions, and traceId

**Owner:** Backend Expert

**Files:**
- Create: `och-common/src/main/java/com/opencallhub/common/api/ApiResponse.java`
- Create: `och-common/src/main/java/com/opencallhub/common/api/PageResponse.java`
- Create: `och-common/src/main/java/com/opencallhub/common/error/ErrorCode.java`
- Create: `och-common/src/main/java/com/opencallhub/common/error/BizException.java`
- Create: `och-common/src/main/java/com/opencallhub/common/trace/TraceContext.java`
- Create: `och-common/src/main/java/com/opencallhub/common/trace/TraceIdGenerator.java`
- Create: `och-common/src/main/java/com/opencallhub/common/trace/TraceIdFilter.java`
- Create: `och-common/src/main/java/com/opencallhub/common/web/GlobalExceptionHandler.java`
- Create: `och-common/src/main/java/com/opencallhub/common/web/ApiResponseAdvice.java`
- Test: `och-common/src/test/java/com/opencallhub/common/api/ApiResponseTest.java`
- Test: `och-common/src/test/java/com/opencallhub/common/trace/TraceIdFilterTest.java`
- Test: `och-common/src/test/java/com/opencallhub/common/web/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write `ApiResponse` contract tests**

Create `och-common/src/test/java/com/opencallhub/common/api/ApiResponseTest.java`:

```java
package com.opencallhub.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
    @Test
    void successIncludesCodeMessageDataAndTraceId() {
        ApiResponse<String> response = ApiResponse.success("ok", "trace-001");

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
        assertThat(response.traceId()).isEqualTo("trace-001");
    }

    @Test
    void failureIncludesErrorCodeMessageAndTraceId() {
        ApiResponse<Object> response = ApiResponse.failure("FOUNDATION_ERROR", "foundation failed", "trace-002");

        assertThat(response.code()).isEqualTo("FOUNDATION_ERROR");
        assertThat(response.message()).isEqualTo("foundation failed");
        assertThat(response.data()).isNull();
        assertThat(response.traceId()).isEqualTo("trace-002");
    }
}
```

- [ ] **Step 2: Run response tests and confirm failure**

Run:

```bash
mvn -pl och-common test -Dtest=ApiResponseTest
```

Expected output contains `cannot find symbol` for `ApiResponse`.

- [ ] **Step 3: Implement `ApiResponse` and `PageResponse`**

Create `och-common/src/main/java/com/opencallhub/common/api/ApiResponse.java`:

```java
package com.opencallhub.common.api;

public record ApiResponse<T>(String code, String message, T data, String traceId) {
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>("0", "success", data, traceId);
    }

    public static <T> ApiResponse<T> failure(String code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId);
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/api/PageResponse.java`:

```java
package com.opencallhub.common.api;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, int pageNo, int pageSize) {
    public PageResponse {
        records = records == null ? List.of() : List.copyOf(records);
    }
}
```

- [ ] **Step 4: Write exception and trace tests**

Create `och-common/src/test/java/com/opencallhub/common/trace/TraceIdFilterTest.java`:

```java
package com.opencallhub.common.trace;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TraceIdFilterTest {
    @AfterEach
    void clearTrace() {
        TraceContext.clear();
    }

    @Test
    void usesIncomingTraceIdAndWritesResponseHeader() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new TraceIdGenerator());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "client-trace-001");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("client-trace-001");
        assertThat(TraceContext.getTraceId()).isNull();
    }

    @Test
    void createsTraceIdWhenHeaderIsMissing() throws Exception {
        TraceIdFilter filter = new TraceIdFilter(new TraceIdGenerator());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getHeader("X-Trace-Id")).isNotBlank();
    }
}
```

Create `och-common/src/test/java/com/opencallhub/common/web/GlobalExceptionHandlerTest.java`:

```java
package com.opencallhub.common.web;

import com.opencallhub.common.api.ApiResponse;
import com.opencallhub.common.error.BizException;
import com.opencallhub.common.error.ErrorCode;
import com.opencallhub.common.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    @AfterEach
    void clearTrace() {
        TraceContext.clear();
    }

    @Test
    void mapsBizExceptionToUnifiedFailureResponse() {
        TraceContext.setTraceId("trace-003");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Object>> response = handler.handleBizException(new BizException(ErrorCode.FORBIDDEN));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
        assertThat(response.getBody().traceId()).isEqualTo("trace-003");
    }
}
```

- [ ] **Step 5: Run new tests and confirm failure**

Run:

```bash
mvn -pl och-common test -Dtest=TraceIdFilterTest,GlobalExceptionHandlerTest
```

Expected output contains `cannot find symbol` for `TraceContext`, `TraceIdFilter`, `GlobalExceptionHandler`, `BizException`, and `ErrorCode`.

- [ ] **Step 6: Implement error and trace classes**

Create `och-common/src/main/java/com/opencallhub/common/error/ErrorCode.java`:

```java
package com.opencallhub.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "not found", HttpStatus.NOT_FOUND),
    FOUNDATION_ERROR("FOUNDATION_ERROR", "foundation error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus status() {
        return status;
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/error/BizException.java`:

```java
package com.opencallhub.common.error;

public class BizException extends RuntimeException {
    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/trace/TraceContext.java`:

```java
package com.opencallhub.common.trace;

public final class TraceContext {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceContext() {
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/trace/TraceIdGenerator.java`:

```java
package com.opencallhub.common.trace;

import java.util.UUID;

public class TraceIdGenerator {
    public String nextTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/trace/TraceIdFilter.java`:

```java
package com.opencallhub.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceIdFilter extends OncePerRequestFilter {
    public static final String TRACE_HEADER = "X-Trace-Id";
    private final TraceIdGenerator generator;

    public TraceIdFilter(TraceIdGenerator generator) {
        this.generator = generator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = generator.nextTraceId();
        }
        try {
            TraceContext.setTraceId(traceId);
            MDC.put("traceId", traceId);
            response.setHeader(TRACE_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            TraceContext.clear();
        }
    }
}
```

- [ ] **Step 7: Implement exception handler and response advice**

Create `och-common/src/main/java/com/opencallhub/common/web/GlobalExceptionHandler.java`:

```java
package com.opencallhub.common.web;

import com.opencallhub.common.api.ApiResponse;
import com.opencallhub.common.error.BizException;
import com.opencallhub.common.error.ErrorCode;
import com.opencallhub.common.trace.TraceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Object>> handleBizException(BizException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity.status(errorCode.status())
                .body(ApiResponse.failure(errorCode.code(), exception.getMessage(), TraceContext.getTraceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.message());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.BAD_REQUEST.code(), message, TraceContext.getTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(ErrorCode.FOUNDATION_ERROR.code(), ErrorCode.FOUNDATION_ERROR.message(), TraceContext.getTraceId()));
    }
}
```

Create `och-common/src/main/java/com/opencallhub/common/web/ApiResponseAdvice.java`:

```java
package com.opencallhub.common.web;

import com.opencallhub.common.api.ApiResponse;
import com.opencallhub.common.trace.TraceContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType)
                && !String.class.isAssignableFrom(parameterType)
                && !byte[].class.isAssignableFrom(parameterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        return ApiResponse.success(body, TraceContext.getTraceId());
    }
}
```

- [ ] **Step 8: Run common tests**

Run:

```bash
mvn -pl och-common test
```

Expected output contains `BUILD SUCCESS`, `Tests run: 5`, and `Failures: 0`.

- [ ] **Step 9: Commit checkpoint**

Run:

```bash
git add och-common/src/main/java och-common/src/test/java
git commit -m "feat: add unified response exceptions and trace id"
```

Expected output contains `feat: add unified response exceptions and trace id`.

### Task 3: Spring Boot API Startup and OpenAPI

**Owner:** Backend Expert

**Files:**
- Create or modify: `och-api/src/main/java/com/opencallhub/api/OpenCallHubApplication.java`
- Create: `och-api/src/main/java/com/opencallhub/api/config/OpenApiConfig.java`
- Create: `och-api/src/main/java/com/opencallhub/api/config/SecurityFoundationConfig.java`
- Create: `och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java`
- Create or modify: `och-api/src/main/resources/application.yml`
- Create: `och-api/src/main/resources/application-local.yml`
- Test: `och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java`

- [ ] **Step 1: Write foundation controller test**

Create `och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java`:

```java
package com.opencallhub.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
@AutoConfigureMockMvc
class FoundationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void healthReturnsUnifiedResponseWithTraceId() throws Exception {
        mockMvc.perform(get("/api/foundation/health").header("X-Trace-Id", "test-trace-101"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "test-trace-101"))
                .andExpect(jsonPath("$.code", equalTo("0")))
                .andExpect(jsonPath("$.data.status", equalTo("UP")))
                .andExpect(jsonPath("$.traceId", equalTo("test-trace-101")));
    }

    @Test
    void openApiEndpointIsRegistered() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()));
    }
}
```

- [ ] **Step 2: Run API tests and confirm failure**

Run:

```bash
mvn -pl och-api -am test -Dtest=FoundationControllerTest
```

Expected output contains a missing bean, missing controller, or missing OpenAPI configuration failure.

- [ ] **Step 3: Create application entrypoint and OpenAPI config**

Create or modify `och-api/src/main/java/com/opencallhub/api/OpenCallHubApplication.java`:

```java
package com.opencallhub.api;

import com.opencallhub.common.trace.TraceIdFilter;
import com.opencallhub.common.trace.TraceIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.opencallhub")
public class OpenCallHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenCallHubApplication.class, args);
    }

    @Bean
    TraceIdGenerator traceIdGenerator() {
        return new TraceIdGenerator();
    }

    @Bean
    FilterRegistrationBean<TraceIdFilter> traceIdFilter(TraceIdGenerator generator) {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(generator));
        registration.setOrder(1);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
```

Create `och-api/src/main/java/com/opencallhub/api/config/OpenApiConfig.java`:

```java
package com.opencallhub.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openCallHubOpenApi() {
        return new OpenAPI().info(new Info()
                .title("openCallHub API")
                .version("1.0.0-phase-1")
                .description("Foundation API contracts for openCallHub"));
    }

    @Bean
    GroupedOpenApi foundationApiGroup() {
        return GroupedOpenApi.builder()
                .group("foundation")
                .pathsToMatch("/api/foundation/**")
                .build();
    }
}
```

- [ ] **Step 4: Add security shell and foundation controller**

Create `och-api/src/main/java/com/opencallhub/api/config/SecurityFoundationConfig.java`:

```java
package com.opencallhub.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityFoundationConfig {
    @Bean
    SecurityFilterChain foundationSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/api/foundation/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                        .anyRequest().permitAll())
                .build();
    }
}
```

Create `och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java`:

```java
package com.opencallhub.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/foundation")
public class FoundationController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "openCallHub",
                "phase", "foundation",
                "time", OffsetDateTime.now().toString()
        );
    }
}
```

- [ ] **Step 5: Add backend configuration**

Create or modify `och-api/src/main/resources/application.yml`:

```yaml
server:
  port: ${OCH_SERVER_PORT:18080}

spring:
  application:
    name: openCallHub-api
  profiles:
    active: ${OCH_PROFILE:local}
  jackson:
    time-zone: UTC
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

springdoc:
  swagger-ui:
    path: /swagger-ui/index.html
  api-docs:
    path: /v3/api-docs

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level traceId=%X{traceId} %logger{36} - %msg%n"
```

Create `och-api/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: ${OCH_DB_URL:jdbc:mysql://localhost:3306/opencallhub?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&createDatabaseIfNotExist=true}
    username: ${OCH_DB_USERNAME:opencallhub}
    password: ${OCH_DB_PASSWORD:opencallhub}
  data:
    redis:
      host: ${OCH_REDIS_HOST:localhost}
      port: ${OCH_REDIS_PORT:6379}
      password: ${OCH_REDIS_PASSWORD:}
  rabbitmq:
    host: ${OCH_RABBIT_HOST:localhost}
    port: ${OCH_RABBIT_PORT:5672}
    username: ${OCH_RABBIT_USERNAME:opencallhub}
    password: ${OCH_RABBIT_PASSWORD:opencallhub}

och:
  redis:
    key-prefix: ${OCH_REDIS_KEY_PREFIX:och:}
    default-ttl-seconds: ${OCH_REDIS_DEFAULT_TTL_SECONDS:300}
  rabbit:
    exchange: ${OCH_RABBIT_EXCHANGE:och.foundation.exchange}
    queue: ${OCH_RABBIT_QUEUE:och.foundation.queue}
    routing-key: ${OCH_RABBIT_ROUTING_KEY:foundation.test}
    dead-letter-exchange: ${OCH_RABBIT_DLX:och.foundation.dlx}
    dead-letter-queue: ${OCH_RABBIT_DLQ:och.foundation.dlq}
  file-storage:
    provider: ${OCH_FILE_STORAGE_PROVIDER:local}
    local-root: ${OCH_FILE_STORAGE_LOCAL_ROOT:./runtime/files}
```

- [ ] **Step 6: Run API tests**

Run:

```bash
mvn -pl och-api -am test -Dtest=FoundationControllerTest
```

Expected output contains `BUILD SUCCESS`, `healthReturnsUnifiedResponseWithTraceId`, and `openApiEndpointIsRegistered`.

- [ ] **Step 7: Start backend and manually verify OpenAPI and traceId**

Run:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Expected console output contains `Started OpenCallHubApplication` and log lines formatted with `traceId=`.

In a second terminal run:

```bash
curl -s -D - -H "X-Trace-Id: manual-trace-001" http://localhost:18080/api/foundation/health
curl -s http://localhost:18080/v3/api-docs
```

Expected first response headers contain `X-Trace-Id: manual-trace-001`, response body contains `"traceId":"manual-trace-001"`, and second response contains `"openapi":"3.`.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add och-api/src/main/java och-api/src/main/resources och-api/src/test/java
git commit -m "feat: add API startup and OpenAPI foundation"
```

Expected output contains `feat: add API startup and OpenAPI foundation`.

### Task 4: Permission Interception Skeleton and Audit Skeleton

**Owner:** Security Expert

**Files:**
- Create: `och-security/src/main/java/com/opencallhub/security/permission/RequiresPermission.java`
- Create: `och-security/src/main/java/com/opencallhub/security/permission/PermissionContext.java`
- Create: `och-security/src/main/java/com/opencallhub/security/permission/PermissionInterceptor.java`
- Create: `och-security/src/main/java/com/opencallhub/security/permission/PermissionWebMvcConfigurer.java`
- Create: `och-security/src/main/java/com/opencallhub/security/audit/Audited.java`
- Create: `och-security/src/main/java/com/opencallhub/security/audit/AuditEvent.java`
- Create: `och-security/src/main/java/com/opencallhub/security/audit/AuditSink.java`
- Create: `och-security/src/main/java/com/opencallhub/security/audit/InMemoryAuditSink.java`
- Create: `och-security/src/main/java/com/opencallhub/security/audit/AuditAspect.java`
- Test: `och-security/src/test/java/com/opencallhub/security/permission/PermissionInterceptorTest.java`
- Test: `och-security/src/test/java/com/opencallhub/security/audit/AuditAspectTest.java`

- [ ] **Step 1: Write permission interceptor test**

Create `och-security/src/test/java/com/opencallhub/security/permission/PermissionInterceptorTest.java`:

```java
package com.opencallhub.security.permission;

import com.opencallhub.common.error.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionInterceptorTest {
    private final PermissionContext permissionContext = new PermissionContext();
    private final PermissionInterceptor interceptor = new PermissionInterceptor(permissionContext);

    @AfterEach
    void clear() {
        permissionContext.clear();
    }

    @Test
    void allowsRequestWhenPermissionExists() throws Exception {
        permissionContext.setPermissions(Set.of("system:menu:view"));
        HandlerMethod handlerMethod = handlerMethod("securedEndpoint");

        assertThatCode(() -> interceptor.preHandle(null, null, handlerMethod)).doesNotThrowAnyException();
    }

    @Test
    void rejectsRequestWhenPermissionIsMissing() throws Exception {
        permissionContext.setPermissions(Set.of("system:user:view"));
        HandlerMethod handlerMethod = handlerMethod("securedEndpoint");

        assertThatThrownBy(() -> interceptor.preHandle(null, null, handlerMethod))
                .isInstanceOf(BizException.class)
                .hasMessage("permission denied: system:menu:view");
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = DemoController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new DemoController(), method);
    }

    static class DemoController {
        @RequiresPermission("system:menu:view")
        void securedEndpoint() {
        }
    }
}
```

- [ ] **Step 2: Write audit aspect test**

Create `och-security/src/test/java/com/opencallhub/security/audit/AuditAspectTest.java`:

```java
package com.opencallhub.security.audit;

import com.opencallhub.common.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AuditAspectTest {
    private final InMemoryAuditSink sink = new InMemoryAuditSink();

    @AfterEach
    void clearTrace() {
        TraceContext.clear();
        sink.clear();
    }

    @Test
    void capturesAnnotatedMethodInvocation() {
        TraceContext.setTraceId("audit-trace-001");
        AuditAspect aspect = new AuditAspect(sink);
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(aspect);
        DemoService proxy = factory.getProxy();

        proxy.createMenu("menu-a");

        assertThat(sink.events()).hasSize(1);
        AuditEvent event = sink.events().get(0);
        assertThat(event.action()).isEqualTo("CREATE_MENU");
        assertThat(event.resource()).isEqualTo("system-menu");
        assertThat(event.traceId()).isEqualTo("audit-trace-001");
        assertThat(event.success()).isTrue();
    }

    static class DemoService {
        @Audited(action = "CREATE_MENU", resource = "system-menu")
        String createMenu(String name) {
            return name;
        }
    }
}
```

- [ ] **Step 3: Run security tests and confirm failure**

Run:

```bash
mvn -pl och-security -am test -Dtest=PermissionInterceptorTest,AuditAspectTest
```

Expected output contains `cannot find symbol` for permission and audit classes.

- [ ] **Step 4: Implement permission skeleton**

Create `och-security/src/main/java/com/opencallhub/security/permission/RequiresPermission.java`:

```java
package com.opencallhub.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String value();
}
```

Create `och-security/src/main/java/com/opencallhub/security/permission/PermissionContext.java`:

```java
package com.opencallhub.security.permission;

import java.util.Set;

public class PermissionContext {
    private static final ThreadLocal<Set<String>> PERMISSIONS = ThreadLocal.withInitial(Set::of);

    public void setPermissions(Set<String> permissions) {
        PERMISSIONS.set(Set.copyOf(permissions));
    }

    public boolean hasPermission(String permission) {
        return PERMISSIONS.get().contains(permission);
    }

    public void clear() {
        PERMISSIONS.remove();
    }
}
```

Create `och-security/src/main/java/com/opencallhub/security/permission/PermissionInterceptor.java`:

```java
package com.opencallhub.security.permission;

import com.opencallhub.common.error.BizException;
import com.opencallhub.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class PermissionInterceptor implements HandlerInterceptor {
    private final PermissionContext permissionContext;

    public PermissionInterceptor(PermissionContext permissionContext) {
        this.permissionContext = permissionContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequiresPermission annotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }
        if (annotation == null || permissionContext.hasPermission(annotation.value())) {
            return true;
        }
        throw new BizException(ErrorCode.FORBIDDEN, "permission denied: " + annotation.value());
    }
}
```

Create `och-security/src/main/java/com/opencallhub/security/permission/PermissionWebMvcConfigurer.java`:

```java
package com.opencallhub.security.permission;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionWebMvcConfigurer implements WebMvcConfigurer {
    @Bean
    PermissionContext permissionContext() {
        return new PermissionContext();
    }

    @Bean
    PermissionInterceptor permissionInterceptor(PermissionContext permissionContext) {
        return new PermissionInterceptor(permissionContext);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor(permissionContext()))
                .addPathPatterns("/api/**");
    }
}
```

- [ ] **Step 5: Implement audit skeleton**

Create `och-security/src/main/java/com/opencallhub/security/audit/Audited.java`:

```java
package com.opencallhub.security.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String action();
    String resource();
}
```

Create `och-security/src/main/java/com/opencallhub/security/audit/AuditEvent.java`:

```java
package com.opencallhub.security.audit;

import java.time.OffsetDateTime;

public record AuditEvent(String action, String resource, String traceId, boolean success, String errorMessage, OffsetDateTime occurredAt) {
}
```

Create `och-security/src/main/java/com/opencallhub/security/audit/AuditSink.java`:

```java
package com.opencallhub.security.audit;

public interface AuditSink {
    void record(AuditEvent event);
}
```

Create `och-security/src/main/java/com/opencallhub/security/audit/InMemoryAuditSink.java`:

```java
package com.opencallhub.security.audit;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class InMemoryAuditSink implements AuditSink {
    private final List<AuditEvent> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void record(AuditEvent event) {
        events.add(event);
    }

    public List<AuditEvent> events() {
        synchronized (events) {
            return List.copyOf(events);
        }
    }

    public void clear() {
        events.clear();
    }
}
```

Create `och-security/src/main/java/com/opencallhub/security/audit/AuditAspect.java`:

```java
package com.opencallhub.security.audit;

import com.opencallhub.common.trace.TraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Aspect
@Component
public class AuditAspect {
    private final AuditSink auditSink;

    public AuditAspect(AuditSink auditSink) {
        this.auditSink = auditSink;
    }

    @Around("@annotation(audited)")
    public Object recordAudit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            auditSink.record(new AuditEvent(audited.action(), audited.resource(), TraceContext.getTraceId(), true, null, OffsetDateTime.now()));
            return result;
        } catch (Throwable throwable) {
            auditSink.record(new AuditEvent(audited.action(), audited.resource(), TraceContext.getTraceId(), false, throwable.getMessage(), OffsetDateTime.now()));
            throw throwable;
        }
    }
}
```

- [ ] **Step 6: Run security tests**

Run:

```bash
mvn -pl och-security -am test -Dtest=PermissionInterceptorTest,AuditAspectTest
```

Expected output contains `BUILD SUCCESS`, `Tests run: 3`, and `Failures: 0`.

- [ ] **Step 7: Commit checkpoint**

Run:

```bash
git add och-security/src/main/java och-security/src/test/java
git commit -m "feat: add permission and audit skeletons"
```

Expected output contains `feat: add permission and audit skeletons`.

### Task 5: Redis Wrapper, RabbitMQ Event Abstraction, File Storage, and Migration

**Owners:** Architect, Backend Expert, DevOps Expert

**Files:**
- Create: `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCache.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCacheProperties.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/DomainEvent.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/EventPublisher.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/EventSubscriber.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventProperties.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventPublisher.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/mq/FoundationEventListener.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/file/FileStorage.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/file/StoredFile.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/file/LocalFileStorage.java`
- Create: `och-infra/src/main/java/com/opencallhub/infra/file/FileStorageProperties.java`
- Create: `och-infra/src/main/resources/db/migration/V1__foundation_schema.sql`
- Create: `docker/docker-compose.foundation.yml`
- Create: `.env.example`
- Test: `och-infra/src/test/java/com/opencallhub/infra/redis/RedisCacheTest.java`
- Test: `och-infra/src/test/java/com/opencallhub/infra/mq/RabbitEventPublisherTest.java`
- Test: `och-infra/src/test/java/com/opencallhub/infra/file/LocalFileStorageTest.java`

- [ ] **Step 1: Write Redis wrapper test**

Create `och-infra/src/test/java/com/opencallhub/infra/redis/RedisCacheTest.java`:

```java
package com.opencallhub.infra.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisCacheTest {
    @Test
    void prefixesKeysAndReadsValue() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        RedisCacheProperties properties = new RedisCacheProperties("och:", 300);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.get("och:foundation:key")).thenReturn("value-1");

        RedisCache cache = new RedisCache(template, properties);

        Optional<String> value = cache.get("foundation:key");

        assertThat(value).contains("value-1");
    }

    @Test
    void writesValueWithDefaultTtl() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        RedisCacheProperties properties = new RedisCacheProperties("och:", 300);
        when(template.opsForValue()).thenReturn(ops);

        RedisCache cache = new RedisCache(template, properties);
        cache.set("foundation:key", "value-1");

        verify(ops).set("och:foundation:key", "value-1", Duration.ofSeconds(300));
    }
}
```

- [ ] **Step 2: Write RabbitMQ publisher test**

Create `och-infra/src/test/java/com/opencallhub/infra/mq/RabbitEventPublisherTest.java`:

```java
package com.opencallhub.infra.mq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitEventPublisherTest {
    @Test
    void publishesDomainEventToConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitEventProperties properties = new RabbitEventProperties(
                "och.foundation.exchange",
                "och.foundation.queue",
                "foundation.test",
                "och.foundation.dlx",
                "och.foundation.dlq"
        );
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate, properties);
        DomainEvent event = new DomainEvent("evt-1", "FOUNDATION_TEST", OffsetDateTime.parse("2026-05-19T00:00:00Z"), "trace-evt-1", Map.of("message", "hello"));

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend("och.foundation.exchange", "foundation.test", event);
    }
}
```

- [ ] **Step 3: Write local file storage test**

Create `och-infra/src/test/java/com/opencallhub/infra/file/LocalFileStorageTest.java`:

```java
package com.opencallhub.infra.file;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void storesFileUnderConfiguredRoot() throws Exception {
        FileStorageProperties properties = new FileStorageProperties("local", tempDir.toString());
        LocalFileStorage storage = new LocalFileStorage(properties);

        StoredFile storedFile = storage.store("foundation/hello.txt", new ByteArrayInputStream("hello".getBytes()), "text/plain");

        assertThat(storedFile.path()).isEqualTo("foundation/hello.txt");
        assertThat(storedFile.contentType()).isEqualTo("text/plain");
        assertThat(Files.readString(tempDir.resolve("foundation/hello.txt"))).isEqualTo("hello");
    }
}
```

- [ ] **Step 4: Run infra tests and confirm failure**

Run:

```bash
mvn -pl och-infra -am test -Dtest=RedisCacheTest,RabbitEventPublisherTest,LocalFileStorageTest
```

Expected output contains `cannot find symbol` for Redis, RabbitMQ, and file storage classes.

- [ ] **Step 5: Implement Redis wrapper**

Create `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCacheProperties.java`:

```java
package com.opencallhub.infra.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "och.redis")
public record RedisCacheProperties(String keyPrefix, long defaultTtlSeconds) {
    public RedisCacheProperties {
        keyPrefix = keyPrefix == null ? "och:" : keyPrefix;
        defaultTtlSeconds = defaultTtlSeconds <= 0 ? 300 : defaultTtlSeconds;
    }
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/redis/RedisCache.java`:

```java
package com.opencallhub.infra.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisCache {
    private final StringRedisTemplate redisTemplate;
    private final RedisCacheProperties properties;

    public RedisCache(StringRedisTemplate redisTemplate, RedisCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(namespaced(key), value, Duration.ofSeconds(properties.defaultTtlSeconds()));
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(namespaced(key)));
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(namespaced(key));
    }

    private String namespaced(String key) {
        return properties.keyPrefix() + key;
    }
}
```

- [ ] **Step 6: Implement RabbitMQ event abstraction**

Create `och-infra/src/main/java/com/opencallhub/infra/mq/DomainEvent.java`:

```java
package com.opencallhub.infra.mq;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

public record DomainEvent(String eventId, String eventType, OffsetDateTime occurredAt, String traceId, Map<String, Object> payload) implements Serializable {
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/mq/EventPublisher.java`:

```java
package com.opencallhub.infra.mq;

public interface EventPublisher {
    void publish(DomainEvent event);
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/mq/EventSubscriber.java`:

```java
package com.opencallhub.infra.mq;

public interface EventSubscriber {
    void handle(DomainEvent event);
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventProperties.java`:

```java
package com.opencallhub.infra.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "och.rabbit")
public record RabbitEventProperties(String exchange, String queue, String routingKey, String deadLetterExchange, String deadLetterQueue) {
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/mq/RabbitEventPublisher.java`:

```java
package com.opencallhub.infra.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitEventProperties properties;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate, RabbitEventProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(DomainEvent event) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), event);
    }
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/mq/FoundationEventListener.java`:

```java
package com.opencallhub.infra.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class FoundationEventListener implements EventSubscriber {
    private final AtomicReference<DomainEvent> lastEvent = new AtomicReference<>();

    @Override
    @RabbitListener(queues = "${och.rabbit.queue}")
    public void handle(DomainEvent event) {
        lastEvent.set(event);
    }

    public Optional<DomainEvent> lastEvent() {
        return Optional.ofNullable(lastEvent.get());
    }
}
```

- [ ] **Step 7: Implement file storage abstraction**

Create `och-infra/src/main/java/com/opencallhub/infra/file/FileStorage.java`:

```java
package com.opencallhub.infra.file;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {
    StoredFile store(String path, InputStream inputStream, String contentType) throws IOException;
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/file/StoredFile.java`:

```java
package com.opencallhub.infra.file;

public record StoredFile(String path, String contentType, long size) {
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/file/FileStorageProperties.java`:

```java
package com.opencallhub.infra.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "och.file-storage")
public record FileStorageProperties(String provider, String localRoot) {
    public FileStorageProperties {
        provider = provider == null ? "local" : provider;
        localRoot = localRoot == null ? "./runtime/files" : localRoot;
    }
}
```

Create `och-infra/src/main/java/com/opencallhub/infra/file/LocalFileStorage.java`:

```java
package com.opencallhub.infra.file;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class LocalFileStorage implements FileStorage {
    private final Path root;

    public LocalFileStorage(FileStorageProperties properties) {
        this.root = Path.of(properties.localRoot()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(String path, InputStream inputStream, String contentType) throws IOException {
        Path target = root.resolve(path).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("file path escapes storage root: " + path);
        }
        Files.createDirectories(target.getParent());
        long size = Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(path, contentType, size);
    }
}
```

- [ ] **Step 8: Add migration SQL**

Create `och-infra/src/main/resources/db/migration/V1__foundation_schema.sql`:

```sql
CREATE TABLE foundation_migration_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    marker VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO foundation_migration_check(marker) VALUES ('phase-1-foundation');

CREATE TABLE audit_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trace_id VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    resource VARCHAR(128) NOT NULL,
    success TINYINT(1) NOT NULL,
    error_message VARCHAR(512) NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_event_trace_id (trace_id),
    INDEX idx_audit_event_resource_action (resource, action)
);
```

- [ ] **Step 9: Add local dependency startup files**

Create `docker/docker-compose.foundation.yml`:

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: och-foundation-mysql
    environment:
      MYSQL_DATABASE: opencallhub
      MYSQL_USER: opencallhub
      MYSQL_PASSWORD: opencallhub
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
    command: ["--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-proot"]
      interval: 5s
      timeout: 3s
      retries: 20

  redis:
    image: redis:7.2
    container_name: och-foundation-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 20

  rabbitmq:
    image: rabbitmq:3.13-management
    container_name: och-foundation-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: opencallhub
      RABBITMQ_DEFAULT_PASS: opencallhub
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 5s
      timeout: 5s
      retries: 20
```

Create `.env.example`:

```dotenv
OCH_PROFILE=local
OCH_SERVER_PORT=18080
OCH_DB_URL=jdbc:mysql://localhost:3306/opencallhub?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&createDatabaseIfNotExist=true
OCH_DB_USERNAME=opencallhub
OCH_DB_PASSWORD=opencallhub
OCH_REDIS_HOST=localhost
OCH_REDIS_PORT=6379
OCH_REDIS_PASSWORD=
OCH_REDIS_KEY_PREFIX=och:
OCH_REDIS_DEFAULT_TTL_SECONDS=300
OCH_RABBIT_HOST=localhost
OCH_RABBIT_PORT=5672
OCH_RABBIT_USERNAME=opencallhub
OCH_RABBIT_PASSWORD=opencallhub
OCH_RABBIT_EXCHANGE=och.foundation.exchange
OCH_RABBIT_QUEUE=och.foundation.queue
OCH_RABBIT_ROUTING_KEY=foundation.test
OCH_RABBIT_DLX=och.foundation.dlx
OCH_RABBIT_DLQ=och.foundation.dlq
OCH_FILE_STORAGE_PROVIDER=local
OCH_FILE_STORAGE_LOCAL_ROOT=./runtime/files
```

- [ ] **Step 10: Enable configuration properties scanning**

Modify `och-api/src/main/java/com/opencallhub/api/OpenCallHubApplication.java` to add `@ConfigurationPropertiesScan(basePackages = "com.opencallhub")`:

```java
package com.opencallhub.api;

import com.opencallhub.common.trace.TraceIdFilter;
import com.opencallhub.common.trace.TraceIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.opencallhub")
@ConfigurationPropertiesScan(basePackages = "com.opencallhub")
public class OpenCallHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenCallHubApplication.class, args);
    }

    @Bean
    TraceIdGenerator traceIdGenerator() {
        return new TraceIdGenerator();
    }

    @Bean
    FilterRegistrationBean<TraceIdFilter> traceIdFilter(TraceIdGenerator generator) {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter(generator));
        registration.setOrder(1);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
```

Expected result: Spring registers `RedisCacheProperties`, `RabbitEventProperties`, and `FileStorageProperties`.

- [ ] **Step 11: Run infra unit tests**

Run:

```bash
mvn -pl och-infra -am test -Dtest=RedisCacheTest,RabbitEventPublisherTest,LocalFileStorageTest
```

Expected output contains `BUILD SUCCESS`, `Tests run: 4`, and `Failures: 0`.

- [ ] **Step 12: Start dependencies and verify migration from a clean database**

Run:

```bash
docker compose -f docker/docker-compose.foundation.yml up -d
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Expected output contains Docker services `och-foundation-mysql`, `och-foundation-redis`, and `och-foundation-rabbitmq` as healthy or running. Backend output contains `Successfully applied 1 migration` or `Current version of schema opencallhub: 1`, followed by `Started OpenCallHubApplication`.

- [ ] **Step 13: Commit checkpoint**

Run:

```bash
git add och-infra/src/main/java och-infra/src/main/resources och-infra/src/test/java docker/docker-compose.foundation.yml .env.example och-api/src/main/java/com/opencallhub/api/OpenCallHubApplication.java
git commit -m "feat: add infrastructure foundation abstractions"
```

Expected output contains `feat: add infrastructure foundation abstractions`.

### Task 6: Foundation Smoke Endpoints for Redis, RabbitMQ, File Storage, and Migration

**Owners:** Backend Expert, QA Expert

**Files:**
- Modify: `och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java`
- Test: `och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java`

- [ ] **Step 1: Extend controller tests for smoke endpoints**

Modify `och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java` by adding mocked bean fields and tests:

```java
@org.springframework.boot.test.mock.mockito.MockBean
com.opencallhub.infra.redis.RedisCache redisCache;

@org.springframework.boot.test.mock.mockito.MockBean
com.opencallhub.infra.mq.EventPublisher eventPublisher;

@org.springframework.boot.test.mock.mockito.MockBean
com.opencallhub.infra.mq.FoundationEventListener foundationEventListener;

@org.springframework.boot.test.mock.mockito.MockBean
org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

@Test
void redisSmokeWritesAndReadsValue() throws Exception {
    org.mockito.Mockito.when(redisCache.get("foundation:smoke")).thenReturn(java.util.Optional.of("ok"));

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/foundation/redis/smoke"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.key", equalTo("foundation:smoke")))
            .andExpect(jsonPath("$.data.value", equalTo("ok")));

    org.mockito.Mockito.verify(redisCache).set("foundation:smoke", "ok");
}

@Test
void rabbitSmokePublishesEvent() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/foundation/rabbit/smoke").header("X-Trace-Id", "rabbit-trace-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.eventType", equalTo("FOUNDATION_TEST")))
            .andExpect(jsonPath("$.data.traceId", equalTo("rabbit-trace-001")));

    org.mockito.Mockito.verify(eventPublisher).publish(org.mockito.ArgumentMatchers.argThat(event ->
            event.eventType().equals("FOUNDATION_TEST") && event.traceId().equals("rabbit-trace-001")));
}

@Test
void migrationSmokeReadsMarkerThroughJdbcTemplate() throws Exception {
    org.mockito.Mockito.when(jdbcTemplate.queryForObject(
            "select marker from foundation_migration_check where marker = 'phase-1-foundation'",
            String.class
    )).thenReturn("phase-1-foundation");

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/foundation/migration/smoke"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.marker", equalTo("phase-1-foundation")));
}
```

Expected result: tests compile after imports are added. `JdbcTemplate` is explicitly mocked because this test excludes `DataSourceAutoConfiguration`; the migration smoke test stubs `queryForObject` so the controller test remains executable without a real database. If the file uses wildcard static imports, use explicit class references shown above to avoid import ambiguity.

- [ ] **Step 2: Run smoke endpoint tests and confirm failure**

Run:

```bash
mvn -pl och-api -am test -Dtest=FoundationControllerTest
```

Expected output contains `No mapping for POST /api/foundation/redis/smoke` or similar failing assertion.

- [ ] **Step 3: Implement smoke endpoints**

Replace `och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java` with:

```java
package com.opencallhub.api.controller;

import com.opencallhub.common.trace.TraceContext;
import com.opencallhub.infra.file.FileStorage;
import com.opencallhub.infra.file.StoredFile;
import com.opencallhub.infra.mq.DomainEvent;
import com.opencallhub.infra.mq.EventPublisher;
import com.opencallhub.infra.mq.FoundationEventListener;
import com.opencallhub.infra.redis.RedisCache;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/foundation")
public class FoundationController {
    private final RedisCache redisCache;
    private final EventPublisher eventPublisher;
    private final FoundationEventListener foundationEventListener;
    private final FileStorage fileStorage;
    private final JdbcTemplate jdbcTemplate;

    public FoundationController(RedisCache redisCache, EventPublisher eventPublisher, FoundationEventListener foundationEventListener,
                                FileStorage fileStorage, JdbcTemplate jdbcTemplate) {
        this.redisCache = redisCache;
        this.eventPublisher = eventPublisher;
        this.foundationEventListener = foundationEventListener;
        this.fileStorage = fileStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "openCallHub", "phase", "foundation", "time", OffsetDateTime.now().toString());
    }

    @PostMapping("/redis/smoke")
    public Map<String, Object> redisSmoke() {
        redisCache.set("foundation:smoke", "ok");
        String value = redisCache.get("foundation:smoke").orElse("missing");
        return Map.of("key", "foundation:smoke", "value", value);
    }

    @PostMapping("/rabbit/smoke")
    public DomainEvent rabbitSmoke() {
        DomainEvent event = new DomainEvent(UUID.randomUUID().toString(), "FOUNDATION_TEST", OffsetDateTime.now(), TraceContext.getTraceId(), Map.of("message", "hello"));
        eventPublisher.publish(event);
        return event;
    }

    @GetMapping("/rabbit/last")
    public Map<String, Object> rabbitLast() {
        return foundationEventListener.lastEvent()
                .map(event -> Map.<String, Object>of("consumed", true, "eventId", event.eventId(), "eventType", event.eventType(), "traceId", event.traceId()))
                .orElseGet(() -> Map.of("consumed", false));
    }

    @PostMapping("/file/smoke")
    public StoredFile fileSmoke() throws Exception {
        return fileStorage.store("foundation/smoke.txt", new ByteArrayInputStream("foundation-ok".getBytes(StandardCharsets.UTF_8)), "text/plain");
    }

    @GetMapping("/migration/smoke")
    public Map<String, Object> migrationSmoke() {
        String marker = jdbcTemplate.queryForObject("select marker from foundation_migration_check where marker = 'phase-1-foundation'", String.class);
        return Map.of("marker", marker);
    }
}
```

- [ ] **Step 4: Run API tests**

Run:

```bash
mvn -pl och-api -am test -Dtest=FoundationControllerTest
```

Expected output contains `BUILD SUCCESS` and all `FoundationControllerTest` tests pass.

- [ ] **Step 5: Verify local smoke endpoints against real dependencies**

Run:

```bash
docker compose -f docker/docker-compose.foundation.yml up -d
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

In another terminal run:

```bash
curl -s -X POST http://localhost:18080/api/foundation/redis/smoke
curl -s -X POST -H "X-Trace-Id: smoke-rabbit-001" http://localhost:18080/api/foundation/rabbit/smoke
curl -s http://localhost:18080/api/foundation/rabbit/last
curl -s -X POST http://localhost:18080/api/foundation/file/smoke
curl -s http://localhost:18080/api/foundation/migration/smoke
```

Expected outputs respectively contain `"value":"ok"`, `"eventType":"FOUNDATION_TEST"`, `"consumed":true`, `"path":"foundation/smoke.txt"`, and `"marker":"phase-1-foundation"`.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-api/src/main/java/com/opencallhub/api/controller/FoundationController.java och-api/src/test/java/com/opencallhub/api/controller/FoundationControllerTest.java
git commit -m "feat: add foundation smoke endpoints"
```

Expected output contains `feat: add foundation smoke endpoints`.

### Task 7: Vue 3 App Structure, Routes, Menus, State, Request, and WebSocket Wrappers

**Owner:** Frontend Expert

**Files:**
- Create or modify: `waihu-app/package.json`
- Create: `waihu-app/vite.config.ts`
- Create: `waihu-app/playwright.config.ts`
- Create: `waihu-app/tsconfig.json`
- Create: `waihu-app/index.html`
- Create: `waihu-app/src/main.ts`
- Create: `waihu-app/src/App.vue`
- Create: `waihu-app/src/router/index.ts`
- Create: `waihu-app/src/router/routes.ts`
- Create: `waihu-app/src/stores/auth.ts`
- Create: `waihu-app/src/stores/permission.ts`
- Create: `waihu-app/src/stores/menu.ts`
- Create: `waihu-app/src/stores/app.ts`
- Create: `waihu-app/src/services/request.ts`
- Create: `waihu-app/src/services/websocket.ts`
- Create: `waihu-app/src/services/foundationApi.ts`
- Create: `waihu-app/src/types/api.ts`
- Create: `waihu-app/src/types/menu.ts`
- Create: `waihu-app/src/types/websocket.ts`
- Test: `waihu-app/src/tests/request.spec.ts`
- Test: `waihu-app/src/tests/router.spec.ts`
- Test: `waihu-app/src/tests/stores.spec.ts`
- Test: `waihu-app/src/tests/websocket.spec.ts`

- [ ] **Step 1: Create frontend package and TypeScript config**

Create `waihu-app/package.json`:

```json
{
  "name": "opencallhub-console",
  "version": "1.0.0-phase-1",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite --host 0.0.0.0 --port 5173",
    "build": "vue-tsc --noEmit && vite build",
    "test:unit": "vitest run",
    "test:e2e": "playwright test",
    "lint": "eslint src --ext .ts,.vue"
  },
  "dependencies": {
    "@vitejs/plugin-vue": "latest",
    "axios": "latest",
    "pinia": "latest",
    "vue": "latest",
    "vue-router": "latest"
  },
  "devDependencies": {
    "@playwright/test": "latest",
    "@types/node": "latest",
    "@vue/test-utils": "latest",
    "eslint": "latest",
    "eslint-plugin-vue": "latest",
    "jsdom": "latest",
    "typescript": "latest",
    "vite": "latest",
    "vitest": "latest",
    "vue-tsc": "latest"
  }
}
```

Create `waihu-app/tsconfig.json`:

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "moduleResolution": "Node",
    "strict": true,
    "jsx": "preserve",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "types": ["vitest/globals", "node"]
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue", "e2e/**/*.ts"],
  "references": []
}
```

Create `waihu-app/vite.config.ts`:

```ts
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': 'http://localhost:18080',
      '/ws': {
        target: 'ws://localhost:18080',
        ws: true
      }
    }
  },
  test: {
    environment: 'jsdom'
  }
})
```

- [ ] **Step 2: Write request wrapper test**

Create `waihu-app/src/tests/request.spec.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { extractApiData } from '@/services/request'
import type { ApiResponse } from '@/types/api'

describe('request wrapper', () => {
  it('extracts data and traceId from unified response', () => {
    const response: ApiResponse<{ status: string }> = {
      code: '0',
      message: 'success',
      data: { status: 'UP' },
      traceId: 'trace-front-001'
    }

    const result = extractApiData(response)

    expect(result.data.status).toBe('UP')
    expect(result.traceId).toBe('trace-front-001')
  })

  it('throws an error with backend message when response code is not success', () => {
    const response: ApiResponse<null> = {
      code: 'FORBIDDEN',
      message: 'permission denied',
      data: null,
      traceId: 'trace-front-002'
    }

    expect(() => extractApiData(response)).toThrow('permission denied traceId=trace-front-002')
  })
})
```

- [ ] **Step 3: Write router and store tests**

Create `waihu-app/src/tests/router.spec.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { appRoutes } from '@/router/routes'

describe('route permission shell', () => {
  it('defines login and authenticated home routes', () => {
    expect(appRoutes.some(route => route.path === '/login')).toBe(true)
    const root = appRoutes.find(route => route.path === '/')
    expect(root?.meta?.requiresAuth).toBe(true)
    expect(root?.children?.some(route => route.path === 'home')).toBe(true)
  })
})
```

Create `waihu-app/src/tests/stores.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import { useMenuStore } from '@/stores/menu'

describe('Pinia state conventions', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('keeps auth token and user profile in auth store', () => {
    const auth = useAuthStore()
    auth.setSession('token-1', { id: 'u1', name: 'Admin' })
    expect(auth.token).toBe('token-1')
    expect(auth.user?.name).toBe('Admin')
  })

  it('checks permission codes in permission store', () => {
    const permission = usePermissionStore()
    permission.setPermissions(['system:menu:view'])
    expect(permission.hasPermission('system:menu:view')).toBe(true)
    expect(permission.hasPermission('system:user:delete')).toBe(false)
  })

  it('builds grouped menu items from route metadata', () => {
    const menu = useMenuStore()
    menu.loadFoundationMenus()
    expect(menu.groups.map(group => group.title)).toContain('Workbench')
  })
})
```

- [ ] **Step 4: Write WebSocket wrapper test**

Create `waihu-app/src/tests/websocket.spec.ts`:

```ts
import { describe, expect, it, vi } from 'vitest'
import { createWebSocketClient } from '@/services/websocket'

class FakeSocket {
  static instances: FakeSocket[] = []
  url: string
  sent: string[] = []
  onopen: (() => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  onclose: (() => void) | null = null

  constructor(url: string) {
    this.url = url
    FakeSocket.instances.push(this)
  }

  send(message: string) {
    this.sent.push(message)
  }

  close() {
    this.onclose?.()
  }
}

describe('WebSocket wrapper', () => {
  it('connects and dispatches typed messages', () => {
    vi.stubGlobal('WebSocket', FakeSocket)
    const received: string[] = []
    const client = createWebSocketClient('/ws/agent', message => received.push(message.type))

    client.connect()
    FakeSocket.instances[0].onmessage?.({ data: JSON.stringify({ type: 'PING', traceId: 'ws-trace-001', payload: {} }) } as MessageEvent)

    expect(FakeSocket.instances[0].url).toContain('/ws/agent')
    expect(received).toEqual(['PING'])
    vi.unstubAllGlobals()
  })
})
```

- [ ] **Step 5: Run frontend tests and confirm failure**

Run:

```bash
cd waihu-app && npm install && npm run test:unit
```

Expected output contains module resolution failures for `@/services/request`, `@/router/routes`, stores, or WebSocket wrapper.

- [ ] **Step 6: Implement frontend types and request wrapper**

Create `waihu-app/src/types/api.ts`:

```ts
export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  traceId: string
}

export interface PageResponse<T> {
  records: T[]
  total: number
  pageNo: number
  pageSize: number
}

export interface ExtractedApiData<T> {
  data: T
  traceId: string
}
```

Create `waihu-app/src/services/request.ts`:

```ts
import axios from 'axios'
import type { ApiResponse, ExtractedApiData } from '@/types/api'

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use(config => {
  config.headers = config.headers ?? {}
  config.headers['X-Trace-Id'] = crypto.randomUUID()
  return config
})

export function extractApiData<T>(response: ApiResponse<T>): ExtractedApiData<T> {
  if (response.code !== '0') {
    throw new Error(`${response.message} traceId=${response.traceId}`)
  }
  return { data: response.data, traceId: response.traceId }
}

export async function getApi<T>(url: string): Promise<ExtractedApiData<T>> {
  const response = await http.get<ApiResponse<T>>(url)
  return extractApiData(response.data)
}

export async function postApi<T>(url: string, body?: unknown): Promise<ExtractedApiData<T>> {
  const response = await http.post<ApiResponse<T>>(url, body)
  return extractApiData(response.data)
}
```

- [ ] **Step 7: Implement routes and Pinia stores**

Create `waihu-app/src/types/menu.ts`:

```ts
export interface MenuItem {
  title: string
  path: string
  permission?: string
}

export interface MenuGroup {
  title: string
  items: MenuItem[]
}
```

Create `waihu-app/src/router/routes.ts`:

```ts
import type { RouteRecordRaw } from 'vue-router'

export const appRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'console',
    component: () => import('@/layouts/ConsoleLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: 'Home', menuGroup: 'Workbench', permission: 'home:view' }
      },
      {
        path: 'patterns',
        name: 'patterns',
        component: () => import('@/views/PatternDemoView.vue'),
        meta: { title: 'Patterns', menuGroup: 'System Foundation', permission: 'foundation:patterns:view' }
      }
    ]
  }
]
```

Create `waihu-app/src/router/index.ts`:

```ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import { appRoutes } from './routes'

export const router = createRouter({
  history: createWebHistory(),
  routes: appRoutes
})

router.beforeEach(to => {
  const auth = useAuthStore()
  const permission = usePermissionStore()
  if (to.meta.public) {
    return true
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/login'
  }
  const permissionCode = to.meta.permission as string | undefined
  if (permissionCode && !permission.hasPermission(permissionCode)) {
    return '/home'
  }
  return true
})
```

Create `waihu-app/src/stores/auth.ts`:

```ts
import { defineStore } from 'pinia'

export interface UserProfile {
  id: string
  name: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null as UserProfile | null
  }),
  getters: {
    isAuthenticated: state => state.token.length > 0
  },
  actions: {
    setSession(token: string, user: UserProfile) {
      this.token = token
      this.user = user
    },
    clearSession() {
      this.token = ''
      this.user = null
    }
  }
})
```

Create `waihu-app/src/stores/permission.ts`:

```ts
import { defineStore } from 'pinia'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [] as string[]
  }),
  actions: {
    setPermissions(permissions: string[]) {
      this.permissions = [...permissions]
    },
    hasPermission(permission: string) {
      return this.permissions.includes(permission)
    }
  }
})
```

Create `waihu-app/src/stores/menu.ts`:

```ts
import { defineStore } from 'pinia'
import type { MenuGroup } from '@/types/menu'

export const useMenuStore = defineStore('menu', {
  state: () => ({
    groups: [] as MenuGroup[]
  }),
  actions: {
    loadFoundationMenus() {
      this.groups = [
        { title: 'Workbench', items: [{ title: 'Home', path: '/home', permission: 'home:view' }] },
        { title: 'System Foundation', items: [{ title: 'Patterns', path: '/patterns', permission: 'foundation:patterns:view' }] }
      ]
    }
  }
})
```

Create `waihu-app/src/stores/app.ts`:

```ts
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    lastTraceId: ''
  }),
  actions: {
    setSidebarCollapsed(collapsed: boolean) {
      this.sidebarCollapsed = collapsed
    },
    setLastTraceId(traceId: string) {
      this.lastTraceId = traceId
    }
  }
})
```

- [ ] **Step 8: Implement WebSocket and foundation API wrappers**

Create `waihu-app/src/types/websocket.ts`:

```ts
export interface WebSocketMessage<T = unknown> {
  type: string
  traceId: string
  payload: T
}
```

Create `waihu-app/src/services/websocket.ts`:

```ts
import type { WebSocketMessage } from '@/types/websocket'

export interface WebSocketClient {
  connect(): void
  send(message: WebSocketMessage): void
  close(): void
}

export function createWebSocketClient(path: string, onMessage: (message: WebSocketMessage) => void): WebSocketClient {
  let socket: WebSocket | null = null

  return {
    connect() {
      const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
      socket = new WebSocket(`${protocol}://${window.location.host}${path}`)
      socket.onmessage = event => onMessage(JSON.parse(event.data) as WebSocketMessage)
    },
    send(message: WebSocketMessage) {
      socket?.send(JSON.stringify(message))
    },
    close() {
      socket?.close()
      socket = null
    }
  }
}
```

Create `waihu-app/src/services/foundationApi.ts`:

```ts
import { getApi, postApi } from '@/services/request'

export interface FoundationHealth {
  status: string
  service: string
  phase: string
  time: string
}

export function getFoundationHealth() {
  return getApi<FoundationHealth>('/foundation/health')
}

export function smokeRedis() {
  return postApi<{ key: string; value: string }>('/foundation/redis/smoke')
}
```

- [ ] **Step 9: Bootstrap Vue application**

Create `waihu-app/index.html`:

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>openCallHub Console</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

Create `waihu-app/src/main.ts`:

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import './styles/theme.css'
import './styles/layout.css'

createApp(App).use(createPinia()).use(router).mount('#app')
```

Create `waihu-app/src/App.vue`:

```vue
<template>
  <RouterView />
</template>
```

- [ ] **Step 10: Run frontend unit tests**

Run:

```bash
cd waihu-app && npm run test:unit
```

Expected output contains `Test Files 4 passed` and all request, router, store, and WebSocket tests pass.

- [ ] **Step 11: Commit checkpoint**

Run:

```bash
git add waihu-app/package.json waihu-app/package-lock.json waihu-app/vite.config.ts waihu-app/tsconfig.json waihu-app/index.html waihu-app/src/main.ts waihu-app/src/App.vue waihu-app/src/router waihu-app/src/stores waihu-app/src/services waihu-app/src/types waihu-app/src/tests
git commit -m "feat: add Vue foundation routing state and API wrappers"
```

Expected output contains `feat: add Vue foundation routing state and API wrappers`.

### Task 8: Layout Shell, Menu Shell, Page Patterns, Login Shell, and Home Shell

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/layouts/ConsoleLayout.vue`
- Create: `waihu-app/src/components/menu/AppMenu.vue`
- Create: `waihu-app/src/components/patterns/DataTablePattern.vue`
- Create: `waihu-app/src/components/patterns/FormPattern.vue`
- Create: `waihu-app/src/components/patterns/DetailDrawerPattern.vue`
- Create: `waihu-app/src/views/LoginView.vue`
- Create: `waihu-app/src/views/HomeView.vue`
- Create: `waihu-app/src/views/PatternDemoView.vue`
- Create: `waihu-app/src/styles/theme.css`
- Create: `waihu-app/src/styles/layout.css`
- Create: `waihu-app/e2e/login-home.spec.ts`
- Create: `waihu-app/playwright.config.ts`

- [ ] **Step 1: Create layout and menu shell**

Create `waihu-app/src/components/menu/AppMenu.vue`:

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useMenuStore } from '@/stores/menu'

const menuStore = useMenuStore()
onMounted(() => menuStore.loadFoundationMenus())
</script>

<template>
  <nav class="app-menu" aria-label="Main menu">
    <section v-for="group in menuStore.groups" :key="group.title" class="menu-group">
      <h3>{{ group.title }}</h3>
      <RouterLink v-for="item in group.items" :key="item.path" :to="item.path" class="menu-item">
        {{ item.title }}
      </RouterLink>
    </section>
  </nav>
</template>
```

Create `waihu-app/src/layouts/ConsoleLayout.vue`:

```vue
<script setup lang="ts">
import AppMenu from '@/components/menu/AppMenu.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="console-layout">
    <aside class="console-sidebar">
      <div class="brand">openCallHub</div>
      <AppMenu />
    </aside>
    <main class="console-main">
      <header class="console-header">
        <span>Foundation Console</span>
        <span class="user-name">{{ auth.user?.name ?? 'Guest' }}</span>
      </header>
      <section class="console-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>
```

- [ ] **Step 2: Create table, form, and detail drawer patterns**

Create `waihu-app/src/components/patterns/DataTablePattern.vue`:

```vue
<script setup lang="ts">
defineProps<{ rows: Array<Record<string, string>> }>()
</script>

<template>
  <table class="data-table-pattern">
    <thead>
      <tr><th>Name</th><th>Status</th><th>Owner</th></tr>
    </thead>
    <tbody>
      <tr v-for="row in rows" :key="row.name">
        <td>{{ row.name }}</td>
        <td>{{ row.status }}</td>
        <td>{{ row.owner }}</td>
      </tr>
    </tbody>
  </table>
</template>
```

Create `waihu-app/src/components/patterns/FormPattern.vue`:

```vue
<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<template>
  <form class="form-pattern">
    <h3>{{ title }}</h3>
    <label>
      Name
      <input value="Foundation item" aria-label="Name" />
    </label>
    <label>
      Status
      <select aria-label="Status"><option>Enabled</option><option>Disabled</option></select>
    </label>
    <button type="button">Save Draft</button>
  </form>
</template>
```

Create `waihu-app/src/components/patterns/DetailDrawerPattern.vue`:

```vue
<script setup lang="ts">
defineProps<{ open: boolean; title: string }>()
</script>

<template>
  <aside v-if="open" class="detail-drawer-pattern" aria-label="Detail drawer">
    <h3>{{ title }}</h3>
    <p>Trace, audit, dependencies, and operation history appear in this drawer pattern.</p>
  </aside>
</template>
```

- [ ] **Step 3: Create login and home shell pages**

Create `waihu-app/src/views/LoginView.vue`:

```vue
<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const router = useRouter()
const auth = useAuthStore()
const permission = usePermissionStore()

function login() {
  auth.setSession('foundation-token', { id: 'admin', name: 'Foundation Admin' })
  permission.setPermissions(['home:view', 'foundation:patterns:view'])
  router.push('/home')
}
</script>

<template>
  <main class="login-shell">
    <section class="login-card">
      <h1>openCallHub</h1>
      <p>Phase 1 Foundation Login Shell</p>
      <button type="button" @click="login">Enter Console</button>
    </section>
  </main>
</template>
```

Create `waihu-app/src/views/HomeView.vue`:

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getFoundationHealth } from '@/services/foundationApi'

const status = ref('loading')
const traceId = ref('')

onMounted(async () => {
  try {
    const response = await getFoundationHealth()
    status.value = response.data.status
    traceId.value = response.traceId
  } catch (error) {
    status.value = error instanceof Error ? error.message : 'unknown error'
  }
})
</script>

<template>
  <section class="home-shell">
    <h1>Home</h1>
    <p>Backend foundation status: <strong>{{ status }}</strong></p>
    <p>Last traceId: <code>{{ traceId || 'waiting for backend response' }}</code></p>
  </section>
</template>
```

Create `waihu-app/src/views/PatternDemoView.vue`:

```vue
<script setup lang="ts">
import DataTablePattern from '@/components/patterns/DataTablePattern.vue'
import FormPattern from '@/components/patterns/FormPattern.vue'
import DetailDrawerPattern from '@/components/patterns/DetailDrawerPattern.vue'

const rows = [
  { name: 'Unified response', status: 'Ready', owner: 'Backend Expert' },
  { name: 'Menu shell', status: 'Ready', owner: 'Frontend Expert' }
]
</script>

<template>
  <section class="pattern-demo">
    <h1>Page Patterns</h1>
    <DataTablePattern :rows="rows" />
    <FormPattern title="Foundation Form Pattern" />
    <DetailDrawerPattern :open="true" title="Foundation Detail Drawer" />
  </section>
</template>
```

- [ ] **Step 4: Create styles**

Create `waihu-app/src/styles/theme.css`:

```css
:root {
  --och-bg: #f5f7fb;
  --och-panel: #ffffff;
  --och-primary: #165dff;
  --och-text: #172033;
  --och-muted: #667085;
  --och-border: #d9e1ec;
}

* { box-sizing: border-box; }
body { margin: 0; font-family: Inter, Arial, sans-serif; color: var(--och-text); background: var(--och-bg); }
button { background: var(--och-primary); color: white; border: 0; border-radius: 8px; padding: 10px 16px; cursor: pointer; }
input, select { border: 1px solid var(--och-border); border-radius: 8px; padding: 8px 10px; }
```

Create `waihu-app/src/styles/layout.css`:

```css
.console-layout { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
.console-sidebar { background: #101828; color: white; padding: 20px; }
.brand { font-size: 20px; font-weight: 700; margin-bottom: 24px; }
.menu-group { margin-bottom: 20px; }
.menu-group h3 { color: #98a2b3; font-size: 12px; text-transform: uppercase; }
.menu-item { display: block; color: white; text-decoration: none; padding: 8px 0; }
.console-main { display: flex; flex-direction: column; }
.console-header { height: 56px; background: var(--och-panel); border-bottom: 1px solid var(--och-border); display: flex; align-items: center; justify-content: space-between; padding: 0 24px; }
.console-content { padding: 24px; }
.login-shell { min-height: 100vh; display: grid; place-items: center; }
.login-card, .home-shell, .pattern-demo, .form-pattern, .detail-drawer-pattern { background: var(--och-panel); border: 1px solid var(--och-border); border-radius: 16px; padding: 24px; }
.data-table-pattern { width: 100%; border-collapse: collapse; margin-bottom: 16px; background: var(--och-panel); }
.data-table-pattern th, .data-table-pattern td { border: 1px solid var(--och-border); padding: 10px; text-align: left; }
.form-pattern { display: grid; gap: 12px; margin-bottom: 16px; }
.form-pattern label { display: grid; gap: 6px; }
.detail-drawer-pattern { border-left: 4px solid var(--och-primary); }
```

- [ ] **Step 5: Add Playwright config and E2E login-home test**

Create `waihu-app/playwright.config.ts`:

```ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  reporter: 'list',
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'on-first-retry'
  },
  webServer: {
    command: 'npm run dev -- --host 127.0.0.1 --port 5173',
    url: 'http://127.0.0.1:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
```

Create `waihu-app/e2e/login-home.spec.ts`:

```ts
import { test, expect } from '@playwright/test'

test('login shell renders and navigates to home shell', async ({ page }) => {
  await page.route('**/api/foundation/health', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ code: '0', message: 'success', data: { status: 'UP', service: 'openCallHub', phase: 'foundation', time: '2026-05-19T00:00:00Z' }, traceId: 'e2e-trace-001' })
  }))

  await page.goto('/login')
  await expect(page.getByText('Phase 1 Foundation Login Shell')).toBeVisible()
  await page.getByRole('button', { name: 'Enter Console' }).click()
  await expect(page.getByRole('heading', { name: 'Home' })).toBeVisible()
  await expect(page.getByText('Backend foundation status:')).toBeVisible()
  await expect(page.getByText('e2e-trace-001')).toBeVisible()
})
```

- [ ] **Step 6: Run frontend build and E2E test**

Run:

```bash
cd waihu-app
npm run build
npx playwright install chromium
npm run test:e2e -- --project=chromium
```

Expected build output contains `vite v` and `built in`. Expected E2E output contains `1 passed`.

- [ ] **Step 7: Start frontend manually**

Run:

```bash
cd waihu-app && npm run dev
```

Expected output contains `Local: http://localhost:5173/`. Opening `http://localhost:5173/login` renders the login shell, clicking `Enter Console` renders the home shell.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add waihu-app/src/layouts waihu-app/src/components waihu-app/src/views waihu-app/src/styles waihu-app/e2e waihu-app/playwright.config.ts
git commit -m "feat: add console layout login home and page patterns"
```

Expected output contains `feat: add console layout login home and page patterns`.

### Task 9: DevOps Local Startup and Environment Verification

**Owner:** DevOps Expert

**Files:**
- Modify: `.env.example`
- Modify: `docker/docker-compose.foundation.yml`
- Test: local commands listed below

- [ ] **Step 1: Verify environment variable names are complete**

Open `.env.example` and confirm it contains exactly these keys:

```dotenv
OCH_PROFILE=local
OCH_SERVER_PORT=18080
OCH_DB_URL=jdbc:mysql://localhost:3306/opencallhub?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&createDatabaseIfNotExist=true
OCH_DB_USERNAME=opencallhub
OCH_DB_PASSWORD=opencallhub
OCH_REDIS_HOST=localhost
OCH_REDIS_PORT=6379
OCH_REDIS_PASSWORD=
OCH_REDIS_KEY_PREFIX=och:
OCH_REDIS_DEFAULT_TTL_SECONDS=300
OCH_RABBIT_HOST=localhost
OCH_RABBIT_PORT=5672
OCH_RABBIT_USERNAME=opencallhub
OCH_RABBIT_PASSWORD=opencallhub
OCH_RABBIT_EXCHANGE=och.foundation.exchange
OCH_RABBIT_QUEUE=och.foundation.queue
OCH_RABBIT_ROUTING_KEY=foundation.test
OCH_RABBIT_DLX=och.foundation.dlx
OCH_RABBIT_DLQ=och.foundation.dlq
OCH_FILE_STORAGE_PROVIDER=local
OCH_FILE_STORAGE_LOCAL_ROOT=./runtime/files
```

Expected result: every listed key appears once.

- [ ] **Step 2: Start local dependencies**

Run:

```bash
docker compose -f docker/docker-compose.foundation.yml up -d
```

Expected output contains `Container och-foundation-mysql`, `Container och-foundation-redis`, and `Container och-foundation-rabbitmq` with `Started` or `Running`.

- [ ] **Step 3: Verify dependency ports**

Run:

```bash
docker compose -f docker/docker-compose.foundation.yml ps
```

Expected output shows MySQL `3306`, Redis `6379`, RabbitMQ `5672`, and RabbitMQ Management `15672` mapped to localhost.

- [ ] **Step 4: Start backend**

Run:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Expected output contains `Started OpenCallHubApplication` and no `BeanCreationException`.

- [ ] **Step 5: Start frontend**

Run:

```bash
cd waihu-app && npm run dev
```

Expected output contains `Local: http://localhost:5173/` and no TypeScript syntax errors.

- [ ] **Step 6: Commit checkpoint**

Run only if `.env.example` or `docker/docker-compose.foundation.yml` needed corrections during this task:

```bash
git add .env.example docker/docker-compose.foundation.yml
git commit -m "chore: align foundation local startup environment"
```

Expected output contains `chore: align foundation local startup environment`. If neither file changed, do not create a commit for this task.

### Task 10: QA Foundation Test Gates and Acceptance Verification

**Owner:** QA Expert

**Files:**
- Test only: Maven, npm, Docker Compose, curl, and browser verification commands

- [ ] **Step 1: Run backend foundation tests**

Run:

```bash
mvn -pl och-api -am test -DskipTests=false
```

Expected output contains `BUILD SUCCESS`, and test summaries for `och-common`, `och-infra`, `och-security`, and `och-api` contain `Failures: 0`.

- [ ] **Step 2: Run frontend unit tests and build**

Run:

```bash
cd waihu-app && npm run test:unit && npm run build
```

Expected output contains `Test Files 4 passed` and Vite build output with `built in`.

- [ ] **Step 3: Run frontend E2E test**

Run:

```bash
cd waihu-app && npm run test:e2e -- --project=chromium
```

Expected output contains `1 passed`.

- [ ] **Step 4: Verify database migration from a clean database**

Run:

```bash
docker compose -f docker/docker-compose.foundation.yml down -v
docker compose -f docker/docker-compose.foundation.yml up -d
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Expected backend output contains `Successfully applied 1 migration` or `Current version of schema opencallhub: 1`. Then run:

```bash
curl -s http://localhost:18080/api/foundation/migration/smoke
```

Expected response contains `"marker":"phase-1-foundation"`.

- [ ] **Step 5: Verify traceId in request log and response**

Run:

```bash
curl -s -D - -H "X-Trace-Id: qa-trace-001" http://localhost:18080/api/foundation/health
```

Expected response headers contain `X-Trace-Id: qa-trace-001`, response body contains `"traceId":"qa-trace-001"`, and backend console logs one request line with `traceId=qa-trace-001`.

- [ ] **Step 6: Verify RabbitMQ publish and consume**

Run:

```bash
curl -s -X POST -H "X-Trace-Id: qa-rabbit-001" http://localhost:18080/api/foundation/rabbit/smoke
curl -s http://localhost:18080/api/foundation/rabbit/last
```

Expected first response contains `"eventType":"FOUNDATION_TEST"` and `"traceId":"qa-rabbit-001"`. Expected second response contains `"consumed":true`, `"eventType":"FOUNDATION_TEST"`, and `"traceId":"qa-rabbit-001"`.

- [ ] **Step 7: Verify Redis read/write**

Run:

```bash
curl -s -X POST http://localhost:18080/api/foundation/redis/smoke
```

Expected response contains `"key":"foundation:smoke"` and `"value":"ok"`.

- [ ] **Step 8: Verify OpenAPI**

Run:

```bash
curl -s http://localhost:18080/v3/api-docs
```

Expected response contains `"openapi":"3.` and `"title":"openCallHub API"`.

- [ ] **Step 9: Verify frontend login and home shell**

Run:

```bash
cd waihu-app && npm run dev
```

Open `http://localhost:5173/login`, click `Enter Console`, and verify the page shows `Home`, `Backend foundation status:`, and a traceId once backend is running.

Expected result: login shell renders, route guard allows authenticated shell navigation, menu shell shows `Workbench`, and home shell renders backend status.

- [ ] **Step 10: Commit final Phase 1 checkpoint**

Run after all QA steps pass:

```bash
git status --short
git add pom.xml och-common och-infra och-security och-api docker .env.example waihu-app
git commit -m "feat: complete phase 1 foundation"
```

Expected output contains `feat: complete phase 1 foundation`. `git status --short` before staging must not show unrelated files; if it does, stage only the Phase 1 files listed in this plan.

## Acceptance Gate

Phase 1 is accepted only when backend starts, frontend starts, login shell renders, OpenAPI is reachable, traceId appears in one request log and response, RabbitMQ publishes and consumes one test event, Redis read/write works, and database migration runs from a clean database.

## Scope Coverage Checklist

- Maven module foundation: Task 1.
- Unified response: Task 2.
- Unified exception handling: Task 2.
- traceId: Tasks 2, 3, and 10.
- OpenAPI: Task 3.
- Permission interception skeleton: Task 4.
- Audit skeleton: Task 4.
- RabbitMQ event abstraction: Tasks 5, 6, and 10.
- Redis wrapper: Tasks 5, 6, and 10.
- File storage abstraction: Tasks 5 and 6.
- Migration mechanism: Tasks 5, 6, and 10.
- Vue 3 app structure: Task 7.
- Route permission shell: Task 7.
- Menu shell: Task 8.
- Request wrapper: Task 7.
- WebSocket wrapper: Task 7.
- Pinia state conventions: Task 7.
- Layout shell: Task 8.
- Table/form/detail drawer patterns: Task 8.
- Login and home shell: Task 8.
- Local startup and environment variables: Task 9.
- Foundation test gates: Task 10.
