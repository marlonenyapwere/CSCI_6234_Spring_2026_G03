<div align="center">

<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
<img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
<img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" />

<br />
<br />

# 🧩 Extensible Workflow Orchestration Engine

**A production-grade, extensible workflow engine built with Java & Spring Boot.**  
Define complex workflows in JSON. Execute them with state, logic, and real-world integrations.

<br />

[![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)](.)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](.)
[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)](.)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square)](.)

</div>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-high-level-architecture)
- [Design Principles](#-core-design-principles)
- [Step Types](#-supported-step-types)
- [Demo Workflows](#-demo-workflows)
- [API Reference](#-api-reference)
- [Database Schema](#-database-schema)
- [Setup & Configuration](#-setup--configuration)
- [Challenges Solved](#-challenges-solved)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 🚀 Overview

Modern business processes are not linear — they involve decisions, human approvals, retries, loops, and external integrations. Traditional hardcoded flows can't keep up.

This project provides a **generic, data-driven workflow engine** where:

- 📄 Workflows are defined as **JSON** — no code changes needed
- ⚙️ A **runtime engine** handles all execution logic
- 💾 State is **persisted to PostgreSQL** and fully resumable
- 🔌 Real integrations ship out of the box: **HTTP calls** and **Email (SMTP/Gmail)**

> 💡 Inspired by [Netflix Conductor](https://github.com/Netflix/conductor) and [Camunda BPM](https://camunda.com/), this project distills core orchestration concepts into a clean, teachable system.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📝 **JSON Workflow Definitions** | Fully data-driven — define any workflow without writing code |
| 💾 **Stateful Persistence** | Execution state saved to PostgreSQL; survives restarts |
| ⏸️ **Pause & Resume** | `USER_TASK` steps halt execution until a human responds |
| 🔀 **Conditional Branching** | True/false paths based on runtime decisions |
| 🔁 **Repeat / Loop** | Execute a step N times with a single definition |
| 🌐 **HTTP Integration** | Call external REST APIs as a workflow step |
| 📧 **Email Integration** | Send emails via SMTP / Gmail mid-workflow |
| 🔌 **Extensible Registry** | Add new step types without touching the engine core |
| 📘 **Swagger Docs** | Auto-generated REST API documentation |

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────┐
│         Client (React / Postman)    │
└──────────────────┬──────────────────┘
                   │ HTTP
┌──────────────────▼──────────────────┐
│         REST API (Controller)       │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│       Service Layer (WorkflowService)│
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│           Workflow Engine           │
│  ┌─────────────────────────────┐    │
│  │      WorkflowExecutor       │    │
│  ├─────────────────────────────┤    │
│  │      StepTypeRegistry       │    │
│  ├─────────────────────────────┤    │
│  │  Step Handlers              │    │
│  │  TASK · USER_TASK           │    │
│  │  HTTP  · EMAIL              │    │
│  ├─────────────────────────────┤    │
│  │  Execution Models           │    │
│  │  Sequential · Conditional   │    │
│  │  Repeat                     │    │
│  ├─────────────────────────────┤    │
│  │      State Management       │    │
│  └─────────────────────────────┘    │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│     Persistence Layer (JPA/Hibernate)│
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│              PostgreSQL             │
└─────────────────────────────────────┘
```

---

## 🧠 Core Design Principles

### 1 · Separation of Concerns

| Component | Responsibility |
|---|---|
| `WorkflowDefinition` | Represents the workflow as parsed JSON |
| `WorkflowExecutor` | Drives step-by-step execution |
| `StepTypeRegistry` | Maps step type strings → handler implementations |
| `WorkflowInstance` | Tracks all runtime state for a running workflow |
| `Persistence Layer` | Stores definitions, instances, and step results |

### 2 · Stateful Execution

Each `WorkflowInstance` tracks:

```
currentStepId   →  Where execution is right now
waitingStepId   →  Which step is paused for human input
status          →  RUNNING | WAITING | COMPLETED | FAILED
data_json       →  Arbitrary runtime data passed between steps
```

This enables **resume-after-pause**, **human-in-the-loop**, and **fault tolerance**.

### 3 · Execution Loop

```
Execute Step → Persist State → Determine Next Step → Repeat
```

### 4 · Step Lifecycle

Every step exits with exactly one of these statuses:

```
✅ COMPLETED   →  Step finished successfully
❌ FAILED      →  Step threw an error
⏸️ WAITING     →  Step paused, awaiting external input
⏭️ SKIPPED     →  Step bypassed by a conditional branch
```

---

## 🔀 Supported Step Types

### `TASK` — Basic Execution Unit

```json
{
  "id": "task1",
  "type": "TASK",
  "message": "Processing payment...",
  "nextStepId": "nextStep"
}
```

### `USER_TASK` — Human-in-the-Loop

```json
{
  "id": "approval",
  "type": "USER_TASK",
  "message": "Please approve or reject this request.",
  "nextStepId": "decision"
}
```

> ⏸️ Execution **pauses** here until a `PUT /resume` request is received.

### `CONDITIONAL` — Branching Logic

```json
{
  "id": "decision",
  "type": "CONDITIONAL",
  "onSuccess": {
    "id": "approvedTask",
    "type": "TASK",
    "message": "Request approved."
  },
  "onFailure": {
    "id": "rejectedTask",
    "type": "TASK",
    "message": "Request rejected."
  }
}
```

### `REPEAT` — Loop Construct

```json
{
  "id": "repeatBlock",
  "type": "REPEAT",
  "times": 3,
  "step": {
    "id": "loopTask",
    "type": "TASK",
    "message": "Running iteration..."
  },
  "nextStepId": "afterLoop"
}
```

### `HTTP` — External API Call

```json
{
  "id": "httpCall",
  "type": "HTTP",
  "method": "GET",
  "url": "https://jsonplaceholder.typicode.com/posts/1",
  "nextStepId": "processResult"
}
```

### `EMAIL` — Send Notification

```json
{
  "id": "sendEmail",
  "type": "EMAIL",
  "from": "your@gmail.com",
  "to": "recipient@gmail.com",
  "subject": "Workflow Update",
  "body": "Your workflow step has completed.",
  "nextStepId": "nextStep"
}
```

---

## 🧪 Demo Workflows

<details>
<summary><strong>1. Simple Sequential Workflow</strong></summary>

```json
{
  "id": "simpleFlow",
  "name": "Simple Sequential Flow",
  "root": {
    "id": "step1",
    "type": "TASK",
    "message": "Step 1",
    "nextStepId": "step2"
  },
  "steps": [
    {
      "id": "step2",
      "type": "TASK",
      "message": "Step 2",
      "nextStepId": "end"
    },
    {
      "id": "end",
      "type": "TASK",
      "message": "Done"
    }
  ]
}
```

</details>

<details>
<summary><strong>2. Conditional Workflow (User Driven)</strong></summary>

```json
{
  "id": "conditionalFlow",
  "name": "Conditional Flow",
  "root": {
    "id": "userInput",
    "type": "USER_TASK",
    "message": "Approve?",
    "nextStepId": "decision"
  },
  "steps": [
    {
      "id": "decision",
      "type": "CONDITIONAL",
      "onSuccess": {
        "id": "approved",
        "type": "TASK",
        "message": "Approved"
      },
      "onFailure": {
        "id": "rejected",
        "type": "TASK",
        "message": "Rejected"
      }
    }
  ]
}
```

</details>

<details>
<summary><strong>3. Repeat Workflow</strong></summary>

```json
{
  "id": "repeatFlow",
  "name": "Repeat Flow",
  "root": {
    "id": "repeatBlock",
    "type": "REPEAT",
    "times": 3,
    "step": {
      "id": "loopTask",
      "type": "TASK",
      "message": "Running iteration"
    },
    "nextStepId": "end"
  },
  "steps": [
    {
      "id": "end",
      "type": "TASK",
      "message": "Done"
    }
  ]
}
```

</details>

<details>
<summary><strong>4. HTTP Workflow</strong></summary>

```json
{
  "id": "httpFlow",
  "name": "HTTP Flow",
  "root": {
    "id": "callApi",
    "type": "HTTP",
    "method": "GET",
    "url": "https://jsonplaceholder.typicode.com/posts/1",
    "nextStepId": "end"
  },
  "steps": [
    {
      "id": "end",
      "type": "TASK",
      "message": "API call complete"
    }
  ]
}
```

</details>

<details>
<summary><strong>5. Email Workflow</strong></summary>

```json
{
  "id": "emailFlow",
  "name": "Email Flow",
  "root": {
    "id": "sendEmail",
    "type": "EMAIL",
    "from": "your@gmail.com",
    "to": "your@gmail.com",
    "subject": "Workflow Test",
    "body": "Email sent successfully",
    "nextStepId": "end"
  },
  "steps": [
    {
      "id": "end",
      "type": "TASK",
      "message": "Done"
    }
  ]
}
```

</details>

<details>
<summary><strong>6. 🌟 Full Demo Flow (All Features Combined)</strong></summary>

```json
{
  "id": "classDemoFlow",
  "name": "Full Demo Flow",
  "root": {
    "id": "start",
    "type": "TASK",
    "message": "Start workflow",
    "nextStepId": "review"
  },
  "steps": [
    {
      "id": "review",
      "type": "USER_TASK",
      "message": "Approve or reject?",
      "nextStepId": "decision"
    },
    {
      "id": "decision",
      "type": "CONDITIONAL",
      "onSuccess": {
        "id": "notifyApproved",
        "type": "EMAIL",
        "to": "your@gmail.com",
        "subject": "Approved",
        "body": "Application approved",
        "nextStepId": "repeatBlock"
      },
      "onFailure": {
        "id": "notifyRejected",
        "type": "EMAIL",
        "to": "your@gmail.com",
        "subject": "Rejected",
        "body": "Application rejected",
        "nextStepId": "repeatBlock"
      }
    },
    {
      "id": "repeatBlock",
      "type": "REPEAT",
      "times": 2,
      "step": {
        "id": "auditTask",
        "type": "TASK",
        "message": "Writing audit log"
      },
      "nextStepId": "end"
    },
    {
      "id": "end",
      "type": "TASK",
      "message": "Workflow completed"
    }
  ]
}
```

</details>

---

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/workflow/{definitionId}` | Start a new workflow instance |
| `GET` | `/api/workflow/{workflowId}` | Get the current state of a workflow |
| `PUT` | `/api/workflow/{workflowId}/resume` | Resume a paused `USER_TASK` workflow |

### Resume a Paused Workflow

```http
PUT /api/workflow/{workflowId}/resume
Content-Type: application/json

{
  "approved": true
}
```

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html#/
```

---

## 🗄️ Database Schema

### `workflow_definition`

| Column | Type | Description |
|---|---|---|
| `id` | `VARCHAR` | Unique workflow definition ID |
| `name` | `VARCHAR` | Human-readable workflow name |
| `version` | `INT` | Schema version |
| `definition_json` | `TEXT` | Full JSON workflow definition |

### `workflow_instance`

| Column | Type | Description |
|---|---|---|
| `id` | `UUID` | Unique instance ID |
| `definition_id` | `VARCHAR` | Reference to the workflow definition |
| `current_step_id` | `VARCHAR` | Currently executing step |
| `waiting_step_id` | `VARCHAR` | Step awaiting human input |
| `status` | `ENUM` | `RUNNING` · `WAITING` · `COMPLETED` · `FAILED` |
| `data_json` | `TEXT` | Runtime data payload |
| `start_time` | `TIMESTAMP` | Execution start timestamp |
| `end_time` | `TIMESTAMP` | Execution end timestamp |

### `step_execution`

| Column | Type | Description |
|---|---|---|
| `step_id` | `VARCHAR` | Reference to the step |
| `status` | `ENUM` | Step result status |
| `timestamps` | `TIMESTAMP` | Start / end time of this step |

---

## ⚙️ Setup & Configuration

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Run the Application

```bash
mvn spring-boot:run
```

### PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/workflows
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

### Gmail / SMTP

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> 🔐 Use a [Gmail App Password](https://support.google.com/accounts/answer/185833), not your regular account password.

---

## ⚠️ Challenges Solved

| Challenge | Solution |
|---|---|
| Infinite loops in `REPEAT` | Iteration counter with bounded execution guard |
| Conditional branching correctness | Separate `onSuccess` / `onFailure` subgraph resolution |
| `WAITING` vs `COMPLETED` confusion | Explicit step lifecycle enum with strict transitions |
| Step extensibility | Registry pattern — new handlers plug in without engine changes |
| Runtime vs definition separation | `WorkflowDefinition` is immutable; `WorkflowInstance` holds all mutable state |

---

## 🔮 Roadmap

- [ ] Expression engine for dynamic conditions (`amount > 5000`)
- [ ] Parallel step execution
- [ ] Configurable retry policies per step
- [ ] Distributed worker nodes
- [ ] Event-driven workflows via Kafka
- [ ] Drag-and-drop workflow builder UI

---

## 🎯 Key Takeaway

> This project demonstrates how complex, real-world business processes can be abstracted into a **stateful, extensible execution engine** — enabling reliable, scalable orchestration without hardcoded logic.

---

## 👤 Author

**Marlone Nyapwere**  
*MS Computer Science — George Washington University*

---

## ⭐ Inspiration

- [Netflix Conductor](https://github.com/Netflix/conductor)
- [Camunda BPM](https://camunda.com/)
- Enterprise workflow systems (Temporal, Airflow, AWS Step Functions)

---

<div align="center">

*If you found this project useful, consider giving it a ⭐ on GitHub!*

</div>