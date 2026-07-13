# openCallHub Inbound Schema Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the inbound-domain database schema and deterministic seed data for call core, IVR, ACD, agent workspace, customer lookup, and inbound reporting.

**Architecture:** This task creates the storage foundation for the full inbound production implementation. The schema uses new domain tables rather than extending old call / IVR / ACD / agent tables, while keeping the current `com.och` module boundaries and MyBatis-Plus conventions. Migrations are verified by a Spring Boot smoke test before any service-layer implementation proceeds.

**Tech Stack:** Java 17, Spring Boot 3.5.3, Maven, MyBatis-Plus, MySQL-compatible SQL, Flyway-style SQL migration directories if enabled by the application, Spring Boot Test, JdbcTemplate, AssertJ.

---

## Scope Check

This plan implements only Task 1 from `docs/superpowers/plans/2026-06-09-opencallhub-inbound-production-plan.md`.

It creates schema and seed data only. It does not implement ESL command handling, IVR runtime logic, ACD dispatch logic, phone-bar APIs, frontend pages, WebSocket fanout, recording token generation, or real FreeSWITCH acceptance.

Reference documents:

- Spec: `docs/superpowers/specs/2026-06-09-opencallhub-inbound-production-design.md`
- Master plan: `docs/superpowers/plans/2026-06-09-opencallhub-inbound-production-plan.md`

## File Structure

### SQL migrations

- Create: `och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql`
  - Owns call session, leg, event, dedup, command, call record, recording record, and service record tables.
- Create: `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql`
  - Owns IVR flow, version, node, edge, variable, execution, node execution, execution trace, voicemail box, and voicemail message tables.
- Create: `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql`
  - Owns skill group, queue, skill group agent relation, agent status, dispatch rule, overflow rule, SLA rule, queue session, and queue metric snapshot tables.
- Create: `och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql`
  - Owns agent phone-bar session, agent call command, screen pop, and after-call work tables.
- Create: `och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql`
  - Owns customer profile, customer phone, and inbound customer association tables.
- Create: `och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql`
  - Owns inbound daily, queue daily, and agent inbound daily report tables.

### Test files

- Create: `och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java`
  - Verifies all new inbound tables exist and deterministic seed rows are available in test profile.

### Configuration files

- Modify only if the smoke test proves migrations are not discovered automatically: `och-api/src/main/resources/application.yml`
  - Add module migration locations for call, IVR, ACD, customer, and report resources.

## Task 1: Write Failing Migration Smoke Test

**Files:**
- Create: `och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java`

