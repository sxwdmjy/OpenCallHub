# Flyway Migration Audit Report

**Date**: 2026-06-17
**Branch**: refactoring
**Scope**: All `och-*/src/main/resources/db/migration/V*_*.sql` files across all modules

---

## 1. Executive Summary

The audit identified **1 critical issue** (version number conflict that will cause Flyway to fail at startup), **2 high-severity issues** (large groups of entities without migration scripts), and several medium/low concerns. **Immediate action is required on the V13 conflict.**

---

## 2. Migration Inventory

A total of **16 migration scripts** across **15 modules** were found:

| # | Version | Module | File | Tables Created |
|---|---------|--------|------|---------------|
| 1 | V1 | och-infra | V1__foundation_schema.sql | `foundation_migration_check`, `audit_event` |
| 2 | V3 | och-resource | V3__resource_schema.sql | `res_dependency`, `res_audit`, `res_sip_account`, `res_extension`, `res_phone_number`, `res_number_pool`, `res_audio_asset`, `res_ai_plugin` |
| 3 | V4 | och-call | V4__call_core_schema.sql | `call_session`, `call_leg`, `call_event` |
| 4 | V6 | och-acd | V6__acd_schema.sql | `acd_skill_group`, `acd_agent_status`, `acd_queue_session` |
| 5 | V7 | och-call | V7__agent_inbound_schema.sql | `call_record`, `recording_record`, `service_record` |
| 6 | V8 | och-customer | V8__customer_schema.sql | `customer_profile`, `customer_phone` |
| 7 | V9 | och-report | V9__inbound_report_schema.sql | `report_inbound_daily`, `report_agent_inbound_daily` |
| 8 | V10 | och-call-task | V10__outbound_task_schema.sql | `outbound_task`, `outbound_customer`, `outbound_dial_attempt` |
| 9 | V11 | och-compliance | V11__dnc_blacklist_schema.sql | `compliance_dnc`, `compliance_blacklist` |
| 10 | V12 | och-ai | V12__ai_robot_schema.sql | `robot_flow`, `robot_flow_version`, `robot_session`, `ai_conversation`, `ai_conversation_turn`, `robot_intent`, `plugin_call_log`, `ai_cost_record`, `ai_barge_in_log` |
| 11 | **V13** | och-ticket | V13__ticket_schema.sql | `ticket`, `ticket_flow` |
| 12 | **V13** | och-esl | V13__fs_node.sql | `fs_node` |
| 13 | V14 | och-knowledge | V14__knowledge_schema.sql | `knowledge_document`, `script_template` |
| 14 | V15 | och-quality | V15__quality_schema.sql | `quality_template`, `quality_score`, `quality_appeal` |
| 15 | V16 | och-notice | V16__notice_schema.sql | `notice_message` |
| 16 | V17 | och-batch | V17__batch_schema.sql | `batch_job` |

---

## 3. Critical Issues

### 3.1 V13 Version Number Conflict (CRITICAL)

**Two modules both define migration V13:**

| Module | File |
|--------|------|
| och-ticket | `V13__ticket_schema.sql` |
| och-esl | `V13__fs_node.sql` |

**Impact**: The `och-api` module depends on ALL sub-modules (confirmed in `och-api/pom.xml` lines 59-152). At startup, Flyway scans the combined classpath and finds two migrations with the same version `V13`. Flyway requires globally unique version numbers. **This will cause the application to fail at startup** with an error like:

```
FlywayException: Found more than one migration with version 13
```

**Fix required**: Rename one of the conflicting migrations. Since V13 is the natural position for `och-ticket` in the version sequence (after V12 ai, before V14 knowledge), the `och-esl` migration should be reassigned to a version AFTER V17, such as **V18**.

---

## 4. High-Severity Issues

### 4.1 Missing Versions (Gaps)

Versions **V2** and **V5** are absent from all modules. These gaps do not cause errors but indicate either:
- Deleted migrations (possible rollback of removed features)
- Intentional reservation (future IVR or system module migrations)

