# AcxiomCRM — How to run

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | **17** | Mandatory. See the warning below. |
| Node.js | 18+ | For the React frontend. |
| MySQL | 8.0 | Running on port 3306. |

Maven itself does not need to be installed — the project ships the Maven Wrapper (`mvnw`).

### JDK 17 is not optional

The build **requires JDK 17**. If `JAVA_HOME` is unset, Maven picks up whatever `java` is first on `PATH`. On a machine where that is JDK 21+ or 25, the build fails with a flood of errors such as:

```
cannot find symbol: method getName()
cannot find symbol: method setName()
```

Those are not real errors in the code. Lombok hooks into the compiler's internal AST to generate getters and setters, and it cannot attach to newer `javac` internals — so the generated methods never exist and every call to them looks undefined.

Set `JAVA_HOME` before building:

```bash
export JAVA_HOME="/c/Users/ASUS/jdk-temurin/jdk-17.0.20.1+1"   # Git Bash
```

```powershell
$env:JAVA_HOME = "C:\Users\ASUS\jdk-temurin\jdk-17.0.20.1+1"    # PowerShell
```

Confirm with `./mvnw -v` — it must report `Java version: 17`.

## Database

Run once, as a MySQL administrator:

```bash
mysql -u root -p < docs/mysql-setup.sql
```

That creates the `acxiomcrm` database and the `acxiom` application user. Edit the password inside the file first. The application reads `DB_NAME`, `DB_USER` and `DB_PASSWORD` from the environment — no credential is stored in source control.

## Backend

```bash
cd backend
export JAVA_HOME="/c/Users/ASUS/jdk-temurin/jdk-17.0.20.1+1"
export DB_NAME=acxiomcrm
export DB_USER=acxiom
export DB_PASSWORD='<your password>'
./mvnw spring-boot:run
```

Starts on <http://localhost:8080>. On first start Hibernate creates the schema and `DataSeeder` inserts three roles and three accounts:

| Email | Password | Role |
|---|---|---|
| `admin@acxiomcrm.local` | `Admin@123` | ADMIN |
| `manager@acxiomcrm.local` | `Manager@123` | MANAGER |
| `sales@acxiomcrm.local` | `Sales@123` | SALES_EXECUTIVE |

Change these before deploying anywhere real.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on <http://localhost:5173>. Open that URL — the app calls the backend on `localhost:8080` directly, and CORS is pinned to the dev-server origin in `acxiom.cors.allowed-origin`.

For a production bundle: `npm run build`, which writes `frontend/dist/`.

## Tests

```bash
cd backend
export JAVA_HOME="/c/Users/ASUS/jdk-temurin/jdk-17.0.20.1+1"
./mvnw test
```

31 tests. They run against an **in-memory H2 database** in MySQL-compatibility mode, so they need no MySQL server and can run anywhere. H2 is a test-scope dependency only; the application itself always runs on MySQL.

The suite deliberately does **not** disable CSRF. It fetches a real token from `GET /api/auth/csrf` and sends it back, so the security filter chain under test is the same one that runs in production.

## Configuration reference

All in `backend/src/main/resources/application.properties`.

| Property | Default | Purpose |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `acxiomcrm` | Database location. |
| `DB_USER` / `DB_PASSWORD` | `acxiom` / *(empty)* | Database credentials. Always from the environment. |
| `server.port` | `8080` | Backend port. |
| `acxiom.cors.allowed-origin` | `http://localhost:5173` | The only origin allowed to send credentialed requests. |
| `acxiom.security.password.min-length` | `8` | Password policy minimum (§6.2). |
| `acxiom.security.lockout.max-failed-attempts` | `5` | Failed logins before lockout (§6.3). |
| `acxiom.security.lockout.duration-minutes` | `15` | How long the lock lasts (§6.3). |

`spring.jpa.hibernate.ddl-auto=update` creates missing tables without dropping existing data. For a controlled deployment, switch it to `validate` and manage the schema with migration scripts.
