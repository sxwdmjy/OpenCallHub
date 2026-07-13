# openCallHub Phase 2 Permission and Resource Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Phase 2 system permission and resource management so administrators can manage users, departments, roles, menus, button permissions, data scopes, audit logs, and all call/AI resources with dependency-safe operations.

**Architecture:** Phase 2 builds on the Phase 1 foundation modules without depending on later call execution phases. `och-system` owns identity, department, role, menu, button permission, data scope, login log, and operation audit domains; `och-resource` owns SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, AI plugin resources, resource dependencies, and resource audit. Frontend pages under `waihu-app/src/views/system` and `waihu-app/src/views/resource` consume standardized REST APIs, permission metadata, data-scope filters, dependency-check results, and audit panels.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Web MVC, Spring Security, Flyway, MySQL, JUnit 5, Mockito, Spring Boot Test, Vue 3, TypeScript, Vite, Vue Router, Pinia, Axios, Vitest, Playwright, Ant Design Vue.

---

## Agentic Worker Note

This plan is implementation guidance only. Execute it after Phase 1 exists and passes its acceptance gate. Use an isolated worktree at execution time, do not modify later-phase domains, and do not mix unrelated source changes into the commit checkpoints listed below. Commit commands are checkpoints for implementers; this planning task does not run them.

## Goal

Phase 2 implements:

- System permission center: users, departments, roles, menus, button permissions, data scopes, login logs, and operation audit.
- Resource center: SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, AI plugin resources, resource dependency checks, and resource audit.
- Permission and resource frontend pages with dependency-check UI and audit panels.
- Tests proving role menu differences, data scope filtering for two roles, secret masking, resource dependency blocking, and audit records for every resource mutation.

## Architecture

### Backend boundaries

- `och-system` is the system administration bounded context. It persists users, departments, roles, menus, role-menu bindings, role-button permissions, data-scope rules, login logs, and operation audit records. It exposes `/api/system/**` endpoints and supplies permission/data-scope services reused by other modules.
- `och-resource` is the reusable resource inventory bounded context. It persists communication resources, file/storage resources, AI plugin resources, resource dependency edges, and resource audit records. It exposes `/api/resource/**` endpoints and rejects unsafe deletes through a dependency checker before writes occur.
- `och-security` remains the interception boundary from Phase 1 and is extended to load permissions, role codes, data-scope predicates, and masked secret responses from `och-system`.
- Cross-module access direction is `och-api -> och-system/och-resource -> och-common/och-security/och-infra`. `och-system` may expose interfaces used by `och-resource`; `och-resource` must not directly mutate system users or roles.

### Frontend boundaries

- `waihu-app/src/views/system`: permission center pages for users, departments, roles, menus, button permissions, data scopes, login logs, and operation audit.
- `waihu-app/src/views/resource`: resource center pages for SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, AI plugin resources, dependencies, and resource audit.
- `waihu-app/src/services/systemApi.ts` and `waihu-app/src/services/resourceApi.ts`: typed API clients matching backend DTOs.
- `waihu-app/src/stores/permission.ts`: loads menu/button permissions from `/api/system/auth/me` and controls route/menu/button rendering.
- Shared dependency-check UI is `waihu-app/src/components/resource/DependencyCheckDrawer.vue` and is opened before destructive resource operations.

### Role owners

- Product Expert: resource operations and admin role path
- Architect: resource dependency model and permission boundary
- Backend Expert: system/resource domain services and transactions
- Frontend Expert: permission center and resource center pages
- Communications Expert: SIP, trunk, FreeSWITCH, Kamailio resource semantics
- AI Voice Expert: AI plugin resource semantics
- Security Expert: data scope, audit, secret masking
- QA Expert: permission and dependency regression cases

## Tech Stack

- Backend runtime: Java 17 and Spring Boot from Phase 1.
- Backend persistence: Flyway SQL migrations and MySQL tables with common `id`, `tenant_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `status`, and `remark` columns.
- Backend tests: JUnit 5, Mockito, Spring Boot Test, MockMvc, and repository integration tests against the Phase 1 test database setup.
- Frontend runtime: Vue 3, TypeScript, Vue Router, Pinia, Axios, Ant Design Vue, and Phase 1 layout/pattern components.
- Frontend tests: Vitest for stores/components/API adapters and Playwright for role-menu/data-scope/dependency regression scenarios.

## File Structure

### Backend files to create

- `och-system/src/main/resources/db/migration/V2__system_permission_schema.sql`: users, departments, roles, menus, button permissions, data scopes, login logs, operation audit, seed admin/operator/agent roles, seed console menus.
- `och-resource/src/main/resources/db/migration/V3__resource_schema.sql`: SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, AI plugin resources, dependency edges, resource audit, seed sample resources.
- `och-system/src/main/java/com/opencallhub/system/domain/user/User.java`: user aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/dept/Department.java`: department aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/role/Role.java`: role aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/menu/Menu.java`: menu aggregate containing route, component, permission code, and visible flag.
- `och-system/src/main/java/com/opencallhub/system/domain/permission/ButtonPermission.java`: button permission aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/scope/DataScopeRule.java`: role data-scope aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/audit/OperationAudit.java`: operation audit aggregate.
- `och-system/src/main/java/com/opencallhub/system/domain/login/LoginLog.java`: login log aggregate.
- `och-system/src/main/java/com/opencallhub/system/application/auth/AuthApplicationService.java`: login log creation and current-user permission DTO.
- `och-system/src/main/java/com/opencallhub/system/application/user/UserApplicationService.java`: user CRUD, role assignment, department assignment, data-scope-aware list query.
- `och-system/src/main/java/com/opencallhub/system/application/dept/DepartmentApplicationService.java`: department tree CRUD.
- `och-system/src/main/java/com/opencallhub/system/application/role/RoleApplicationService.java`: role CRUD, role-menu assignment, role-button assignment, data-scope assignment.
- `och-system/src/main/java/com/opencallhub/system/application/menu/MenuApplicationService.java`: menu tree CRUD and role menu projection.
- `och-system/src/main/java/com/opencallhub/system/application/audit/OperationAuditService.java`: append/query operation audit.
- `och-system/src/main/java/com/opencallhub/system/application/scope/DataScopeService.java`: resolves `ALL`, `DEPT_TREE`, `DEPT_ONLY`, and `SELF` filters.
- `och-system/src/main/java/com/opencallhub/system/api/SystemAuthController.java`: `/api/system/auth/login`, `/api/system/auth/me`, and `/api/system/auth/logout`.
- `och-system/src/main/java/com/opencallhub/system/api/UserController.java`: `/api/system/users` CRUD and assignment APIs.
- `och-system/src/main/java/com/opencallhub/system/api/DepartmentController.java`: `/api/system/departments` CRUD/tree APIs.
- `och-system/src/main/java/com/opencallhub/system/api/RoleController.java`: `/api/system/roles` CRUD and binding APIs.
- `och-system/src/main/java/com/opencallhub/system/api/MenuController.java`: `/api/system/menus` CRUD/tree APIs.
- `och-system/src/main/java/com/opencallhub/system/api/AuditController.java`: `/api/system/login-logs` and `/api/system/operation-audits` query APIs.
- `och-resource/src/main/java/com/opencallhub/resource/domain/ResourceType.java`: enum for `SIP_ACCOUNT`, `EXTENSION`, `PHONE_NUMBER`, `NUMBER_POOL`, `TRUNK_GATEWAY`, `FREESWITCH_NODE`, `KAMAILIO_NODE`, `AUDIO_ASSET`, `STORAGE_CONFIG`, and `AI_PLUGIN`.
- `och-resource/src/main/java/com/opencallhub/resource/domain/sip/SipAccount.java`: SIP account resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/extension/Extension.java`: extension resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/number/PhoneNumber.java`: phone number resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/number/NumberPool.java`: number pool resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/trunk/TrunkGateway.java`: SIP trunk gateway resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/node/FreeSwitchNode.java`: FreeSWITCH node resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/node/KamailioNode.java`: Kamailio node resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/audio/AudioAsset.java`: audio asset metadata resource.
- `och-resource/src/main/java/com/opencallhub/resource/domain/storage/StorageConfig.java`: file/object storage config resource with masked secret fields.
- `och-resource/src/main/java/com/opencallhub/resource/domain/ai/AiPluginResource.java`: ASR/TTS/LLM/Workflow plugin resource with masked secret fields.
- `och-resource/src/main/java/com/opencallhub/resource/domain/dependency/ResourceDependency.java`: dependency edge aggregate.
- `och-resource/src/main/java/com/opencallhub/resource/domain/audit/ResourceAudit.java`: resource audit aggregate.
- `och-resource/src/main/java/com/opencallhub/resource/application/ResourceDependencyService.java`: creates dependency edges, checks blockers, returns dependency graph.
- `och-resource/src/main/java/com/opencallhub/resource/application/ResourceAuditService.java`: writes audit records for mutations.
- `och-resource/src/main/java/com/opencallhub/resource/application/SipResourceService.java`: SIP account and extension transactions.
- `och-resource/src/main/java/com/opencallhub/resource/application/NumberResourceService.java`: phone number and number pool transactions.
- `och-resource/src/main/java/com/opencallhub/resource/application/TrunkGatewayService.java`: trunk gateway transactions and secret masking.
- `och-resource/src/main/java/com/opencallhub/resource/application/NodeResourceService.java`: FreeSWITCH and Kamailio node transactions.
- `och-resource/src/main/java/com/opencallhub/resource/application/AudioAssetService.java`: audio asset metadata transactions.
- `och-resource/src/main/java/com/opencallhub/resource/application/StorageConfigService.java`: storage config transactions and secret masking.
- `och-resource/src/main/java/com/opencallhub/resource/application/AiPluginResourceService.java`: AI plugin config transactions and secret masking.
- `och-resource/src/main/java/com/opencallhub/resource/api/SipResourceController.java`: `/api/resource/sip-accounts` and `/api/resource/extensions` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/NumberResourceController.java`: `/api/resource/phone-numbers` and `/api/resource/number-pools` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/TrunkGatewayController.java`: `/api/resource/trunk-gateways` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/NodeResourceController.java`: `/api/resource/freeswitch-nodes` and `/api/resource/kamailio-nodes` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/AudioAssetController.java`: `/api/resource/audio-assets` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/StorageConfigController.java`: `/api/resource/storage-configs` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/AiPluginResourceController.java`: `/api/resource/ai-plugins` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/ResourceDependencyController.java`: `/api/resource/dependencies/check` and `/api/resource/dependencies/graph` APIs.
- `och-resource/src/main/java/com/opencallhub/resource/api/ResourceAuditController.java`: `/api/resource/audits` APIs.

### Backend test files to create

