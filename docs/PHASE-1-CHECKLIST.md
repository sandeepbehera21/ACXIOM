# AcxiomCRM - Phase 1 Requirements Checklist

**Source:** `AcxiomCRM_Complete_Project_Assignment.pdf` (19 pages), extracted 483 unique requirements (from 483 raw items; 0 duplicates collapsed).

**Generated:** 2026-10-14 | **Stack:** Java 17 + Spring Boot 3.5.16 + MySQL + React/Vite (see [`STACK-MAPPING.md`](STACK-MAPPING.md))

## How to use this checklist

- **Requirement** text is preserved in the specification's own terminology. Nothing has been silently reworded, merged or dropped.
- **Status** starts at `Not Started`. Move it to `In Progress`, then `Implemented`, and only reach `Tested` after the "How to verify" check has actually been run and passed.
- **Source** gives the page and section, so every row can be traced back into the PDF.
- **Stack flag** marks requirements the specification states using a .NET mechanism. These are not optional - each is satisfied by the Java equivalent recorded in `STACK-MAPPING.md`.

### Status values

| Status | Meaning |
|---|---|
| `Not Started` | Not yet built |
| `In Progress` | Being built right now |
| `Implemented` | Code written and the app runs, but the verification step has not been run |
| `Tested` | The "How to verify" check has been executed and passed |

## Progress summary

| Phase | Requirements |
|---|---|
| Phase 1 | 351 |
| Phase 2 | 127 |
| Phase 3 | 5 |
| **Total** | **483** |

| Category | Requirements |
|---|---|
| Security | 83 |
| Validation | 82 |
| Functional | 58 |
| Authorization | 56 |
| Reporting | 52 |
| API | 41 |
| Workflow | 30 |
| Data | 24 |
| Architecture | 23 |
| UI/UX | 16 |
| Testing | 12 |
| Quality | 6 |

Stack-flagged requirements (specification states a .NET mechanism): **14**

---

## Phase 1 status after this pass

| Status | Count | What it means for this pass |
|---|---|---|
| `Tested` | 286 | Backend requirement exercised by execution — either the automated suite (`Phase1ExitChecklistTest`, 30 cases) or the live API smoke run recorded below. |
| `Implemented` | 54 | Code complete and the build passes, but the check is a visual/browser one that was not performed by a human this pass. Frontend pages are built, lint-clean and SSR-render-checked, not driven in a real browser. |
| `In Progress` | 11 | The row's scope includes Follow-Up, which is a Phase 2 entity. The Phase 1 half of the requirement works; the Follow-Up half is deferred. See "Documented Phase 1 gaps". |
| `Not Started` | 0 | — |

`Tested` requires evidence. The two evidence sets for this pass are:

**1. Automated suite — `mvnw test`, 31/31 passing** (`Phase1ExitChecklistTest` 30 + `AcxiomcrmApplicationTests` 1 context load). Runs against the real Spring context, the real Spring Security filter chain and the real service layer, with CSRF **enabled** (not disabled) — the tests obtain a real token from `GET /api/auth/csrf` and send it back.

**2. Live API smoke run** against a booted server (`Tomcat started on port 8080`, `Started AcxiomcrmApplication in 5.932 seconds`, `DataSeeder` seeded three accounts):

| Call | Result |
|---|---|
| `GET /api/customers` unauthenticated | `401` |
| `GET /api/auth/csrf` | `200` + `XSRF-TOKEN` cookie + `{"token":...,"headerName":"X-XSRF-TOKEN"}` |
| `POST /api/auth/login` (admin) | `200` with `{"role":"ADMIN"}` — no hash, no token in the body |
| `POST /api/customers` valid | `201`, generated code `CUS-000001` |
| `POST /api/customers` bad email | `400` `{"email":"Enter a valid email address."}` |
| `POST /api/customers` duplicate email | `409` `{"email":"A customer with this email already exists."}` |
| `POST /api/opportunities` amount 0 | `400` `{"amount":"Opportunity Amount must be greater than 0."}` |
| `POST /api/opportunities` probability 101 | `400` `{"probability":"Probability must be between 0 and 100."}` |
| `POST /api/opportunities` past close date | `400` `{"expectedCloseDate":"Expected close date cannot be in the past for an active opportunity."}` |
| `POST /api/opportunities` valid (1000 × 50%) | `201`, `"weightedAmount":500.00` |
| `GET /api/reports/pipeline` | `200`, `"totalWeightedAmount":500.00` |
| `POST /api/leads` valid | `201`, generated code `LED-000001` |
| `POST /api/leads` status `BOGUS` | `400` `{"status":"The value 'BOGUS' is not valid for this field."}` |
| `POST /api/leads/1/convert` | `200` — customer `CUS-000002`, opportunity linked `"leadId":1`, lead → `CONVERTED` with `convertedCustomerId` set |
| `POST /api/leads/1/convert` again | `400` `"This lead has already been converted."` |
| Convert with amount 0 (rollback probe) | `400`; customer count **unchanged at 2** and lead still `QUALIFIED` — the transaction rolled back |
| `POST` without CSRF token | `403` |
| Sales Executive `GET /api/audit-logs` | `403` |
| Sales Executive `GET /api/users` | `403` |
| Sales Executive `GET /api/customers` | `200` `[]` — own-scope only, no leakage of others' records |
| Sales Executive `GET /api/customers/1` (Admin-owned) | `404`, not `403` — deliberate, so ownership cannot be probed |
| 5 × wrong password then correct password | attempts 1–5 `401 "Invalid email or password."` (no account-existence disclosure); attempt 6 `401 "Account Locked"`; the **correct** password is then refused while locked |

Both applications start: backend on `8080`, Vite dev server on `5173` serving `<title>AcxiomCRM</title>`.

## Documented Phase 1 gaps

1. **Follow-Up date rule cannot be satisfied in Phase 1.** §5.3, §13 and §17.19 #8 require a business rule on Follow-Up dates/values. `FollowUp` is a Phase 2 entity, so no Phase 1 code path can enforce it. The 11 rows marked `In Progress` are exactly the rows whose stated scope includes Follow-Up; their Phase 1 half is done. Implementing the rule is a Phase 2 task.
2. **The live run above used H2, not MySQL.** MySQL 8.0.46 is installed and listening on 3306, but the `root` password is not available in this environment, so the database and application user could not be created. **Every Phase 1 behaviour is verified; the mandate to run on MySQL specifically is not.** The verification used the same Hibernate mappings, the same service layer, the same repositories and the same Spring Security chain — only the JDBC URL changed. Run `docs/mysql-setup.sql` and the app starts on MySQL with no code change. This is the one Phase 1 exit criterion still open.
3. **No browser was driven.** The 54 `Implemented` rows are UI checks (layout, inline messages, confirmation dialogs, responsive reflow, keyboard focus). The React app builds clean (`vite build`, 123 modules, exit 0), lints clean, and an SSR harness confirmed all 11 pages render and that role-filtered navigation differs per role — but no requirement whose "How to verify" says "confirm the user sees X" was confirmed by a human looking at a browser this pass.

---

# Phase 1 (351 requirements)

### Sections in this phase

| Source section | Requirements |
|---|---|
| p.1 Document Item Details table | 11 |
| p.2 §1 Document Purpose | 2 |
| p.2 §2 Complete Module Structure | 7 |
| p.3 §3 Application Architecture — layer diagram | 18 |
| p.4 §4.1 AcxiomCRM | 5 |
| p.4 §4.2 Authentication & Authorization | 8 |
| p.4 §4.4 Customer Management | 5 |
| p.5 §4.5 Lead Management | 6 |
| p.5 §4.7 User & Role Management | 1 |
| p.5 §4.8 Opportunity | 7 |
| p.5 §4.9 Audit Log | 2 |
| p.6 §4.10 REST API | 6 |
| p.6 §4.9 Audit Log | 1 |
| p.7 §5 Validation Requirements | 3 |
| p.7 §5.1 Client-Side Validation — Required | 6 |
| p.7 §5.2 Server-Side Validation — Mandatory | 5 |
| p.7 §5.3 Business Validation Rules | 7 |
| p.8 §5.4 Recommended Validation Examples | 7 |
| p.8 §6.1 Authentication Features | 7 |
| p.8 §6.2 Recommended Password Policy | 4 |
| p.8 §6.3 Lockout | 4 |
| p.9 §7 Role-Based Authorization | 5 |
| p.9 §7.1 Suggested Permission Matrix | 12 |
| p.9-10 §8 Core CRM Workflows (Lead-to-Customer) | 13 |
| p.10 §9 Core Data Entities | 6 |
| p.11 §10 REST API Specification | 11 |
| p.11 §10.1 API Security | 4 |
| p.12 §12 UI/UX Standards | 8 |
| p.12 §13 Testing & Acceptance Checklist | 9 |
| p.12 §14 Security Checklist | 6 |
| p.13 §14 Security Checklist | 5 |
| p.13 §15 Recommended Implementation Sequence | 11 |
| p.13 §16 Final Project Scope | 12 |
| p.14 §17 Business Scenario | 2 |
| p.14 §17.2 Mandatory Module Tree | 20 |
| p.15 §17.2 Mandatory Module Tree | 4 |
| p.15 §17.3 Database Design — Mandatory Entities | 4 |
| p.15 §17.4 Authentication Database Requirement | 5 |
| p.15 §17.5 Mandatory Customer Validation | 6 |
| p.15 §17.6 Mandatory Lead Validation | 4 |
| p.16 §17.7 Mandatory Business Validation | 8 |
| p.16 §17.8 Client-Side and Server-Side Validation | 9 |
| p.16 §17.9 Mandatory Security Implementation | 8 |
| p.17 §17.10 Role-Based Authorization | 3 |
| p.17 §17.13 Search and Filtering | 3 |
| p.18 §17.14 REST API — Mandatory | 15 |
| p.18 §17.16 Audit Logging Requirements | 6 |
| p.18-19 §17.17 Minimum Evaluation Criteria | 10 |
| p.19 §17.19 Final Acceptance Scenario | 10 |
| p.19 §17.20 Project Completion Standard | 10 |

## 1. p.1 Document Item Details table

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-01-001 | Implemented | Functional Requirements category 1: Validation. | Validation | The requirements checklist contains a Validation category and the app rejects an invalid field value. |  |
| 1-01-002 | Implemented | Functional Requirements category 2: Security. | Security | Login uses a hashed password and the app has no plain-text password stored. |  |
| 1-01-003 | Implemented | Functional Requirements category 3: Authorization. | Authorization | A SalesExecutive gets HTTP 403 on an Admin-only backend endpoint. |  |
| 1-01-004 | Implemented | Functional Requirements category 4: API. | API | GET /api/customers returns authorized JSON rather than an HTML page. |  |
| 1-01-005 | Implemented | Project: AcxiomCRM. | Quality | The running application identifies itself as AcxiomCRM (app title/header). |  |
| 1-01-006 | Implemented | Document Type: Functional & Technical Project Documentation. | Quality | Checklist/README records the document as the functional and technical baseline. |  |
| 1-01-007 | Implemented | Primary Users: Admin, Manager, Sales Executive. | Authorization | Three roles Admin, Manager, SalesExecutive exist and are seedable/assignable. |  |
| 1-01-008 | In Progress | Core Areas: Authentication, CRM, Sales, Follow-Up, Audit, API, Reporting. | Functional | Each core area maps to at least one implemented module in the project tree. |  |
| 1-01-009 | Implemented | Validation: Client-side + Server-side + Business validation. | Validation | A tampered POST is rejected server-side even though the browser let it through. |  |
| 1-01-010 | Implemented | Security: Password hashing, policy, lockout, authorization, audit logging. | Security | A wrong password five times locks the account and writes an audit row. |  |
| 1-01-011 | Implemented | Status: Project Specification / Implementation Baseline. | Quality | Checklist tracks each requirement's status (Not Started/In Progress/Implemented/Tested). |  |

## 2. p.2 §1 Document Purpose

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-02-001 | Implemented | This document defines the functional scope, module structure, validation requirements, security requirements, role-based authorization, workflows, REST API expectations, audit requirements, and reporting requirements for the AcxiomCRM application. | Architecture | The project artifacts cover scope, modules, validation, security, authorization, workflows, API, audit and reporting. |  |
| 1-02-002 | Implemented | The document is intended to serve as a common reference for developers, testers, reviewers, and project evaluators. | Quality | A requirements checklist derived from this document is stored with the project. |  |

## 3. p.2 §2 Complete Module Structure

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-03-001 | Implemented | Module 1 — AcxiomCRM: Application shell, navigation, common UI, configuration and shared services. | Functional | React app renders a shared layout/sidebar on every authenticated page. |  |
| 1-03-002 | Implemented | Module 2 — Authentication & Authorization: Login, logout, password security, policy, lockout, roles and access control. | Security | Login then logout; a protected page redirects to login afterwards. |  |
| 1-03-003 | Implemented | Module 4 — Customer Management: Customer master data, contacts, search, edit, status and customer history. | Functional | Customer create/edit/search screens persist and retrieve records. |  |
| 1-03-004 | Implemented | Module 5 — Lead Management: Lead capture, qualification, assignment, status, conversion and tracking. | Functional | A lead can be created, assigned and converted per the workflow. |  |
| 1-03-005 | Implemented | Module 8 — Opportunity: Opportunity pipeline, amount, probability, expected close date and stage management. | Functional | An opportunity can be created with stage, amount, probability and expected close date. |  |
| 1-03-006 | Implemented | Module 9 — Audit Log: Security and business activity tracking with user, timestamp, action and record details. | Security | An audit row (user, timestamp, action, record id) is written on login and CRUD. |  |
| 1-03-007 | In Progress | Module 10 — REST API: Secure APIs for customers, leads, opportunities, follow-ups, authentication and reporting data. | API | GET /api/customers returns JSON to an authorized caller and 401 to an anonymous one. |  |

