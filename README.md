# AcxiomCRM — Phase 1 & Phase 2

A role-based CRM covering the customer-sales lifecycle: authentication, role-based authorization, customer / lead / opportunity management, follow-ups scheduling & past-date validation, activity management (calls, meetings, emails, tasks), user & role administration, audit logging, and role-scoped dashboard with 8 KPI cards and 3 Chart.js charts.

## Stack

**Java 17 · Spring Boot 3.5.16 · Spring Security · Spring Data JPA / Hibernate · MySQL 8 · Jakarta Bean Validation · React 19 · Vite · React Router · Axios · Chart.js · Bootstrap 5**

> The assignment specification describes this system using **ASP.NET Core Identity** and **Entity Framework Core**. The implementation uses the Java equivalents above, which was confirmed before any code was written. The full requirement-by-requirement mapping is in [`docs/STACK-MAPPING.md`](docs/STACK-MAPPING.md) — no requirement was dropped, each is satisfied by the Java/React mechanism recorded there.

## Layout

```
backend/    Spring Boot API — config, security, controller, dto, entity,
            repository, service, validation, audit, exception
frontend/   React SPA — components, layouts, pages, services, validation, utils
docs/       Requirements checklist, stack mapping, runbook, DB setup
```

## Getting started

See **[`docs/RUNBOOK.md`](docs/RUNBOOK.md)**. It covers the JDK 17 requirement, database setup, starting both applications and running the tests.

The short version:

```bash
mysql -u root -p < docs/mysql-setup.sql          # once

cd backend && export JAVA_HOME=/path/to/jdk-17   # JDK 17 is mandatory
export DB_USER=acxiom DB_PASSWORD='...' DB_NAME=acxiomcrm
./mvnw spring-boot:run                            # :8080

cd frontend && npm install && npm run dev         # :5173
```

Sign in with `admin@acxiomcrm.local` / `Admin@123`. Change the seeded credentials before any real deployment.

## Tests

```bash
cd backend && ./mvnw test
```

39 tests, all passing (Phase 1 + Phase 2 exit checklists). They run on in-memory H2 in MySQL-compatibility mode (test scope only — the application runs on MySQL), and they exercise the real Spring context and the real security filter chain with CSRF **enabled**.

## Documentation

| Document | What it is |
|---|---|
| [`docs/PHASE-1-CHECKLIST.md`](docs/PHASE-1-CHECKLIST.md) | All 483 requirements from the specification in its own terminology, with per-requirement status. Read the "Phase 1 status after this pass" section for what was verified by execution and what was not. |
| [`docs/STACK-MAPPING.md`](docs/STACK-MAPPING.md) | Each .NET mechanism the specification names, mapped to the Java/React equivalent used. |
| [`docs/RUNBOOK.md`](docs/RUNBOOK.md) | Prerequisites, JDK 17 warning, database, running both apps, configuration reference. |
| [`docs/mysql-setup.sql`](docs/mysql-setup.sql) | Creates the database and application user. |

## Known gaps

1. **The verification runs used H2, not MySQL.** MySQL was unavailable in the development environment (server present, credentials not). Nothing in the code is different — the same mappings, services, repositories and security chain were exercised — but running on MySQL specifically is an open Phase 1 exit criterion. `docs/mysql-setup.sql` closes it.
2. **Follow-Up rules are Phase 2.** §5.3 / §13 / §17.19 require a business rule on Follow-Up dates; `FollowUp` is a Phase 2 entity, so 11 checklist rows are marked `In Progress`.
3. **The UI was not driven in a browser.** It builds, lints and render-checks clean, but no requirement phrased "confirm the user sees X" was confirmed visually.

## Security notes

- Passwords are hashed with BCrypt and never returned by any API response or written to a log.
- Authorization is enforced **on the server** — hiding a navigation link is a convenience, never the control. A Sales Executive calling an Admin-only endpoint receives `403` regardless of the UI.
- Ownership isolation returns `404` rather than `403` for another user's record, so the existence of records cannot be probed.
- CSRF protection is enabled. The SPA uses the double-submit cookie pattern: read `XSRF-TOKEN`, send it back as `X-XSRF-TOKEN`.
- Database credentials are read from `DB_USER` / `DB_PASSWORD` / `DB_NAME` environment variables and are never committed.