- `och-system/src/test/java/com/opencallhub/system/application/MenuPermissionServiceTest.java`: proves role menu projections differ by role.
- `och-system/src/test/java/com/opencallhub/system/application/DataScopeServiceTest.java`: proves supervisor and agent filters differ.
- `och-system/src/test/java/com/opencallhub/system/application/OperationAuditServiceTest.java`: proves system mutations append audit records.
- `och-system/src/test/java/com/opencallhub/system/api/SystemPermissionControllerTest.java`: MockMvc contract tests for auth, role menus, and button permissions.
- `och-resource/src/test/java/com/opencallhub/resource/application/ResourceDependencyServiceTest.java`: proves blocker detection for unsafe deletion.
- `och-resource/src/test/java/com/opencallhub/resource/application/ResourceMutationAuditTest.java`: proves every resource create/update/delete writes audit.
- `och-resource/src/test/java/com/opencallhub/resource/application/SecretMaskingTest.java`: proves trunk, storage, and AI plugin secrets are never returned raw.
- `och-resource/src/test/java/com/opencallhub/resource/api/ResourceControllerTest.java`: MockMvc contract tests for resource CRUD, dependency check, and audit query.

### Backend files to modify

- `pom.xml`: add `och-resource` module after `och-system` if Phase 1 did not create it.
- `och-api/pom.xml`: add dependencies on `och-system` and `och-resource`.
- `och-system/pom.xml`: add dependencies on `och-common`, `och-security`, `och-infra`, Spring Web, Validation, JDBC/JPA or MyBatis used by Phase 1, MySQL runtime, and test dependencies.
- `och-resource/pom.xml`: add dependencies on `och-common`, `och-security`, `och-system`, `och-infra`, Spring Web, Validation, JDBC/JPA or MyBatis used by Phase 1, MySQL runtime, and test dependencies.
- `och-security/src/main/java/com/opencallhub/security/permission/PermissionContext.java`: replace Phase 1 in-memory permission source with current-user permission loading contract.
- `och-security/src/main/java/com/opencallhub/security/audit/AuditAspect.java`: route audited system/resource operations to durable audit services.
- `och-api/src/main/resources/application.yml`: include `och-system` and `och-resource` migration locations if Flyway does not already scan module classpaths.

### Frontend files to create

- `waihu-app/src/types/system.ts`: typed DTOs for user, department, role, menu, button permission, data scope, login log, and operation audit.
- `waihu-app/src/types/resource.ts`: typed DTOs for every resource, dependency check, dependency graph, and resource audit.
- `waihu-app/src/services/systemApi.ts`: typed client for `/api/system/**`.
- `waihu-app/src/services/resourceApi.ts`: typed client for `/api/resource/**`.
- `waihu-app/src/stores/resource.ts`: resource list filters, active dependency check, and audit drawer state.
- `waihu-app/src/components/permission/PermissionButton.vue`: hides or disables buttons based on loaded button permissions.
- `waihu-app/src/components/permission/DataScopeTag.vue`: displays effective data scope on list pages.
- `waihu-app/src/components/resource/DependencyCheckDrawer.vue`: destructive-operation preflight drawer.
- `waihu-app/src/components/resource/ResourceAuditTimeline.vue`: audit timeline for resource detail drawers.
- `waihu-app/src/components/resource/SecretMaskField.vue`: masked secret display and write-only input.
- `waihu-app/src/views/system/UserListView.vue`: user list, create/edit drawer, role/department assignment.
- `waihu-app/src/views/system/DepartmentTreeView.vue`: department tree and edit drawer.
- `waihu-app/src/views/system/RoleListView.vue`: role list and role detail.
- `waihu-app/src/views/system/RolePermissionView.vue`: role menu/button/data-scope assignment page.
- `waihu-app/src/views/system/MenuListView.vue`: menu tree and button permission editor.
- `waihu-app/src/views/system/LoginLogView.vue`: login log query page.
- `waihu-app/src/views/system/OperationAuditView.vue`: operation audit query page.
- `waihu-app/src/views/resource/SipAccountView.vue`: SIP account list and edit drawer.
- `waihu-app/src/views/resource/ExtensionView.vue`: extension list and binding drawer.
- `waihu-app/src/views/resource/PhoneNumberView.vue`: phone number list and edit drawer.
- `waihu-app/src/views/resource/NumberPoolView.vue`: number pool list and phone-number assignment.
- `waihu-app/src/views/resource/TrunkGatewayView.vue`: trunk gateway list and masked credential drawer.
- `waihu-app/src/views/resource/FreeSwitchNodeView.vue`: FreeSWITCH node list and health metadata form.
- `waihu-app/src/views/resource/KamailioNodeView.vue`: Kamailio node list and SIP routing metadata form.
- `waihu-app/src/views/resource/AudioAssetView.vue`: audio asset list, upload metadata form, and usage panel.
- `waihu-app/src/views/resource/StorageConfigView.vue`: storage config list and masked credential drawer.
- `waihu-app/src/views/resource/AiPluginResourceView.vue`: AI plugin resource list, masked credential drawer, provider test action.
- `waihu-app/src/views/resource/ResourceAuditView.vue`: resource audit query page.

### Frontend files to modify

- `waihu-app/src/router/routes.ts`: add system and resource grouped route records with permission metadata.
- `waihu-app/src/router/index.ts`: enforce route permission metadata using the Phase 2 permission store.
- `waihu-app/src/stores/permission.ts`: load menus, route permissions, button permissions, and data-scope summary from `/api/system/auth/me`.
- `waihu-app/src/components/menu/AppMenu.vue`: render backend menu tree and hide unauthorized menus.

### Frontend test files to create

- `waihu-app/src/tests/permission-store.spec.ts`: proves menus and buttons differ by role.
- `waihu-app/src/tests/dependency-check-drawer.spec.ts`: proves blockers are rendered and delete confirmation is disabled.
- `waihu-app/src/tests/secret-mask-field.spec.ts`: proves secrets render as masked values and submit only when changed.
- `waihu-app/e2e/phase2-permission-resource.spec.ts`: Playwright regression for admin/operator role menus, data-scope list filtering, resource configuration, dependency-blocked delete, and audit visibility.

## Bite-Sized Tasks

### Task 1: Phase 2 Module Wiring and Migrations

**Owner:** Architect

**Files:**
- Modify: `pom.xml`
- Modify: `och-api/pom.xml`
- Modify: `och-system/pom.xml`
- Create: `och-resource/pom.xml`
- Create: `och-system/src/main/resources/db/migration/V2__system_permission_schema.sql`
- Create: `och-resource/src/main/resources/db/migration/V3__resource_schema.sql`
- Test: migration validation command output

- [ ] **Step 1: Verify Phase 1 acceptance artifacts exist**

Run from `e:/workspace/openCallHub`:

```bash
mvn -pl och-api -am test -DskipTests=false
```

Expected output contains `BUILD SUCCESS`. If it fails because Phase 1 classes are missing, stop and finish Phase 1 before starting this plan.

- [ ] **Step 2: Add or verify `och-resource` in the Maven reactor**

Modify `pom.xml` so the `<modules>` section contains `och-system` followed by `och-resource`:

```xml
<module>och-system</module>
<module>och-resource</module>
```

Expected result: `mvn -q help:evaluate -Dexpression=project.modules -DforceStdout` prints both `och-system` and `och-resource`.

- [ ] **Step 3: Create `och-resource/pom.xml`**

Create `och-resource/pom.xml`:

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
    <artifactId>och-resource</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-security</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-system</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.opencallhub</groupId>
            <artifactId>och-infra</artifactId>
            <version>${project.version}</version>
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

- [ ] **Step 4: Update API/system dependencies**

In `och-api/pom.xml`, add:

```xml
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-system</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.opencallhub</groupId>
    <artifactId>och-resource</artifactId>
    <version>${project.version}</version>
</dependency>
```

In `och-system/pom.xml`, add dependencies on `och-common`, `och-security`, and `och-infra` with `${project.version}`.

Expected result: `mvn -pl och-resource -am test -DskipTests` resolves reactor dependencies without `Could not find artifact` errors.

- [ ] **Step 5: Create system permission migration**

Create `och-system/src/main/resources/db/migration/V2__system_permission_schema.sql` with tables named exactly:

```sql
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    parent_id BIGINT NOT NULL DEFAULT 0,
    dept_name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(64) NOT NULL,
    path VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_department_code (tenant_id, dept_code, deleted),
    KEY idx_sys_department_parent (tenant_id, parent_id, deleted)
);

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(120),
    dept_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_user_username (tenant_id, username, deleted),
    KEY idx_sys_user_dept (tenant_id, dept_id, deleted)
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_role_code (tenant_id, role_code, deleted)
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_role (tenant_id, user_id, role_id)
);

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    route_path VARCHAR(200) NOT NULL,
    component_path VARCHAR(200) NOT NULL,
    permission_code VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_menu_permission (tenant_id, permission_code, deleted),
    KEY idx_sys_menu_parent (tenant_id, parent_id, deleted)
);

CREATE TABLE sys_button_permission (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    menu_id BIGINT NOT NULL,
    button_code VARCHAR(120) NOT NULL,
    button_name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_button_code (tenant_id, button_code, deleted),
    KEY idx_sys_button_menu (tenant_id, menu_id, deleted)
);

CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_role_menu (tenant_id, role_id, menu_id)
);

CREATE TABLE sys_role_button (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    role_id BIGINT NOT NULL,
    button_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_role_button (tenant_id, role_id, button_id)
);

CREATE TABLE sys_data_scope_rule (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    role_id BIGINT NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    dept_ids VARCHAR(1000) NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_data_scope_role (tenant_id, role_id, deleted)
);

CREATE TABLE sys_login_log (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    user_id BIGINT,
    username VARCHAR(64) NOT NULL,
    login_result VARCHAR(32) NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    trace_id VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sys_login_user_time (tenant_id, username, created_at)
);

CREATE TABLE sys_operation_audit (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    operator_id BIGINT,
    operator_name VARCHAR(100),
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    before_json JSON,
    after_json JSON,
    result VARCHAR(32) NOT NULL,
    trace_id VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sys_operation_target (tenant_id, target_type, target_id, created_at),
    KEY idx_sys_operation_operator (tenant_id, operator_id, created_at)
);
```

Append deterministic local/test seed data to the same migration. These seed rows are required by backend tests and Playwright E2E. Password values below are BCrypt hashes, not plaintext. Use the project password encoder locally; if the encoder output differs because BCrypt salt is regenerated, configure test login to accept the deterministic fixture hashes shown here or replace both hashes with encoder-generated hashes for `admin123` and `agent123` in a fixture-only local/test migration. Do not store plaintext passwords in production seed rows.

