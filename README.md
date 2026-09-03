# Phongora AW Backend (Modular Monolith)

Backend system for Phongora AW, architected as a **Modular Monolith** with an independent **API Gateway** reverse proxy.

---

## 1. Architectural Overview

The backend was refactored from distributed microservices to an in-process **Modular Monolith**:
- **Unified Runtime (`app`)**: A single Spring Boot JVM process hosting all domain modules (`auth`, `hr`, `workflow`, `document`, `finance`, `notification`) with shared security, unified Swagger UI, and embedded Camunda 7 BPMN engine.
- **In-Memory Synchronous Calls**: Modules communicate via direct Java service interfaces (`Auth` ➔ `hr.EmployeeService`, `HR` ➔ `workflow.WorkflowService`), eliminating HTTP/network latency and serialization overhead.
- **Asynchronous Event-Driven Flows**: Kafka broker handles asynchronous event streaming (e.g., workflow process initiation, task completions, and PDF report triggers).
- **Consolidated Database (`aw_db`)**: A single PostgreSQL database housing all business entities and Camunda process state.
- **Independent API Gateway (`gateway`)**: Spring Cloud Gateway running on port `8080` acting as a single entry point / reverse proxy in front of the monolith.

### System Architecture

```
                    [ Client / Frontend (Port 3000) ]
                                   │
                           (HTTP - Port 8080)
                                   ▼
                       [ gateway (Reverse Proxy) ]
                                   │
                           (HTTP - Port 8081)
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     app (Modular Monolith Runner)                      │
│                                                                        │
│   ┌──────────────┐    In-Memory Java API    ┌──────────────────────┐   │
│   │     auth     │ ───────────────────────► │          hr          │   │
│   └──────────────┘                          └──────────┬───────────┘   │
│                                                        │               │
│                                          In-Memory API │               │
│                                                        ▼               │
│   ┌──────────────┐                          ┌──────────────────────┐   │
│   │   document   │                          │       workflow       │   │
│   └──────┬───────┘                          │  (Embedded Camunda)  │   │
│          ▲                                  └──────────┬───────────┘   │
│          └───────────────── Kafka ─────────────────────┘               │
│                         (Async Events)                                 │
│                                                                        │
│   ┌──────────────┐    ┌──────────────────┐  ┌──────────────────────┐   │
│   │   finance    │    │   notification   │  │        common        │   │
│   └──────────────┘    └──────────────────┘  └──────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
                   [ PostgreSQL: aw_db (Unified DB) ]
```

---

## 2. Project Modules

| Module | Type | Description |
| :--- | :--- | :--- |
| **`common`** | Library | Shared security utilities, JWT filter, base DTOs, events, exceptions, response wrappers. |
| **`workflow`** | Library | Camunda 7 engine integration, BPMN definitions, process delegates, and workflow services. |
| **`hr`** | Library | Human Resource domain: employees, org units, job titles, headcount planning. |
| **`auth`** | Library | Authentication & authorization, user/role management, token issuance. |
| **`document`** | Library | JasperReports document compilation and PDF export services. |
| **`finance`** | Library | Finance domain services (skeleton). |
| **`notification`** | Library | Notification services (skeleton). |
| **`gateway`** | Application | Spring Cloud Gateway (port `8080`) reverse proxy routing requests to the monolith. |
| **`app`** | Application | Executable Spring Boot Modular Monolith launcher (port `8081`). |

---

## 3. Prerequisites

- **Java**: JDK 17 or JDK 21
- **Build Tool**: Apache Maven 3.9+
- **Container Runtime**: Docker and Docker Compose

---

## 4. Build Instructions

Maven automatically compiles and packages modules according to the dependency hierarchy defined in root `pom.xml`:

```
1. phongora-aw-backend  (Root POM)
2. common               (Shared library)
3. workflow             (Workflow domain)
4. hr                   (HR domain)
5. auth                 (Auth domain)
6. document             (Document domain)
7. finance              (Finance domain)
8. notification         (Notification domain)
9. gateway              (Gateway executable)
10. app                 (Monolith executable)
```

Run from the root directory:

```bash
mvn clean package -DskipTests
```

