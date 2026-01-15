# 1. SYSTEM ARCHITECTURE OVERVIEW

## 1.1 High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                              │
├──────────────────────────────┬──────────────────────────────────────┤
│   BORROWER PORTAL            │        BANK ADMIN PORTAL             │
│  (React/Next.js SPA)         │      (React/Next.js SPA)             │
│  - Registration/Login        │  - Bank Account Setup                │
│  - Application Form          │  - Application Queue                 │
│  - Offer Comparison          │  - Offer Submission                  │
│  - Scenario Calculator       │  - Configuration Console             │
└──────────────────────────────┴──────────────────────────────────────┘
                               │
                        (HTTPS/REST + WebSocket)
                               │
┌─────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY LAYER                                │
│              (Spring Cloud Gateway / Kong)                          │
│  - Request routing & rate limiting                                  │
│  - Authentication/Authorization (JWT)                              │
│  - Audit logging                                                    │
└─────────────────────────────────────────────────────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌────────────────┐  ┌──────────────────┐  ┌────────────────┐
│  USER SERVICE  │  │APPLICATION SERVICE│  │ OFFER SERVICE  │
│  - Auth        │  │ - Submission      │  │ - Calculation  │
│  - Profiles    │  │ - Status tracking │  │ - Comparison   │
│  - KYC         │  │ - History         │  │ - Bank mgmt    │
└────────────────┘  └──────────────────┘  └────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────────────┐
│                   SHARED SERVICES LAYER                             │
├──────────────────────────────┬──────────────────────────────────────┤
│  - Notification Service      │  - Audit & Compliance Service       │
│  - Rate Card Engine          │  - Consent Management Service       │
│  - Exchange Rate Service     │  - E-Signature Service (Phase 2)    │
└──────────────────────────────┴──────────────────────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌────────────────┐  ┌──────────────────┐  ┌────────────────┐
│ POSTGRESQL DB  │  │  REDIS CACHE     │  │   FILE STORE   │
│  - User Data   │  │  - Sessions      │  │  - Audit Logs  │
│  - Apps/Offers │  │  - Rate Cards    │  │  - Documents   │
│  - Audit Logs  │  │  - Notifications │  │  - Consents    │
└────────────────┘  └──────────────────┘  └────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────────────┐
│                    EXTERNAL INTEGRATIONS                            │
├──────────────────────────────┬──────────────────────────────────────┤
│  - Email Service (SendGrid)  │  - E-Signature (DocuSign/Local)    │
│  - SMS Service (Nexmo)       │  - Bank APIs (Phase 2)             │
│  - Analytics (Segment)       │  - Exchange Rate APIs               │
└──────────────────────────────┴──────────────────────────────────────┘
```

## 1.2 Service-to-Service Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                      BORROWER PORTAL                                │
│                    (React/Next.js Client)                           │
└──────────────────────┬──────────────────────────────────────────────┘
                       │
                       ├─────── JWT Token ─────┐
                       │                        │
                       ▼                        ▼
        ┌────────────────────────┐   ┌─────────────────────┐
        │   API GATEWAY          │   │  Rate Limiter       │
        │ - Request Validation   │   │  (Redis)            │
        │ - JWT Verification     │   │                     │
        │ - Route Dispatch       │   │  Max: 100 req/sec   │
        └────┬─────┬──────┬─────┘   └─────────────────────┘
             │     │      │
      ┌──────▼──┐ ┌┴──────▼──┐ ┌─────────────┐
      │  USER   │ │  APP     │ │  OFFER      │
      │ SERVICE │ │ SERVICE  │ │ SERVICE     │
      │         │ │          │ │             │
      │ ┌─────┐ │ │ ┌──────┐ │ │ ┌────────┐ │
      │ │Auth │ │ │ │Submit │ │ │ │Calcula-│ │
      │ │/Reg │ │ │ │Status │ │ │ │tion    │ │
      │ │KYC  │ │ │ │History│ │ │ │Mgmt    │ │
      │ └──┬──┘ │ │ └──┬───┘ │ │ └───┬────┘ │
      └────┼────┘ └────┼────┘ └─────┼──────┘
           │           │            │
           └───────┬───┴────┬───────┘
                   │        │
         ┌─────────▼──┐  ┌──▼──────────┐
         │NOTIFICATION│  │  AUDIT &    │
         │ SERVICE    │  │ COMPLIANCE  │
         │            │  │             │
         │- Email     │  │- Logs       │
         │- SMS       │  │- Consent    │
         │- Queue     │  │- Immutable  │
         └─────┬──────┘  └──┬──────────┘
               │            │
         ┌─────▼────────────▼──┐
         │  SHARED DATA LAYER  │
         │                     │
         │ ┌─────────────────┐ │
         │ │   PostgreSQL    │ │
         │ │   (Primary DB)  │ │
         │ └─────────────────┘ │
         │                     │
         │ ┌─────────────────┐ │
         │ │   Redis Cache   │ │
         │ │ (Sessions, Rate │ │
         │ │  Cards, Tokens) │ │
         │ └─────────────────┘ │
         │                     │
         │ ┌─────────────────┐ │
         │ │   S3 Storage    │ │
         │ │ (Audit Logs,    │ │
         │ │  Documents)     │ │
         │ └─────────────────┘ │
         └─────────────────────┘
```