```sql
INSERT INTO sys_department (id, tenant_id, parent_id, dept_name, dept_code, path, status, created_by, updated_by) VALUES
(10, 1, 0, 'Head Office', 'HQ', '/10', 'ENABLED', 1, 1),
(20, 1, 10, 'Operations Center', 'OPS', '/10/20', 'ENABLED', 1, 1),
(21, 1, 20, 'Agent Team A', 'AGENT_A', '/10/20/21', 'ENABLED', 1, 1),
(22, 1, 20, 'Agent Team B', 'AGENT_B', '/10/20/22', 'ENABLED', 1, 1);

INSERT INTO sys_user (id, tenant_id, username, display_name, password_hash, phone, email, dept_id, status, remark, created_by, updated_by) VALUES
(1, 1, 'admin', 'System Administrator', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiZr5Upy7xg7p3e3KQk9JmY9xZ0iM7e', '13800000001', 'admin@opencallhub.local', 10, 'ENABLED', 'Local/test fixture password is admin123', 1, 1),
(9, 1, 'agent01', 'Agent One', '$2a$10$8K1p/a0dL1LXMIgoEDFrwO1RkP9x6n2jVzJjPjT3fBq3KqXWjM0uG', '13800000009', 'agent01@opencallhub.local', 21, 'ENABLED', 'Local/test fixture password is agent123', 1, 1);

INSERT INTO sys_role (id, tenant_id, role_code, role_name, status, remark, created_by, updated_by) VALUES
(1, 1, 'ADMIN', 'Administrator', 'ENABLED', 'Full platform administration', 1, 1),
(2, 1, 'OPERATOR', 'Operations Supervisor', 'ENABLED', 'Operations and resource management with department-tree data scope', 1, 1),
(3, 1, 'AGENT', 'Agent', 'ENABLED', 'Self-service agent console access', 1, 1);

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_by) VALUES
(1, 1, 1, 1, 1),
(2, 1, 9, 3, 1);

INSERT INTO sys_menu (id, tenant_id, parent_id, menu_name, menu_type, route_path, component_path, permission_code, sort_order, visible, status, created_by, updated_by) VALUES
(100, 1, 0, 'System', 'DIRECTORY', '/system', 'Layout', 'system:view', 10, 1, 'ENABLED', 1, 1),
(101, 1, 100, 'Users', 'MENU', '/system/users', 'system/UserListView', 'system:user:view', 11, 1, 'ENABLED', 1, 1),
(102, 1, 100, 'Departments', 'MENU', '/system/departments', 'system/DepartmentTreeView', 'system:department:view', 12, 1, 'ENABLED', 1, 1),
(103, 1, 100, 'Roles', 'MENU', '/system/roles', 'system/RoleListView', 'system:role:view', 13, 1, 'ENABLED', 1, 1),
(104, 1, 100, 'Role Permissions', 'MENU', '/system/role-permissions', 'system/RolePermissionView', 'system:permission:view', 14, 1, 'ENABLED', 1, 1),
(105, 1, 100, 'Menus', 'MENU', '/system/menus', 'system/MenuListView', 'system:menu:view', 15, 1, 'ENABLED', 1, 1),
(106, 1, 100, 'Login Logs', 'MENU', '/system/login-logs', 'system/LoginLogView', 'system:login-log:view', 16, 1, 'ENABLED', 1, 1),
(107, 1, 100, 'Operation Audit', 'MENU', '/system/operation-audits', 'system/OperationAuditView', 'system:operation-audit:view', 17, 1, 'ENABLED', 1, 1),
(200, 1, 0, 'Resources', 'DIRECTORY', '/resource', 'Layout', 'resource:view', 20, 1, 'ENABLED', 1, 1),
(201, 1, 200, 'SIP Accounts', 'MENU', '/resource/sip-accounts', 'resource/SipAccountView', 'resource:sip:view', 21, 1, 'ENABLED', 1, 1),
(202, 1, 200, 'Extensions', 'MENU', '/resource/extensions', 'resource/ExtensionView', 'resource:extension:view', 22, 1, 'ENABLED', 1, 1),
(203, 1, 200, 'Phone Numbers', 'MENU', '/resource/phone-numbers', 'resource/PhoneNumberView', 'resource:phone-number:view', 23, 1, 'ENABLED', 1, 1),
(204, 1, 200, 'Number Pools', 'MENU', '/resource/number-pools', 'resource/NumberPoolView', 'resource:number-pool:view', 24, 1, 'ENABLED', 1, 1),
(205, 1, 200, 'Trunk Gateways', 'MENU', '/resource/trunk-gateways', 'resource/TrunkGatewayView', 'resource:trunk:view', 25, 1, 'ENABLED', 1, 1),
(206, 1, 200, 'FreeSWITCH Nodes', 'MENU', '/resource/freeswitch-nodes', 'resource/FreeSwitchNodeView', 'resource:freeswitch:view', 26, 1, 'ENABLED', 1, 1),
(207, 1, 200, 'Kamailio Nodes', 'MENU', '/resource/kamailio-nodes', 'resource/KamailioNodeView', 'resource:kamailio:view', 27, 1, 'ENABLED', 1, 1),
(208, 1, 200, 'Audio Assets', 'MENU', '/resource/audio-assets', 'resource/AudioAssetView', 'resource:audio:view', 28, 1, 'ENABLED', 1, 1),
(209, 1, 200, 'Storage Configs', 'MENU', '/resource/storage-configs', 'resource/StorageConfigView', 'resource:storage:view', 29, 1, 'ENABLED', 1, 1),
(210, 1, 200, 'AI Plugins', 'MENU', '/resource/ai-plugins', 'resource/AiPluginResourceView', 'resource:ai-plugin:view', 30, 1, 'ENABLED', 1, 1),
(211, 1, 200, 'Resource Audit', 'MENU', '/resource/audits', 'resource/ResourceAuditView', 'resource:audit:view', 31, 1, 'ENABLED', 1, 1),
(300, 1, 0, 'Agent Home', 'MENU', '/agent/home', 'agent/HomeView', 'agent:home:view', 40, 1, 'ENABLED', 1, 1);

INSERT INTO sys_button_permission (id, tenant_id, menu_id, button_code, button_name, status, created_by, updated_by) VALUES
(1001, 1, 101, 'system:user:create', 'Create User', 'ENABLED', 1, 1),
(1002, 1, 101, 'system:user:update', 'Update User', 'ENABLED', 1, 1),
(1003, 1, 101, 'system:user:delete', 'Delete User', 'ENABLED', 1, 1),
(1004, 1, 103, 'system:role:assign', 'Assign Role', 'ENABLED', 1, 1),
(1005, 1, 104, 'system:permission:save', 'Save Permissions', 'ENABLED', 1, 1),
(2001, 1, 205, 'resource:trunk:create', 'Create Trunk Gateway', 'ENABLED', 1, 1),
(2002, 1, 205, 'resource:trunk:update', 'Update Trunk Gateway', 'ENABLED', 1, 1),
(2003, 1, 205, 'resource:trunk:delete', 'Delete Trunk Gateway', 'ENABLED', 1, 1),
(2004, 1, 203, 'resource:phone-number:delete', 'Delete Phone Number', 'ENABLED', 1, 1),
(2005, 1, 210, 'resource:ai-plugin:create', 'Create AI Plugin', 'ENABLED', 1, 1),
(3001, 1, 300, 'agent:home:view', 'View Agent Home', 'ENABLED', 1, 1);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, created_by) VALUES
(1, 1, 1, 100, 1), (2, 1, 1, 101, 1), (3, 1, 1, 102, 1), (4, 1, 1, 103, 1), (5, 1, 1, 104, 1), (6, 1, 1, 105, 1), (7, 1, 1, 106, 1), (8, 1, 1, 107, 1),
(9, 1, 1, 200, 1), (10, 1, 1, 201, 1), (11, 1, 1, 202, 1), (12, 1, 1, 203, 1), (13, 1, 1, 204, 1), (14, 1, 1, 205, 1), (15, 1, 1, 206, 1), (16, 1, 1, 207, 1), (17, 1, 1, 208, 1), (18, 1, 1, 209, 1), (19, 1, 1, 210, 1), (20, 1, 1, 211, 1),
(21, 1, 2, 200, 1), (22, 1, 2, 201, 1), (23, 1, 2, 202, 1), (24, 1, 2, 203, 1), (25, 1, 2, 204, 1), (26, 1, 2, 205, 1), (27, 1, 2, 211, 1),
(28, 1, 3, 300, 1);

INSERT INTO sys_role_button (id, tenant_id, role_id, button_id, created_by) VALUES
(1, 1, 1, 1001, 1), (2, 1, 1, 1002, 1), (3, 1, 1, 1003, 1), (4, 1, 1, 1004, 1), (5, 1, 1, 1005, 1),
(6, 1, 1, 2001, 1), (7, 1, 1, 2002, 1), (8, 1, 1, 2003, 1), (9, 1, 1, 2004, 1), (10, 1, 1, 2005, 1),
(11, 1, 2, 2001, 1), (12, 1, 2, 2002, 1), (13, 1, 2, 2004, 1),
(14, 1, 3, 3001, 1);

INSERT INTO sys_data_scope_rule (id, tenant_id, role_id, scope_type, dept_ids, status, remark, created_by, updated_by) VALUES
(1, 1, 1, 'ALL', '', 'ENABLED', 'Administrators can see all tenant data', 1, 1),
(2, 1, 2, 'DEPT_TREE', '20,21,22', 'ENABLED', 'Operators can see operations department tree', 1, 1),
(3, 1, 3, 'SELF', '', 'ENABLED', 'Agents can see only their own data', 1, 1);
```

- [ ] **Step 6: Create resource inventory migration**

Create `och-resource/src/main/resources/db/migration/V3__resource_schema.sql` with tables named exactly:

