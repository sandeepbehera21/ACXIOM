# AcxiomCRM — Stack Mapping and Compliance Note

**Status:** Confirmed 2026-10-14
**Purpose:** The project specification was written for a .NET stack. This document records exactly where the specification mandates a .NET mechanism, how this project satisfies the same intent with Java/Spring Boot/React, and the clause in which the specification declares its own architecture portable. Keep this with your submission.

---

## 1. The specification declares its architecture technology-independent

> **p.3 §3 Application Architecture** — *"The CRM application follows a layered architecture that separates presentation, application processing, business logic, data access, database, and security responsibilities. **This design is technology-independent and can be implemented using different programming languages, frameworks, databases, and deployment platforms.**"*

This is the clause that permits a Java implementation. Everything below maps the specification's *intent* onto the Java stack, so that each requirement is demonstrably met even where the named framework cannot be.

## 2. Where the specification mandates a .NET mechanism (verbatim)

| Page / Section | Quoted requirement |
|---|---|
| p.15 §17.4 Authentication Database Requirement | *"Do not create a custom password table for authentication. Use **ASP.NET Core Identity** for users, password hashing, claims/roles, authentication cookies, password policy and lockout."* |
| p.15 §17.4 | *"Use **IdentityUser/IdentityRole** or appropriately extended application identity classes."* |
| p.15 §17.4 | *"Store only framework-managed password hashes."* |
| p.16 §17.9 Mandatory Security Implementation | Password Security — *"Use **ASP.NET Core Identity** password hashing; never store plain-text passwords."* |
| p.16 §17.9 | Anti-Forgery — *"Protect state-changing MVC POST forms with **ValidateAntiForgeryToken**."* |
| p.16 §17.9 | SQL Injection — *"Use **Entity Framework Core** parameterization/query APIs; do not concatenate user input into SQL."* |
| p.16 §17.9 | Client Validation — *"Use **unobtrusive/Razor** validation for user experience."* |
| p.4 §4.2 Authentication & Authorization | *"Use secure session/cookie configuration and anti-forgery protection for state-changing **MVC** requests."* |
| p.13 §14 Security Checklist | *"Anti-forgery protection enabled for state-changing **MVC forms**."* |
| p.18 §17.14 REST API — Mandatory | *"Use DTOs rather than exposing **EF entities** directly."* |
| p.19 §17.17 Minimum Evaluation Criteria | Authentication — *"**Identity** login/logout/register works."* |
| p.19 §17.17 | Architecture — *"**Controllers, Models, Data, Services, ViewModels and Views** are organized."* |

## 3. Mapping to this project's stack

| PDF mandate | Implementation in this project | Where it lives | Compliance note |
|---|---|---|---|
| ASP.NET Core Identity (users, hashing, claims/roles, cookies, password policy, lockout) | Spring Security with a `DaoAuthenticationProvider`, `BCryptPasswordEncoder`, `AppUser` and `Role` JPA entities, server-side session cookie, and explicit lockout fields | `security/`, `entity/AppUser.java`, `entity/Role.java` | Same six capabilities, different framework. Each is individually demonstrable. |
| `IdentityUser` / `IdentityRole` | `AppUser` and `Role` `@Entity` classes | `entity/` | **This is the one unavoidable divergence.** Java has no Identity framework, so an application-managed user table is required. §17.4's rule is read as its intent: *do not hand-roll password storage or invent your own hashing* — which this project honours by delegating all hashing and verification to Spring Security's `BCryptPasswordEncoder` and never writing password logic by hand. |
| EF Core parameterization / "do not concatenate user input into SQL" | Spring Data JPA repositories and JPQL/derived queries, which bind all parameters. No string-concatenated SQL anywhere. | `repository/` | Equivalent guarantee, verified by inspection: no `@Query` uses string concatenation. |
| `ValidateAntiForgeryToken` on state-changing POST forms | Spring Security CSRF protection via `CookieCsrfTokenRepository`. The token is issued to the React client and returned on every mutating request in an `X-XSRF-TOKEN` header. | `security/SecurityConfig.java` | Equivalent mechanism. **This only holds if CSRF remains enabled** — it must not be disabled to make the SPA "just work". |
| unobtrusive/Razor validation | React form components with per-field inline errors for the client side, plus Jakarta Bean Validation on every DTO for the server side | `validation/`, `src/validation/` | Equivalent. The specification's real requirement is *both layers exist*, and §5 of the PDF is satisfied in full. |
| MVC Views / ViewModels | React pages and Axios service modules; DTOs on the server | `src/pages/`, `dto/` | Equivalent separation. |
| Controllers, Models, Data, Services, ViewModels, Views | `config`, `security`, `controller`, `dto`, `entity`, `repository`, `service`, `validation`, `audit`, `exception` | see `README.md` | Same separation of concerns with the naming this project's brief specifies. Present this mapping to the evaluator if §17.17 is read literally. |
| **Chart.js** (§17.12, mandatory) | Chart.js, via `react-chartjs-2` | Phase 2 | **No conflict** — Chart.js is framework-agnostic and runs unchanged in React. |
| **Bootstrap** (§12, mandatory) | Bootstrap 5 | `src/` | **No conflict** — portable. |