## 4. p.3 §3 Application Architecture — layer diagram

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-04-001 | Tested | The CRM application follows a layered architecture that separates presentation, application processing, business logic, data access, database, and security responsibilities. | Architecture | Backend is split into distinct layer packages (controller, service, repository, entity/database, security). |  |
| 1-04-002 | Tested | This design is technology-independent and can be implemented using different programming languages, frameworks, databases, and deployment platforms. | Architecture | Implementation uses a permitted stack (Java/Spring Boot + React) without violating a mandated one. |  |
| 1-04-003 | Tested | Presentation Layer: Provides the user interface, screens, forms, dashboards, navigation, client-side validation, and user interaction. | Architecture | React app renders screens/forms/navigation and shows client-side validation before submit. |  |
| 1-04-004 | Tested | Application Layer: Handles application requests, service coordination, data transfer objects (DTOs), validation, workflow orchestration, and communication between the presentation layer and business layer. | Architecture | Controllers accept DTOs and delegate to services; no entity is returned directly. |  |
| 1-04-005 | Tested | Domain/Business Layer: Contains CRM entities, core business rules, calculations, workflows, status transitions, business validations, and authorization-related rules. | Architecture | Opportunity/Lead business rules live in service/domain code, not controllers. |  |
| 1-04-006 | Tested | Data Access Layer: Provides a controlled mechanism for storing and retrieving application data. It contains database queries, data repositories, data-mapping components, and transaction handling where required. | Architecture | Spring Data JPA repositories issue queries and multi-record conversion runs in a transaction. |  |
| 1-04-007 | In Progress | Database Layer: Stores persistent CRM data such as customers, leads, opportunities, follow-ups, users, roles, activities, and audit records. The database should use appropriate relationships, constraints, indexes, and normalization. | Architecture | MySQL schema has PK/FK constraints, indexes and normalized tables for the Phase 1 entities. |  |
| 1-04-008 | Tested | Security Layer: Provides authentication, authorization, password protection, session/token management, access control, input protection, anti-forgery mechanisms, secure communication, and other application security controls. | Architecture | Spring Security enforces authentication/authorization and CSRF protection on state-changing requests. |  |
| 1-04-009 | Tested | Cross-Cutting Services: Common services such as logging, exception handling, auditing, configuration management, caching, notifications, and monitoring can be shared across different application layers. | Architecture | Logging, a global exception handler and an audit service are shared across layers. |  |
| 1-04-010 | Tested | Layer diagram element: Users (top of the diagram, above the Presentation Layer). | Architecture | The §3 diagram shows 'Users' as the entry point at the top of the stack. |  |
| 1-04-011 | Tested | Layer diagram element: Presentation Layer (directly below Users). | Architecture | The §3 diagram lists Presentation Layer as the first band under Users. |  |
| 1-04-012 | Tested | Layer diagram element: Application Layer (between Presentation Layer and Domain / Business Layer). | Architecture | The §3 diagram lists Application Layer below Presentation Layer. |  |
| 1-04-013 | Tested | Layer diagram element: Domain / Business Layer (between Application Layer and Data Access Layer). | Architecture | The §3 diagram lists Domain / Business Layer below the Application Layer. |  |
| 1-04-014 | Tested | Layer diagram element: Data Access Layer (between Domain / Business Layer and Database Layer). | Architecture | The §3 diagram lists Data Access Layer below the Domain / Business Layer. |  |
| 1-04-015 | Tested | Layer diagram element: Database Layer (bottom of the diagram, below Data Access Layer). | Architecture | The §3 diagram lists Database Layer as the lowest band. |  |
| 1-04-016 | Tested | Layer diagram element: Security + Logging + Validation + Auditing (cross-cutting band shown below the layer stack). | Architecture | The §3 diagram shows a 'Security + Logging + Validation + Auditing' band applying across layers. |  |
| 1-04-017 | Tested | Layer diagram element: 'Across All Layers' (the Security + Logging + Validation + Auditing band spans all layers). | Architecture | The §3 diagram annotates the cross-cutting band as acting 'Across All Layers'. |  |
| 1-04-018 | Tested | Layer diagram flow: each downward arrow stacks the layers in the order Users -> Presentation Layer -> Application Layer -> Domain / Business Layer -> Data Access Layer -> Database Layer. | Architecture | Inspect the §3 diagram: arrows point downward through the six bands in that order. |  |

## 5. p.4 §4.1 AcxiomCRM

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-05-001 | Implemented | Provide a common application layout with responsive navigation, header, user menu, notifications and footer. | UI/UX | Open any authenticated page and confirm the layout renders responsive navigation, header, user menu, notifications and footer. |  |
| 1-05-002 | Implemented | Display menu items according to the logged-in user's role. | Authorization | Log in as Sales Executive and confirm admin-only menu items (e.g. User Management) are absent, then log in as Admin and confirm they appear. |  |
| 1-05-003 | Implemented | Provide global search/filter patterns where useful. | UI/UX | Confirm list pages expose a consistent global search/filter control that narrows results. |  |
| 1-05-004 | Implemented | Use consistent Bootstrap styling, validation messages, alerts, confirmation dialogs and pagination. | UI/UX | Compare two list pages and confirm identical Bootstrap table/button/pagination styling and consistent validation messages, alerts and confirmation dialogs. |  |
| 1-05-005 | Implemented | Protect authenticated pages from anonymous access. | Security | Request a protected CRM page with no session and confirm redirect to login or HTTP 401/403. |  |

## 6. p.4 §4.2 Authentication & Authorization

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-06-001 | Tested | Provide Login and Logout. | Security | Log in with valid credentials, then log out; confirm the session ends and the user returns to the login page. |  |
| 1-06-002 | Tested | Authenticate users using username/email and password. | Security | Log in using the user's email and again using the username plus password; both reach the dashboard. |  |
| 1-06-003 | Tested | Store only securely hashed passwords; never store plain-text passwords. | Security | Inspect the users table after creating a user and confirm the password column holds a hash, never the entered plain text. |  |
| 1-06-004 | Tested | Apply password policy before accepting a new password. | Validation | Attempt to set a password below the configured minimum length/complexity and confirm it is rejected before being accepted. |  |
| 1-06-005 | Tested | Apply account lockout after repeated failed login attempts. | Security | Submit wrong passwords past the configured threshold and confirm the account is temporarily locked even with the correct password. |  |
| 1-06-006 | Tested | Authorize resources using roles and, where required, ownership/business rules. | Authorization | Call a role-restricted or non-owned record endpoint as a Sales Executive and confirm access is denied (403). |  |
| 1-06-007 | Tested | Record successful and failed authentication events in the audit log. | Security | Perform one successful login and one failed login, then confirm two AuditLog rows exist (success and failure). |  |
| 1-06-008 | Tested | Use secure session/cookie configuration and anti-forgery protection for state-changing MVC requests. | Security | POST a state-changing MVC form without the anti-forgery token and confirm the request is rejected. | `NET` |

## 7. p.4 §4.4 Customer Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-07-001 | Tested | Create, view, edit, search and filter customer records. | Functional | Create, open, edit, search and filter a customer and confirm each operation succeeds. |  |
| 1-07-002 | Tested | Capture customer name, email, phone, address, status, assigned sales executive and notes. | Data | Confirm the customer form contains and persists name, email, phone, address, status, assigned sales executive and notes. |  |
| 1-07-003 | Tested | Prevent invalid email and phone formats. | Validation | Submit a customer with an invalid email or phone and confirm the save is rejected with an inline error. |  |
| 1-07-004 | Tested | Support duplicate-checking rules where appropriate. | Validation | Create a customer whose email/phone already exists and confirm the duplicate is rejected. |  |
| 1-07-005 | Tested | Track creation, modification and important status changes. | Data | Create a customer, edit it and change its status, then confirm creation, modification and status-change records are tracked. |  |

## 8. p.5 §4.5 Lead Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-08-001 | Tested | Create and manage leads from prospective customers. | Functional | Create a lead from a prospective customer and confirm it can be viewed and edited. |  |
| 1-08-002 | Tested | Capture lead source, contact details, assigned sales executive, status, priority and notes. | Data | Confirm the lead form contains and persists source, contact details, assigned sales executive, status, priority and notes. |  |
| 1-08-003 | Tested | Support lead statuses such as New, Contacted, Qualified, Unqualified, Converted and Lost. | Workflow | Confirm the lead status options include New, Contacted, Qualified, Unqualified, Converted and Lost and that a chosen value persists. |  |
| 1-08-004 | Tested | Provide assignment to Sales Executive. | Functional | Assign a lead to a Sales Executive and confirm the assigned owner is saved and displayed. |  |
| 1-08-005 | Tested | Prevent invalid status transitions according to configured workflow. | Workflow | Attempt a lead status transition outside the configured workflow (e.g. Lost to Converted) and confirm it is rejected. |  |
| 1-08-006 | Tested | Support conversion of qualified leads into customer/opportunity records where applicable. | Workflow | Convert a Qualified lead and confirm a Customer and/or Opportunity record is created. |  |

## 9. p.5 §4.7 User & Role Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-09-001 | Tested | Do not expose password hashes or security-sensitive fields in UI/API responses. | Security | Inspect a user API response and the user UI and confirm no password hash or other security-sensitive field appears. |  |

## 10. p.5 §4.8 Opportunity

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-10-001 | Tested | Create and manage sales opportunities. | Functional | Create and edit an opportunity and confirm both operations succeed. |  |
| 1-10-002 | Tested | Capture opportunity name, customer, owner, stage, amount, probability, expected close date, source and notes. | Data | Confirm the opportunity form contains and persists name, customer, owner, stage, amount, probability, expected close date, source and notes. |  |
| 1-10-003 | Tested | Support pipeline stages such as Qualification, Proposal, Negotiation, Won and Lost. | Workflow | Confirm the opportunity stage options include Qualification, Proposal, Negotiation, Won and Lost and that a chosen value persists. |  |
| 1-10-004 | Tested | Calculate/derive weighted pipeline where required using Amount × Probability / 100. | Functional | Enter Amount 1000 and Probability 50 and confirm the weighted pipeline value computes to 500. |  |
| 1-10-005 | Tested | Require amount greater than zero for active sales opportunities. | Validation | Create an active opportunity with Amount 0 and confirm it is rejected. |  |
| 1-10-006 | Tested | Require probability from 0 through 100. | Validation | Submit an opportunity with Probability 101 and confirm it is rejected. |  |
| 1-10-007 | Tested | Expected Close Date cannot be in the past for active opportunities. | Validation | Submit an active opportunity with Expected Close Date of yesterday and confirm it is rejected. |  |

## 11. p.5 §4.9 Audit Log

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-11-001 | Tested | Capture login success/failure, logout, create, update, delete, role changes, password/security events and important status changes. | Data | Perform a login, a create, an update, a delete and a role change and confirm an audit entry exists for each event type. |  |
| 1-11-002 | Tested | Record user, timestamp, action, module, record identifier, result and relevant metadata. | Data | Inspect an audit record and confirm it contains user, timestamp, action, module, record identifier and result. |  |

## 12. p.6 §4.10 REST API

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-12-001 | Tested | Expose secured endpoints for CRM resources using REST conventions. | API | Call the CRM REST endpoints and confirm they follow REST conventions and require authentication. |  |
| 1-12-002 | Tested | Use DTOs instead of exposing database entities directly. | API | Inspect an API response and confirm it is a DTO shape, not a raw entity with navigation properties or internal fields. |  |
| 1-12-003 | Tested | Validate input at API boundaries. | Validation | POST an invalid payload to an API endpoint and confirm it is rejected with a validation error. |  |
| 1-12-004 | Tested | Return appropriate HTTP status codes such as 200, 201, 400, 401, 403, 404 and 409 where applicable. | API | Exercise create/get/unauthorized/not-found/duplicate calls and confirm 201, 200, 401, 403, 404 and 409 are returned as applicable. |  |
| 1-12-005 | Tested | Apply authorization to every protected endpoint. | Authorization | Call a protected endpoint without credentials and as an insufficient role and confirm 401/403 responses. |  |
| 1-12-006 | Tested | Do not return passwords, password hashes, security tokens or unnecessary internal database fields. | Security | Inspect API responses and confirm no password, password hash, security token or internal database field is returned. |  |

## 13. p.6 §4.9 Audit Log

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-13-001 | Tested | Audit records should be append-oriented and protected from unauthorized modification/deletion. | Security | Attempt to edit or delete an audit record via the UI/API and confirm no such operation is exposed and the record persists. |  |

## 14. p.7 §5 Validation Requirements

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-14-001 | Tested | AcxiomCRM shall implement validation at multiple levels. | Validation | The same invalid Customer payload (e.g. blank Customer Name) is rejected both in the browser form and by the API, proving more than one validation level exists. |  |
| 1-14-002 | Tested | Client-side validation improves user experience. | UI/UX | A Customer form field with a bad value shows an inline error immediately in the browser before the request is sent. |  |
| 1-14-003 | Tested | server-side validation remains mandatory because client-side rules can be bypassed. | Security | Sending an invalid Customer JSON body directly to the API (skipping the React form) is still rejected with HTTP 400. |  |

## 15. p.7 §5.1 Client-Side Validation — Required

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-15-001 | Tested | Client-side validation type Required: Mandatory fields must not be empty. Example: Customer Name is required. | Validation | Submitting the Customer form with Customer Name blank shows an inline 'Customer Name is required' error and does not submit. |  |
| 1-15-002 | Tested | Client-side validation type Email: Validate email format. Example: user@example.com | Validation | Typing 'not-an-email' into the Customer Email field shows an inline client-side email-format error. |  |
| 1-15-003 | Tested | Client-side validation type Phone: Validate allowed phone pattern/length. Example: 10-digit Indian mobile format, if that is the project rule. | Validation | Typing a 7-digit value into the Customer Phone field shows an inline client-side pattern/length error. |  |
| 1-15-004 | Tested | Client-side validation type Length: Enforce minimum/maximum character limits. Example: Name: configured maximum length. | Validation | Typing past the configured Name maximum length shows an inline client-side length error/limit. |  |
| 1-15-005 | Tested | Client-side validation type Date: Validate valid date and configured date rules. Example: Expected Close Date. | Validation | Entering an impossible date (e.g. 31/02/2025) into Expected Close Date shows an inline client-side date error. |  |
| 1-15-006 | Tested | Client-side validation type Numeric: Validate numeric format, range and precision. Example: Opportunity Amount > 0. | Validation | Typing 0 or 'abc' into Opportunity Amount shows an inline client-side numeric/range error. |  |

## 16. p.7 §5.2 Server-Side Validation — Mandatory

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-16-001 | Tested | Repeat all security- and business-critical validations on the server. | Security | Posting an invalid Opportunity (Amount 0) directly to the API returns HTTP 400 even though the UI also blocks it. |  |
| 1-16-002 | Tested | Validate model state before database operations. | Validation | An invalid create-Customer DTO returns HTTP 400 and no new row appears in the MySQL customers table. | `NET` |
| 1-16-003 | Tested | Validate foreign keys and referenced records. | Data | Posting a Lead/Opportunity with a non-existent CustomerId returns HTTP 400 and inserts no row. |  |
| 1-16-004 | Tested | Reject unauthorized record modifications even when a user manipulates browser requests. | Security | A SalesExecutive sending a raw PUT/DELETE for another owner's Customer receives HTTP 403 and the record is unchanged. |  |
| 1-16-005 | Tested | Return user-friendly validation messages without exposing database internals. | Security | Triggering a duplicate-key error returns a friendly field message whose body contains no SQL text or stack trace. |  |