```sql
CREATE TABLE res_sip_account (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    sip_username VARCHAR(64) NOT NULL,
    auth_realm VARCHAR(120) NOT NULL,
    auth_password_cipher VARCHAR(500) NOT NULL,
    display_name VARCHAR(100),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_sip_username (tenant_id, sip_username, deleted)
);

CREATE TABLE res_extension (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    extension_no VARCHAR(32) NOT NULL,
    sip_account_id BIGINT NOT NULL,
    user_id BIGINT,
    dept_id BIGINT,
    register_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_extension_no (tenant_id, extension_no, deleted),
    KEY idx_res_extension_user (tenant_id, user_id, deleted)
);

CREATE TABLE res_number_pool (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    pool_code VARCHAR(64) NOT NULL,
    pool_name VARCHAR(100) NOT NULL,
    dept_id BIGINT,
    concurrency_limit INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_number_pool_code (tenant_id, pool_code, deleted)
);

CREATE TABLE res_phone_number (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    phone_number VARCHAR(32) NOT NULL,
    direction VARCHAR(32) NOT NULL,
    number_pool_id BIGINT,
    trunk_gateway_id BIGINT,
    concurrency_limit INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_phone_number (tenant_id, phone_number, deleted),
    KEY idx_res_phone_pool (tenant_id, number_pool_id, deleted)
);

CREATE TABLE res_trunk_gateway (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    gateway_code VARCHAR(64) NOT NULL,
    gateway_name VARCHAR(100) NOT NULL,
    sip_server VARCHAR(120) NOT NULL,
    sip_port INT NOT NULL DEFAULT 5060,
    transport VARCHAR(16) NOT NULL DEFAULT 'UDP',
    auth_username VARCHAR(120),
    auth_password_cipher VARCHAR(500),
    max_concurrency INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_trunk_code (tenant_id, gateway_code, deleted)
);

CREATE TABLE res_freeswitch_node (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    node_code VARCHAR(64) NOT NULL,
    host VARCHAR(120) NOT NULL,
    esl_port INT NOT NULL DEFAULT 8021,
    esl_password_cipher VARCHAR(500) NOT NULL,
    weight INT NOT NULL DEFAULT 100,
    max_channels INT NOT NULL DEFAULT 0,
    health_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_fs_node_code (tenant_id, node_code, deleted)
);

CREATE TABLE res_kamailio_node (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    node_code VARCHAR(64) NOT NULL,
    host VARCHAR(120) NOT NULL,
    sip_port INT NOT NULL DEFAULT 5060,
    transport VARCHAR(16) NOT NULL DEFAULT 'UDP',
    health_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_kam_node_code (tenant_id, node_code, deleted)
);

CREATE TABLE res_audio_asset (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    asset_code VARCHAR(64) NOT NULL,
    asset_name VARCHAR(100) NOT NULL,
    audio_type VARCHAR(32) NOT NULL,
    storage_uri VARCHAR(500) NOT NULL,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_audio_code (tenant_id, asset_code, deleted)
);

CREATE TABLE res_storage_config (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    config_code VARCHAR(64) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    bucket_name VARCHAR(120) NOT NULL,
    access_key_cipher VARCHAR(500),
    secret_key_cipher VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_storage_code (tenant_id, config_code, deleted)
);

CREATE TABLE res_ai_plugin_resource (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    plugin_code VARCHAR(64) NOT NULL,
    plugin_name VARCHAR(100) NOT NULL,
    plugin_type VARCHAR(32) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    endpoint VARCHAR(300) NOT NULL,
    api_key_cipher VARCHAR(500),
    model_name VARCHAR(120),
    timeout_ms INT NOT NULL DEFAULT 5000,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_ai_plugin_code (tenant_id, plugin_code, deleted)
);

CREATE TABLE res_dependency (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    source_type VARCHAR(64) NOT NULL,
    source_id BIGINT NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id BIGINT NOT NULL,
    dependency_kind VARCHAR(64) NOT NULL,
    description VARCHAR(300) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_res_dependency (tenant_id, source_type, source_id, target_type, target_id, dependency_kind, deleted),
    KEY idx_res_dependency_target (tenant_id, target_type, target_id, deleted)
);

CREATE TABLE res_audit (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    operator_id BIGINT,
    operator_name VARCHAR(100),
    resource_type VARCHAR(64) NOT NULL,
    resource_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    before_json JSON,
    after_json JSON,
    result VARCHAR(32) NOT NULL,
    trace_id VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_res_audit_resource (tenant_id, resource_type, resource_id, created_at),
    KEY idx_res_audit_operator (tenant_id, operator_id, created_at)
);
```

Append local/test seed resources required by backend tests and Playwright E2E. Cipher columns use deterministic fixture ciphertext strings to exercise masking without storing raw secrets. These rows create dependency blockers so unsafe deletes can be tested before later call-flow phases exist.

```sql
INSERT INTO res_freeswitch_node (id, tenant_id, node_code, host, esl_port, esl_password_cipher, weight, max_channels, health_status, status, created_by, updated_by) VALUES
(1, 1, 'fs-main', '10.0.10.10', 8021, 'cipher:local-test-esl-secret', 100, 500, 'UP', 'ENABLED', 1, 1);

INSERT INTO res_kamailio_node (id, tenant_id, node_code, host, sip_port, transport, health_status, status, created_by, updated_by) VALUES
(1, 1, 'kam-main', '10.0.10.20', 5060, 'UDP', 'UP', 'ENABLED', 1, 1);

INSERT INTO res_trunk_gateway (id, tenant_id, gateway_code, gateway_name, sip_server, sip_port, transport, auth_username, auth_password_cipher, max_concurrency, status, created_by, updated_by) VALUES
(1, 1, 'gw-main', 'Main Carrier', 'sip.carrier.local', 5060, 'UDP', 'carrier', 'cipher:carrier-secret', 100, 'ENABLED', 1, 1);

INSERT INTO res_sip_account (id, tenant_id, sip_username, auth_realm, auth_password_cipher, display_name, status, created_by, updated_by) VALUES
(1, 1, 'sip-agent01', 'opencallhub.local', 'cipher:sip-agent-secret', 'Agent One SIP', 'ENABLED', 1, 1);

INSERT INTO res_extension (id, tenant_id, extension_no, sip_account_id, user_id, dept_id, register_status, status, created_by, updated_by) VALUES
(1, 1, '8001', 1, 9, 21, 'REGISTERED', 'ENABLED', 1, 1);

INSERT INTO res_number_pool (id, tenant_id, pool_code, pool_name, dept_id, concurrency_limit, status, created_by, updated_by) VALUES
(10, 1, 'pool-main-outbound', 'Main Outbound Pool', 20, 200, 'ENABLED', 1, 1);

INSERT INTO res_phone_number (id, tenant_id, phone_number, direction, number_pool_id, trunk_gateway_id, concurrency_limit, status, created_by, updated_by) VALUES
(20, 1, '4008001000', 'BOTH', 10, 1, 50, 'ENABLED', 1, 1);

INSERT INTO res_storage_config (id, tenant_id, config_code, provider, endpoint, bucket_name, access_key_cipher, secret_key_cipher, status, created_by, updated_by) VALUES
(1, 1, 'storage-local', 'LOCAL', 'file:///tmp/opencallhub', 'audio-assets', 'cipher:local-access-key', 'cipher:local-secret-key', 'ENABLED', 1, 1);

INSERT INTO res_audio_asset (id, tenant_id, asset_code, asset_name, audio_type, storage_uri, duration_ms, status, created_by, updated_by) VALUES
(1, 1, 'welcome-main', 'Main Welcome Prompt', 'PROMPT', 'storage://storage-local/welcome-main.wav', 3200, 'ENABLED', 1, 1);

INSERT INTO res_ai_plugin_resource (id, tenant_id, plugin_code, plugin_name, plugin_type, provider, endpoint, api_key_cipher, model_name, timeout_ms, status, created_by, updated_by) VALUES
(1, 1, 'asr-main', 'Main ASR', 'ASR', 'DIFY', 'https://ai.local/asr', 'cipher:ai-secret-key', 'asr-local', 5000, 'ENABLED', 1, 1);

INSERT INTO res_dependency (id, tenant_id, source_type, source_id, target_type, target_id, dependency_kind, description, created_by) VALUES
(1, 1, 'EXTENSION', 1, 'SIP_ACCOUNT', 1, 'USES_SIP_ACCOUNT', 'Extension 8001 uses SIP account sip-agent01', 1),
(2, 1, 'PHONE_NUMBER', 20, 'TRUNK_GATEWAY', 1, 'ROUTES_THROUGH_TRUNK', 'Phone number 4008001000 routes through Main Carrier', 1),
(3, 1, 'NUMBER_POOL', 10, 'PHONE_NUMBER', 20, 'POOL_MEMBER', 'Main outbound pool uses 4008001000', 1),
(4, 1, 'TRUNK_GATEWAY', 1, 'FREESWITCH_NODE', 1, 'HOSTED_BY_FREESWITCH', 'Main Carrier is hosted by FreeSWITCH node fs-main', 1),
(5, 1, 'SIP_ACCOUNT', 1, 'KAMAILIO_NODE', 1, 'REGISTERS_THROUGH_KAMAILIO', 'SIP account sip-agent01 registers through Kamailio node kam-main', 1),
(6, 1, 'AUDIO_ASSET', 1, 'STORAGE_CONFIG', 1, 'STORED_IN_STORAGE', 'Main Welcome Prompt is stored in storage-local', 1),
(7, 1, 'AI_PLUGIN', 1, 'STORAGE_CONFIG', 1, 'USES_STORAGE', 'Main ASR uses storage-local for logs and cached audio', 1);
```

Expected result: migrations define every Phase 2 system/resource table and seed the admin/agent login fixtures plus sample resources in one executable schema path.

- [ ] **Step 7: Validate migrations**

Run:

```bash
mvn -pl och-api -am test -Dtest=MigrationSmokeTest
```

Expected output after adding a Phase 1 or Phase 2 `MigrationSmokeTest` contains `BUILD SUCCESS` and proves Flyway applied `V2__system_permission_schema.sql` and `V3__resource_schema.sql` on a clean database. The smoke test must also assert the seeded rows exist: `sys_user.username IN ('admin', 'agent01')`, `sys_role.role_code IN ('ADMIN', 'OPERATOR', 'AGENT')`, `res_phone_number.phone_number = '4008001000'`, and `res_dependency.description = 'Main outbound pool uses 4008001000'`.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add pom.xml och-api/pom.xml och-system/pom.xml och-resource/pom.xml och-system/src/main/resources/db/migration/V2__system_permission_schema.sql och-resource/src/main/resources/db/migration/V3__resource_schema.sql
git commit -m "feat: add permission and resource schema"
```

Expected output contains `feat: add permission and resource schema`.

### Task 2: System Permission Domain and APIs

**Owner:** Backend Expert

**Files:**
- Create: system domain/application/API files listed in the File Structure section
- Modify: `och-security/src/main/java/com/opencallhub/security/permission/PermissionContext.java`
- Test: `och-system/src/test/java/com/opencallhub/system/application/MenuPermissionServiceTest.java`
- Test: `och-system/src/test/java/com/opencallhub/system/application/DataScopeServiceTest.java`
- Test: `och-system/src/test/java/com/opencallhub/system/api/SystemPermissionControllerTest.java`

- [ ] **Step 1: Write role menu projection test**

Create `och-system/src/test/java/com/opencallhub/system/application/MenuPermissionServiceTest.java`:

```java
package com.opencallhub.system.application;

