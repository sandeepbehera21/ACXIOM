# AcxiomCRM — Enterprise Sales & Customer Lifecycle Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.5.16](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-purple.svg)](https://vitejs.dev/)
[![Bootstrap 5](https://img.shields.io/badge/Bootstrap-5-blueviolet.svg)](https://getbootstrap.com/)
[![Tests Passing](https://img.shields.io/badge/Tests-39%2F39%20Passing-success.svg)]()

A comprehensive, role-based Customer Relationship Management (CRM) platform covering the entire customer-sales lifecycle. Built with a production-grade **Java 17 / Spring Boot 3** backend and a responsive **React 19 / Vite** frontend.

---

## Table of Contents

- [Key Features](#key-features)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Pre-Seeded Demo Accounts](#pre-seeded-demo-accounts)
- [Quick Start Guide](#quick-start-guide)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#1-backend-setup)
  - [Frontend Setup](#2-frontend-setup)
- [Running Automated Tests](#running-automated-tests)
- [System Capabilities & Modules](#system-capabilities--modules)
  - [Phase 1 Modules](#phase-1-modules)
  - [Phase 2 Modules](#phase-2-modules)
  - [Landing Page & Home Page](#landing-page--home-page)
- [Security & Compliance](#security--compliance)
- [API Overview](#api-overview)
- [Repository Structure](#repository-structure)

---

## Key Features

- **Public Landing Page:** Interactive marketing page with feature showcases, architecture highlights, and **1-click demo logins**.
- **Personalized CRM Home Page:** Live executive dashboard featuring 8 real-time KPI metrics, 3 interactive Chart.js charts, and quick-action creation shortcuts.
- **Customer 360° Management:** Complete customer account lifecycle, contact records, and active/inactive status toggling.
- **Lead Qualification & 1-Click Conversion:** Atomic transaction converting qualified leads into both a Customer account and an active Opportunity.
- **Stage-Weighted Pipeline Forecasting:** Deal progression through 5 stages (`Qualification`, `Proposal`, `Negotiation`, `Won`, `Lost`) with automated weighted probability calculations.
- **Follow-Up Scheduling & Validation:** Automated enforcement rejecting past dates (`§17.19`), tracking completion status, and logging audit events.
- **Multi-Channel Activity Logging:** Calls, Meetings, Tasks, and Email interactions with filtering by type, date, status, and assigned user.
- **Role-Based Access Control (RBAC):** Server-enforced authorization across `Admin`, `Manager`, and `SalesExecutive` roles with row-level ownership isolation.
- **Tamper-Evident Audit Trail:** Immutable logging of all successful logins, failed attempts, record mutations, and role changes.

---

## Architecture & Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17 LTS, Spring Boot 3.5.16, Spring Security, Spring Data JPA, Hibernate ORM |
| **Database** | MySQL 8 (Production) / In-Memory H2 in MySQL mode (Acceptance Tests) |
| **Security** | BCrypt password hashing, Double-Submit CSRF cookie token, HttpOnly Session Cookie |
| **Frontend** | React 19, Vite 8, React Router v7, Axios, Chart.js, react-chartjs-2, Bootstrap 5 |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) with normalized error envelopes |

---

## Pre-Seeded Demo Accounts

The database automatically seeds three accounts on initial startup:

| Role | Email | Password | Permissions Scope |
|---|---|---|---|
| **Sales Manager** | `manager@acxiomcrm.local` | `Manager@123` | Team-wide customer & pipeline visibility, manager user directory |
| **System Admin** | `admin@acxiomcrm.local` | `Admin@123` | Full administrative control, user role management, system audit logs |
| **Sales Executive** | `sales@acxiomcrm.local` | `Sales@123` | Personal assigned leads, opportunities, follow-ups, and activities |

---

## Quick Start Guide

### Prerequisites

- **Java JDK 17** (Temurin / OpenJDK 17)
- **Node.js 18+** & **npm 9+**
- **Git**

---

### 1. Backend Setup

```bash
cd backend

# On Windows PowerShell:
$env:JAVA_HOME = "C:\Users\<YourUser>\jdk-temurin\jdk-17.0.20.1+1" # Or your JDK 17 path
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"

# On Linux / macOS:
export JAVA_HOME=/path/to/jdk-17
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The Spring Boot API will start on **`http://localhost:8080`**.

---

### 2. Frontend Setup

In a separate terminal window:

```bash
cd frontend
npm install
npm run dev
```

The React / Vite application will be available at **`http://localhost:5173`**.

Vite dev server is pre-configured with a reverse proxy for `/api` pointing to `http://localhost:8080`, ensuring cross-origin CSRF tokens and session cookies work seamlessly.

---

## Running Automated Tests

The test suite exercises the complete Spring context, real database repositories, and Spring Security filters (including CSRF validation):

```bash
cd backend
./mvnw test
```

**Results:**
- **39 / 39 tests passing** (0 failures, 0 errors, 0 skipped).
- Phase 1 Exit Checklist: **30 tests passing**.
- Phase 2 Exit Checklist: **8 tests passing**.
- Spring Boot Application Context: **1 test passing**.

---

## System Capabilities & Modules

### Phase 1 Modules
- **Authentication & Self-Registration:** Login, logout, session restoration (`/api/auth/me`), and public registration.
- **Customer Management:** Full CRUD, search, active filtering, and ownership assignment.
- **Lead Pipeline:** Creation, status updates (`NEW`, `CONTACTED`, `QUALIFIED`, `LOST`), and conversion into customers & opportunities.
- **Opportunity Management:** Deal stage pipeline, closing date tracking, probability weighting, and financial totals.
- **Audit Logging:** Searchable, filterable audit log for administrative compliance.

### Phase 2 Modules
- **Follow-Up Management:** Scheduling meetings, calls, and tasks linked to leads, opportunities, or customers. Enforces future-date business rule (`"Follow-up date cannot be earlier than today."`).
- **Activity Tracking:** Logging of sales interactions (`CALL`, `MEETING`, `TASK`, `EMAIL`) with status progression.
- **User Administration:** Admin endpoint to create users, update roles, toggle active status, and search user accounts.
- **Analytics & Reporting:** 8 KPI metrics and 3 Chart.js visualizations (`Lead Status`, `Opportunity Pipeline`, and `Monthly Sales`).

### Landing Page & Home Page
- **Landing Page (`/` when signed out, or `/landing`):** Modern SaaS homepage with feature cards, security architecture overview, and 1-click demo login buttons.
- **Home Page (`/dashboard` or `/home`):** Personalized operational dashboard greeting the user, displaying quick action buttons, live KPI cards, interactive charts, and pipeline stage breakdown.

---

## Security & Compliance

1. **Password Protection:** Hashes are generated using BCrypt with adaptive salted work factor. Plaintext passwords are never stored or exposed.
2. **Double-Submit CSRF:** Protection enabled on all non-GET API endpoints. Frontend automatically retrieves and echoes the `X-XSRF-TOKEN` header.
3. **Server-Side Authorization:** Implemented with Spring Security method security (`@PreAuthorize`) and URL security rules. The API enforces permissions independently of UI controls.
4. **Ownership Isolation:** Sales Executives are restricted to their assigned records. Accessing cross-owner records returns `404 Not Found` to prevent entity probing.
5. **Auditing:** Sensitive security events (login success, login failure, lockout, role changes, record deletions) are persisted to the database.

---

## API Overview

| Method | Endpoint | Description | Role / Auth |
|---|---|---|---|
| `GET` | `/api/auth/csrf` | Fetch CSRF token & cookie | Public |
| `POST` | `/api/auth/login` | User login session | Public |
| `POST` | `/api/auth/logout` | Terminate session | Authenticated |
| `POST` | `/api/auth/register` | Self-register Sales Executive | Public |
| `GET` | `/api/auth/me` | Fetch active authenticated user | Authenticated |
| `GET` | `/api/reports/pipeline` | 8 KPIs, 3 charts data, stage breakdown | Authenticated |
| `GET/POST` | `/api/customers` | List / Create customers | Authenticated |
| `GET/POST` | `/api/leads` | List / Create leads | Authenticated |
| `POST` | `/api/leads/{id}/convert` | Convert lead to Customer + Opportunity | Authenticated |
| `GET/POST` | `/api/opportunities` | List / Create opportunities | Authenticated |
| `GET/POST` | `/api/followups` | List / Schedule follow-ups | Authenticated |
| `GET/POST` | `/api/activities` | List / Log sales activities | Authenticated |
| `GET/POST` | `/api/users` | List / Create users | Admin / Manager |
| `GET` | `/api/audit-logs` | Query security audit log | Admin only |

---

## Repository Structure

```
.
├── backend/                  # Spring Boot 3.5.16 Application
│   ├── src/main/java/        # Domain entities, repositories, services, controllers, security
│   ├── src/main/resources/   # Application properties (local, test, MySQL)
│   ├── src/test/java/        # 39 acceptance tests (Phase 1 & Phase 2 checklists)
│   └── pom.xml               # Maven dependencies and build configuration
├── frontend/                 # React 19 + Vite 8 SPA
│   ├── src/components/       # UI components (KPI tiles, modals, form fields, badges)
│   ├── src/layouts/          # AppLayout, Sidebar, TopBar
│   ├── src/pages/            # LandingPage, HomePage, Customer, Lead, Opportunity, FollowUp, Activity, User, Audit
│   ├── src/services/         # Axios API client with CSRF interceptor & feature services
│   ├── src/styles/           # Application styling (app.css)
│   └── vite.config.js        # Vite dev server proxy configuration
├── docs/                     # Specifications, runbook, MySQL setup script, stack mapping
├── .gitignore                # Production git ignore rules
└── README.md                 # Complete project documentation
```

---

## License

This project was developed for the AcxiomCRM technical evaluation.