- [ ] **Step 1: Create the smoke test**

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
        assertThat(count("call_event_dedup")).isGreaterThanOrEqualTo(0);
        assertThat(count("call_command")).isGreaterThanOrEqualTo(0);
        assertThat(count("call_record")).isGreaterThanOrEqualTo(0);
        assertThat(count("recording_record")).isGreaterThanOrEqualTo(0);
        assertThat(count("service_record")).isGreaterThanOrEqualTo(0);

        assertThat(count("ivr_flow")).isGreaterThanOrEqualTo(1);
        assertThat(count("ivr_flow_version")).isGreaterThanOrEqualTo(1);
        assertThat(count("ivr_node")).isGreaterThanOrEqualTo(6);
        assertThat(count("ivr_edge")).isGreaterThanOrEqualTo(7);
        assertThat(count("ivr_voicemail_box")).isGreaterThanOrEqualTo(1);

        assertThat(count("acd_skill_group")).isGreaterThanOrEqualTo(1);
        assertThat(count("acd_queue")).isGreaterThanOrEqualTo(1);
        assertThat(count("acd_skill_group_agent")).isGreaterThanOrEqualTo(1);
        assertThat(count("acd_agent_status")).isGreaterThanOrEqualTo(1);

        assertThat(count("agent_phone_bar_session")).isGreaterThanOrEqualTo(0);
        assertThat(count("agent_call_command")).isGreaterThanOrEqualTo(0);
        assertThat(count("agent_screen_pop")).isGreaterThanOrEqualTo(0);
        assertThat(count("agent_after_call_work")).isGreaterThanOrEqualTo(0);

        assertThat(count("customer_profile")).isGreaterThanOrEqualTo(1);
        assertThat(count("customer_phone")).isGreaterThanOrEqualTo(1);
        assertThat(count("inbound_customer_association")).isGreaterThanOrEqualTo(0);

        assertThat(count("report_inbound_daily")).isGreaterThanOrEqualTo(0);
        assertThat(count("report_queue_daily")).isGreaterThanOrEqualTo(0);
        assertThat(count("report_agent_inbound_daily")).isGreaterThanOrEqualTo(0);
    }

    private Integer count(String tableName) {
        return jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    }
}
```

- [ ] **Step 2: Run the smoke test before migrations are added**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected result: `BUILD FAILURE`. The failure should be an SQL grammar or table-not-found error for one of the new inbound tables, proving the test is checking real schema existence.

## Task 2: Create Call Core Schema

**Files:**
- Create: `och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql`

- [ ] **Step 1: Create call core migration**

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
    hold_count INT DEFAULT 0,
    transfer_count INT DEFAULT 0,
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
    after_call_seconds INT DEFAULT 0,
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

- [ ] **Step 2: Validate SQL file is copied into module resources**

Run:

```bash
mvn -pl och-call resources:resources
```

Expected result: `BUILD SUCCESS`, and Maven output says resources were copied from `src/main/resources`.

## Task 3: Create IVR Schema and Deterministic Seed Flow

**Files:**
- Create: `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql`

- [ ] **Step 1: Create IVR migration**

Create `och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql` with these tables:

```sql
CREATE TABLE IF NOT EXISTS ivr_flow (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    flow_code VARCHAR(64) NOT NULL,
    flow_name VARCHAR(128) NOT NULL,
    published_version_id BIGINT,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_ivr_flow_code (tenant_id, flow_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR流程表';

CREATE TABLE IF NOT EXISTS ivr_flow_version (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    flow_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    version_state VARCHAR(32) NOT NULL,
    graph_hash VARCHAR(96),
    published_at DATETIME,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_ivr_flow_version (tenant_id, flow_id, version_no, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR流程版本表';

CREATE TABLE IF NOT EXISTS ivr_node (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    version_id BIGINT NOT NULL,
    node_id VARCHAR(96) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    config_json JSON,
    pos_x INT DEFAULT 0,
    pos_y INT DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_ivr_node (tenant_id, version_id, node_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR节点表';

CREATE TABLE IF NOT EXISTS ivr_edge (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    version_id BIGINT NOT NULL,
    edge_id VARCHAR(96) NOT NULL,
    source_node_id VARCHAR(96) NOT NULL,
    target_node_id VARCHAR(96) NOT NULL,
    condition_expr VARCHAR(256),
    label VARCHAR(128),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_ivr_edge (tenant_id, version_id, edge_id, deleted),
    KEY idx_ivr_edge_source (tenant_id, version_id, source_node_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR连线表';

CREATE TABLE IF NOT EXISTS ivr_variable (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    version_id BIGINT NOT NULL,
    variable_name VARCHAR(96) NOT NULL,
    default_value VARCHAR(512),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_ivr_variable (tenant_id, version_id, variable_name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR变量表';
```

Append execution and voicemail tables:

```sql
CREATE TABLE IF NOT EXISTS ivr_execution (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    call_id VARCHAR(64) NOT NULL,
    flow_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    current_node_id VARCHAR(96),
    execution_state VARCHAR(32) NOT NULL,
    enter_reason VARCHAR(128),
    exit_reason VARCHAR(128),
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME,
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    KEY idx_ivr_execution_call (tenant_id, call_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR执行实例表';

CREATE TABLE IF NOT EXISTS ivr_node_execution (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    execution_id BIGINT NOT NULL,
    node_id VARCHAR(96) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    input_payload JSON,
    output_payload JSON,
    variable_snapshot JSON,
    started_at DATETIME NOT NULL,
    ended_at DATETIME,
    cost_millis BIGINT DEFAULT 0,
    error_message VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ivr_node_execution (tenant_id, execution_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR节点执行历史表';

CREATE TABLE IF NOT EXISTS ivr_execution_trace (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    execution_id BIGINT NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    trace_type VARCHAR(64) NOT NULL,
    trace_message VARCHAR(512) NOT NULL,
    payload JSON,
    occurred_at DATETIME NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ivr_trace_execution (tenant_id, execution_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IVR执行轨迹表';

CREATE TABLE IF NOT EXISTS ivr_voicemail_box (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    box_code VARCHAR(64) NOT NULL,
    box_name VARCHAR(128) NOT NULL,
    skill_group_id BIGINT,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    UNIQUE KEY uk_voicemail_box (tenant_id, box_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语音信箱表';

CREATE TABLE IF NOT EXISTS ivr_voicemail_message (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    box_id BIGINT NOT NULL,
    call_id VARCHAR(64) NOT NULL,
    caller_number VARCHAR(32),
    recording_id BIGINT,
    message_state VARCHAR(32) NOT NULL DEFAULT 'NEW',
    trace_id VARCHAR(64) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(512),
    KEY idx_voicemail_message_box (tenant_id, box_id, message_state, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语音留言表';
```

- [ ] **Step 2: Add deterministic IVR seed data**

Append these seed rows to `V11__inbound_ivr_schema.sql`:

```sql
INSERT INTO ivr_flow (id, tenant_id, flow_code, flow_name, published_version_id, enabled, created_at, status, deleted)
VALUES (100, 1, '4008001000', '默认呼入IVR', 1001, 1, CURRENT_TIMESTAMP, 'ENABLED', 0);

INSERT INTO ivr_flow_version (id, tenant_id, flow_id, version_no, version_state, published_at, created_at, status, deleted)
VALUES (1001, 1, 100, 1, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ENABLED', 0);

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

INSERT INTO ivr_voicemail_box (id, tenant_id, box_code, box_name, skill_group_id, created_at, status, deleted)
VALUES (10, 1, 'support-after-hours', '客服非工作时间语音信箱', 10, CURRENT_TIMESTAMP, 'ENABLED', 0);
```

- [ ] **Step 3: Validate IVR resources copy**

Run:

```bash
mvn -pl och-ivr resources:resources
```

Expected result: `BUILD SUCCESS`.

## Task 4: Create ACD Schema and Seed Queue

**Files:**
- Create: `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql`

- [ ] **Step 1: Create ACD migration**

Create `och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql` with the ACD table definitions and deterministic seed rows. Required seed IDs are skill group `10`, queue `20`, agent relation `30`, and agent status `40`.

Use this seed block exactly after table creation:

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

- [ ] **Step 2: Validate ACD resources copy**

Run:

```bash
mvn -pl och-acd resources:resources
```

Expected result: `BUILD SUCCESS`.

## Task 5: Create Agent Workspace Schema

**Files:**
- Create: `och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql`

- [ ] **Step 1: Create agent workspace migration**

Create tables:

1. `agent_phone_bar_session`
2. `agent_call_command`
3. `agent_screen_pop`
4. `agent_after_call_work`

Each table must include `tenant_id`, `agent_id` when applicable, `call_id` when applicable, audit fields, `deleted`, `status`, and indexes by `agent_id` and `call_id`.

- [ ] **Step 2: Validate call resources copy again**

Run:

```bash
mvn -pl och-call resources:resources
```

Expected result: `BUILD SUCCESS`.

## Task 6: Create Customer and Report Schemas

**Files:**
- Create: `och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql`
- Create: `och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql`

- [ ] **Step 1: Create customer migration**

Create `customer_profile`, `customer_phone`, and `inbound_customer_association`. Seed customer `id=10001`, name `测试客户`, phone `13800000001`.

- [ ] **Step 2: Create report migration**

Create `report_inbound_daily`, `report_queue_daily`, and `report_agent_inbound_daily`. Use `report_date`, count fields, average seconds fields, `sla_rate`, audit fields, and unique keys by tenant/date/dimension.

- [ ] **Step 3: Validate resources copy**

Run:

```bash
mvn -pl och-customer,och-report resources:resources
```

Expected result: `BUILD SUCCESS`.

## Task 7: Run Full Migration Smoke Test

**Files:**
- Modify only if needed: `och-api/src/main/resources/application.yml`

- [ ] **Step 1: Run smoke test**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected result: `BUILD SUCCESS`, `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 2: Add migration locations if the smoke test cannot see module SQL**

If the failure says tables are missing while SQL files exist under module resources, modify `och-api/src/main/resources/application.yml` under the test/local datasource section to include module classpath migration locations. Use the existing Flyway configuration style in the file. If no Flyway configuration exists, add this shape under `spring.flyway`:

```yaml
spring:
  flyway:
    enabled: true
    locations:
      - classpath:db/migration
```

Because dependent module resources are on the test classpath, `classpath:db/migration` is the first choice. Only add explicit module locations if the existing project structure requires them.

- [ ] **Step 3: Re-run smoke test after configuration fix**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected result: `BUILD SUCCESS`.

## Task 8: Run Final Schema Verification

**Files:**
- All schema files from this plan.

- [ ] **Step 1: Compile backend dependency chain**

Run:

```bash
mvn -pl och-api -am test -DskipTests
```

Expected result: `BUILD SUCCESS` across the reactor modules required by `och-api`.

- [ ] **Step 2: Run migration smoke test one final time**

Run:

```bash
mvn -pl och-api -am test -Dtest=InboundMigrationSmokeTest
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 3: Commit schema plan checkpoint**

Commit only after user approval to commit:

```bash
git add och-call/src/main/resources/db/migration/V10__inbound_call_core_schema.sql och-ivr/src/main/resources/db/migration/V11__inbound_ivr_schema.sql och-acd/src/main/resources/db/migration/V12__inbound_acd_schema.sql och-call/src/main/resources/db/migration/V13__inbound_agent_workspace_schema.sql och-customer/src/main/resources/db/migration/V14__inbound_customer_schema.sql och-report/src/main/resources/db/migration/V15__inbound_report_schema.sql och-api/src/test/java/com/och/api/e2e/InboundMigrationSmokeTest.java och-api/src/main/resources/application.yml
git commit -m "feat: add inbound domain schema"
```

## Self-Review

### Spec coverage

This plan covers the schema foundation from the production inbound spec: call core, IVR, ACD, agent workspace, customer lookup, customer association, recording metadata, service records, and inbound reports. It does not implement runtime behavior, which is covered by later subsystem plans.

### Placeholder scan

No placeholder terms are used. Every SQL file path, smoke test path, command, and expected result is specified.

### Type consistency

Seed IDs are consistent across schemas: IVR transfer node uses `skillGroupId=10`; ACD skill group is `10`; ACD queue is `20`; test agent is `9`; test extension is `8001`; test caller is `13800000001`; test called number is `4008001000`.