import com.opencallhub.system.application.menu.MenuApplicationService;
import com.opencallhub.system.domain.menu.Menu;
import com.opencallhub.system.domain.role.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MenuPermissionServiceTest {
    @Test
    void adminAndAgentReceiveDifferentMenuTrees() {
        Menu systemMenu = new Menu(100L, 0L, "System", "/system", "system:view", true);
        Menu resourceMenu = new Menu(200L, 0L, "Resources", "/resource", "resource:view", true);
        Menu agentHome = new Menu(300L, 0L, "Agent Home", "/agent/home", "agent:home:view", true);
        Role admin = new Role(1L, "ADMIN", "Administrator");
        Role agent = new Role(2L, "AGENT", "Agent");
        MenuApplicationService service = MenuApplicationService.inMemoryForTest(
                List.of(systemMenu, resourceMenu, agentHome),
                admin.id(), Set.of(systemMenu.id(), resourceMenu.id()),
                agent.id(), Set.of(agentHome.id())
        );

        assertThat(service.visibleMenusForRole(admin.id())).extracting(Menu::routePath)
                .containsExactly("/system", "/resource");
        assertThat(service.visibleMenusForRole(agent.id())).extracting(Menu::routePath)
                .containsExactly("/agent/home");
    }
}
```

- [ ] **Step 2: Write data scope test for two roles**

Create `och-system/src/test/java/com/opencallhub/system/application/DataScopeServiceTest.java`:

```java
package com.opencallhub.system.application;

import com.opencallhub.system.application.scope.DataScopeFilter;
import com.opencallhub.system.application.scope.DataScopeService;
import com.opencallhub.system.domain.scope.DataScopeRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeServiceTest {
    @Test
    void supervisorSeesDepartmentTreeWhileAgentSeesSelfOnly() {
        DataScopeService service = DataScopeService.inMemoryForTest(List.of(
                new DataScopeRule(10L, 2L, "DEPT_TREE", "20,21,22"),
                new DataScopeRule(11L, 3L, "SELF", "")
        ));

        DataScopeFilter supervisor = service.resolveForUser(100L, 20L, List.of(2L));
        DataScopeFilter agent = service.resolveForUser(101L, 21L, List.of(3L));

        assertThat(supervisor.scopeType()).isEqualTo("DEPT_TREE");
        assertThat(supervisor.deptIds()).containsExactly(20L, 21L, 22L);
        assertThat(agent.scopeType()).isEqualTo("SELF");
        assertThat(agent.userId()).isEqualTo(101L);
    }
}
```

Expected initial result: both tests fail with missing domain/application classes.

- [ ] **Step 3: Implement minimal system domain records**

Create records with these constructors and accessors:

```java
package com.opencallhub.system.domain.menu;

public record Menu(Long id, Long parentId, String menuName, String routePath, String permissionCode, boolean visible) {
}
```

```java
package com.opencallhub.system.domain.role;

public record Role(Long id, String roleCode, String roleName) {
}
```

```java
package com.opencallhub.system.domain.scope;

public record DataScopeRule(Long id, Long roleId, String scopeType, String deptIds) {
}
```

Also create `User`, `Department`, `ButtonPermission`, `LoginLog`, and `OperationAudit` records with fields matching migration columns and package names from the File Structure section.

- [ ] **Step 4: Implement menu and data scope services**

Create `DataScopeFilter` and `DataScopeService`:

```java
package com.opencallhub.system.application.scope;

import java.util.List;

public record DataScopeFilter(String scopeType, Long userId, List<Long> deptIds) {
}
```

```java
package com.opencallhub.system.application.scope;

import com.opencallhub.system.domain.scope.DataScopeRule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataScopeService {
    private final Map<Long, DataScopeRule> rulesByRoleId;

    public DataScopeService(List<DataScopeRule> rules) {
        this.rulesByRoleId = rules.stream().collect(Collectors.toMap(DataScopeRule::roleId, Function.identity()));
    }

    public static DataScopeService inMemoryForTest(List<DataScopeRule> rules) {
        return new DataScopeService(rules);
    }

    public DataScopeFilter resolveForUser(Long userId, Long deptId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            DataScopeRule rule = rulesByRoleId.get(roleId);
            if (rule == null) {
                continue;
            }
            if ("ALL".equals(rule.scopeType())) {
                return new DataScopeFilter("ALL", userId, List.of());
            }
            if ("DEPT_TREE".equals(rule.scopeType()) || "DEPT_ONLY".equals(rule.scopeType())) {
                List<Long> deptIds = Arrays.stream(rule.deptIds().split(","))
                        .filter(value -> !value.isBlank())
                        .map(Long::valueOf)
                        .toList();
                return new DataScopeFilter(rule.scopeType(), userId, deptIds);
            }
            if ("SELF".equals(rule.scopeType())) {
                return new DataScopeFilter("SELF", userId, List.of(deptId));
            }
        }
        return new DataScopeFilter("SELF", userId, List.of(deptId));
    }
}
```

Implement `MenuApplicationService.inMemoryForTest(...)` so it returns only menu IDs assigned to the requested role and excludes `visible=false` menus.

- [ ] **Step 5: Run system domain tests**

Run:

```bash
mvn -pl och-system test -Dtest=MenuPermissionServiceTest,DataScopeServiceTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 6: Implement REST API contracts**

Create controllers with these endpoint contracts:

```text
POST   /api/system/auth/login
GET    /api/system/auth/me
POST   /api/system/auth/logout
GET    /api/system/users
POST   /api/system/users
PUT    /api/system/users/{id}
DELETE /api/system/users/{id}
PUT    /api/system/users/{id}/roles
PUT    /api/system/users/{id}/department
GET    /api/system/departments/tree
POST   /api/system/departments
PUT    /api/system/departments/{id}
DELETE /api/system/departments/{id}
GET    /api/system/roles
POST   /api/system/roles
PUT    /api/system/roles/{id}
DELETE /api/system/roles/{id}
PUT    /api/system/roles/{id}/menus
PUT    /api/system/roles/{id}/buttons
PUT    /api/system/roles/{id}/data-scope
GET    /api/system/menus/tree
POST   /api/system/menus
PUT    /api/system/menus/{id}
DELETE /api/system/menus/{id}
GET    /api/system/login-logs
GET    /api/system/operation-audits
```

Expected response envelope for `GET /api/system/auth/me`:

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "roles": ["ADMIN"],
    "menus": [{"id": 100, "name": "System", "routePath": "/system"}],
    "buttonPermissions": ["system:user:create", "resource:trunk:update"],
    "dataScope": {"scopeType": "ALL", "deptIds": []}
  },
  "traceId": "trace-from-filter"
}
```

- [ ] **Step 7: Write and run MockMvc permission API tests**

Create `SystemPermissionControllerTest` with assertions for:

```java
mockMvc.perform(get("/api/system/auth/me").header("X-Test-User", "admin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"))
        .andExpect(jsonPath("$.data.menus[0].routePath").value("/system"));

mockMvc.perform(get("/api/system/auth/me").header("X-Test-User", "agent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roles[0]").value("AGENT"))
        .andExpect(jsonPath("$.data.menus[0].routePath").value("/agent/home"));
```

Run:

```bash
mvn -pl och-system test -Dtest=SystemPermissionControllerTest
```

Expected output contains `BUILD SUCCESS`. The Maven test summary or Surefire XML reports show `SystemPermissionControllerTest` passed and its assertions prove role menus render differently by API role projection.

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add och-system/src/main/java och-system/src/test/java och-security/src/main/java
git commit -m "feat: add system permission services"
```

Expected output contains `feat: add system permission services`.

### Task 3: Durable Audit, Login Logs, and Security Masking Boundary

**Owner:** Security Expert

**Files:**
- Create: `och-system/src/main/java/com/opencallhub/system/application/audit/OperationAuditService.java`
- Create: `och-system/src/main/java/com/opencallhub/system/application/auth/LoginLogService.java`
- Modify: `och-security/src/main/java/com/opencallhub/security/audit/AuditAspect.java`
- Create: `och-system/src/test/java/com/opencallhub/system/application/OperationAuditServiceTest.java`
- Create: `och-resource/src/test/java/com/opencallhub/resource/application/SecretMaskingTest.java`

- [ ] **Step 1: Write operation audit service test**

Create `OperationAuditServiceTest`:

```java
package com.opencallhub.system.application;

import com.opencallhub.system.application.audit.OperationAuditCommand;
import com.opencallhub.system.application.audit.OperationAuditService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationAuditServiceTest {
    @Test
    void appendRecordsOperatorTargetResultAndTraceId() {
        OperationAuditService service = OperationAuditService.inMemoryForTest();

        service.append(new OperationAuditCommand(1L, "admin", "SYSTEM", "CREATE", "USER", "9", "{}", "{\"username\":\"agent01\"}", "SUCCESS", "trace-audit-1"));

        assertThat(service.queryByTarget("USER", "9")).hasSize(1);
        assertThat(service.queryByTarget("USER", "9").get(0).operatorName()).isEqualTo("admin");
        assertThat(service.queryByTarget("USER", "9").get(0).traceId()).isEqualTo("trace-audit-1");
    }
}
```

- [ ] **Step 2: Implement audit command and service**

Create `OperationAuditCommand` as a record with the test fields. Implement `OperationAuditService.inMemoryForTest()`, `append(OperationAuditCommand)`, and `queryByTarget(String targetType, String targetId)`. The database-backed implementation must insert into `sys_operation_audit` inside the same transaction as the audited mutation when called by application services.

- [ ] **Step 3: Write secret masking test**

Create `SecretMaskingTest`:

```java
package com.opencallhub.resource.application;

import com.opencallhub.resource.application.security.SecretMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretMaskingTest {
    @Test
    void masksTrunkStorageAndAiPluginSecrets() {
        SecretMasker masker = new SecretMasker();

        assertThat(masker.mask("abc123456789")).isEqualTo("abc********789");
        assertThat(masker.mask("short")).isEqualTo("******");
        assertThat(masker.mask(null)).isNull();
    }
}
```

- [ ] **Step 4: Implement `SecretMasker`**

Create `och-resource/src/main/java/com/opencallhub/resource/application/security/SecretMasker.java`:

```java
package com.opencallhub.resource.application.security;

public class SecretMasker {
    public String mask(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() < 8) {
            return "******";
        }
        return value.substring(0, 3) + "********" + value.substring(value.length() - 3);
    }
}
```

All resource response DTOs for `auth_password`, `auth_password_cipher`, `esl_password`, `esl_password_cipher`, `access_key`, `secret_key`, `api_key`, and `api_key_cipher` must call `SecretMasker.mask(...)` before leaving the service layer.

- [ ] **Step 5: Run audit and masking tests**

Run:

