# openCallHub Multi-Role Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the approved openCallHub end-to-end refactor design into a set of executable, role-owned implementation streams with clear phase gates.

**Architecture:** This is a master orchestration plan, not a single coding sprint. The approved spec covers 18 product domains and 9 implementation phases, so implementation must be split into independent phase plans before code changes start. Each phase produces working, testable software and has architecture, frontend, backend, communications, AI voice, DevOps, security, QA, and product review gates.

**Tech Stack:** Java 17, Spring Boot, Maven multi-module, MySQL, Redis, RabbitMQ, FreeSWITCH, Kamailio, Vue 3, Vite, Pinia, Ant Design Vue, LogicFlow, Docker Compose, Prometheus, Grafana.

---

## Scope Check

The design spec at `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md` covers multiple independent subsystems. It must not be executed as one giant plan. This master plan creates sub-plans per implementation phase:

1. Foundation and architecture base
2. System permission and resource management
3. Call core, IVR, and ACD
4. Agent workspace and inbound closure
5. Outbound center with preview, predictive, and robot modes
6. AI contact center
7. Customer, ticket, knowledge, and quality
8. Reports, notice, compliance, and batch
9. Deployment, observability, performance, and security acceptance

Each phase plan must be saved as `docs/superpowers/plans/2026-05-19-opencallhub-phase-N-<name>.md` before implementation starts.

---

## Role Ownership

| Role | Owns | Must Review |
|---|---|---|
| Product Expert | business closure, roles, KPIs, acceptance | every phase exit |
| Architect | module boundaries, state machines, events, maintainability | every backend-affecting task |
| Frontend Expert | navigation, workspace, forms/tables, designer UX | every UI task |
| Backend Expert | domain model, transactions, idempotency, concurrency, tests | every backend task |
| Communications Expert | FreeSWITCH, Kamailio, RTPengine, ESL, call recovery | phases 3-6 and 9 |
| AI Voice Expert | ASR/TTS/LLM/workflow, latency, barge-in, transfer | phases 5-6 and 8 |
| DevOps Expert | deploy, capacity, monitoring, alerting, backup, rollback | phases 1 and 9, plus infra tasks |
| Security Expert | internal isolation, permissions, secrets, audit, masking | every phase with data access |
| QA Expert | test strategy, E2E, regression, performance, safety gates | every phase exit |

---

## Collaboration Entry Document

`docs/superpowers/plans/2026-06-05-opencallhub-refactor-ownership-schedule.md` is the collaboration entry document for later contributors.

It records the concrete employee-to-scope ownership matrix, overall S0-S12 refactor schedule, phase owner matrix, refactor coding principles, and design-pattern usage boundaries. The master plan remains responsible for phase orchestration and technical acceptance gates; the collaboration entry document remains responsible for who owns each area and how new contributors join the refactor safely.

Update the collaboration entry document whenever employees, ownership boundaries, schedule, or phase gates change. Keep detailed task execution steps in the phase plans to avoid duplicating implementation details here.

---

## File Structure for Planning Deliverables

Create or modify only planning files in this master-plan step:

- Create: `docs/superpowers/plans/2026-05-19-opencallhub-master-implementation-plan.md`
  - Responsibility: this orchestration plan and role/phase ownership.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-1-foundation.md`
  - Responsibility: foundation implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-2-permission-resource.md`
  - Responsibility: system permissions and resource management implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-3-call-ivr-acd.md`
  - Responsibility: call core, IVR, ACD implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-4-agent-inbound.md`
  - Responsibility: agent workspace and inbound service closure.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-5-outbound.md`
  - Responsibility: preview, predictive, robot outbound implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-6-ai-contact-center.md`
  - Responsibility: AI plugins and robot conversation implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-7-customer-ticket-quality.md`
  - Responsibility: customer, ticket, knowledge, quality implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-8-report-notice-compliance-batch.md`
  - Responsibility: reporting, notice, compliance, batch implementation plan.
- Create later: `docs/superpowers/plans/2026-05-19-opencallhub-phase-9-production-acceptance.md`
  - Responsibility: deployment, observability, performance, security acceptance.

---

## Task 1: Create Phase 1 Foundation Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-1-foundation.md`