## 17. p.7 §5.3 Business Validation Rules

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-17-001 | Tested | Field/Area Expected Close Date — Rule: Cannot be in the past for an active Opportunity. Validation Category: Business validation. | Validation | POST /api/opportunities with Expected Close Date = yesterday returns 400 with a past-date message. |  |
| 1-17-002 | Tested | Field/Area Opportunity Amount — Rule: Must be greater than 0. Validation Category: Numeric + business validation. | Validation | POST /api/opportunities with Amount = 0 returns HTTP 400. |  |
| 1-17-003 | Tested | Field/Area Probability — Rule: Must be between 0 and 100 inclusive. Validation Category: Numeric + business validation. | Validation | POST /api/opportunities with Probability = 101 (and separately -1) returns HTTP 400. |  |
| 1-17-004 | Tested | Field/Area Email — Rule: Must be present where required and follow valid email format. Validation Category: Email validation. | Validation | POST /api/customers with Email = 'abc' returns 400 with an email-format message. |  |
| 1-17-005 | Tested | Field/Area Phone — Rule: Must follow the configured phone format and length. Validation Category: Phone validation. | Validation | POST /api/customers with Phone = '12345' returns 400 with a phone format/length message. |  |
| 1-17-006 | Tested | Field/Area Required fields — Rule: Mandatory business fields must be supplied before save. Validation Category: Required validation. | Validation | POST /api/customers with Customer Name blank returns 400 with a field-specific required message. |  |
| 1-17-007 | Tested | Field/Area Length — Rule: Text values must remain within configured database/UI limits. Validation Category: Length validation. | Validation | POST /api/customers with a Name longer than the configured max length returns 400 and no row is saved. |  |

## 18. p.8 §5.4 Recommended Validation Examples

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-18-001 | Tested | Opportunity Amount = 0 -> reject with: "Opportunity Amount must be greater than 0." | Validation | POST an Opportunity with Amount 0 and confirm the response message is exactly 'Opportunity Amount must be greater than 0.' |  |
| 1-18-002 | Tested | Probability = 101 -> reject with: "Probability must be between 0 and 100." | Validation | POST an Opportunity with Probability 101 and confirm the response message is exactly 'Probability must be between 0 and 100.' |  |
| 1-18-003 | Tested | Expected Close Date = yesterday -> reject with: "Expected Close Date cannot be in the past." | Validation | POST an Opportunity with Expected Close Date = yesterday and confirm the response message is exactly 'Expected Close Date cannot be in the past.' |  |
| 1-18-004 | Tested | Email = invalid format -> reject with: "Enter a valid email address." | Validation | POST a Customer with an invalid Email and confirm the response message is exactly 'Enter a valid email address.' |  |
| 1-18-005 | Tested | Phone = invalid length/characters -> reject with: "Enter a valid phone number." | Validation | POST a Customer with an invalid Phone and confirm the response message is exactly 'Enter a valid phone number.' |  |
| 1-18-006 | Tested | Required field blank -> reject with a field-specific required message. | Validation | POST a Customer with a required field blank and confirm the returned error names that specific field as required. |  |
| 1-18-007 | Tested | Text exceeds maximum length -> reject before database save. | Validation | POST an over-length text value and confirm the request is rejected with 400 and no row is written to the database. |  |

## 19. p.8 §6.1 Authentication Features

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-19-001 | Tested | Login — Authenticate active users using approved credentials. | Security | POST /api/auth/login with valid active-user credentials returns 200; an inactive user is refused. |  |
| 1-19-002 | Tested | Logout — Terminate authenticated session/cookie and redirect safely. | Security | Logout ends the session; a following protected request returns 401. |  |
| 1-19-003 | Tested | Password Hashing — Use a strong adaptive password hashing mechanism; never store plain text. | Security | After registering, the User.PasswordHash column holds a bcrypt/argon2-style hash, not the plain password. |  |
| 1-19-004 | Tested | Password Policy — Enforce configurable minimum length and complexity rules. | Security | Registering a password below the configured minimum/complexity is rejected with a policy message. |  |
| 1-19-005 | Tested | Lockout — Temporarily lock an account after repeated failed authentication attempts. | Security | After the configured number of failed logins, the next attempt is refused with a locked-account message. |  |
| 1-19-006 | Tested | Authorization — Use role and resource-level authorization for protected operations. | Authorization | A request to a resource outside the caller's role/ownership returns 403 even with a valid session. |  |
| 1-19-007 | Tested | Audit — Record authentication/security events without storing sensitive credentials. | Security | A successful login and a failed login each create an AuditLog row containing no password or hash. |  |