Either way, Flyway will simply skip these versions. Not a runtime problem but worth documenting.

### 4.2 och-system: 32 Entities with NO Migration Scripts

The `och-system` module has **32 Java entities** annotated with `@TableName`, but **zero migration scripts** in `och-system/src/main/resources/db/migration/`. The directory does not exist.

Affected tables:
- **Call infrastructure**: `call_engine`, `call_display`, `call_display_pool`, `call_display_pool_rel`, `call_in_phone`, `call_in_phone_rel`, `call_record`, `call_route`, `call_schedule`, `call_skill`, `call_skill_agent_rel`
- **Corporate/Region**: `corp_info`, `och_region`, `och_area_code`, `phone_location`
- **FreeSWITCH**: `fs_acl`, `fs_cdr`, `fs_config`, `fs_dialplan`, `fs_modules`, `fs_sip_gateway`, `sip_agent`, `voice_file`
- **System**: `sys_user`, `sys_role`, `sys_menu`, `sys_role_menu`, `sys_user_role`, `sys_category`, `sys_file`, `sys_oper_log`
- **Knowledge organizers**: `ko_dispatcher`, `ko_subscriber`

**Impact**: These tables will never be created by Flyway. If the application attempts to use any of these entities, it will encounter `Table doesn't exist` SQL errors at runtime.

**Note**: The `fs_node` table IS created by V13 in `och-esl` — its entity lives in `och-system`, but the migration lives in `och-esl`. This cross-module dependency is architecturally fragile.

### 4.3 och-ivr: 4 Entities with NO Migration Scripts

`och-ivr` has 4 entities but no migration directory:
- `ivr_flow`
- `ivr_node`
- `ivr_edge`
- `ivr_execution`

These are likely the intended occupants of the missing **V2** or **V5** slots.

### 4.4 Duplicate Entity Name: `call_record` in Two Modules

Two different Java classes both map to the same table name `call_record`:

| Module | Class | Schema Style | Fields |
|--------|-------|-------------|--------|
| och-system | `CallRecord` | Legacy (create_by, create_time, del_flag) | 27 fields including CDR-level detail (call_state, answer_flag, hangup_dir, file_path, ringing_path) |
| och-call | `CallRecord` | Refactored (created_at, deleted) | 19 fields as post-call summary (session_id, ring_seconds, talk_seconds, hold_count) |

Both use `@TableName("call_record")` but the schemas are **fundamentally incompatible**. The V7 migration in `och-call` creates the table matching the `och-call` entity. The `och-system` entity maps to a different (non-existent via Flyway) version of the same table.

**Impact**: If MyBatis-Plus scans both modules' entity packages, there will be a mapper conflict. The `och-system` entity will also fail at runtime since its columns don't exist in the table created by V7.

---

## 5. Medium-Severity Issues

### 5.1 No Foreign Key Constraints Anywhere

Across all 16 migration scripts creating 42+ tables, **zero foreign key constraints are defined**. All cross-table references use logical foreign keys (BIGINT columns like `agent_id`, `session_id`, `queue_id`, `customer_id`) without `FOREIGN KEY ... REFERENCES ...` clauses.

Key relationships that lack FK enforcement:
- `call_session.queue_id` -> `acd_skill_group.id`
- `call_session.agent_id` -> (agent ID from system)
- `call_record.session_id` -> `call_session.id`
- `call_record.agent_id` -> (agent ID from system)
- `acd_agent_status.skill_group_id` -> `acd_skill_group.id`
- `acd_queue_session.skill_group_id` -> `acd_skill_group.id`
- `outbound_customer.task_id` -> `outbound_task.id`
- `outbound_dial_attempt.task_id` -> `outbound_task.id`
- `outbound_dial_attempt.customer_id` -> `outbound_customer.id`
- `customer_phone.customer_id` -> `customer_profile.id`
- `robot_flow_version.flow_id` -> `robot_flow.id`
- `quality_score.template_id` -> `quality_template.id`
- `quality_appeal.score_id` -> `quality_score.id`