- [ ] **Step 1: Use writing-plans for Phase 1**

Run the planning skill with this exact scope:

```text
Create the Phase 1 implementation plan for openCallHub foundation and architecture base. Scope: Maven module foundation, unified response, unified exception handling, traceId, OpenAPI, permission interception skeleton, audit skeleton, RabbitMQ event abstraction, Redis wrapper, file storage abstraction, migration mechanism, Vue 3 app structure, route permission shell, menu shell, request wrapper, WebSocket wrapper, Pinia state conventions, layout shell, table/form/detail drawer patterns, login and home shell. Include TDD steps, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

Use these role owners in the phase plan:

```markdown
- Architect: module boundaries, base layering, event abstraction
- Backend Expert: Spring Boot foundation, response, exceptions, traceId, OpenAPI, Redis, RabbitMQ
- Frontend Expert: Vue app shell, routing, request wrapper, WebSocket wrapper, layout patterns
- DevOps Expert: local startup and environment variables
- Security Expert: permission interception skeleton and audit skeleton
- QA Expert: foundation test gates
```

- [ ] **Step 3: Add Phase 1 acceptance gate**

The phase plan must include this acceptance gate exactly:

```markdown
Phase 1 is accepted only when backend starts, frontend starts, login shell renders, OpenAPI is reachable, traceId appears in one request log and response, RabbitMQ publishes and consumes one test event, Redis read/write works, and database migration runs from a clean database.
```

- [ ] **Step 4: Review Phase 1 plan against the spec**

Expected result: every Phase 1 requirement from spec section 9 maps to at least one task in the phase plan.

- [ ] **Step 5: Commit Phase 1 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-1-foundation.md
git commit -m "docs: add phase 1 foundation implementation plan"
```

---

## Task 2: Create Phase 2 Permission and Resource Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-2-permission-resource.md`

- [ ] **Step 1: Use writing-plans for Phase 2**

```text
Create the Phase 2 implementation plan for openCallHub system permission and resource management. Scope: users, departments, roles, menus, button permissions, data scopes, login logs, operation audit, SIP accounts, extensions, phone numbers, number pools, trunk gateways, FreeSWITCH nodes, Kamailio nodes, audio assets, storage configs, AI plugin resources, resource dependency checks, resource audit. Include database migrations, APIs, frontend pages, dependency check UI, tests, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Product Expert: resource operations and admin role path
- Architect: resource dependency model and permission boundary
- Backend Expert: system/resource domain services and transactions
- Frontend Expert: permission center and resource center pages
- Communications Expert: SIP, trunk, FreeSWITCH, Kamailio resource semantics
- AI Voice Expert: AI plugin resource semantics
- Security Expert: data scope, audit, secret masking
- QA Expert: permission and dependency regression cases
```

- [ ] **Step 3: Add Phase 2 acceptance gate**

```markdown
Phase 2 is accepted only when role menus render differently by role, phone numbers/trunks/extensions/audio assets/AI plugins can be configured, resource dependencies prevent unsafe deletion, all resource mutations write audit records, and data scope filtering is demonstrated for at least two roles.
```

- [ ] **Step 4: Commit Phase 2 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-2-permission-resource.md
git commit -m "docs: add phase 2 permission and resource plan"
```

---

## Task 3: Create Phase 3 Call, IVR, and ACD Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-3-call-ivr-acd.md`

- [ ] **Step 1: Use writing-plans for Phase 3**

```text
Create the Phase 3 implementation plan for openCallHub call core, IVR, and ACD. Scope: och-call, och-esl boundary, call session, call leg, call event, call state machine, ESL event normalization, phone control command wrappers, IVR flow/version/node/edge/variable model, IVR runtime engine, menu node, schedule check node, transfer voicemail node, play, collect digits, condition, api call, transfer agent, transfer skill group, hangup, voicemail box/messages, skill groups, queues, agent status machine, dispatch strategies, overflow rules, SLA rules, queue monitor, IVR test run and execution trace. Include tests for state machines, idempotency, event ordering, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Architect: call/IVR/ACD state machines and event consistency
- Backend Expert: domain models, transactions, idempotency, concurrency
- Communications Expert: FreeSWITCH, ESL, DTMF, bridge, recording, recovery
- Frontend Expert: IVR designer, node panels, ACD config and monitor pages
- Product Expert: IVR and ACD business closure
- QA Expert: inbound E2E and failure simulation
- Security Expert: call data and recording access controls
```