This generates two runnable JARs:
- `app/target/app-1.0-SNAPSHOT.jar` (Modular Monolith)
- `gateway/target/gateway-1.0-SNAPSHOT.jar` (API Gateway)

---

## 5. Step-by-Step Run Order

Follow this exact order to start the system:

### Step 1: Start Infrastructure (Docker Compose)

Start PostgreSQL, Redis, Kafka, and Kafka UI:

```bash
docker compose up -d
```

| Container | Host Port | Internal Port | Description |
| :--- | :--- | :--- | :--- |
| **aw-postgres** | `5432` | `5432` | PostgreSQL (Initializes `aw_db` via `init.sql`) |
| **aw-redis** | `6379` | `6379` | Redis cache & token blocklist |
| **aw-kafka** | `9094` | `9092` | Apache Kafka broker (KRaft mode) |
| **aw-kafka-ui** | `8086` | `8080` | Kafka UI dashboard |

### Step 2: Start the Modular Monolith (`app`)

The monolith boots all domains, auto-creates Camunda tables in `aw_db`, runs `DatabaseSeeder`, and listens on port **`8081`**.

- **Option A - Maven:**
  ```bash
  mvn spring-boot:run -pl app
  ```
- **Option B - Executable JAR:**
  ```bash
  java -jar app/target/app-1.0-SNAPSHOT.jar
  ```
- **Option C - IDE:**
  Run the `main` method in `com.aw.app.AwMonolithApplication`.

### Step 3: Start the API Gateway (`gateway`)

Once the Monolith is running on port `8081`, launch the Spring Cloud Gateway on port **`8080`**.

- **Option A - Maven:**
  ```bash
  mvn spring-boot:run -pl gateway
  ```
- **Option B - Executable JAR:**
  ```bash
  java -jar gateway/target/gateway-1.0-SNAPSHOT.jar
  ```
- **Option C - IDE:**
  Run the `main` method in `com.aw.gateway.GatewayApplication`.

---

## 6. API Endpoints & Access URLs

Requests can be made through the Gateway (`http://localhost:8080`) or directly to the Monolith (`http://localhost:8081`):

| Service / Function | Gateway Entry (Port 8080) | Direct Monolith Entry (Port 8081) |
| :--- | :--- | :--- |
| **Auth: Login** | `POST /api/v1/auth/login` | `POST /api/v1/login` |
| **Auth: Register** | `POST /api/v1/auth/register` | `POST /api/v1/register` |
| **Auth: Refresh Token** | `POST /api/v1/auth/refresh-token` | `POST /api/v1/refresh-token` |
| **Auth: Profile** | `GET /api/v1/auth/profile` | `GET /api/v1/profile` |
| **HR: Seed Employee** | `POST /api/v1/hr/employees/seed` | `POST /api/v1/employees/seed` |
| **HR: Get Employee** | `GET /api/v1/hr/employees/{id}` | `GET /api/v1/employees/{id}` |
| **HR: Headcount Plans** | `GET /api/v1/hr/headcount-plans/{id}` | `GET /api/v1/headcount-plans/{id}` |
| **Workflow: Status** | `GET /api/v1/process/status/{id}` | `GET /api/v1/workflow/status/{id}` |
| **Workflow: Perform** | `POST /api/v1/process/perform/{id}` | `POST /api/v1/workflow/perform/{id}` |
| **Camunda Webapp** | `GET /camunda` | `GET /camunda` |
| **Camunda REST API** | `GET /engine-rest/**` | `GET /engine-rest/**` |
| **Swagger UI Docs** | `http://localhost:8080/swagger-ui.html` | `http://localhost:8081/swagger-ui.html` |
| **OpenAPI Specification**| `http://localhost:8080/api/v1/monolith/api-docs` | `http://localhost:8081/api-docs` |
| **Kafka UI Dashboard** | — | `http://localhost:8086` |

---

## 7. Default Credentials

- **Monolith System Admin**:
  - Username: `sys_admin`
  - Password: `123456`
- **Camunda Webapp Admin**:
  - Username: `admin`
  - Password: `admin`
- **PostgreSQL Database**:
  - Database: `aw_db`
  - Username: `awuser`
  - Password: `phongora1T@Dmin`