This is a deliberate design choice (performance, microservice boundaries), but it is worth documenting as a known trade-off.

### 5.2 `foundation_migration_check` and `audit_event` Have No Java Entities

The V1 migration creates two tables with no corresponding `@TableName` entity in any module:
- `foundation_migration_check` — likely intentional (not an application entity, more of a migration tracking table)
- `audit_event` — references `user_id` pointing to a `sys_user` table that has no migration

### 5.3 `ai_barge_in_log` Has No Java Entity

V12 (och-ai) creates `ai_barge_in_log` table, but no corresponding entity class exists with `@TableName("ai_barge_in_log")`. The table comment says "(预留)" meaning "reserved", so this may be intentional.

---

## 6. Low-Severity Issues

### 6.1 Index Naming Convention Inconsistency

Index naming is mostly consistent but has minor variations:

| Convention | Example | Where Used |
|-----------|---------|------------|
| `idx_<column>` | `idx_state`, `idx_agent` | Most modules |
| `idx_<column1>_<column2>` | `idx_group_state`, `idx_task_state` | och-acd, och-call-task |
| `uk_<name>` | `uk_code`, `uk_call_id` | Most modules |
| `uk_<column1>_<column2>` | `uk_agent_date`, `uk_phone_customer` | och-report, och-customer |

Overall, these are acceptable variations. However, a project-wide convention document would help maintain consistency.

### 6.2 Column Naming Convention Differences

The `och-esl` V13 migration uses a **different column naming convention** from all other modules:

| och-esl (V13) | Other Modules |
|---------------|---------------|
| `create_by` | `created_by` |
| `create_time` | `created_at` |
| `update_by` | `updated_by` |
| `update_time` | `updated_at` |
| `del_flag` (TINYINT, 0/1) | `deleted` (TINYINT(1), 0/1) |

This reflects the `och-system` module's entity convention (which came from a previous architecture). As `och-esl` was integrated later, it inherited this legacy naming pattern.

### 6.3 Backtick-Quoted Identifiers in V13 (esl) Only

`V13__fs_node.sql` is the only migration that uses backtick-quoted SQL identifiers (MySQL-specific). All other migrations use unquoted identifiers. While functionally equivalent, it is a style inconsistency.

---

## 7. Summary of Required Actions

| Priority | Issue | Action |
|----------|-------|--------|
| **P0 - Critical** | V13 version conflict (och-ticket vs och-esl) | Rename `och-esl` migration from V13 to V18 (or higher) |
| **P1 - High** | och-system has 32 entities with no migrations | Either create Flyway migrations for these tables (V2, V5, V18+) OR remove the entities if they belong to a legacy system managed outside Flyway |
| **P1 - High** | och-ivr has 4 entities with no migrations | Create migrations (V2 or V5) for `ivr_flow`, `ivr_node`, `ivr_edge`, `ivr_execution` |
| **P1 - High** | Duplicate `call_record` entity across modules | Resolve the duplication: keep only one `CallRecord` class, or use different table names |
| **P2 - Medium** | No FK constraints | Document the design decision; consider adding FKs where referential integrity is critical |
| **P2 - Medium** | `fs_node` entity in och-system, migration in och-esl | Consolidate: move entity to och-esl or move migration to och-system |
| **P3 - Low** | Inconsistent column naming (esl) | Align esl V13 column names with project convention OR document as legacy pattern |
| **P3 - Low** | Missing V2, V5 | Document as intentional or assign to och-system / och-ivr migrations |

---

## 8. Aggregation Confirmation

The `och-api/pom.xml` confirms that `och-api` depends on all 16 sub-modules (including och-esl, och-ticket, och-system, och-ivr, och-ai, etc.). Flyway will scan the entire classpath and execute all discovered migrations in version order. This is why the V13 conflict is a **hard blocker** — both V13 scripts will be discovered simultaneously.