- [ ] **Step 3: Add Phase 3 acceptance gate**

```markdown
Phase 3 is accepted only when an inbound call can enter IVR, menu DTMF routes to the correct branch, non-business hours can route to voicemail, business hours can enter ACD, ACD assigns an available agent by configured strategy, IVR execution trace is viewable, and queue metrics update in real time.
```

- [ ] **Step 4: Commit Phase 3 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-3-call-ivr-acd.md
git commit -m "docs: add phase 3 call ivr acd plan"
```

---

## Task 4: Create Phase 4 Agent Workspace and Inbound Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-4-agent-inbound.md`

- [ ] **Step 1: Use writing-plans for Phase 4**

```text
Create the Phase 4 implementation plan for openCallHub agent workspace and inbound closure. Scope: agent status state machine, phone bar APIs, WebSocket agent events, screen pop, customer card, call controls answer/hangup/hold/unhold/mute/unmute/blind transfer/consult transfer, after-call work, call record, recording record, service record, simple customer association, inbound report aggregation. Include frontend immersive workspace, tests, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Product Expert: agent journey and after-call workflow
- Frontend Expert: immersive workspace and high-frequency interactions
- Backend Expert: phone bar API and state persistence
- Communications Expert: bridge, hold, transfer, hangup event correctness
- QA Expert: inbound E2E regression
- Security Expert: customer and recording visibility
```

- [ ] **Step 3: Add Phase 4 acceptance gate**

```markdown
Phase 4 is accepted only when an agent can go online, receive screen pop, answer, hold, transfer, hang up, submit after-call work, and then see call record, recording record, and service record with agent state driving ACD availability.
```

- [ ] **Step 4: Commit Phase 4 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-4-agent-inbound.md
git commit -m "docs: add phase 4 agent inbound plan"
```

---

## Task 5: Create Phase 5 Outbound Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-5-outbound.md`

- [ ] **Step 1: Use writing-plans for Phase 5**

```text
Create the Phase 5 implementation plan for openCallHub outbound center. Scope: outbound task model, preview dialing, predictive dialing, robot dialing task orchestration, customer list import, DNC filtering, blacklist filtering, outbound time window validation, retry rules, number pool and trunk concurrency, predictive dialing tuning, abandonment protection, task progress WebSocket, result statistics. Include tests for duplicate dialing prevention, concurrency, rate tuning, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Product Expert: outbound task creation wizard and metrics
- Architect: outbound state machine and task boundaries
- Backend Expert: task scheduling, locks, idempotency, retry rules
- Communications Expert: outbound dialing, bridge, failure handling
- AI Voice Expert: robot outbound handoff to AI session
- Frontend Expert: task wizard, preview UI, predictive monitor, robot session list
- QA Expert: three-mode outbound E2E
- Security Expert: DNC, blacklist, task data scope
```

- [ ] **Step 3: Add Phase 5 acceptance gate**

```markdown
Phase 5 is accepted only when preview outbound can claim and dial a customer, predictive outbound can auto-tune while respecting abandonment thresholds, robot outbound can schedule calls, DNC/blacklist/time-window checks are enforced, task progress updates in real time, and outbound results are aggregated.
```

- [ ] **Step 4: Commit Phase 5 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-5-outbound.md
git commit -m "docs: add phase 5 outbound plan"
```

---

## Task 6: Create Phase 6 AI Contact Center Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-6-ai-contact-center.md`

- [ ] **Step 1: Use writing-plans for Phase 6**

```text
Create the Phase 6 implementation plan for openCallHub AI contact center. Scope: ASR plugin config and call, TTS plugin config and call, LLM/workflow plugin abstraction, Dify adapter, RAGFlow adapter, LangFlow adapter, internal robot flow engine, bot version management, AI conversation trace, intent result, slot values, summary, AI quality result, plugin call logs, cost statistics, barge-in design hooks, silence/no-response handling, transfer-to-agent conditions. Include tests with mocked plugin providers, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- AI Voice Expert: ASR/TTS/LLM/workflow chain, latency, barge-in, context, transfer
- Backend Expert: plugin abstraction, logs, cost, sessions
- Communications Expert: audio playback/recording integration points
- Frontend Expert: plugin config, bot management, AI session trace
- Product Expert: robot outcomes and AI metrics
- Security Expert: encrypted secrets and AI content audit
- QA Expert: timeout, retry, silence, no-response, transfer tests
```