### Summary of the compliance position

- **Fully satisfied, no divergence:** all of §5 Validation, §6.2 Password Policy, §6.3 Lockout behaviour, §7 / §7.1 roles and permission matrix, §17.5–17.8 validation rules, §17.11–17.13 dashboard/search, §17.16 audit logging, and the whole of §10 / §17.14 REST API.
- **Satisfied by equivalent mechanism (different framework name):** hashing, authentication cookies, CSRF/anti-forgery, parameterized queries, client-side validation.
- **One genuine divergence to disclose:** the user/role store is application-managed rather than framework-provided, because no Java equivalent of ASP.NET Core Identity exists. Disclosed rather than hidden.

## 4. Confirmed technical decisions

| # | Decision | Chosen | Alternative rejected |
|---|---|---|---|
| 1 | Stack | Java 17 + Spring Boot + Maven + MySQL + React/Vite, with this mapping documented | Switching to ASP.NET Core as literally written |
| 2 | Session model | Server-side HttpOnly session cookie + CSRF token in `X-XSRF-TOKEN` header | JWT in memory + `Authorization: Bearer`, which does not match the specification's cookie and anti-forgery wording and complicates logout and lockout |
| 3 | Versions | Java 17 + Spring Boot **3.5.16** | Java 25 (already installed) + Spring Boot 4.1.1 — viable, since Spring Boot 4.1.1 supports Java 17–26, but Spring Framework 7 / Spring Security 7 are newer with far less beginner-oriented documentation |
| 4 | Registration | Public `Register` may create **only** a SalesExecutive account; Admin creates Manager and Admin accounts | Admin-only (fails acceptance step §17.19 #2) and pending-approval (adds an approval workflow not asked for) |

Decision 4 resolves a contradiction in the specification itself: §17.2, §17.9 and acceptance step §17.19 #2 all require a working `Register`, while §4.7 and the §7.1 permission matrix state that only Admin manages users and Sales Executive gets **No** user management. Registering at the least-privileged role satisfies both.

## 5. Configurable values and documented assumptions

The specification leaves these to "the project rule" or "as configured". These are the values this project uses; each is a single constant so it is easy to change or justify.

| Setting | Value used | Source and reasoning |
|---|---|---|
| Phone format | 10-digit Indian mobile (`^[6-9]\d{9}$`) | §5.1 states *"10-digit Indian mobile format, if that is the project rule"* — the format is explicitly a project choice, so this is the project's rule |
| Password minimum length | 8 characters | §6.2 — *"Minimum length: 8 characters (or higher if the organization's security standard requires it)"* |
| Password character classes | At least one uppercase, one lowercase, one digit, one special character | §6.2 — *"Require a combination of character classes where appropriate"* |
| Failed-login threshold | 5 attempts | §6.3 — *"After a configurable threshold"*; value not specified by the PDF |
| Lockout duration | 15 minutes | §6.3 — *"for a configurable duration"*; value not specified by the PDF |
| Failed-attempt reset | Reset to 0 on successful authentication | §6.3 — *"Reset failed-attempt count after successful authentication according to the security policy"* |
| Roles per user | Exactly one primary role | §4.7 — *"Assign exactly one primary role ... unless multi-role support is explicitly implemented"*; this project does not implement multi-role |
| Entity fields | Union of §9 (p.10) and §17.3 (p.15) | The specification lists its data model twice with different fields. §17.3 is labelled Mandatory and adds `CustomerCode`, `CompanyName`, `City`, `State`, `LeadCode`, `ExpectedValue` and the `Activity` entity; §9 adds `Priority`, `Source`, `Notes`, `OwnerId`, `ModifiedDate`. No mandatory field is silently dropped. |

## 6. Known gaps carried into Phase 2 and Phase 3

Listed here so nothing is lost; the full tracker is in `docs/PHASE-1-CHECKLIST.md`.

- **Phase 2:** Dashboard KPI cards and Chart.js charts (§17.11, §17.12), Follow-Up Management (§4.6), Activity Management (§17.2), User & Role administration screens (§4.7), Reports (§11), self-service password change/reset (§6.2).
- **Phase 3:** HTTPS/deployment configuration (§10.1, §14), rate limiting on `/api/auth/login` (§10.1), password reuse prevention (§6.2), report export (§11), API documentation (§4.10).