```bash
mvn -pl och-system,och-resource -am test -Dtest=OperationAuditServiceTest,SecretMaskingTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-system/src/main/java och-system/src/test/java och-resource/src/main/java och-resource/src/test/java och-security/src/main/java
git commit -m "feat: add durable audit and secret masking"
```

Expected output contains `feat: add durable audit and secret masking`.

### Task 4: Resource Domain Services and Dependency Checks

**Owner:** Backend Expert

**Files:**
- Create: all `och-resource/src/main/java/com/opencallhub/resource/domain/**` files listed in File Structure
- Create: all `och-resource/src/main/java/com/opencallhub/resource/application/**` service files listed in File Structure
- Create: `och-resource/src/test/java/com/opencallhub/resource/application/ResourceDependencyServiceTest.java`
- Create: `och-resource/src/test/java/com/opencallhub/resource/application/ResourceMutationAuditTest.java`

- [ ] **Step 1: Write resource dependency blocker test**

Create `ResourceDependencyServiceTest`:

```java
package com.opencallhub.resource.application;

import com.opencallhub.resource.domain.ResourceType;
import com.opencallhub.resource.domain.dependency.ResourceDependency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceDependencyServiceTest {
    @Test
    void phoneNumberDeleteIsBlockedWhenNumberPoolDependsOnIt() {
        ResourceDependencyService service = ResourceDependencyService.inMemoryForTest(List.of(
                new ResourceDependency(1L, ResourceType.NUMBER_POOL, 10L, ResourceType.PHONE_NUMBER, 20L, "POOL_MEMBER", "Main outbound pool uses 4008001000")
        ));

        DependencyCheckResult result = service.checkDelete(ResourceType.PHONE_NUMBER, 20L);

        assertThat(result.safe()).isFalse();
        assertThat(result.blockers()).extracting(DependencyBlocker::sourceType).containsExactly(ResourceType.NUMBER_POOL);
        assertThat(result.blockers()).extracting(DependencyBlocker::description).containsExactly("Main outbound pool uses 4008001000");
    }
}
```

- [ ] **Step 2: Implement dependency model and service**

Create:

```java
package com.opencallhub.resource.domain;

public enum ResourceType {
    SIP_ACCOUNT,
    EXTENSION,
    PHONE_NUMBER,
    NUMBER_POOL,
    TRUNK_GATEWAY,
    FREESWITCH_NODE,
    KAMAILIO_NODE,
    AUDIO_ASSET,
    STORAGE_CONFIG,
    AI_PLUGIN
}
```

Create `ResourceDependency`, `DependencyBlocker`, and `DependencyCheckResult` records. Implement `ResourceDependencyService.checkDelete(ResourceType targetType, Long targetId)` to return `safe=false` when any non-deleted dependency has matching `targetType` and `targetId`.

- [ ] **Step 3: Write resource mutation audit test**

Create `ResourceMutationAuditTest`:

```java
package com.opencallhub.resource.application;

import com.opencallhub.resource.domain.ResourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceMutationAuditTest {
    @Test
    void createUpdateDeleteAllWriteResourceAuditRecords() {
        ResourceAuditService auditService = ResourceAuditService.inMemoryForTest();
        NumberResourceService service = NumberResourceService.inMemoryForTest(auditService);

        Long id = service.createPhoneNumber("4008001000", "INBOUND", 1L);
        service.updatePhoneNumber(id, "4008001001", "INBOUND", 1L);
        service.deletePhoneNumber(id, 1L);

        assertThat(auditService.query(ResourceType.PHONE_NUMBER, id))
                .extracting(audit -> audit.action())
                .containsExactly("CREATE", "UPDATE", "DELETE");
    }
}
```

- [ ] **Step 4: Implement resource services with audit on every mutation**

Implement `ResourceAuditService.inMemoryForTest()`, `ResourceAuditService.append(...)`, and `ResourceAuditService.query(ResourceType, Long)`. Implement create/update/delete methods for all resource services so each mutation appends a `res_audit` row with action `CREATE`, `UPDATE`, or `DELETE`, including `operator_id`, `resource_type`, `resource_id`, `before_json`, `after_json`, `result`, and `trace_id`.

The services must enforce these dependency edges:

```text
EXTENSION -> SIP_ACCOUNT when an extension uses a SIP account
PHONE_NUMBER -> TRUNK_GATEWAY when a number routes through a trunk
NUMBER_POOL -> PHONE_NUMBER when a pool contains a number
TRUNK_GATEWAY -> FREESWITCH_NODE when a trunk is hosted by a FreeSWITCH node
SIP_ACCOUNT -> KAMAILIO_NODE when a SIP account registers through Kamailio
AUDIO_ASSET -> STORAGE_CONFIG when audio metadata points to configured storage
AI_PLUGIN -> STORAGE_CONFIG when provider logs or cached audio use storage
```

- [ ] **Step 5: Run resource service tests**

Run:

```bash
mvn -pl och-resource test -Dtest=ResourceDependencyServiceTest,ResourceMutationAuditTest
```

Expected output contains `BUILD SUCCESS`.

- [ ] **Step 6: Commit checkpoint**

Run:

```bash
git add och-resource/src/main/java och-resource/src/test/java
git commit -m "feat: add resource services and dependency checks"
```

Expected output contains `feat: add resource services and dependency checks`.

### Task 5: Resource REST APIs

**Owner:** Communications Expert

**Files:**
- Create: all resource controller files listed in File Structure
- Create: `och-resource/src/test/java/com/opencallhub/resource/api/ResourceControllerTest.java`

- [ ] **Step 1: Write resource API contract test**

Create `ResourceControllerTest` with these MockMvc assertions:

```java
mockMvc.perform(post("/api/resource/trunk-gateways")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"gatewayCode\":\"gw-main\",\"gatewayName\":\"Main Carrier\",\"sipServer\":\"sip.carrier.local\",\"sipPort\":5060,\"authUsername\":\"carrier\",\"authPassword\":\"carrier-secret\",\"maxConcurrency\":100}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.gatewayCode").value("gw-main"))
        .andExpect(jsonPath("$.data.authPassword").value("car********ret"));

mockMvc.perform(post("/api/resource/dependencies/check")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"targetType\":\"PHONE_NUMBER\",\"targetId\":20,\"operation\":\"DELETE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.safe").value(false))
        .andExpect(jsonPath("$.data.blockers[0].sourceType").value("NUMBER_POOL"));

mockMvc.perform(get("/api/resource/audits?resourceType=TRUNK_GATEWAY&resourceId=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.records[0].action").value("CREATE"));
```

- [ ] **Step 2: Implement resource endpoints**

Implement these endpoint contracts:

```text
GET    /api/resource/sip-accounts
POST   /api/resource/sip-accounts
PUT    /api/resource/sip-accounts/{id}
DELETE /api/resource/sip-accounts/{id}
GET    /api/resource/extensions
POST   /api/resource/extensions
PUT    /api/resource/extensions/{id}
DELETE /api/resource/extensions/{id}
GET    /api/resource/phone-numbers
POST   /api/resource/phone-numbers
PUT    /api/resource/phone-numbers/{id}
DELETE /api/resource/phone-numbers/{id}
GET    /api/resource/number-pools
POST   /api/resource/number-pools
PUT    /api/resource/number-pools/{id}
DELETE /api/resource/number-pools/{id}
PUT    /api/resource/number-pools/{id}/phone-numbers
GET    /api/resource/trunk-gateways
POST   /api/resource/trunk-gateways
PUT    /api/resource/trunk-gateways/{id}
DELETE /api/resource/trunk-gateways/{id}
GET    /api/resource/freeswitch-nodes
POST   /api/resource/freeswitch-nodes
PUT    /api/resource/freeswitch-nodes/{id}
DELETE /api/resource/freeswitch-nodes/{id}
GET    /api/resource/kamailio-nodes
POST   /api/resource/kamailio-nodes
PUT    /api/resource/kamailio-nodes/{id}
DELETE /api/resource/kamailio-nodes/{id}
GET    /api/resource/audio-assets
POST   /api/resource/audio-assets
PUT    /api/resource/audio-assets/{id}
DELETE /api/resource/audio-assets/{id}
GET    /api/resource/storage-configs
POST   /api/resource/storage-configs
PUT    /api/resource/storage-configs/{id}
DELETE /api/resource/storage-configs/{id}
GET    /api/resource/ai-plugins
POST   /api/resource/ai-plugins
PUT    /api/resource/ai-plugins/{id}
DELETE /api/resource/ai-plugins/{id}
POST   /api/resource/dependencies/check
GET    /api/resource/dependencies/graph
GET    /api/resource/audits
```

Delete endpoints must call `ResourceDependencyService.checkDelete(...)` before deleting. When blockers exist, return HTTP 409 with unified response code `RESOURCE_DEPENDENCY_BLOCKED` and include blockers in `data.blockers`.

- [ ] **Step 3: Run resource API tests**

Run:

```bash
mvn -pl och-resource -am test -Dtest=ResourceControllerTest
```

Expected output contains `BUILD SUCCESS`. The test report for `ResourceControllerTest` validates masked trunk credentials, dependency check, and resource audit query.

- [ ] **Step 4: Verify OpenAPI lists Phase 2 APIs**

Run backend:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

Then run:

```bash
curl -s http://localhost:18080/v3/api-docs | python -m json.tool > /tmp/och-openapi.json
```

Expected `/tmp/och-openapi.json` contains `/api/system/auth/me`, `/api/system/roles`, `/api/resource/trunk-gateways`, `/api/resource/ai-plugins`, and `/api/resource/dependencies/check`.

- [ ] **Step 5: Commit checkpoint**

Run:

```bash
git add och-resource/src/main/java och-resource/src/test/java och-api/src/main/resources
git commit -m "feat: expose resource management APIs"
```

Expected output contains `feat: expose resource management APIs`.

### Task 6: Permission Center Frontend

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/types/system.ts`
- Create: `waihu-app/src/services/systemApi.ts`
- Create: `waihu-app/src/components/permission/PermissionButton.vue`
- Create: `waihu-app/src/components/permission/DataScopeTag.vue`
- Create: system views listed in File Structure
- Modify: `waihu-app/src/router/routes.ts`
- Modify: `waihu-app/src/router/index.ts`
- Modify: `waihu-app/src/stores/permission.ts`
- Modify: `waihu-app/src/components/menu/AppMenu.vue`
- Test: `waihu-app/src/tests/permission-store.spec.ts`

- [ ] **Step 1: Write permission store test**

Create `waihu-app/src/tests/permission-store.spec.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermissionStore } from '@/stores/permission'