- [ ] **Step 3: Add Phase 6 acceptance gate**

```markdown
Phase 6 is accepted only when ASR/TTS plugins can be configured and tested, Dify/RAGFlow/LangFlow adapters can be configured, one robot outbound conversation completes end-to-end in a mocked provider test, AI conversation trace is complete, summary/tags/cost are visible, and plugin failures produce logs and alerts.
```

- [ ] **Step 4: Commit Phase 6 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-6-ai-contact-center.md
git commit -m "docs: add phase 6 ai contact center plan"
```

---

## Task 7: Create Phase 7 Customer, Ticket, Knowledge, and Quality Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-7-customer-ticket-quality.md`

- [ ] **Step 1: Use writing-plans for Phase 7**

```text
Create the Phase 7 implementation plan for openCallHub customer, ticket, knowledge, and quality. Scope: customer profiles, customer phones, customer pools, tags, follow records, assignment/recycle rules, duplicate detection, tickets, service records, SLA rules, ticket flow, timeout reminders, FAQ, knowledge documents, scripts, script versions, publish records, usage statistics, quality templates, quality rules, manual quality scoring, AI quality review, appeals, review records. Include pages, APIs, migrations, tests, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Product Expert: service closure and role paths
- Backend Expert: customer/ticket/knowledge/quality domains
- Frontend Expert: customer detail, ticket center, knowledge center, quality workspace
- AI Voice Expert: AI quality review integration
- Security Expert: customer data masking and data scope
- QA Expert: customer-service-quality E2E
```

- [ ] **Step 3: Add Phase 7 acceptance gate**

```markdown
Phase 7 is accepted only when customers can be imported, assigned, followed up, linked to calls and tickets, agents can search knowledge and scripts, quality users can score calls, AI quality results can be reviewed, and full customer service history is visible under data permissions.
```

- [ ] **Step 4: Commit Phase 7 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-7-customer-ticket-quality.md
git commit -m "docs: add phase 7 customer ticket quality plan"
```

---

## Task 8: Create Phase 8 Report, Notice, Compliance, and Batch Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-8-report-notice-compliance-batch.md`

- [ ] **Step 1: Use writing-plans for Phase 8**

```text
Create the Phase 8 implementation plan for openCallHub report, notice, compliance, and batch. Scope: report aggregation jobs, inbound reports, outbound reports, agent performance, queue SLA reports, AI robot reports, quality reports, notice messages, notice templates, send logs, read states, batch jobs, import/export records, item errors, sensitive data masking, recording access policy, export audit, archive policy, AI content trace. Include migrations, APIs, frontend pages, async task progress, tests, exact files, commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- Product Expert: operational metrics and dashboards
- Backend Expert: aggregation, async jobs, notice, compliance services
- Frontend Expert: report center, notice center, batch progress, audit pages
- DevOps Expert: data retention and archive jobs
- Security Expert: masking, recording access, export audit
- QA Expert: report accuracy and compliance regression
```

- [ ] **Step 3: Add Phase 8 acceptance gate**

```markdown
Phase 8 is accepted only when daily reports aggregate, notifications can be sent/read, imports and exports show progress and failure details, masking is enforced, recording access is permission-controlled, export actions are audited, and batch retries are visible.
```