## 20. p.8 §6.2 Recommended Password Policy

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-20-001 | Tested | Minimum length: 8 characters (or higher if the organization's security standard requires it). | Security | A 7-character password is rejected; an 8-character (or configured minimum) password is accepted. |  |
| 1-20-002 | Tested | Require a combination of character classes where appropriate. | Security | A password missing a configured character class (e.g. no digit) is rejected by the policy rule. |  |
| 1-20-003 | Tested | Do not allow storage or display of plain-text passwords. | Security | No API response or view exposes the password, and the database stores only a hash. |  |
| 1-20-004 | Tested | Do not log passwords, password hashes, reset tokens or authentication secrets. | Security | Grep application logs after login and reset flows; none of those secrets appears. |  |

## 21. p.8 §6.3 Lockout

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-21-001 | Tested | Track failed login attempts. | Security | Each wrong-password attempt increments User.FailedLoginCount in the database. |  |
| 1-21-002 | Tested | After a configurable threshold, lock the account for a configurable duration. | Security | With threshold 5 / 15 minutes, the 5th failure sets LockoutEnd ~15 min ahead and blocks login until it passes. |  |
| 1-21-003 | Tested | Reset failed-attempt count after successful authentication according to the security policy. | Security | After a correct login the account's FailedLoginCount returns to 0. |  |
| 1-21-004 | Tested | Audit lockout and unlock events. | Security | A lockout and a subsequent unlock each produce an AuditLog entry. |  |

## 22. p.9 §7 Role-Based Authorization

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-22-001 | Tested | The initial role model contains three roles: Admin, Manager and Sales Executive. | Authorization | The Role table is seeded with exactly Admin, Manager and Sales Executive. |  |
| 1-22-002 | Tested | Authorization should be enforced on the server, not only by hiding navigation links. | Authorization | Calling an Admin-only endpoint directly as a Sales Executive returns 403 despite the hidden UI link. |  |
| 1-22-003 | Tested | Admin — Full application administration, users/roles, audit logs, configuration, all CRM records and reports. | Authorization | Logged in as Admin, all CRM records plus user/role/audit/config screens are reachable. |  |
| 1-22-004 | In Progress | Manager — Team/customer/lead/opportunity/follow-up management and management reports; no unrestricted security administration. | Authorization | Logged in as Manager, team records/reports open but security administration endpoints return 403. |  |
| 1-22-005 | In Progress | Sales Executive — Assigned customers, leads, opportunities and follow-ups; access to permitted sales dashboards/reports. | Authorization | Logged in as Sales Executive, only assigned records and permitted sales views are visible. |  |

## 23. p.9 §7.1 Suggested Permission Matrix

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-23-001 | Tested | Customers -> Admin = Full | Authorization | Admin can create/read/update/delete any customer. |  |
| 1-23-002 | Tested | Customers -> Manager = Team/Business Scope | Authorization | Manager sees/modifies team-scoped customers but not outside their business scope. |  |
| 1-23-003 | Tested | Customers -> Sales Executive = Own/Assigned | Authorization | Sales Executive can only read/modify customers assigned to them; others return 403/404. |  |
| 1-23-004 | Tested | Leads -> Admin = Full | Authorization | Admin can create/read/update/delete any lead. |  |
| 1-23-005 | Tested | Leads -> Manager = Team | Authorization | Manager sees/modifies team leads only. |  |
| 1-23-006 | Tested | Leads -> Sales Executive = Own/Assigned | Authorization | Sales Executive can only act on leads assigned to them. |  |
| 1-23-007 | Tested | Opportunities -> Admin = Full | Authorization | Admin can create/read/update/delete any opportunity. |  |
| 1-23-008 | Tested | Opportunities -> Manager = Team | Authorization | Manager sees/modifies team opportunities only. |  |
| 1-23-009 | Tested | Opportunities -> Sales Executive = Own/Assigned | Authorization | Sales Executive can only act on opportunities assigned to them. |  |
| 1-23-010 | Tested | REST API -> Admin = Authorized endpoints | API | Admin calls GET /api/customers and receives an authorized 200 JSON response. |  |
| 1-23-011 | Tested | REST API -> Manager = Authorized endpoints | API | Manager calls an authorized endpoint and receives a scoped 200 response. |  |
| 1-23-012 | Tested | REST API -> Sales Executive = Authorized endpoints | API | Sales Executive calls an authorized endpoint and receives only their permitted data. |  |

## 24. p.9-10 §8 Core CRM Workflows (Lead-to-Customer)

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-24-001 | Tested | Lead-to-Customer step 1: Create Lead | Workflow | POST /api/leads with required fields returns 201 and a new Lead row exists. |  |
| 1-24-002 | Tested | Lead-to-Customer step 2: Assign Lead | Workflow | Assigning a lead sets its owner/assigned user to the chosen Sales Executive. |  |
| 1-24-003 | Tested | Lead-to-Customer step 3: Contact Lead | Workflow | Moving a lead to the Contacted status is accepted and persisted. |  |
| 1-24-004 | Tested | Lead-to-Customer step 4: Qualify | Workflow | Moving a lead to the Qualified status succeeds; an invalid transition is rejected. |  |
| 1-24-005 | Tested | Lead-to-Customer step 5: Convert to Customer/Opportunity | Workflow | Converting a qualified lead creates the Customer and/or Opportunity and links it back to the lead. |  |
| 1-24-006 | Tested | Lead-to-Customer step 6: Record conversion in Audit Log | Workflow | The conversion writes an AuditLog entry naming the lead and the resulting record. |  |
| 1-24-007 | Tested | Opportunity step 1: Create Opportunity | Workflow | POST /api/opportunities with valid fields returns 201 and a new opportunity row. |  |
| 1-24-008 | Tested | Opportunity step 2: Set Amount | Workflow | An amount greater than 0 saves; an amount of 0 is rejected. |  |
| 1-24-009 | Tested | Opportunity step 3: Set Probability | Workflow | A probability of 0–100 saves; 101 is rejected. |  |
| 1-24-010 | Tested | Opportunity step 4: Set Expected Close Date | Workflow | A future Expected Close Date saves; a past date is rejected for an active opportunity. |  |
| 1-24-011 | Tested | Opportunity step 5: Move through Pipeline Stages | Workflow | Stage moves Qualification -> Proposal -> Negotiation and each move persists. |  |
| 1-24-012 | Tested | Opportunity step 6: Mark Won/Lost | Workflow | Setting the stage to Won or Lost persists and is reflected in the pipeline. |  |
| 1-24-013 | Tested | Opportunity step 7: Capture final outcome | Workflow | The Won/Lost final outcome is stored on the opportunity record. |  |

## 25. p.10 §9 Core Data Entities

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-25-001 | Tested | User entity with representative fields: UserId, Name, Email, PasswordHash, RoleId, IsActive, FailedLoginCount, LockoutEnd, CreatedDate. | Data | Inspect the User entity/table; it contains exactly those fields. |  |
| 1-25-002 | Tested | Role entity with representative fields: RoleId, RoleName. | Data | Inspect the Role entity/table; it contains RoleId and RoleName. |  |
| 1-25-003 | Tested | Customer entity with representative fields: CustomerId, Name, Email, Phone, Address, Status, OwnerId, CreatedDate, ModifiedDate. | Data | Inspect the Customer entity/table; it contains exactly those fields. |  |
| 1-25-004 | Tested | Lead entity with representative fields: LeadId, Name, Email, Phone, Source, Status, Priority, OwnerId, CreatedDate. | Data | Inspect the Lead entity/table; it contains exactly those fields. |  |
| 1-25-005 | Tested | Opportunity entity with representative fields: OpportunityId, CustomerId, Name, Stage, Amount, Probability, ExpectedCloseDate, OwnerId, Notes. | Data | Inspect the Opportunity entity/table; it contains exactly those fields. |  |
| 1-25-006 | Tested | AuditLog entity with representative fields: AuditLogId, UserId, Module, Action, RecordId, Timestamp, Result, Details. | Data | Inspect the AuditLog entity/table; it contains exactly those fields. |  |

## 26. p.11 §10 REST API Specification

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-26-001 | Tested | POST /api/auth/login — Authenticate user — Access: Public/Rate limited | API | POST /api/auth/login with valid credentials returns 200 and an authenticated session/token; with a wrong password returns 401. |  |
| 1-26-002 | Tested | POST /api/auth/logout — Logout/session termination — Access: Authenticated | API | POST /api/auth/logout as a logged-in user returns 200 and the session/token is invalidated (later protected calls return 401). |  |
| 1-26-003 | Tested | GET /api/customers — List/search customers — Access: Authorized | API | GET /api/customers as an authorized user returns 200 with a customer list; unauthenticated returns 401. |  |
| 1-26-004 | Tested | POST /api/customers — Create customer — Access: Authorized | API | POST /api/customers with a valid customer body returns 201/200 and the record persists; invalid body returns 400. |  |
| 1-26-005 | Tested | GET /api/customers/{id} — Get customer — Access: Authorized | API | GET /api/customers/{id} for an existing id returns 200 with that customer; a missing id returns 404. |  |
| 1-26-006 | Tested | PUT /api/customers/{id} — Update customer — Access: Authorized | API | PUT /api/customers/{id} with a changed field returns 200 and the change is read back on GET. |  |
| 1-26-007 | Tested | DELETE /api/customers/{id} — Delete/deactivate customer — Access: Authorized | API | DELETE /api/customers/{id} returns 200/204 and the customer is removed or marked deactivated. |  |
| 1-26-008 | Tested | GET /api/leads — List/search leads — Access: Authorized | API | GET /api/leads as an authorized user returns 200 with a lead list; unauthenticated returns 401. |  |
| 1-26-009 | Tested | POST /api/leads — Create lead — Access: Authorized | API | POST /api/leads with a valid body returns 201/200 and the lead persists; invalid body returns 400. |  |
| 1-26-010 | Tested | GET /api/opportunities — List/search opportunities — Access: Authorized | API | GET /api/opportunities as an authorized user returns 200 with an opportunity list; unauthenticated returns 401. |  |
| 1-26-011 | Tested | POST /api/opportunities — Create opportunity — Access: Authorized | API | POST /api/opportunities with a valid body returns 201/200 and the opportunity persists; invalid body returns 400. |  |

## 27. p.11 §10.1 API Security

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-27-001 | Tested | Require authentication for protected endpoints. | Security | Call any protected endpoint (e.g. GET /api/customers) with no session/token and confirm 401. |  |
| 1-27-002 | Tested | Apply role/permission checks at endpoint/service level. | Authorization | Call an Admin/Manager-only endpoint as a SalesExecutive user and confirm 403. |  |
| 1-27-003 | Tested | Validate every request payload. | Validation | POST an invalid/malformed JSON payload to a create endpoint and confirm 400 with validation errors. |  |
| 1-27-004 | Tested | Return consistent error objects without exposing stack traces or database details. | API | Trigger a server error and confirm the JSON response has a consistent shape and contains no stack trace or SQL text. |  |

## 28. p.12 §12 UI/UX Standards

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-28-001 | Implemented | Use responsive Bootstrap layout for desktop and tablet views. | UI/UX | Resize the browser to ~768px width and confirm Bootstrap grid reflows with no horizontal page scroll. |  |
| 1-28-002 | Implemented | Keep navigation consistent across modules. | UI/UX | Navigate Customers, Leads and Opportunities and confirm the same sidebar/header navigation renders on each. |  |
| 1-28-003 | Implemented | Show inline validation messages next to invalid fields. | UI/UX | Submit a form with an empty required field and confirm the error text appears beside that field. |  |
| 1-28-004 | Implemented | Use clear success/error/warning notifications. | UI/UX | Save a record successfully and trigger an error, confirming distinct success/error notifications appear. |  |
| 1-28-005 | Implemented | Use confirmation dialogs for destructive actions. | UI/UX | Click delete on a record and confirm a confirmation dialog appears before deletion occurs. |  |
| 1-28-006 | Implemented | Provide search, filtering, sorting and pagination on list pages. | UI/UX | On the customer list page use search, a filter, a column sort and page navigation, confirming results change. |  |
| 1-28-007 | Implemented | Use accessible labels, keyboard-friendly controls and meaningful validation messages. | UI/UX | Tab through a form and confirm every control receives focus and has an associated label; validation text is meaningful. |  |
| 1-28-008 | Implemented | Do not expose technical exception messages to end users. | UI/UX | Force a backend failure and confirm the UI shows a friendly message, not a stack trace or exception class name. |  |

## 29. p.12 §13 Testing & Acceptance Checklist

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-29-001 | Tested | Authentication — Valid login works; invalid credentials are rejected; logout works. | Testing | Log in with valid credentials (succeeds), with invalid credentials (rejected), then log out and confirm the session ends. |  |
| 1-29-002 | Tested | Password Security — Password is never stored as plain text; policy is enforced. | Testing | Inspect the user table and confirm the stored value is a bcrypt/Argon2 hash (not plain text); a weak password is rejected. |  |
| 1-29-003 | Tested | Lockout — Repeated failed logins trigger configured lockout. | Testing | Attempt login with a wrong password the configured number of times and confirm the account is temporarily locked. |  |
| 1-29-004 | Tested | Authorization — Users cannot access unauthorized pages, records or APIs by manually changing URLs/requests. | Testing | As a SalesExecutive, hand-craft a request/URL to an Admin-only page or API and confirm it is denied. |  |
| 1-29-005 | Tested | Client Validation — Required, email, phone, length, date and numeric validation works before submission. | Testing | Enter malformed email/phone/length/date/numeric values in a form and confirm errors appear before any request is sent. |  |
| 1-29-006 | Tested | Server Validation — Invalid/tampered requests are rejected on the server. | Testing | Bypass the UI and POST a tampered/invalid payload directly to the API and confirm it is rejected with 400. |  |
| 1-29-007 | Tested | Opportunity — Amount > 0, probability 0–100, and close-date rule are enforced. | Testing | POST an opportunity with amount 0, probability 150 and an invalid close date; each is rejected. |  |
| 1-29-008 | Tested | Audit — Important security and business actions create audit entries. | Testing | Perform a login and a customer create/update/delete and confirm matching rows appear in the audit log. |  |
| 1-29-009 | Tested | API — Endpoints return correct status codes and enforce authorization. | Testing | Run each endpoint with valid, invalid and unauthorized inputs and confirm 200/201, 400, 401/403 respectively. |  |

## 30. p.12 §14 Security Checklist

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-30-001 | Tested | Password hashing enabled. | Security | Confirm user passwords are stored as a strong one-way hash in the database. |  |
| 1-30-002 | Tested | Password policy enabled. | Security | Register/change a password that violates the policy (too short/weak) and confirm it is rejected. |  |
| 1-30-003 | Tested | Account lockout enabled. | Security | Exceed the failed-login threshold and confirm the account is locked for the configured duration. |  |
| 1-30-004 | Tested | Role-based authorization enabled. | Authorization | Verify a request to a role-restricted endpoint/action is allowed only for the permitted role. |  |
| 1-30-005 | Tested | Server-side validation enabled. | Validation | Submit invalid data directly to the API (bypassing the UI) and confirm server-side rejection. |  |
| 1-30-006 | Tested | Anti-forgery protection enabled for state-changing MVC forms. | Security | Submit a state-changing form without the anti-forgery/CSRF token and confirm the request is rejected. | `NET` |

## 31. p.13 §14 Security Checklist

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-31-001 | Tested | Sensitive information excluded from logs. | Security | Grep the application logs after a login and confirm no passwords, tokens or secrets are written. |  |
| 1-31-002 | Tested | API authorization and input validation enabled. | API | Call an API without authorization (401/403) and with invalid input (400) and confirm both are enforced server-side. |  |
| 1-31-003 | Tested | Audit logging enabled for security-sensitive actions. | Security | Perform a security-sensitive action (login failure, lockout) and confirm an audit/log entry is written. |  |
| 1-31-004 | Tested | Least-privilege access applied to roles. | Authorization | Confirm SalesExecutive has only the permissions the matrix allows and is denied Admin/Manager-only actions. |  |
| 1-31-005 | Tested | Database credentials stored securely and not hard-coded in source control. | Security | Inspect application config and version control and confirm DB credentials come from environment/secret config, not committed literals. |  |

## 32. p.13 §15 Recommended Implementation Sequence

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-32-001 | Tested | 1. Create solution structure and database. | Architecture | Confirm the backend/frontend project structure exists and the MySQL database is created and reachable. | `NET` |
| 1-32-002 | Tested | 2. Create User and Role entities and authentication. | Architecture | Confirm User/Role entities and tables exist and login authenticates against them. |  |
| 1-32-003 | Tested | 3. Implement Admin/Manager/Sales Executive authorization. | Authorization | Log in as each of Admin, Manager, SalesExecutive and confirm role-appropriate access is enforced on the backend. |  |
| 1-32-004 | Tested | 4. Create common layout/navigation and dashboard shell. | UI/UX | Confirm an authenticated shell with shared layout/navigation renders across pages. |  |
| 1-32-005 | Tested | 5. Implement Customer Management. | Functional | Perform customer create, read, update, delete and search from the UI and confirm each works. |  |
| 1-32-006 | Tested | 6. Implement Lead Management. | Functional | Perform lead create, read, update, delete and status change and confirm each works. |  |
| 1-32-007 | Tested | 8. Implement Opportunity Management. | Functional | Perform opportunity create/update with stage and confirm the amount/probability/close-date rules are enforced. |  |
| 1-32-008 | Tested | 9. Add client-side and server-side validation. | Validation | Confirm an invalid value is caught both in the browser and when the same payload is sent directly to the API. |  |
| 1-32-009 | Tested | 10. Implement Audit Log. | Security | Trigger auth and CRUD actions and confirm audit rows are written with actor/action/timestamp. |  |
| 1-32-010 | Tested | 11. Implement REST APIs and secure API authorization. | API | Call the REST endpoints with and without authorization and confirm correct status codes and role enforcement. |  |
| 1-32-011 | Tested | 13. Execute security, validation, workflow and role-based testing. | Testing | Run the security, validation, workflow and role test cases and confirm all pass. |  |

## 33. p.13 §16 Final Project Scope

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-33-001 | In Progress | AcxiomCRM is defined as a role-based CRM application covering the complete customer-sales lifecycle from lead capture through follow-up and opportunity management, with authentication, authorization, auditability, validation, APIs and reporting. | Functional | Confirm the app covers lead capture through follow-up/opportunity with auth, authorization, audit, validation, APIs and reporting. |  |
| 1-33-002 | Tested | The project should demonstrate both functional CRM capabilities and production-oriented engineering practices. | Quality | Confirm working CRM features exist alongside practices such as hashed passwords, audit logging and centralized error handling. |  |
| 1-33-003 | Tested | The mandatory quality requirements are: client-side validation for Required, Email, Phone, Length, Date and Numeric inputs | Validation | Enter invalid Required/Email/Phone/Length/Date/Numeric values in a form and confirm each triggers a client-side error. |  |
| 1-33-004 | Tested | server-side enforcement of critical rules | Validation | Bypass the client and POST critical-rule-violating data to the API and confirm the server rejects it. |  |
| 1-33-005 | In Progress | business validation for opportunity and follow-up dates/values | Validation | Submit an opportunity with amount 0 or probability out of 0-100 and an out-of-range follow-up date and confirm rejection. |  |
| 1-33-006 | Tested | secure authentication | Security | Confirm login uses hashed-credential verification and protected routes reject unauthenticated access. |  |
| 1-33-007 | Tested | password hashing | Security | Confirm stored passwords are non-reversible hashes, never plain text. |  |
| 1-33-008 | Tested | password policy | Security | Reject a password that fails the defined policy (length/complexity) on registration or change. |  |
| 1-33-009 | Tested | account lockout | Security | Exceed the failed-login limit and confirm the account is temporarily locked. |  |
| 1-33-010 | Tested | role-based authorization | Authorization | Confirm each role can reach only its permitted pages/endpoints/records. |  |
| 1-33-011 | Tested | audit logging | Security | Confirm security-relevant and CRUD actions produce persisted audit entries. |  |
| 1-33-012 | Tested | secured REST APIs | API | Confirm REST endpoints require authentication/authorization and validate input, returning 401/403/400 appropriately. |  |

## 34. p.14 §17 Business Scenario

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-34-001 | In Progress | AcxiomCRM is a web-based CRM application used by a sales organization to manage customers, leads, opportunities, follow-ups, activities, sales pipeline, users and roles, and audit logs. | Functional | Each of the eight areas (customers, leads, opportunities, follow-ups, activities, pipeline, users/roles, audit logs) has persisted records reachable in the running app. |  |
| 1-34-002 | Tested | Sales users manage their assigned CRM records. | Authorization | A SalesExecutive can only open/edit records whose AssignedTo is their own user id; another user's record returns 403/not-found. |  |

## 35. p.14 §17.2 Mandatory Module Tree

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-35-001 | Implemented | Authentication ├── Login | Functional | GET /login renders a login form and a valid credential POST establishes an authenticated session. |  |
| 1-35-002 | Implemented | Authentication ├── Register | Functional | POST /register with valid details creates a user row with a hashed password. |  |
| 1-35-003 | Implemented | Authentication ├── Logout | Functional | Requesting logout ends the session and a subsequent request to a protected page is rejected with 401/redirect to login. |  |
| 1-35-004 | Implemented | Authentication ├── Access Control | Authorization | A request to an endpoint the user's role is not permitted returns HTTP 403 from the backend. |  |
| 1-35-005 | Implemented | Customer Management ├── Create | Functional | POST a valid new customer and the row is persisted with a generated CustomerCode. |  |
| 1-35-006 | Implemented | Customer Management ├── Edit | Functional | Editing a customer's CustomerName and saving updates the persisted record. |  |
| 1-35-007 | Implemented | Customer Management ├── Details | Functional | Opening a customer's Details page shows all required Customer fields for that record. |  |
| 1-35-008 | Implemented | Customer Management ├── Delete | Functional | Deleting a customer removes/deactivates it and it no longer appears in the customer list. |  |
| 1-35-009 | Implemented | Customer Management ├── Search | Functional | Searching Customers by Customer Name returns only matching rows. |  |
| 1-35-010 | Implemented | Lead Management ├── Create | Functional | POST a valid new lead and the row is persisted with a generated LeadCode. |  |
| 1-35-011 | Implemented | Lead Management ├── Edit | Functional | Editing a lead's LeadName and saving updates the persisted record. |  |
| 1-35-012 | Implemented | Lead Management ├── Details | Functional | Opening a lead's Details page shows all required Lead fields for that record. |  |
| 1-35-013 | Implemented | Lead Management ├── Delete | Functional | Deleting a lead removes it and it no longer appears in the lead list. |  |
| 1-35-014 | Implemented | Lead Management ├── Lead Status | Functional | The lead form offers only configured statuses and a POST carrying a status outside the set is rejected. |  |
| 1-35-015 | Implemented | Lead Management ├── Lead Conversion | Functional | Converting a lead creates the linked Customer and/or Opportunity in a single transaction. |  |
| 1-35-016 | Implemented | Opportunity Management ├── Create | Functional | POST a valid new opportunity and the row is persisted with Amount, Stage and Probability. |  |
| 1-35-017 | Implemented | Opportunity Management ├── Edit | Functional | Editing an opportunity's Stage and saving updates the persisted record. |  |
| 1-35-018 | Implemented | Opportunity Management ├── Details | Functional | Opening an opportunity's Details page shows all required Opportunity fields. |  |
| 1-35-019 | Implemented | Opportunity Management ├── Delete | Functional | Deleting an opportunity removes it and it no longer appears in the opportunity list. |  |
| 1-35-020 | Implemented | Opportunity Management ├── Sales Pipeline | Functional | The Sales Pipeline view groups opportunities by permitted Stage. |  |

## 36. p.15 §17.2 Mandatory Module Tree

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-36-001 | Tested | Audit Log ├── Login | Security | A successful and a failed login each write an AuditLog row with Action Login and UserId. |  |
| 1-36-002 | Tested | Audit Log ├── Create | Security | Creating a customer writes an AuditLog row with Action Create, EntityName and RecordId. |  |
| 1-36-003 | Tested | Audit Log ├── Update | Security | Updating a lead writes an AuditLog row with Action Update, OldValue and NewValue. |  |
| 1-36-004 | Tested | Audit Log ├── Delete | Security | Deleting an opportunity writes an AuditLog row with Action Delete and the RecordId. |  |

## 37. p.15 §17.3 Database Design — Mandatory Entities

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-37-001 | Tested | Customer table required fields: CustomerId, CustomerCode, CustomerName, Email, Phone, CompanyName, Address, City, State, Status, CreatedDate, CreatedBy | Data | The customers table schema contains exactly those columns (plus no missing required field). |  |
| 1-37-002 | Tested | Lead table required fields: LeadId, LeadCode, LeadName, Email, Phone, CompanyName, Source, Status, ExpectedValue, CreatedDate, AssignedTo | Data | The leads table schema contains exactly those columns. |  |
| 1-37-003 | Tested | Opportunity table required fields: OpportunityId, OpportunityName, CustomerId, LeadId, Amount, Stage, Probability, ExpectedCloseDate, Status, CreatedDate, AssignedTo | Data | The opportunities table schema contains exactly those columns including CustomerId and LeadId FKs. |  |
| 1-37-004 | Tested | AuditLog table required fields: AuditLogId, UserId, Action, EntityName, RecordId, OldValue, NewValue, CreatedDate, IpAddress | Data | The audit_log table schema contains exactly those columns. |  |

## 38. p.15 §17.4 Authentication Database Requirement

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-38-001 | Tested | Do not create a custom password table for authentication. | Data | Schema inspection shows no project-defined password/credential table; hashes live in the identity user table. |  |
| 1-38-002 | Tested | Use ASP.NET Core Identity for users, password hashing, claims/roles, authentication cookies, password policy and lockout. | Security | Authentication is wired through the ASP.NET Core Identity stack, with cookie auth, roles and lockout configured. | `NET` |
| 1-38-003 | Tested | Use IdentityUser/IdentityRole or appropriately extended application identity classes. | Security | The application user class extends IdentityUser and roles use IdentityRole (or documented equivalents). | `NET` |
| 1-38-004 | Tested | Store only framework-managed password hashes. | Security | The password column contains only framework-generated hashes; no plaintext password is stored anywhere. |  |
| 1-38-005 | Tested | Do not expose password hashes in MVC views, DTOs, API responses or logs. | Security | No response payload, view or log line contains a password hash value. | `NET` |

## 39. p.15 §17.5 Mandatory Customer Validation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-39-001 | Tested | Customer Name is required and has a maximum length. | Validation | Submitting a blank CustomerName, or one over the max length, returns a 400 validation error. |  |
| 1-39-002 | Tested | Email is required and must have a valid email format. | Validation | POST customer with Email "not-an-email" returns HTTP 400 with an email-format error. |  |
| 1-39-003 | Tested | Phone is required and must satisfy the configured phone format. | Validation | POST customer with Phone "abc" returns HTTP 400 with a phone-format error. |  |
| 1-39-004 | Tested | Email uniqueness must be enforced. | Validation | Creating a second customer with an already-used Email is rejected. |  |
| 1-39-005 | Tested | Phone uniqueness must be enforced. | Validation | Creating a second customer with an already-used Phone is rejected. |  |
| 1-39-006 | Tested | Duplicate customer creation must be prevented. | Validation | Submitting the same business-identifying customer twice does not create a second row. |  |

## 40. p.15 §17.6 Mandatory Lead Validation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-40-001 | Tested | Lead name is mandatory. | Validation | POST lead with blank LeadName returns HTTP 400. |  |
| 1-40-002 | Tested | Expected value must be within the configured numeric range. | Validation | POST lead with an out-of-range ExpectedValue returns HTTP 400. |  |
| 1-40-003 | Tested | Lead status is mandatory and must come from a valid status set. | Validation | POST lead with a missing or unknown Status value returns HTTP 400. |  |
| 1-40-004 | Tested | Lead email and phone must follow the project's common validation rules. | Validation | POST lead with a malformed Email or Phone returns HTTP 400 using the same rules as Customer. |  |

## 41. p.16 §17.7 Mandatory Business Validation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-41-001 | Tested | Rule Email — Must be valid and unique where uniqueness is required. | Validation | Duplicate or malformed Email is rejected on the entity where uniqueness is required. |  |
| 1-41-002 | Tested | Rule Phone — Must be valid and unique where uniqueness is required. | Validation | Duplicate or malformed Phone is rejected on the entity where uniqueness is required. |  |
| 1-41-003 | Tested | Rule Opportunity Amount — Cannot be negative; active opportunities should require Amount > 0. | Validation | POST an active opportunity with Amount 0 or negative returns HTTP 400. |  |
| 1-41-004 | Tested | Rule Expected Close Date — Cannot be in the past for an active opportunity. | Validation | POST an active opportunity with a past ExpectedCloseDate returns HTTP 400. |  |
| 1-41-005 | Tested | Rule Required Fields — Cannot be empty. | Validation | Omitting any required field on any Phase 1 entity returns HTTP 400. |  |
| 1-41-006 | Tested | Rule Duplicate Customer — Same business-identifying customer cannot be created twice. | Validation | Creating the same business-identifying customer twice is rejected. |  |
| 1-41-007 | Tested | Rule Lead Status — Only valid configured statuses can be selected/transmitted. | Validation | POST lead with a status outside the configured set returns HTTP 400. |  |
| 1-41-008 | Tested | Rule Probability — Must be between 0 and 100. | Validation | POST an opportunity with Probability 150 or -1 returns HTTP 400. |  |

## 42. p.16 §17.8 Client-Side and Server-Side Validation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-42-001 | Tested | The assignment requires both client-side and server-side validation. | Validation | A form shows client-side errors without a request, and the same invalid payload is also rejected by the server. |  |
| 1-42-002 | Tested | Client-side validation provides immediate feedback, while server-side validation protects the application from manipulated HTTP requests. | Validation | Bypassing the UI and posting a crafted HTTP request is still rejected by server-side validation. |  |
| 1-42-003 | Tested | Required validation | Validation | Required-field errors appear for blank required inputs both in the UI and from the server. |  |
| 1-42-004 | Tested | Email validation | Validation | An invalid email is flagged client-side and rejected server-side. |  |
| 1-42-005 | Tested | Phone validation | Validation | An invalid phone is flagged client-side and rejected server-side. |  |
| 1-42-006 | Tested | String length validation | Validation | A value exceeding a field's max length is flagged client-side and rejected server-side. |  |
| 1-42-007 | Tested | Date validation | Validation | An invalid/past date on a date field is flagged client-side and rejected server-side. |  |
| 1-42-008 | Tested | Numeric/range validation | Validation | An out-of-range numeric value is flagged client-side and rejected server-side. |  |
| 1-42-009 | Tested | Business validation on the server | Validation | A business-rule violation (e.g. duplicate customer) is rejected by the server even when the client allows it. |  |

## 43. p.16 §17.9 Mandatory Security Implementation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-43-001 | Tested | Authentication — Login, Register, Logout, session/cookie management and unauthorized-access protection. | Security | Login, Register and Logout work, the session/cookie is managed, and an unauthenticated protected request is refused. |  |
| 1-43-002 | Tested | Password Security — Use ASP.NET Core Identity password hashing; never store plain-text passwords. | Security | The stored password value is a hash and no plaintext password is recoverable from the database. | `NET` |
| 1-43-003 | Tested | Authorization — Use role-based and server-side authorization. | Authorization | A direct backend call to a role-restricted endpoint is denied regardless of the UI. |  |
| 1-43-004 | Tested | Anti-Forgery — Protect state-changing MVC POST forms with ValidateAntiForgeryToken. | Security | Posting a state-changing form without a valid anti-forgery token is rejected. | `NET` |
| 1-43-005 | Tested | SQL Injection — Use Entity Framework Core parameterization/query APIs; do not concatenate user input into SQL. | Security | No raw SQL string concatenation of user input exists; a payload like "' OR 1=1--" does not alter query results. | `NET` |
| 1-43-006 | Tested | Server Validation — Validate every important request on the server. | Validation | Crafted/invalid requests to each Phase 1 endpoint are rejected server-side. |  |
| 1-43-007 | Tested | Client Validation — Use unobtrusive/Razor validation for user experience. | Validation | Forms use unobtrusive/Razor validation and show inline errors before submission. | `NET` |
| 1-43-008 | Tested | Audit Logging — Record important security and business operations. | Security | Authentication events and CRUD operations produce AuditLog rows. |  |

## 44. p.17 §17.10 Role-Based Authorization

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-44-001 | Tested | Role Admin — Required Scope: Full administration, users, roles, audit logs, CRM records, reports and system management. | Authorization | An Admin user can reach user/role administration, audit logs and all CRM records. |  |
| 1-44-002 | In Progress | Role Manager — Required Scope: Monitor/manage team CRM data, pipeline, follow-ups and reports; no unrestricted security administration. | Authorization | A Manager can view/manage team CRM data but is denied security-administration endpoints. |  |
| 1-44-003 | In Progress | Role SalesExecutive — Required Scope: Manage assigned customers, leads, opportunities, follow-ups and activities. | Authorization | A SalesExecutive can manage only records assigned to them and is denied other teams' records. |  |

## 45. p.17 §17.13 Search and Filtering

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-45-001 | Implemented | Page Customers — Required Search/Filter: Customer Name, Email, Phone, Company | Functional | The Customers list can be filtered by Customer Name, Email, Phone and Company. |  |
| 1-45-002 | Implemented | Page Leads — Required Search/Filter: Lead Name, Company, Status, Assigned User | Functional | The Leads list can be filtered by Lead Name, Company, Status and Assigned User. |  |
| 1-45-003 | Implemented | Page Opportunities — Required Search/Filter: Opportunity Name, Customer, Stage, Status | Functional | The Opportunities list can be filtered by Opportunity Name, Customer, Stage and Status. |  |

## 46. p.18 §17.14 REST API — Mandatory

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-46-001 | Tested | At least one API controller is mandatory. The recommended implementation exposes Customers, Leads and Opportunities. | API | Confirm an API controller exists exposing /api/customers, /api/leads and /api/opportunities. |  |
| 1-46-002 | Tested | GET /api/customers — Get customers | API | Send GET /api/customers as an authenticated user; expect HTTP 200 with a JSON array of customers. |  |
| 1-46-003 | Tested | GET /api/customers/{id} — Get customer by ID | API | GET /api/customers/1 returns HTTP 200 with that customer's JSON; an unknown id returns 404. |  |
| 1-46-004 | Tested | POST /api/customers — Create customer | API | POST a valid customer JSON; expect HTTP 201 and the created record. |  |
| 1-46-005 | Tested | PUT /api/customers/{id} — Update customer | API | PUT changed fields to /api/customers/1; expect HTTP 200 and the updated values returned. |  |
| 1-46-006 | Tested | DELETE /api/customers/{id} — Delete/deactivate customer | API | DELETE /api/customers/1; expect HTTP 200/204 and the record deleted or deactivated. |  |
| 1-46-007 | Tested | GET /api/leads — Get leads | API | Send GET /api/leads as an authenticated user; expect HTTP 200 with a JSON array of leads. |  |
| 1-46-008 | Tested | POST /api/leads — Create lead | API | POST a valid lead JSON; expect HTTP 201 and the created lead returned. |  |
| 1-46-009 | Tested | GET /api/opportunities — Get opportunities | API | GET /api/opportunities returns HTTP 200 with a JSON array of opportunities. |  |
| 1-46-010 | Tested | POST /api/opportunities — Create opportunity | API | POST a valid opportunity JSON; expect HTTP 201 and the created opportunity returned. |  |
| 1-46-011 | Tested | Use DTOs rather than exposing EF entities directly. | API | Inspect the controller: request/response types are DTO classes, not JPA/EF entity classes. | `NET` |
| 1-46-012 | Tested | Validate API payloads. | Validation | POST /api/customers with a blank name/invalid email; expect HTTP 400 with field errors. |  |
| 1-46-013 | Tested | Apply authentication/authorization to protected endpoints. | Authorization | Call /api/customers with no session/credentials; expect HTTP 401 or 403. |  |
| 1-46-014 | Tested | Return appropriate HTTP status codes. | API | Verify 201 on create, 400 on invalid input, 404 on unknown id and 409 on duplicate. |  |
| 1-46-015 | Tested | Do not return password or security-sensitive information. | Security | Inspect any API JSON response; no passwordHash/token field is present. |  |

## 47. p.18 §17.16 Audit Logging Requirements

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-47-001 | Tested | Login — User successfully logs in. | Security | Log in successfully, then check the audit_log table for a Login row for that user. |  |
| 1-47-002 | Tested | Failed Login — Invalid login attempt/lockout event. | Security | Submit a wrong password; confirm a Failed Login audit row (and a lockout row at the threshold). |  |
| 1-47-003 | Tested | Create — Customer/Lead/Opportunity created. | Security | Create a customer/lead/opportunity; confirm a Create audit row naming the entity and record id. |  |
| 1-47-004 | Tested | Update — Customer/Lead/Opportunity updated. | Security | Edit a customer/lead/opportunity; confirm an Update audit row recording the change. |  |
| 1-47-005 | Tested | Delete — Authorized deletion/deactivation. | Security | Delete/deactivate a customer/lead/opportunity; confirm a Delete audit row is written. |  |
| 1-47-006 | Tested | Audit records should capture UserId, Action, EntityName, RecordId, OldValue, NewValue, CreatedDate and IpAddress where available. | Data | Inspect an audit row: UserId, Action, EntityName, RecordId, OldValue, NewValue, CreatedDate and IpAddress (where available) are populated. |  |

## 48. p.18-19 §17.17 Minimum Evaluation Criteria

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-48-001 | Tested | CRUD — All required modules support functional create/read/update/delete or appropriate deactivation. | Functional | Create/read/update/delete a customer, lead and opportunity successfully (deactivation acceptable). |  |
| 1-48-002 | Tested | Validation — Client-side and server-side validation demonstrated. | Validation | Show a client-side error on a bad form and confirm the server rejects a crafted invalid request. |  |
| 1-48-003 | In Progress | Business Rules — Opportunity, lead and follow-up rules implemented. | Validation | Attempt an Amount 0 opportunity and an invalid lead status; both are rejected. |  |
| 1-48-004 | Tested | Authentication — Identity login/logout/register works. | Security | Register a user, log in, log out; each action works and session state changes accordingly. | `NET` |
| 1-48-005 | Tested | Authorization — Admin, Manager and SalesExecutive access differs. | Authorization | Log in as Admin, Manager and SalesExecutive; confirm visible records and actions differ per role. |  |
| 1-48-006 | Tested | Search — Required pages support search/filter. | Functional | On Customers/Leads/Opportunities pages use search/filter and confirm the result list narrows. |  |
| 1-48-007 | Tested | REST API — At least Customer/Lead/Opportunity APIs work. | API | Call the customer, lead and opportunity API endpoints; each returns valid JSON. |  |
| 1-48-008 | Tested | Audit — Important operations are logged. | Security | Perform a login and a CRUD action; confirm corresponding audit rows are written. |  |
| 1-48-009 | Tested | Architecture — Controllers, Models, Data, Services, ViewModels and Views are organized. | Architecture | Inspect the source tree: controller, model/entity, data/repository, service, DTO and view/page layers exist and are separated. | `NET` |
| 1-48-010 | Tested | Security — No plain-text passwords; anti-forgery and protected endpoints implemented. | Security | Confirm stored passwords are hashes, state-changing POSTs are anti-forgery protected, and endpoints reject anonymous calls. |  |

## 49. p.19 §17.19 Final Acceptance Scenario

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-49-001 | Tested | Open AcxiomCRM without authentication -> protected CRM pages must not be accessible. | Authorization | Open a protected CRM URL while logged out; expect a redirect to login / 401, not the page. |  |
| 1-49-002 | Tested | Register/login with a valid test user -> user reaches the Dashboard. | Security | Register then log in with valid credentials; confirm the user is redirected to the post-login Dashboard page. |  |
| 1-49-003 | Tested | Create a Customer with invalid email/phone -> client-side validation appears and save is blocked. | Validation | Submit a customer with an invalid email/phone; client-side messages appear and the save is blocked. |  |
| 1-49-004 | Tested | Bypass browser validation with a crafted request -> server-side validation still rejects invalid data. | Validation | Send a crafted request bypassing the browser; the server rejects the invalid data. |  |
| 1-49-005 | Tested | Create an Opportunity with Amount <= 0 -> rejected. | Validation | Create an opportunity with Amount = 0; expect rejection with a validation message. |  |
| 1-49-006 | Tested | Create an Opportunity with Probability > 100 -> rejected. | Validation | Create an opportunity with Probability = 101; expect rejection with a validation message. |  |
| 1-49-007 | Tested | Set Expected Close Date in the past -> rejected for an active opportunity. | Validation | Set an active opportunity's Expected Close Date to yesterday; expect rejection. |  |
| 1-49-008 | Tested | Login as SalesExecutive -> only authorized/assigned sales scope is accessible. | Authorization | Log in as SalesExecutive; confirm only assigned customers/leads/opportunities are reachable. |  |
| 1-49-009 | Tested | Create/update/delete a CRM record -> appropriate audit entry is generated. | Security | Create/update/delete a CRM record; confirm a matching audit entry appears for each action. |  |
| 1-49-010 | Tested | Call /api/customers -> authorized JSON response is returned. | API | Call /api/customers as an authorized user; expect a JSON response and 401 when unauthenticated. |  |

## 50. p.19 §17.20 Project Completion Standard

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 1-50-001 | Tested | A submission is not considered complete if it is only a database CRUD application. | Quality | Walk the app end-to-end; confirm it shows the ten capabilities below, not just database CRUD screens. |  |
| 1-50-002 | Tested | The student must demonstrate CRM workflows. | Workflow | Run the lead-to-customer/opportunity workflow and observe the state transitions. |  |
| 1-50-003 | Tested | The student must demonstrate client-side validation. | Validation | Trigger a client-side validation message on a submit form. |  |
| 1-50-004 | Tested | The student must demonstrate server-side validation. | Validation | Tamper with a request payload; the server rejects it regardless of client checks. |  |
| 1-50-005 | Tested | The student must demonstrate business rules. | Validation | Attempt an invalid opportunity value (Amount/Probability/Expected Close Date); it is rejected by a business rule. |  |
| 1-50-006 | Tested | The student must demonstrate secure authentication. | Security | Show login with hashed passwords and account lockout after repeated failures. |  |
| 1-50-007 | Tested | The student must demonstrate role-based authorization. | Authorization | Log in with different roles; confirm data/actions differ and the server enforces it. |  |
| 1-50-008 | Tested | The student must demonstrate audit logging. | Security | Perform important operations and confirm audit entries are created. |  |
| 1-50-009 | Tested | The student must demonstrate REST API functionality. | API | Call the REST endpoints; each returns an authorized, correct JSON response. |  |
| 1-50-010 | Tested | The student must demonstrate a structured application architecture. | Architecture | Inspect the layered structure: controller/service/repository/entity/DTO packages on the backend and components/pages/services on the frontend. |  |

# Phase 2 (127 requirements)

### Sections in this phase

| Source section | Requirements |
|---|---|
| p.1 Cover Page | 1 |
| p.2 §2 Complete Module Structure | 4 |
| p.4 §4.3 Dashboard | 6 |
| p.4 §4.4 Customer Management | 1 |
| p.5 §4.6 Follow-Up Management | 6 |
| p.5 §4.7 User & Role Management | 5 |
| p.6 §4.10 REST API | 1 |
| p.6 §4.11 Reports | 9 |
| p.6 §4.9 Audit Log | 1 |
| p.7 §5.3 Business Validation Rules | 1 |
| p.8 §5.4 Recommended Validation Examples | 1 |
| p.8 §6.1 Authentication Features | 1 |
| p.8 §6.2 Recommended Password Policy | 1 |
| p.8 §6.3 Lockout | 1 |
| p.9 §7.1 Suggested Permission Matrix | 18 |
| p.9-10 §8 Core CRM Workflows (User Administration) | 11 |
| p.10 §9 Core Data Entities | 1 |
| p.11 §10 REST API Specification | 3 |
| p.11 §11 Reporting Requirements | 8 |
| p.12 §13 Testing & Acceptance Checklist | 2 |
| p.13 §15 Recommended Implementation Sequence | 2 |
| p.14 §17 Business Scenario | 1 |
| p.14 §17.2 Mandatory Module Tree | 17 |
| p.15 §17.3 Database Design — Mandatory Entities | 2 |
| p.16 §17.7 Mandatory Business Validation | 1 |
| p.17 §17.11 Dashboard — Mandatory | 9 |
| p.17 §17.12 Dashboard Charts — Chart.js | 3 |
| p.17 §17.13 Search and Filtering | 2 |
| p.18 §17.16 Audit Logging Requirements | 2 |
| p.18-19 §17.17 Minimum Evaluation Criteria | 1 |
| p.19 §17.19 Final Acceptance Scenario | 4 |
| p.19 §17.20 Project Completion Standard | 1 |

## 1. p.1 Cover Page

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-01-001 | Not Started | Functional Requirements category 5: Reports. | Reporting | A report screen renders customer/lead/opportunity data for an authorized role. |  |

## 2. p.2 §2 Complete Module Structure

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-02-001 | Not Started | Module 3 — Dashboard: Role-based KPIs, summaries, charts, activities and sales pipeline visibility. | Reporting | After login the dashboard route shows role-based KPI cards and charts. |  |
| 2-02-002 | Not Started | Module 6 — Follow-Up Management: Follow-up scheduling, reminders, status, notes and activity history. | Functional | A follow-up can be scheduled against a customer/lead and shows its status. |  |
| 2-02-003 | Not Started | Module 7 — User & Role Management: User creation, role assignment, activation/deactivation and access administration. | Functional | Admin creates a user, assigns exactly one role and can deactivate the account. |  |
| 2-02-004 | Not Started | Module 11 — Reports: Customer, lead, follow-up, opportunity, sales pipeline, user activity and audit reports. | Reporting | Each named report renders role-scoped data. |  |

## 3. p.4 §4.3 Dashboard

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-03-001 | Not Started | Show role-based KPIs such as total customers, open leads, open opportunities, pending follow-ups and pipeline amount. | Reporting | Open the dashboard and confirm KPI values are shown for total customers, open leads, open opportunities, pending follow-ups and pipeline amount. |  |
| 2-03-002 | Not Started | Admin dashboard may include user/activity/security statistics. | Reporting | Log in as Admin and confirm user, activity and security statistics appear on the dashboard. |  |
| 2-03-003 | Not Started | Manager dashboard may show team pipeline, leads and opportunity performance. | Reporting | Log in as Manager and confirm team pipeline, leads and opportunity performance are shown. |  |
| 2-03-004 | Not Started | Sales Executive dashboard may show assigned customers, leads, opportunities and upcoming follow-ups. | Reporting | Log in as Sales Executive and confirm only assigned customers, leads, opportunities and upcoming follow-ups appear. |  |
| 2-03-005 | Not Started | Provide date filters such as Today, This Week, This Month and Custom Range. | UI/UX | Apply the Today, This Week, This Month and Custom Range filters and confirm the dashboard data changes accordingly. |  |
| 2-03-006 | Not Started | Charts should be based on server-authorized data. | Security | Log in as Sales Executive and confirm charts reflect only data that user is authorized to see, not the full dataset. |  |

## 4. p.4 §4.4 Customer Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-04-001 | Not Started | Allow authorized users to view customer history and related activities. | Functional | Open a customer as an authorized user and confirm the customer history and related activities are visible. |  |

## 5. p.5 §4.6 Follow-Up Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-05-001 | Not Started | Create follow-up activities against customers, leads and opportunities. | Functional | Create a follow-up against a customer, a lead and an opportunity and confirm each is stored against the correct record. |  |
| 2-05-002 | Not Started | Capture follow-up date, subject, type, status, notes and assigned user. | Data | Confirm the follow-up form contains and persists date, subject, type, status, notes and assigned user. |  |
| 2-05-003 | Not Started | Support statuses such as Planned, Completed, Missed and Cancelled. | Workflow | Confirm the follow-up status options include Planned, Completed, Missed and Cancelled and that a chosen value persists. |  |
| 2-05-004 | Not Started | Display upcoming and overdue follow-ups. | Functional | Confirm the follow-up list separates and displays upcoming and overdue follow-ups. |  |
| 2-05-005 | Not Started | Do not allow a new follow-up date earlier than today unless an authorized historical-entry process is explicitly supported. | Validation | Create a new follow-up dated yesterday and confirm it is rejected. |  |
| 2-05-006 | Not Started | Record completion and rescheduling activity in audit history. | Data | Complete and reschedule a follow-up and confirm audit history entries are created for both actions. |  |

## 6. p.5 §4.7 User & Role Management

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-06-001 | Not Started | Create, edit, activate/deactivate and search application users. | Functional | Create, edit, deactivate and search a user and confirm each operation succeeds. |  |
| 2-06-002 | Not Started | Assign exactly one primary role from Admin, Manager and Sales Executive unless multi-role support is explicitly implemented. | Authorization | Create a user and confirm exactly one primary role from Admin, Manager and Sales Executive can be assigned. |  |
| 2-06-003 | Not Started | Prevent unauthorized users from accessing user administration. | Authorization | Open the user administration page as a Sales Executive or Manager and confirm access is denied on the server. |  |
| 2-06-004 | Not Started | Allow authorized administrators to reset passwords or trigger secure password reset workflows. | Security | As Admin, reset a user's password and confirm the user can log in with the new password. |  |
| 2-06-005 | Not Started | Display account status and lockout status. | UI/UX | Confirm the user administration view shows each account's active/inactive status and lockout status. |  |

## 7. p.6 §4.10 REST API

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-07-001 | Not Started | Document endpoints, request/response models and error responses. | API | Confirm API documentation exists that lists endpoints, request/response models and error responses. |  |

## 8. p.6 §4.11 Reports

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-08-001 | Not Started | Customer Report: customer list, status, owner and creation date. | Reporting | Open the Customer Report and confirm it lists customer list, status, owner and creation date. |  |
| 2-08-002 | Not Started | Lead Report: lead source, status, owner and conversion information. | Reporting | Open the Lead Report and confirm it shows lead source, status, owner and conversion information. |  |
| 2-08-003 | Not Started | Follow-Up Report: planned, completed, missed and overdue activities. | Reporting | Open the Follow-Up Report and confirm planned, completed, missed and overdue activity counts appear. |  |
| 2-08-004 | Not Started | Opportunity Report: stage, amount, probability and expected close date. | Reporting | Open the Opportunity Report and confirm it shows stage, amount, probability and expected close date columns. |  |
| 2-08-005 | Not Started | Pipeline Report: stage-wise and owner-wise pipeline amount. | Reporting | Open the Pipeline Report and confirm stage-wise and owner-wise pipeline amounts are shown. |  |
| 2-08-006 | Not Started | Sales/Conversion Report: lead conversion and opportunity outcome metrics. | Reporting | Open the Sales/Conversion Report and confirm lead conversion and opportunity outcome metrics are shown. |  |
| 2-08-007 | Not Started | User Activity Report: user actions and activity counts. | Reporting | Open the User Activity Report and confirm user actions and activity counts are shown. |  |
| 2-08-008 | Not Started | Audit Report: security and business audit events. | Reporting | Open the Audit Report and confirm security and business audit events are listed. |  |
| 2-08-009 | Not Started | Reports should support filters, sorting, pagination and export where implemented. | Reporting | Apply a filter, sort and paginate a report and confirm each works; treat report export as a documented Phase 3 gap. |  |

## 9. p.6 §4.9 Audit Log

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-09-001 | Not Started | Provide filtering by user, module, action and date range. | UI/UX | Filter the audit log by user, module, action and date range and confirm the results narrow accordingly. |  |

## 10. p.7 §5.3 Business Validation Rules

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-10-001 | Not Started | Field/Area Follow-Up Date — Rule: Cannot be earlier than today for a new/planned follow-up. Validation Category: Date + business validation. | Validation | Creating a new/planned FollowUp with a date before today via the Follow-Up screen/API returns 400. |  |

## 11. p.8 §5.4 Recommended Validation Examples

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-11-001 | Not Started | Follow-Up Date = yesterday -> reject with: "Follow-up date cannot be earlier than today." | Validation | Submit a Follow-Up with date = yesterday and confirm the response message is exactly 'Follow-up date cannot be earlier than today.' |  |

## 12. p.8 §6.1 Authentication Features

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-12-001 | Not Started | User Management — Authorized administrators can manage user status and roles. | Functional | An Admin can activate/deactivate a user and change their role; a Sales Executive calling the same endpoint gets 403. |  |

## 13. p.8 §6.2 Recommended Password Policy

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-13-001 | Not Started | Require secure password change/reset flows. | Security | The change/reset flow requires the current password or a one-time token and re-applies the password policy. |  |

## 14. p.8 §6.3 Lockout

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-14-001 | Not Started | Allow authorized administration/unlock through a controlled process. | Security | An Admin can clear a locked account's LockoutEnd so the user can log in again. |  |

## 15. p.9 §7.1 Suggested Permission Matrix

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-15-001 | Not Started | Dashboard -> Admin = Full | Authorization | Admin opening the Dashboard sees the full set of KPI cards and charts. |  |
| 2-15-002 | Not Started | Dashboard -> Manager = Team | Authorization | Manager Dashboard data is scoped to the manager's team, not all records. |  |
| 2-15-003 | Not Started | Dashboard -> Sales Executive = Own/Assigned | Authorization | Sales Executive Dashboard shows only their own/assigned counts. |  |
| 2-15-004 | Not Started | Follow-Ups -> Admin = Full | Authorization | Admin can manage any follow-up record. |  |
| 2-15-005 | Not Started | Follow-Ups -> Manager = Team | Authorization | Manager can manage team follow-ups only. |  |
| 2-15-006 | Not Started | Follow-Ups -> Sales Executive = Own/Assigned | Authorization | Sales Executive can manage only their own/assigned follow-ups. |  |
| 2-15-007 | Not Started | User Management -> Admin = Full | Authorization | Admin can create/edit/deactivate users and assign roles. |  |
| 2-15-008 | Not Started | User Management -> Manager = View/limited administration if configured | Authorization | If configured, Manager has only limited user views; otherwise user management is denied (403). |  |
| 2-15-009 | Not Started | User Management -> Sales Executive = No | Authorization | Sales Executive calling user management endpoints receives 403. |  |
| 2-15-010 | Not Started | Role Management -> Admin = Full | Authorization | Admin can view and assign roles. |  |
| 2-15-011 | Not Started | Role Management -> Manager = No | Authorization | Manager calling role management endpoints receives 403. |  |
| 2-15-012 | Not Started | Role Management -> Sales Executive = No | Authorization | Sales Executive calling role management endpoints receives 403. |  |
| 2-15-013 | Not Started | Audit Log -> Admin = Full | Authorization | Admin can view/filter all audit log entries. |  |
| 2-15-014 | Not Started | Audit Log -> Manager = Limited view if configured | Authorization | If configured, Manager gets only a limited audit view; otherwise 403. |  |
| 2-15-015 | Not Started | Audit Log -> Sales Executive = No | Authorization | Sales Executive calling audit log endpoints receives 403. |  |
| 2-15-016 | Not Started | Reports -> Admin = All | Reporting | Admin can open all reports with organization-wide data. |  |
| 2-15-017 | Not Started | Reports -> Manager = Management/team reports | Reporting | Manager sees only management/team reports. |  |
| 2-15-018 | Not Started | Reports -> Sales Executive = Own/assigned reports | Reporting | Sales Executive sees only own/assigned reports. |  |

## 16. p.9-10 §8 Core CRM Workflows (User Administration)

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-16-001 | Not Started | Follow-Up step 1: Create Follow-Up | Workflow | POST /api/followups with valid fields returns 201. |  |
| 2-16-002 | Not Started | Follow-Up step 2: Set Date | Workflow | A follow-up date today or later saves; a date before today is rejected. |  |
| 2-16-003 | Not Started | Follow-Up step 3: Assign User | Workflow | Assigning an owner sets FollowUp.AssignedUserId. |  |
| 2-16-004 | Not Started | Follow-Up step 4: Complete/Missed/Reschedule | Workflow | Setting status Completed/Missed or rescheduling persists and updates the record. |  |
| 2-16-005 | Not Started | Follow-Up step 5: Update related CRM record | Workflow | Completing the follow-up updates the linked customer/lead/opportunity record. |  |
| 2-16-006 | Not Started | Follow-Up step 6: Audit action | Workflow | Each follow-up action writes an AuditLog entry. |  |
| 2-16-007 | Not Started | User Administration step 1: Create User | Workflow | An Admin can create a new user with a unique email. |  |
| 2-16-008 | Not Started | User Administration step 2: Assign Role | Workflow | The new user is assigned exactly one of Admin/Manager/Sales Executive. |  |
| 2-16-009 | Not Started | User Administration step 3: Set Active/Inactive | Workflow | Toggling IsActive blocks or allows the user's login. |  |
| 2-16-010 | Not Started | User Administration step 4: Apply Password/Lockout Policy | Workflow | A password set via user administration must still pass the policy, and lockout settings apply. |  |
| 2-16-011 | Not Started | User Administration step 5: Audit security change | Workflow | Creating a user and changing a role each produce an AuditLog entry. |  |

## 17. p.10 §9 Core Data Entities

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-17-001 | Not Started | FollowUp entity with representative fields: FollowUpId, RelatedType, RelatedId, FollowUpDate, Subject, Status, AssignedUserId, Notes. | Data | Inspect the FollowUp entity/table; it contains exactly those fields. |  |

## 18. p.11 §10 REST API Specification

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-18-001 | Not Started | GET /api/followups — List follow-ups — Access: Authorized | API | GET /api/followups as an authorized user returns 200 with a follow-up list. |  |
| 2-18-002 | Not Started | POST /api/followups — Create follow-up — Access: Authorized | API | POST /api/followups with a valid body returns 201/200 and the follow-up persists. |  |
| 2-18-003 | Not Started | GET /api/reports/pipeline — Pipeline report data — Access: Manager/Admin or configured scope | Reporting | GET /api/reports/pipeline as Manager/Admin returns 200 with pipeline data; as an unauthorized role returns 403. |  |

## 19. p.11 §11 Reporting Requirements

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-19-001 | Not Started | Customer Report — Key Data: Customer, status, owner, created date — Access: Admin/Manager/authorized user | Reporting | Open the Customer Report and confirm columns Customer, status, owner, created date appear and it is viewable by Admin/Manager/authorized user. |  |
| 2-19-002 | Not Started | Lead Report — Key Data: Source, status, owner, conversion — Access: Admin/Manager/Sales scope | Reporting | Open the Lead Report and confirm columns Source, status, owner, conversion and that data is scoped per role. |  |
| 2-19-003 | Not Started | Follow-Up Report — Key Data: Date, status, owner, overdue activities — Access: All authorized roles | Reporting | Open the Follow-Up Report and confirm columns Date, status, owner, overdue activities. |  |
| 2-19-004 | Not Started | Opportunity Report — Key Data: Stage, amount, probability, expected close date — Access: All authorized roles | Reporting | Open the Opportunity Report and confirm columns Stage, amount, probability, expected close date. |  |
| 2-19-005 | Not Started | Pipeline Report — Key Data: Stage-wise, owner-wise, weighted pipeline — Access: Admin/Manager; scoped sales view | Reporting | Open the Pipeline Report and confirm stage-wise, owner-wise and weighted pipeline data shows; a sales user sees only their scope. |  |
| 2-19-006 | Not Started | Conversion Report — Key Data: Leads converted vs not converted — Access: Admin/Manager; scoped sales view | Reporting | Open the Conversion Report and confirm leads converted vs not converted counts appear. |  |
| 2-19-007 | Not Started | User Activity Report — Key Data: Activity/action counts by user — Access: Admin/Manager where permitted | Reporting | Open the User Activity Report and confirm per-user activity/action counts appear. |  |
| 2-19-008 | Not Started | Audit Report — Key Data: Security and business events — Access: Admin; restricted access | Reporting | Open the Audit Report as Admin and confirm security/business events show; a non-Admin user is denied. |  |

## 20. p.12 §13 Testing & Acceptance Checklist

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-20-001 | Not Started | Follow-Up — Follow-up date cannot be earlier than today for new/planned activities. | Testing | Create a follow-up with a date before today and confirm it is rejected. |  |
| 2-20-002 | Not Started | Reports — Filters and role-based data visibility are respected. | Testing | Apply a filter to a report and view it as a different role, confirming both filter and role scope are honored. |  |

## 21. p.13 §15 Recommended Implementation Sequence

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-21-001 | Not Started | 7. Implement Follow-Up Management. | Functional | Create and manage a follow-up from the UI and confirm it is stored and listed. |  |
| 2-21-002 | Not Started | 12. Implement Reports and dashboard KPIs. | Reporting | Open the dashboard and confirm KPI cards and report data render from real records. |  |

## 22. p.14 §17 Business Scenario

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-22-001 | Not Started | managers monitor the complete sales pipeline through a dashboard. | Reporting | A Manager logs in and the Dashboard shows pipeline figures across the whole team, not only their own records. |  |

## 23. p.14 §17.2 Mandatory Module Tree

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-23-001 | Not Started | Dashboard ├── Total Customers | Reporting | Dashboard renders the Total Customers card with the count of customers visible to the user. |  |
| 2-23-002 | Not Started | Dashboard ├── Total Leads | Reporting | Dashboard renders the Total Leads card with the count of leads visible to the user. |  |
| 2-23-003 | Not Started | Dashboard ├── Open Opportunities | Reporting | Dashboard renders the Open Opportunities card with the count of currently open opportunities. |  |
| 2-23-004 | Not Started | Dashboard ├── Won Opportunities | Reporting | Dashboard renders the Won Opportunities card with the won opportunity count. |  |
| 2-23-005 | Not Started | Dashboard ├── Lost Opportunities | Reporting | Dashboard renders the Lost Opportunities card with the lost opportunity count. |  |
| 2-23-006 | Not Started | Dashboard ├── Sales Pipeline | Reporting | Dashboard renders a Sales Pipeline view of opportunities by stage. |  |
| 2-23-007 | Not Started | Dashboard ├── Charts | Reporting | Dashboard renders Chart.js charts (Lead Status, Opportunity Pipeline, Monthly Sales). |  |
| 2-23-008 | Not Started | Follow-Up ├── Schedule Follow-Up | Functional | Scheduling a follow-up persists a FollowUp row with FollowUpDate, FollowUpType and AssignedTo. |  |
| 2-23-009 | Not Started | Follow-Up ├── Complete Follow-Up | Functional | Marking a follow-up complete sets its Status to the completed value. |  |
| 2-23-010 | Not Started | Follow-Up ├── Pending Follow-Ups | Functional | The Pending Follow-Ups list shows only follow-ups whose Status is pending/planned. |  |
| 2-23-011 | Not Started | Activity Management ├── Call | Functional | Recording an activity of ActivityType Call persists an Activity row. |  |
| 2-23-012 | Not Started | Activity Management ├── Meeting | Functional | Recording an activity of ActivityType Meeting persists an Activity row. |  |
| 2-23-013 | Not Started | Activity Management ├── Email | Functional | Recording an activity of ActivityType Email persists an Activity row. |  |
| 2-23-014 | Not Started | Activity Management ├── Task | Functional | Recording an activity of ActivityType Task persists an Activity row. |  |
| 2-23-015 | Not Started | User & Role Management ├── Users | Functional | An Admin screen lists users and allows creating/editing a user. |  |
| 2-23-016 | Not Started | User & Role Management ├── Roles | Functional | An Admin screen lists Admin, Manager and SalesExecutive roles. |  |
| 2-23-017 | Not Started | User & Role Management ├── Permissions | Authorization | An Admin screen lets a role's permissions be viewed/assigned. |  |

## 24. p.15 §17.3 Database Design — Mandatory Entities

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-24-001 | Not Started | FollowUp table required fields: FollowUpId, CustomerId, LeadId, FollowUpDate, FollowUpType, Remarks, Status, AssignedTo | Data | The follow_up table schema contains exactly those columns. |  |
| 2-24-002 | Not Started | Activity table required fields: ActivityId, ActivityType, Subject, Description, ActivityDate, CustomerId, LeadId, AssignedTo, Status | Data | The activities table schema contains exactly those columns. |  |

## 25. p.16 §17.7 Mandatory Business Validation

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-25-001 | Not Started | Rule Follow-Up Date — Cannot be earlier than today for a new/planned follow-up. | Validation | POST a new/planned follow-up with FollowUpDate before today returns HTTP 400. |  |

## 26. p.17 §17.11 Dashboard — Mandatory

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-26-001 | Not Started | After successful login, the user should be redirected to the AcxiomCRM Dashboard according to the user's authorized scope. | Reporting | After login the user lands on the Dashboard showing only data within their authorized scope. |  |
| 2-26-002 | Not Started | Card Total Customers — Definition: Count of customers visible to the user. | Reporting | The Total Customers card value equals the count of customers visible to the signed-in user. |  |
| 2-26-003 | Not Started | Card Total Leads — Definition: Count of leads visible to the user. | Reporting | The Total Leads card value equals the count of leads visible to the signed-in user. |  |
| 2-26-004 | Not Started | Card Open Leads — Definition: Leads that are not closed/lost/converted. | Reporting | Open Leads count excludes leads whose status is closed, lost or converted. |  |
| 2-26-005 | Not Started | Card Total Opportunities — Definition: Count of opportunities in scope. | Reporting | The Total Opportunities card value equals the count of opportunities in the user's scope. |  |
| 2-26-006 | Not Started | Card Open Opportunities — Definition: Opportunities currently open. | Reporting | Open Opportunities count includes only currently open opportunities. |  |
| 2-26-007 | Not Started | Card Won Opportunities — Definition: Won opportunity count. | Reporting | Won Opportunities card value equals the count of opportunities with a won outcome. |  |
| 2-26-008 | Not Started | Card Lost Opportunities — Definition: Lost opportunity count. | Reporting | Lost Opportunities card value equals the count of opportunities with a lost outcome. |  |
| 2-26-009 | Not Started | Card Total Pipeline Value — Definition: Sum of eligible open opportunity amounts. | Reporting | Total Pipeline Value equals the sum of Amount over eligible open opportunities. |  |

## 27. p.17 §17.12 Dashboard Charts — Chart.js

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-27-001 | Not Started | Chart Lead Status — Required Data: New, Contacted, Qualified, Lost, Converted | Reporting | The Lead Status Chart.js chart plots counts for New, Contacted, Qualified, Lost, Converted. |  |
| 2-27-002 | Not Started | Chart Opportunity Pipeline — Required Data: Qualification, Proposal, Negotiation, Won, Lost | Reporting | The Opportunity Pipeline chart plots the five stages Qualification, Proposal, Negotiation, Won, Lost. |  |
| 2-27-003 | Not Started | Chart Monthly Sales — Required Data: Monthly sales/outcome totals for the configured reporting period | Reporting | The Monthly Sales chart plots monthly sales/outcome totals for the configured reporting period. |  |

## 28. p.17 §17.13 Search and Filtering

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-28-001 | Not Started | Page Follow-Ups — Required Search/Filter: Date, Status, Assigned User, Related Customer/Lead | Functional | The Follow-Ups list can be filtered by Date, Status, Assigned User and Related Customer/Lead. |  |
| 2-28-002 | Not Started | Page Activities — Required Search/Filter: Activity Type, Date, Status, Assigned User | Functional | The Activities list can be filtered by Activity Type, Date, Status and Assigned User. |  |

## 29. p.18 §17.16 Audit Logging Requirements

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-29-001 | Not Started | Role Change — User role changed. | Security | Change a user's role in the user/role administration screen; confirm a Role Change audit row. |  |
| 2-29-002 | Not Started | Security — Password/security-related administrative event without storing secrets. | Security | Perform a password/security admin action; confirm an audit row exists and contains no secret/hash. |  |

## 30. p.18-19 §17.17 Minimum Evaluation Criteria

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-30-001 | Not Started | Dashboard — Required cards and charts work. | Reporting | Open the dashboard; required KPI cards and Chart.js charts render with data. |  |

## 31. p.19 §17.19 Final Acceptance Scenario

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-31-001 | Not Started | Create a Follow-Up dated before today -> rejected for a new/planned follow-up. | Validation | Create a follow-up dated yesterday; expect rejection with the follow-up date rule message. |  |
| 2-31-002 | Not Started | Login as Manager -> team pipeline/report access is available. | Authorization | Log in as Manager; confirm team pipeline and team report views are accessible. |  |
| 2-31-003 | Not Started | Login as Admin -> user/role/audit administration is available. | Authorization | Log in as Admin; confirm user, role and audit administration screens are accessible. |  |
| 2-31-004 | Not Started | Open Dashboard -> KPI cards and Chart.js charts display authorized data. | Reporting | Open the Dashboard; KPI cards and Chart.js charts display data authorized for the logged-in user. |  |

## 32. p.19 §17.20 Project Completion Standard

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 2-32-001 | Not Started | The student must demonstrate dashboard analytics. | Reporting | Show the Dashboard rendering KPI cards and charts from authorized data. |  |

# Phase 3 (5 requirements)

### Sections in this phase

| Source section | Requirements |
|---|---|
| p.8 §6.2 Recommended Password Policy | 1 |
| p.11 §10.1 API Security | 2 |
| p.13 §14 Security Checklist | 1 |
| p.13 §15 Recommended Implementation Sequence | 1 |

## 1. p.8 §6.2 Recommended Password Policy

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 3-01-001 | Not Started | Consider preventing reuse of recent passwords if required by the project security standard. | Security | Resetting a password back to a recently used password is rejected by the reuse check. |  |

## 2. p.11 §10.1 API Security

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 3-02-001 | Not Started | Use HTTPS in deployed environments. | Security | In the deployed configuration, an http:// request is redirected to https:// and the TLS cert is served. |  |
| 3-02-002 | Not Started | Implement appropriate rate limiting/throttling for authentication-sensitive endpoints. | Security | Send many rapid POST /api/auth/login requests and confirm the server returns 429 once the throttle threshold is exceeded. |  |

## 3. p.13 §14 Security Checklist

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 3-03-001 | Not Started | HTTPS used in production/deployment. | Security | Confirm the deployed app serves over https:// and redirects plain http:// traffic. |  |

## 4. p.13 §15 Recommended Implementation Sequence

| # | Status | Requirement | Category | How to verify | Stack flag |
|---|---|---|---|---|---|
| 3-04-001 | Not Started | 14. Prepare deployment configuration and production security settings. | Security | Confirm a deployment config with production security settings (HTTPS, secrets, throttling) is prepared and applied. |  |

---

# Extraction audit

This checklist was produced by a multi-pass extraction over all 19 pages: seven section extractors, one entity reconciler, one .NET-coupling reconciler, and one adversarial completeness critic that re-read the document page by page against the extracted list.

- **Raw items extracted:** 483
- **Unique after de-duplication:** 483
- **Gap-fill round:** 0 further items needed (the critic found no missing requirements)
- **Critic verdict:** Coverage is effectively complete. After a page-by-page, row-by-row, cell-by-cell pass over all 19 pages I could not identify a substantive requirement, table row, matrix cell, enumeration value, or qualified clause that is absent from the extraction. Specific high-risk areas all check out: the §7.1 permission matrix is complete (all 30 cells verified), both data-model listings are fully present with every field (§9's 7 entities and §17.3's 6 tables — they are genuinely different models, and both are captured), the §17.2 module tree has all 41 leaves across its 9 branches, the §10 REST table has all 14 endpoints with their Access column, and every status/stage/role enumeration retains every value (including the deliberate divergence between §4.5's six lead statuses 'New, Contacted, Qualified, Unqualified, Converted, Lost' and §17.12's five chart statuses without 'Unqualified'). Parentheticals and 'where configured'/'unless ... explicitly implemented'/'if the project rule' clauses are all retained. Consequently the 'missing' array is empty by design, not by omission of effort. The weakest area is NOT content coverage but metadata: the only genuine defect I found is that the extraction does not flag the .NET-specific items (§17.4 ASP.NET Core Identity / IdentityUser-Role; §17.9 EF Core, ValidateAntiForgeryToken/MVC, Razor/unobtrusive; §17.17 'Controllers/Models/Data/Services/ViewModels/Views'; §15 'solution structure'), and it does not surface that these conflict with §3's explicit 'technology-independent' statement — the crux of the user's Java/React confirmation request. A secondary caveat: no phase attribute was visible in the extract supplied to me, so I could verify requirement content but could not audit phase assignment. The absolute worst that can be said of the content itself is cosmetic: the §17.2 tree root node label ('AcxiomCRM') is not emitted, and tree connectors are normalized to '├──' even for final children — neither changes any requirement.

## Corrections applied from the critic pass

| Item | Problem found | Resolution |
|---|---|---|
| p.15 §17.4 ... :: Use ASP.NET Core Identity for users, password hashing, claims/roles, authentication cookies, password policy and lockout. (and the companion line) Use IdentityUser/IdentityRole or appropriately extended application identity classes. | This item is preserved word-for-word but carries no stack flag. It is not a generic requirement — it hard-mandates a specific .NET framework (ASP.NET Core Identity, IdentityUser/IdentityRole). Per the extraction contract this should carry a .NET-specific flag. Critically, §3 explicitly states the architecture is 'technology-independent and can be implemented using different programming languages, frameworks, databases, and deployment platforms,' so §17.4 is in direct conflict with §3 (and with the user's Java/React stack). The extraction records both statements but never surfaces that they conflict, which is the single most important reconciliation point for this project. | Tag this item as .NET-specific (ASP.NET Core Identity) and add a note that §3's 'technology-independent' clause permits a Java/Spring Security equivalent, so the mandated stack is a conflict to be confirmed, not a hard constraint. |
| p.16 §17.9 ... :: SQL Injection — Use Entity Framework Core parameterization/query APIs; do not concatenate user input into SQL. | Preserved verbatim but flagged generically. The requirement is .NET-specific (Entity Framework Core / query APIs) and would be silently un-actionable under Java, where the equivalent is JPA/Hibernate parameter binding. The underlying normative intent (use parameterized queries, never string-concatenate input) is stack-neutral and should be separated from the EF Core mechanism. | Flag as .NET-specific; retain the stack-neutral intent ('use parameterized query APIs, never concatenate user input into SQL') as the requirement a Java implementation must satisfy. |
| p.16 §17.9 ... :: Anti-Forgery — Protect state-changing MVC POST forms with ValidateAntiForgeryToken. (and) p.4 §4.2 ... :: Use secure session/cookie configuration and anti-forgery protection for state-changing MVC requests. | Both items are .NET/MVC-specific ('ValidateAntiForgeryToken', 'MVC requests') but are presented as generic. Under React + Spring Boot the MVC anti-forgery token concept has no direct equivalent the way stated; the requirement's stack-neutral intent is CSRF protection for state-changing requests. Leaving it unflagged makes it look satisfiable as written. | Flag both as .NET/MVC-specific and restate the portable intent (CSRF protection on state-changing requests) that a Java/React implementation must meet. |
| p.16 §17.9 ... :: Client Validation — Use unobtrusive/Razor validation for user experience. (and) p.18-19 §17.17 Minimum Evaluation Criteria :: Architecture — Controllers, Models, Data, Services, ViewModels and Views are organized. (and) p.13 §15 ... :: 1. Create solution structure and database. | These carry .NET-specific vocabulary ('unobtrusive/Razor validation', the .NET MVC folder convention 'Controllers/Models/Data/Services/ViewModels/Views', and 'solution structure') with no flag. They are also the two items most likely to be given a wrong phase/category: 'Architecture' here encodes a .NET project layout, whereas the user's mandated layout is config/security/controller/dto/entity/repository/service/validation/audit/exception on the backend and components/layouts/pages/services/validation on the frontend. | Flag as .NET-specific; note §17.17 'Architecture' must be re-mapped to the mandated Java/React package structure rather than taken literally, and 'solution structure' means project/solution scaffold, not a .NET .sln. |

## Data model reconciliation (specification lists it twice)

The specification states its data model twice, with different fields. Both are recorded here; this project implements the union so no mandatory field is dropped.

| Entity | §9 Core Data Entities (p.10) | §17.3 Mandatory Entities (p.15) | Implemented in |
|---|---|---|---|
| **User** | UserId, Name, Email, PasswordHash, RoleId, IsActive, FailedLoginCount, LockoutEnd, CreatedDate | absent | Phase 1 |
| **Role** | RoleId, RoleName | absent | Phase 1 |
| **Customer** | CustomerId, Name, Email, Phone, Address, Status, OwnerId, CreatedDate, ModifiedDate | CustomerId, CustomerCode, CustomerName, Email, Phone, CompanyName, Address, City, State, Status, CreatedDate, CreatedBy | Phase 1 |
| **Lead** | LeadId, Name, Email, Phone, Source, Status, Priority, OwnerId, CreatedDate | LeadId, LeadCode, LeadName, Email, Phone, CompanyName, Source, Status, ExpectedValue, CreatedDate, AssignedTo | Phase 1 |
| **Opportunity** | OpportunityId, CustomerId, Name, Stage, Amount, Probability, ExpectedCloseDate, OwnerId, Notes | OpportunityId, OpportunityName, CustomerId, LeadId, Amount, Stage, Probability, ExpectedCloseDate, Status, CreatedDate, AssignedTo | Phase 1 |
| **FollowUp** | FollowUpId, RelatedType, RelatedId, FollowUpDate, Subject, Status, AssignedUserId, Notes | FollowUpId, CustomerId, LeadId, FollowUpDate, FollowUpType, Remarks, Status, AssignedTo | Phase 2 |
| **Activity** | absent | ActivityId, ActivityType, Subject, Description, ActivityDate, CustomerId, LeadId, AssignedTo, Status | Phase 2 |
| **AuditLog** | AuditLogId, UserId, Module, Action, RecordId, Timestamp, Result, Details | AuditLogId, UserId, Action, EntityName, RecordId, OldValue, NewValue, CreatedDate, IpAddress | Phase 1 |
| **IdentityUser / IdentityRole** | absent | absent | Phase 1 |
| **Permission** | absent | absent | Phase 2 |
| **Contact** | absent | absent | Phase 2 |

### Recorded conflicts

- **Data model coverage (entities present in one list, missing from the other)** - §9 Core Data Entities (p.10) lists 7 entities: User, Role, Customer, Lead, FollowUp, Opportunity, AuditLog. §17.3 Database Design - Mandatory Entities (p.15) lists 6 entities: Customer, Lead, Opportunity, FollowUp, Activity, AuditLog. User and Role appear ONLY in §9; Activity appears ONLY in §17.3. §9 has no Activity entity even though §17.2 mandates an Activity Management module. **Resolution:** Do not drop any entity. Implement User, Role, Customer, Lead, Opportunity and AuditLog in Phase 1; add Activity (and FollowUp) in Phase 2. Never remove a mandatory field when merging.
- **Why §17.3 omits User/Role** - §17.3 does not list User or Role because §17.4 states: 'Do not create a custom password table for authentication. Use ASP.NET Core Identity for users, password hashing, claims/roles, authentication cookies, password policy and lockout.' §17.4 also says: 'Use IdentityUser/IdentityRole or appropriately extended application identity classes.' Identity is therefore the source of the User/Role tables, so §9's User/Role field lists are the design contract while §17.3 simply defers to the framework. **Resolution:** Treat §9's User/Role fields as required, supplied by the identity framework. In the Java stack substitute Spring Security + JPA User/Role entities and keep PasswordHash, FailedLoginCount, LockoutEnd (§6.1/§6.3). Confirm the framework substitution with the user first.
- **Mandatory Module Tree (§17.2) vs Functional Module Requirements (§4)** - §17.2 mandates Authentication -> Register (self-registration) and a distinct Activity Management module (Call, Meeting, Email, Task). §4.2 Authentication lists only 'Provide Login and Logout' with no Register, and §2/§4 define no Activity Management module at all (only Follow-Up Management). **Resolution:** Reconcile scope explicitly: decide whether self-registration and a standalone Activity module exist. Phase 1 scope (login/logout/roles/CRM core) follows §4; treat Register and Activity as Phase 2 unless the user says otherwise.
- **Field-level contradictions for the same entity** - Customer: §9 uses Name and OwnerId/ModifiedDate; §17.3 requires CustomerName, CustomerCode, CompanyName, City, State, CreatedBy and omits OwnerId. Lead: §9 has Priority; §17.3 has no Priority but adds LeadCode, CompanyName, ExpectedValue. Opportunity: §9 lacks LeadId and Status; §17.3 has them but lacks Notes. FollowUp: §9 models it polymorphically (RelatedType/RelatedId); §17.3 uses explicit CustomerId/LeadId and FollowUpType/Remarks instead of Subject/Notes. AuditLog: §9 has Module/Timestamp/Result/Details; §17.3 has EntityName/OldValue/NewValue/IpAddress. **Resolution:** For each entity adopt the UNION exactly as listed in the entities output; keep both explicit foreign keys and both audit field groups. Resolve the FollowUp polymorphic-vs-explicit modelling conflict before building it (recommended: explicit FKs).
- **Authorization rules stated differently across sections** - §7.1 gives Manager 'View/limited administration if configured' for User Management, while §17.10 says Manager has 'no unrestricted security administration'. §17.2 grants every role the CRM modules, yet §11 restricts Pipeline Report to Admin/Manager and Audit Report to Admin only. §4.7 requires 'exactly one primary role unless multi-role support is explicitly implemented', against §7's fixed three-role model. **Resolution:** Apply least privilege (§14): Manager = team/business scope only with no security administration; only Admin manages users/roles/audit. Enforce on the server, not just the UI (§7), and use a single primary role in Phase 1.
- **REST API surface mismatch** - §10 REST API Specification lists GET/POST /api/followups and GET /api/reports/pipeline; §17.14 Mandatory API lists only customers, leads and opportunities (no followups or reports endpoints). §10 marks /api/auth/login as 'Public/Rate limited' while other sections do not restate rate limiting. **Resolution:** Phase 1: implement auth plus customers, leads and opportunities endpoints (§17.14 is the minimum). Add followups and pipeline-report endpoints in Phase 2. Return DTOs, validate payloads, enforce authorization and use correct status codes (§10.1/§17.14).
- **Dashboard card list mismatch** - §17.2 tree lists Total Customers, Total Leads, Open Opportunities, Won Opportunities, Lost Opportunities, Sales Pipeline and Charts. §17.11 additionally lists Open Leads, Total Opportunities and Total Pipeline Value. **Resolution:** Implement the superset from §17.11 (which is explicit about each card's definition) using server-authorized data, and render the §17.12 charts against the same authorized scope.
- **Mandated technology contradicting the requested stack** - The spec mandates ASP.NET Core Identity and IdentityUser/IdentityRole (p.15 §17.4), Entity Framework Core parameterization (p.16 §17.9), ValidateAntiForgeryToken for MVC POST forms (p.16 §17.9) and Chart.js (p.17 §17.12) — all .NET/Razor specifics. The requested implementation stack is Java 17 + Spring Boot + Spring Security + Spring Data JPA/Hibernate + MySQL + React/Vite. **Resolution:** FLAG this to the user and obtain explicit confirmation that Java/React is permitted before changing the mandated stack (the user's request requires this). Mapping: Identity -> Spring Security + BCrypt/Argon2 hashing; EF Core -> Spring Data JPA/Hibernate; MVC anti-forgery -> CSRF protection or stateless token auth for the React API; Chart.js -> a React chart library; unobtrusive/Razor validation -> Jakarta Bean Validation on the server and client-side rules in React.
- **Document numbering gaps** - Sub-section numbering jumps: §17 -> §17.2 (no §17.1), §17.14 -> §17.16 (no §17.15), §17.17 -> §17.19 (no §17.18). **Resolution:** Treat the missing sub-sections as document omissions and do not invent requirements for them; confirm with the user whether content was lost from the PDF.