describe('permission store', () => {
  it('renders different menus and buttons by role', () => {
    setActivePinia(createPinia())
    const store = usePermissionStore()

    store.applyCurrentUser({
      userId: 1,
      username: 'admin',
      roles: ['ADMIN'],
      menus: [{ id: 100, name: 'System', routePath: '/system' }, { id: 200, name: 'Resources', routePath: '/resource' }],
      buttonPermissions: ['system:user:create', 'resource:trunk:update'],
      dataScope: { scopeType: 'ALL', deptIds: [] },
    })
    expect(store.visibleMenus.map((menu) => menu.routePath)).toEqual(['/system', '/resource'])
    expect(store.hasButton('resource:trunk:update')).toBe(true)

    store.applyCurrentUser({
      userId: 9,
      username: 'agent01',
      roles: ['AGENT'],
      menus: [{ id: 300, name: 'Agent Home', routePath: '/agent/home' }],
      buttonPermissions: ['agent:home:view'],
      dataScope: { scopeType: 'SELF', deptIds: [21] },
    })
    expect(store.visibleMenus.map((menu) => menu.routePath)).toEqual(['/agent/home'])
    expect(store.hasButton('resource:trunk:update')).toBe(false)
  })
})
```

- [ ] **Step 2: Implement system DTOs and API client**

Create `waihu-app/src/types/system.ts` with `CurrentUserPermission`, `SystemMenu`, `DataScopeSummary`, `UserRecord`, `DepartmentRecord`, `RoleRecord`, `ButtonPermissionRecord`, `LoginLogRecord`, and `OperationAuditRecord`. Create `waihu-app/src/services/systemApi.ts` functions for every `/api/system/**` endpoint listed in Task 2 Step 6.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on files not yet created in later steps.

- [ ] **Step 3: Implement permission store and menu rendering**

Modify `waihu-app/src/stores/permission.ts` to add `applyCurrentUser(payload: CurrentUserPermission)`, `visibleMenus`, `hasRoute(routePath: string)`, and `hasButton(permissionCode: string)`. Modify `waihu-app/src/components/menu/AppMenu.vue` so it renders `permissionStore.visibleMenus` instead of static menus.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run test -- permission-store.spec.ts` exits with a passing test summary.

- [ ] **Step 4: Implement `PermissionButton.vue`**

Create `waihu-app/src/components/permission/PermissionButton.vue` with props:

```ts
const props = defineProps<{ permission: string; disabledWhenDenied?: boolean }>()
```

Behavior: render the default slot when `hasButton(permission)` is true; render a disabled Ant Design button with text `No permission` when `disabledWhenDenied` is true; render nothing when denied and `disabledWhenDenied` is false.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 5: Implement `DataScopeTag.vue`**

Create `waihu-app/src/components/permission/DataScopeTag.vue`. It displays `All data`, `Department tree`, `Department only`, or `Self only` from the current permission store data scope.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 6: Implement `UserListView.vue`**

Create `waihu-app/src/views/system/UserListView.vue` with search username, department filter, status filter, Create User button, and table columns `username`, `displayName`, `deptName`, `roles`, `status`, and `actions`. Use Phase 1 table/form/detail drawer patterns.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 7: Implement department and role pages**

Create `waihu-app/src/views/system/DepartmentTreeView.vue`, `waihu-app/src/views/system/RoleListView.vue`, and `waihu-app/src/views/system/RolePermissionView.vue`. Department page shows department tree left, detail form right, and Create Child Department button. Role list shows `roleCode`, `roleName`, `status`, and Configure Permissions action. Role permission page shows menu tree checkbox panel, button permission checkbox panel, data scope radio group `ALL`/`DEPT_TREE`/`DEPT_ONLY`/`SELF`, and save button.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 8: Implement menu and audit pages**

Create `waihu-app/src/views/system/MenuListView.vue`, `waihu-app/src/views/system/LoginLogView.vue`, and `waihu-app/src/views/system/OperationAuditView.vue`. Menu page shows menu tree table and button permission sub-table. Login log page shows username/result/date filters and login result table. Operation audit page shows operator/module/action/target/date filters and before/after JSON drawer.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0.

- [ ] **Step 9: Add system routes and route guard**

Modify `waihu-app/src/router/routes.ts` to add system route records for every system page with permission metadata. Modify `waihu-app/src/router/index.ts` so navigation checks `permissionStore.hasRoute(to.path)` and redirects unauthorized users to the Phase 1 unauthorized page or dashboard.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0.

- [ ] **Step 10: Run frontend permission tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- permission-store.spec.ts
```

Expected output contains a passing test summary for one test in `permission-store.spec.ts`.

- [ ] **Step 11: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/system.ts waihu-app/src/services/systemApi.ts waihu-app/src/components/permission waihu-app/src/views/system waihu-app/src/router waihu-app/src/stores/permission.ts waihu-app/src/components/menu/AppMenu.vue waihu-app/src/tests/permission-store.spec.ts
git commit -m "feat: add permission center frontend"
```

Expected output contains `feat: add permission center frontend`.

### Task 7: Resource Center Frontend and Dependency UI

**Owner:** Frontend Expert

**Files:**
- Create: `waihu-app/src/types/resource.ts`
- Create: `waihu-app/src/services/resourceApi.ts`
- Create: `waihu-app/src/stores/resource.ts`
- Create: `waihu-app/src/components/resource/DependencyCheckDrawer.vue`
- Create: `waihu-app/src/components/resource/ResourceAuditTimeline.vue`
- Create: `waihu-app/src/components/resource/SecretMaskField.vue`
- Create: all `waihu-app/src/views/resource/**` files listed in File Structure
- Modify: `waihu-app/src/router/routes.ts`
- Test: `waihu-app/src/tests/dependency-check-drawer.spec.ts`
- Test: `waihu-app/src/tests/secret-mask-field.spec.ts`

- [ ] **Step 1: Write dependency drawer test**

Create `dependency-check-drawer.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import DependencyCheckDrawer from '@/components/resource/DependencyCheckDrawer.vue'

describe('DependencyCheckDrawer', () => {
  it('shows blockers and disables delete confirmation', () => {
    const wrapper = mount(DependencyCheckDrawer, {
      props: {
        open: true,
        result: {
          safe: false,
          blockers: [{ sourceType: 'NUMBER_POOL', sourceId: 10, description: 'Main outbound pool uses 4008001000' }],
        },
      },
    })

    expect(wrapper.text()).toContain('Unsafe deletion blocked')
    expect(wrapper.text()).toContain('Main outbound pool uses 4008001000')
    expect(wrapper.get('[data-test="confirm-delete"]').attributes('disabled')).toBeDefined()
  })
})
```

- [ ] **Step 2: Write secret mask field test**

Create `secret-mask-field.spec.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import SecretMaskField from '@/components/resource/SecretMaskField.vue'

describe('SecretMaskField', () => {
  it('renders masked value and emits changed secret only after input', async () => {
    const wrapper = mount(SecretMaskField, { props: { modelValue: '', maskedValue: 'abc********789' } })

    expect(wrapper.text()).toContain('abc********789')
    await wrapper.get('input').setValue('new-secret')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['new-secret'])
  })
})
```

- [ ] **Step 3: Implement resource DTOs and API client**

Create `waihu-app/src/types/resource.ts` with DTOs for `SipAccount`, `Extension`, `PhoneNumber`, `NumberPool`, `TrunkGateway`, `FreeSwitchNode`, `KamailioNode`, `AudioAsset`, `StorageConfig`, `AiPluginResource`, `DependencyCheckResult`, `DependencyBlocker`, and `ResourceAuditRecord`. Create `waihu-app/src/services/resourceApi.ts` functions for every `/api/resource/**` endpoint listed in Task 5 Step 2.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on components/pages not yet created in later steps.

- [ ] **Step 4: Implement `DependencyCheckDrawer.vue`**

Create `waihu-app/src/components/resource/DependencyCheckDrawer.vue`: shows safe state, blocker list, dependency source type/id, description, Cancel button, disabled Confirm Delete button when `safe=false`, enabled Confirm Delete button when `safe=true`, and `data-test="confirm-delete"` on the confirm button.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run test -- dependency-check-drawer.spec.ts` exits with a passing test summary.

- [ ] **Step 5: Implement resource secret and audit components**

Create `waihu-app/src/components/resource/SecretMaskField.vue` and `waihu-app/src/components/resource/ResourceAuditTimeline.vue`. `SecretMaskField.vue` displays `maskedValue` text, write-only password input, and emits `update:modelValue` only when user types a replacement. `ResourceAuditTimeline.vue` shows action, operatorName, result, traceId, createdAt, and a before/after drawer trigger.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run test -- secret-mask-field.spec.ts` exits with a passing test summary.

- [ ] **Step 6: Implement SIP account and extension pages**

Create `waihu-app/src/views/resource/SipAccountView.vue` and `waihu-app/src/views/resource/ExtensionView.vue`. SIP account page exposes `sipUsername`, `authRealm`, `displayName`, `status`, and masked auth password field. Extension page exposes `extensionNo`, SIP account selector, user selector, dept selector, `registerStatus`, and `status`.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 7: Implement phone number and number pool pages**

Create `waihu-app/src/views/resource/PhoneNumberView.vue` and `waihu-app/src/views/resource/NumberPoolView.vue`. Phone number page exposes `phoneNumber`, direction `INBOUND`/`OUTBOUND`/`BOTH`, number pool selector, trunk selector, and `concurrencyLimit`. Number pool page exposes `poolCode`, `poolName`, dept selector, `concurrencyLimit`, and assigned phone numbers table.

Every delete action on `PhoneNumberView.vue` must call `/api/resource/dependencies/check`, open `DependencyCheckDrawer.vue`, and only call the delete API when `result.safe === true`.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 8: Implement trunk and node pages**

Create `waihu-app/src/views/resource/TrunkGatewayView.vue`, `waihu-app/src/views/resource/FreeSwitchNodeView.vue`, and `waihu-app/src/views/resource/KamailioNodeView.vue`. Trunk page exposes `gatewayCode`, `gatewayName`, `sipServer`, `sipPort`, `transport`, `authUsername`, masked auth password, and `maxConcurrency`. FreeSWITCH page exposes `nodeCode`, `host`, `eslPort`, masked `eslPassword`, `weight`, `maxChannels`, and `healthStatus`. Kamailio page exposes `nodeCode`, `host`, `sipPort`, `transport`, and `healthStatus`.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0 or fails only on pages not yet created in later steps.

- [ ] **Step 9: Implement audio, storage, AI plugin, and audit pages**

Create `waihu-app/src/views/resource/AudioAssetView.vue`, `waihu-app/src/views/resource/StorageConfigView.vue`, `waihu-app/src/views/resource/AiPluginResourceView.vue`, and `waihu-app/src/views/resource/ResourceAuditView.vue`. Audio page exposes `assetCode`, `assetName`, `audioType`, `storageUri`, `durationMs`, and dependency usage panel. Storage page exposes `configCode`, `provider`, `endpoint`, `bucketName`, and masked access/secret keys. AI plugin page exposes `pluginCode`, `pluginName`, plugin type `ASR`/`TTS`/`LLM`/`WORKFLOW`, provider `DIFY`/`RAGFLOW`/`LANGFLOW`/`CUSTOM`, `endpoint`, masked `apiKey`, `modelName`, and `timeoutMs`. Resource audit page exposes resourceType/resourceId/action/operator/date filters and audit timeline.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0.

- [ ] **Step 10: Add resource routes**

Modify `waihu-app/src/router/routes.ts` to add resource route records for every resource page with permission metadata matching the seeded `sys_menu.permission_code` values.

Expected check: `cd e:/workspace/openCallHub/waihu-app && npm run build` exits with code 0.

- [ ] **Step 11: Run frontend resource tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test -- dependency-check-drawer.spec.ts secret-mask-field.spec.ts
```

Expected output contains a passing test summary for two test files: `dependency-check-drawer.spec.ts` and `secret-mask-field.spec.ts`.

- [ ] **Step 12: Commit checkpoint**

Run:

```bash
git add waihu-app/src/types/resource.ts waihu-app/src/services/resourceApi.ts waihu-app/src/stores/resource.ts waihu-app/src/components/resource waihu-app/src/views/resource waihu-app/src/router/routes.ts waihu-app/src/tests/dependency-check-drawer.spec.ts waihu-app/src/tests/secret-mask-field.spec.ts
git commit -m "feat: add resource center frontend"
```

Expected output contains `feat: add resource center frontend`.

### Task 8: Phase 2 End-to-End Regression and Acceptance

**Owner:** QA Expert

**Files:**
- Create: `waihu-app/e2e/phase2-permission-resource.spec.ts`
- Create: `och-api/src/test/java/com/opencallhub/api/testfixture/Phase2SeedHealthController.java`
- Verify: backend and frontend commands listed below

- [ ] **Step 1: Write Playwright Phase 2 regression**

Create `waihu-app/e2e/phase2-permission-resource.spec.ts`. This test assumes Flyway local/test migrations have already seeded `admin/admin123`, `agent01/agent123`, `4008001000`, `pool-main-outbound`, `gw-main`, `sip-agent01`, `8001`, `fs-main`, `kam-main`, `welcome-main`, `storage-local`, and `asr-main`. It must fail fast if these rows are missing instead of silently creating substitute fixtures.

```ts
import { expect, test } from '@playwright/test'

test('phase 2 permission and resource acceptance path', async ({ page, request }) => {
  const seedCheck = await request.get('/api/test-fixtures/phase2/seed-health')
  expect(seedCheck.ok(), 'Phase 2 local/test seed migrations must run before Playwright').toBeTruthy()
  await expect.poll(async () => (await seedCheck.json()).data).toMatchObject({
    users: ['admin', 'agent01'],
    phoneNumber: '4008001000',
    numberPool: 'pool-main-outbound',
    trunkGateway: 'gw-main',
    sipAccount: 'sip-agent01',
    extension: '8001',
    freeSwitchNode: 'fs-main',
    kamailioNode: 'kam-main',
    audioAsset: 'welcome-main',
    storageConfig: 'storage-local',
    aiPlugin: 'asr-main',
    blockerDescription: 'Main outbound pool uses 4008001000',
  })
  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()
  await expect(page.getByText('System')).toBeVisible()
  await expect(page.getByText('Resources')).toBeVisible()

  await page.goto('/resource/trunk-gateways')
  await page.getByRole('button', { name: 'Create Trunk Gateway' }).click()
  await page.getByLabel('Gateway Code').fill('gw-main')
  await page.getByLabel('Gateway Name').fill('Main Carrier')
  await page.getByLabel('SIP Server').fill('sip.carrier.local')
  await page.getByLabel('Auth Username').fill('carrier')
  await page.getByLabel('Auth Password').fill('carrier-secret')
  await page.getByRole('button', { name: 'Save' }).click()
  await expect(page.getByText('gw-main')).toBeVisible()
  await expect(page.getByText('car********ret')).toBeVisible()

  await page.goto('/resource/ai-plugins')
  await page.getByRole('button', { name: 'Create AI Plugin' }).click()
  await page.getByLabel('Plugin Code').fill('asr-main')
  await page.getByLabel('Plugin Name').fill('Main ASR')
  await page.getByLabel('Plugin Type').selectOption('ASR')
  await page.getByLabel('Provider').selectOption('DIFY')
  await page.getByLabel('Endpoint').fill('https://ai.local/asr')
  await page.getByLabel('API Key').fill('ai-secret-key')
  await page.getByRole('button', { name: 'Save' }).click()
  await expect(page.getByText('asr-main')).toBeVisible()

  await page.goto('/resource/phone-numbers')
  await page.getByRole('button', { name: 'Delete 4008001000' }).click()
  await expect(page.getByText('Unsafe deletion blocked')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Confirm Delete' })).toBeDisabled()

  await page.goto('/resource/audits')
  await expect(page.getByText('TRUNK_GATEWAY')).toBeVisible()
  await expect(page.getByText('AI_PLUGIN')).toBeVisible()

  await page.getByRole('button', { name: 'Logout' }).click()
  await page.getByLabel('Username').fill('agent01')
  await page.getByLabel('Password').fill('agent123')
  await page.getByRole('button', { name: 'Login' }).click()
  await expect(page.getByText('Agent Home')).toBeVisible()
  await expect(page.getByText('Resources')).not.toBeVisible()
})
```

- [ ] **Step 2: Add local/test seed-health endpoint for E2E setup**

Create `och-api/src/test/java/com/opencallhub/api/testfixture/Phase2SeedHealthController.java` or the project’s equivalent test-fixture source set. Register it only under `local` and `test` profiles, never production. The endpoint `GET /api/test-fixtures/phase2/seed-health` must query the same database used by the running backend and return:

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "users": ["admin", "agent01"],
    "phoneNumber": "4008001000",
    "numberPool": "pool-main-outbound",
    "trunkGateway": "gw-main",
    "sipAccount": "sip-agent01",
    "extension": "8001",
    "freeSwitchNode": "fs-main",
    "kamailioNode": "kam-main",
    "audioAsset": "welcome-main",
    "storageConfig": "storage-local",
    "aiPlugin": "asr-main",
    "blockerDescription": "Main outbound pool uses 4008001000"
  }
}
```

Expected output contains `BUILD SUCCESS`. The Surefire XML report for `Phase2SeedHealthControllerTest` proves the endpoint returns HTTP 200 after Flyway applies `V2__system_permission_schema.sql` and `V3__resource_schema.sql`.

- [ ] **Step 3: Run backend full Phase 2 tests**

Run:

```bash
mvn -pl och-api,och-system,och-resource -am test
```

Expected output contains `BUILD SUCCESS`. The Maven test summary or Surefire XML reports show `MenuPermissionServiceTest`, `DataScopeServiceTest`, `OperationAuditServiceTest`, `ResourceDependencyServiceTest`, `ResourceMutationAuditTest`, `SecretMaskingTest`, `SystemPermissionControllerTest`, and `ResourceControllerTest` passed.

- [ ] **Step 4: Run frontend unit tests**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run test
```

Expected output contains passing tests for `permission-store.spec.ts`, `dependency-check-drawer.spec.ts`, and `secret-mask-field.spec.ts`.

- [ ] **Step 5: Run frontend build**

Run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run build
```

Expected output contains `built in` and exits with code 0.

- [ ] **Step 6: Run Phase 2 E2E regression**

Start backend:

```bash
mvn -pl och-api -am spring-boot:run -Dspring-boot.run.profiles=local
```

In another terminal run:

```bash
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase2-permission-resource.spec.ts
```

Expected output shows one passing Playwright spec named `phase 2 permission and resource acceptance path`.

- [ ] **Step 7: Manually verify acceptance gate evidence**

Record command output or screenshots proving:

```text
Admin menu contains System and Resources while agent menu contains Agent Home only.
Phone numbers, trunk gateways, extensions, audio assets, and AI plugin resources each have one saved record visible in the UI.
Deleting a phone number used by a number pool opens Unsafe deletion blocked and does not call DELETE.
Resource audit shows CREATE and UPDATE records for trunk gateway, phone number, extension, audio asset, and AI plugin.
Data scope tag shows All data for ADMIN and Self only for AGENT, with user list row counts differing between the two roles.
```

- [ ] **Step 8: Commit checkpoint**

Run:

```bash
git add waihu-app/e2e/phase2-permission-resource.spec.ts och-api/src/test/java/com/opencallhub/api/testfixture/Phase2SeedHealthController.java
git commit -m "test: add phase 2 permission resource regression"
```

Expected output contains `test: add phase 2 permission resource regression`.

## Acceptance Gate

Phase 2 is accepted only when role menus render differently by role, phone numbers/trunks/extensions/audio assets/AI plugins can be configured, resource dependencies prevent unsafe deletion, all resource mutations write audit records, and data scope filtering is demonstrated for at least two roles.

## Final Phase 2 Verification Commands

Run these after all tasks and commits are complete:

```bash
mvn -pl och-api,och-system,och-resource -am test
cd e:/workspace/openCallHub/waihu-app && npm run test
cd e:/workspace/openCallHub/waihu-app && npm run build
cd e:/workspace/openCallHub/waihu-app && npm run e2e -- phase2-permission-resource.spec.ts
```

Expected results:

```text
Backend Maven output contains BUILD SUCCESS.
Frontend unit test output contains all Phase 2 unit suites passing.
Frontend build exits with code 0 and prints built in.
Playwright report shows one passing spec named phase 2 permission and resource acceptance path.
```

## Self-Review

Spec coverage:
- Users, departments, roles, menus, button permissions, data scopes, login logs, and operation audit are covered by Tasks 1, 2, 3, 6, and 8.
- SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, and AI plugin resources are covered by Tasks 1, 4, 5, 7, and 8.
- Resource dependency checks, dependency-check UI, resource audit, secret masking, data scope, and audit records are covered by Tasks 3, 4, 5, 7, and 8.
- Database migrations, APIs, frontend pages, tests, exact files, commands, expected results, and commit checkpoints are present in the task list.

Placeholder scan:
- The plan contains no deferred implementation placeholders and every task includes concrete paths, commands, expected results, or code snippets.

Type consistency:
- `ResourceType`, `DependencyCheckResult`, `DependencyBlocker`, `DataScopeFilter`, `OperationAuditCommand`, and frontend DTO names are introduced before they are used in tests or UI tasks.