- [ ] **Step 4: Commit Phase 8 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-8-report-notice-compliance-batch.md
git commit -m "docs: add phase 8 report notice compliance batch plan"
```

---

## Task 9: Create Phase 9 Production Acceptance Plan

**Files:**
- Read: `docs/superpowers/specs/2026-05-19-opencallhub-refactor-design.md`
- Create: `docs/superpowers/plans/2026-05-19-opencallhub-phase-9-production-acceptance.md`

- [ ] **Step 1: Use writing-plans for Phase 9**

```text
Create the Phase 9 implementation plan for openCallHub production acceptance. Scope: Docker Compose production package, Nginx config, HTTPS guidance, MySQL init and migration scripts, FreeSWITCH config, Kamailio config, Prometheus config, Grafana dashboards, log collection, backup scripts, restore drill, operations manual, performance tests, security tests, deployment acceptance checklist. Include exact files, verification commands, expected results, and commit checkpoints.
```

- [ ] **Step 2: Assign role owners**

```markdown
- DevOps Expert: compose, nginx, monitoring, logs, backup, restore, rollback
- Communications Expert: SIP registration, FreeSWITCH, Kamailio, RTP path checks
- Backend Expert: health checks and metrics
- Frontend Expert: production build and static deployment
- Security Expert: HTTPS, secrets, audit, security test report
- QA Expert: full regression and production acceptance checklist
- Product Expert: final demo scenarios
```

- [ ] **Step 3: Add Phase 9 acceptance gate**

```markdown
Phase 9 is accepted only when Docker Compose starts all core services, SIP extension registration works, inbound/outbound/robot chains work, Prometheus and Grafana show metrics, logs are searchable, database backup and restore are verified, access permissions are secure, performance thresholds pass, and failure drills are documented.
```

- [ ] **Step 4: Commit Phase 9 plan**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-9-production-acceptance.md
git commit -m "docs: add phase 9 production acceptance plan"
```

---

## Task 10: Review the Plan Set Before Implementation

**Files:**
- Read: all `docs/superpowers/plans/2026-05-19-opencallhub-phase-*.md`
- Modify: phase plans only if gaps are found

- [ ] **Step 1: Check spec coverage**

Use this checklist:

```markdown
- [ ] 18 product domains are mapped to phase plans.
- [ ] IVR menu, schedule, voicemail nodes are in Phase 3.
- [ ] Preview, predictive, robot outbound are in Phase 5.
- [ ] ASR/TTS/Dify/RAGFlow/LangFlow are in Phase 6.
- [ ] RabbitMQ and Kafka extension point are in Phase 1 or Phase 3.
- [ ] Internal data isolation, secrets, masking, export control are in Phases 2 and 8.
- [ ] Docker Compose, monitoring, backup, rollback are in Phase 9.
- [ ] Tests and acceptance gates exist in every phase.
```

- [ ] **Step 2: Scan for forbidden placeholders**

Search each plan for these strings and replace them with exact details:

```text
TBD
TODO
待定
待补充
implement later
similar to
add appropriate
write tests for the above
```

Expected result: no occurrences remain in plan files.

- [ ] **Step 3: Check type and name consistency**

Confirm these names are used consistently across plans:

```text
traceId
callId
uuid
legId
agentId
taskId
ivrExecutionId
queueSessionId
eventId
outbox
preview
predictive
robot
```

- [ ] **Step 4: Commit review fixes**

```bash
git add docs/superpowers/plans/2026-05-19-opencallhub-phase-*.md
git commit -m "docs: align opencallhub phase plans with refactor spec"
```

---

## Execution Recommendation

Use subagent-driven execution after the phase plans are generated. Dispatch one fresh subagent per phase-plan task or per role-specific slice inside a phase. Do not let multiple agents modify the same files concurrently. Use these safe parallel lanes after Phase 1 is complete:

- Frontend design system and shell
- Backend resource domain
- Backend event/state-machine foundation
- DevOps compose and observability baseline
- Security permission and audit model

Within a phase, run reviews in this order:

1. Implementer self-test
2. Backend/frontend/domain review by role owner
3. Security review if permissions, secrets, customer data, recordings, or exports are touched
4. QA verification
5. Product acceptance

---

## Master Plan Self-Review

Spec coverage:
- Product domains map to the nine phase plans.
- IVR menu, schedule, voicemail are included in Phase 3.
- Preview, predictive, robot outbound are included in Phase 5.
- ASR/TTS/Dify/RAGFlow/LangFlow are included in Phase 6.
- Resource management and permissions are included in Phase 2.
- Reports, notice, compliance, batch are included in Phase 8.
- Deployment and observability are included in Phase 9.

Placeholder scan:
- This master plan contains no deferred implementation placeholders.

Type consistency:
- Shared identifiers are defined in Task 10 and must be enforced during phase-plan review.