## 1.3 Data Flow: Loan Application Lifecycle

```
BORROWER                API GATEWAY           APPLICATION SERVICE        OFFER SERVICE         NOTIFICATION SERVICE
   │                         │                        │                       │                        │
   │  1. Register/Login       │                        │                       │                        │
   ├────────────────────────>│                        │                       │                        │
   │  (POST /users/login)     │                        │                       │                        │
   │                         │  Verify JWT            │                       │                        │
   │                         │──────────────────┐     │                       │                        │
   │                         │                  │     │                       │                        │
   │<────── JWT Token ───────│<─────────────────┘     │                       │                        │
   │                         │                        │                       │                        │
   │  2. Submit Application   │                        │                       │                        │
   ├──────────────────────────┤  Store Application     │                       │                        │
   │  (POST /applications)    ├──────────────────────>│                       │                        │
   │  {amount, term, type}    │                        │                       │                        │
   │                         │                    Create Loan_Application     │                        │
   │                         │                    in PostgreSQL              │                        │
   │                         │<─────────────────────┤                       │                        │
   │<────────────────────────│ {appId, status}       │                       │                        │
   │                         │                        │                       │                        │
   │  3. Trigger Offer Calc   │                        │  Load RateCard       │                        │
   │                         │                        │  from Redis────────────┤                        │
   │                         │                        │                        │                        │
   │                         │                        │  Calculate Offers     │                        │
   │                         │                        │  (< 500ms)────────────┤                        │
   │                         │                        │                        │                        │
   │                         │                        │  Create preliminary   │                        │
   │                         │                        │  offers──────────────>│ Queue Notifications   │
   │                         │                        │                        ├──────────────────────>│
   │                         │                        │ Update status to      │                        │
   │                         │                        │ OFFERS_RECEIVED       │ Send Email via         │
   │                         │                        │<─────────────────────│ SendGrid               │
   │                         │<─────────────────────────┤                       │                        │
   │<────────────────────────│ {offers[]}             │                       │                        │
   │                         │                        │                       │                        │
   │  4. View Offers          │                        │                       │                        │
   ├──────────────────────────┤  Fetch Offers          │                       │                        │
   │  (GET /offers/{appId})   ├──────────────────────────────────────────────>│                        │
   │                         │                        │                       │                        │
   │                         │                        │                       │ Return Offers         │
   │                         │<──────────────────────────────────────────────┤                        │
   │<────────────────────────│ {8 metrics per offer}  │                       │                        │
   │                         │                        │                       │                        │
   │  5. Select Offer         │                        │  Record Selection     │                        │
   ├──────────────────────────┤  Update Status        │  Send to Banks        │                        │
   │  (POST /offers/{id}/sel) ├──────────────────────>│  Log to Audit──────────────────────────────>│
   │                         │                        │                       │                        │
   │<────────────────────────│ {success}              │                       │                        │
   │                         │                        │                       │                        │
```

## 1.4 Deployment Architecture (Kubernetes)

```
┌─────────────────────────────────────────────────────────────────────┐
│                   AWS / Cloud Provider                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                 EKS Kubernetes Cluster                     │   │
│  │  (3+ Availability Zones: AZ-1, AZ-2, AZ-3)               │   │
│  │                                                            │   │
│  │  ┌──────────────────────────────────────────────────┐    │   │
│  │  │        Load Balancer (ALB)                       │    │   │
│  │  │  - SSL/TLS Termination                           │    │   │
│  │  │  - Route to Kubernetes Services                 │    │   │
│  │  │  - Auto-scaling Rules                            │    │   │
│  │  └──────────┬───────────────────────────────────────┘    │   │
│  │             │                                             │   │
│  │  ┌──────────┴──────────────────────────────────────┐    │   │
│  │  │      Kubernetes Namespaces                      │    │   │
│  │  │  ┌─────────────────────────────────────────┐   │    │   │
│  │  │  │  Namespace: production                  │   │    │   │
│  │  │  │                                          │   │    │   │
│  │  │  │  API Gateway Pods (2 replicas)          │   │    │   │
│  │  │  │  ├─ api-gateway-0 (AZ-1)               │   │    │   │
│  │  │  │  └─ api-gateway-1 (AZ-2)               │   │    │   │
│  │  │  │                                          │   │    │   │
│  │  │  │  User Service Pods (3 replicas)        │   │    │   │
│  │  │  │  ├─ user-service-0 (AZ-1)             │   │    │   │
│  │  │  │  ├─ user-service-1 (AZ-2)             │   │    │   │
│  │  │  │  └─ user-service-2 (AZ-3)             │   │    │   │
│  │  │  │                                          │   │    │   │
│  │  │  │  Application Service Pods (3 replicas) │   │    │   │
│  │  │  │  ├─ app-service-0 (AZ-1)             │   │    │   │
│  │  │  │  ├─ app-service-1 (AZ-2)             │   │    │   │
│  │  │  │  └─ app-service-2 (AZ-3)             │   │    │   │
│  │  │  │                                          │   │    │   │
│  │  │  │  Offer Service Pods (3 replicas)      │   │    │   │
│  │  │  │  ├─ offer-service-0 (AZ-1)           │   │    │   │
│  │  │  │  ├─ offer-service-1 (AZ-2)           │   │    │   │
│  │  │  │  └─ offer-service-2 (AZ-3)           │   │    │   │
│  │  │  │                                          │   │    │   │
│  │  │  │  Notification Service (2 replicas)    │   │    │   │
│  │  │  │  Compliance Service (2 replicas)      │   │    │   │
│  │  │  └─────────────────────────────────────────┘   │    │   │
│  │  └─────────────────────────────────────────────────┘    │   │
│  │                                                            │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │    RDS           │  │   ElastiCache    │  │  S3 Buckets     │  │
│  │  PostgreSQL      │  │   Redis (Cluster)│  │  (Audit Logs)   │  │
│  │  (Multi-AZ)      │  │                  │  │  (Versioning)   │  │
│  │  - Primary: AZ-1 │  │  - Shards x 3    │  │  (Encryption)   │  │
│  │  - Standby: AZ-2 │  │  - Replicas x 2  │  │  (Lifecycle)    │  │
│  │  - Read Replica: │  │  - Auto failover │  │                 │  │
│  │    AZ-3 (Phase 2)│  │                  │  │                 │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 1.5 Application Status State Machine

```
                    ┌─────────┐
                    │  DRAFT  │
                    └────┬────┘
                         │
                  (User submits + consent)
                         │
                         ▼
                  ┌──────────────┐
                  │  SUBMITTED   │
                  └────┬─────────┘
                       │
              (Bank views application)
                       │
                       ▼
                  ┌──────────────┐
                  │   VIEWED     │
                  └────┬─────────┘
                       │
        (Offer service calculates offers)
                       │
                       ▼
             ┌──────────────────────┐
             │ OFFERS_RECEIVED (✓)  │
             └─┬────────────────┬───┘
               │                │
        (Borrower selects)  (14-day expiry)
               │                │
               ▼                ▼
        ┌────────────┐     ┌─────────┐
        │OFFER_SELECT│     │ EXPIRED │
        │ED          │     └─────────┘
        └─┬──────────┘
          │
    (Completion or rejection)
          │
          ▼
     ┌──────────┐
     │COMPLETED │
     └──────────┘
```

## 1.2 Technology Stack Summary

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Frontend** | React 18 / Next.js 13 | Fast builds, SSR, SEO, component reusability |
| **API Gateway** | Spring Cloud Gateway | Centralized routing, rate limiting, Spring ecosystem |
| **Backend Services** | Spring Boot 3.x | Familiar, mature, excellent Spring Data integration |
| **Database** | PostgreSQL 15 | ACID compliance, JSON support, strong data integrity |
| **Cache** | Redis 7.x | Session management, rate card caching, real-time data |
| **Storage** | S3-compatible (MinIO or AWS S3) | Audit logs, document storage, compliance archive |
| **Message Queue** | RabbitMQ / Kafka | Async notifications, event streaming (Phase 2) |
| **Monitoring** | Prometheus + Grafana | Real-time metrics, alert management |
| **Logging** | ELK Stack | Centralized logs, audit trail queries |
| **Deployment** | Docker + Kubernetes | Scalability, multi-tenant isolation |
| **CI/CD** | GitHub Actions / Jenkins | Automated testing, deployment pipelines |

---
