# Credit Aggregator MVP - Full-Stack Architecture

**Date:** 2026-01-13  
**Status:** Architecture Design - Ready for Implementation  
**Architect:** Winston  
**Version:** 1.0

---

## Executive Summary

This document defines a pragmatic, user-centric full-stack architecture for the Credit Aggregator MVP. The system is designed as a two-sided marketplace connecting borrowers with Moldovan banks through a modern, compliant platform optimized for speed, security, and regulatory clarity.

**Core Design Principles:**
1. **User-Centric**: Borrower simplicity (one app) + Bank efficiency (minimal manual work)
2. **Real-Time Calculations**: Preliminary offers in <500ms using configurable rate cards
3. **Regulatory-First**: Data protection, audit trails, and consent management built-in
4. **Scalable-by-Design**: Multi-tenant, microservices-ready backend with Spring Boot
5. **Operational Simplicity**: Monolith-first approach; services split as volume justifies

---

## 1. SYSTEM ARCHITECTURE OVERVIEW

### 1.1 High-Level System Architecture

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

### 1.2 Service-to-Service Interaction Diagram

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

### 1.3 Data Flow: Loan Application Lifecycle

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

### 1.4 Deployment Architecture (Kubernetes)

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

### 1.5 Application Status State Machine

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

### 1.2 Technology Stack Summary

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

## 2. DETAILED SERVICE ARCHITECTURE

### 2.1 User Service

**Responsibility:** Authentication, authorization, user profiles, KYC data management.

**Key Endpoints:**
```
POST   /api/users/register              # Register borrower or bank admin
POST   /api/users/login                 # JWT authentication
GET    /api/users/profile               # Get user profile
PUT    /api/users/profile               # Update profile
POST   /api/users/kyc                   # Submit KYC documents
GET    /api/users/kyc/status            # Get KYC verification status
POST   /api/auth/refresh                # Refresh JWT token
POST   /api/auth/logout                 # Logout & revoke token
```

**Database Schema (Excerpt):**
```sql
-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(20),
  role ENUM('BORROWER', 'BANK_ADMIN') NOT NULL,
  organization_id UUID,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Organizations (Banks) table
CREATE TABLE organizations (
  id UUID PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  tax_id VARCHAR(50) UNIQUE,
  country_code VARCHAR(2) DEFAULT 'MD',
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW()
);
```

**Security:**
- Passwords: bcrypt with salt factor 12
- Tokens: JWT (RS256 signature) with 15-minute expiration
- Refresh tokens stored in Redis with revocation capability

---

### 2.2 Application Service

**Responsibility:** Loan application submission, validation, status tracking, history management.

**Key Endpoints:**
```
POST   /api/applications                # Submit new loan application
GET    /api/applications/{id}           # Get application details
GET    /api/applications                # List borrower's applications
PUT    /api/applications/{id}           # Update application (draft state only)
GET    /api/applications/{id}/status    # Get application status
POST   /api/applications/{id}/consent   # Submit explicit consent
GET    /api/applications/{id}/history   # Get application status history
```

**Database Schema:**
```sql
-- Loan Applications
CREATE TABLE loan_applications (
  id UUID PRIMARY KEY,
  borrower_id UUID NOT NULL,
  loan_type VARCHAR(50) NOT NULL,
  loan_amount DECIMAL(15, 2) NOT NULL,
  loan_currency VARCHAR(3) DEFAULT 'MDL',
  loan_term_months INT NOT NULL,
  rate_type VARCHAR(20),
  annual_income DECIMAL(15, 2),
  employment_status VARCHAR(50),
  application_status VARCHAR(20) DEFAULT 'DRAFT',
  submitted_at TIMESTAMP,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (borrower_id) REFERENCES users(id)
);

-- Consent & Data Sharing Audit
CREATE TABLE consents (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  borrower_id UUID NOT NULL,
  consent_type VARCHAR(100) NOT NULL,
  is_granted BOOLEAN NOT NULL,
  granted_at TIMESTAMP,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (application_id) REFERENCES loan_applications(id),
  FOREIGN KEY (borrower_id) REFERENCES users(id)
);
```

---

### 2.3 Offer Service

**Responsibility:** Preliminary offer calculation, offer management, comparison logic.

**Key Endpoints:**
```
POST   /api/offers                      # Create preliminary offer (internal)
GET    /api/offers/application/{id}     # Get all offers for application
GET    /api/offers/{id}                 # Get single offer details
POST   /api/offers/{id}/select          # Borrower selects offer
POST   /api/bank/offers                 # Bank submits binding offer
GET    /api/bank/applications           # Bank views application queue
PUT    /api/bank/offers/{id}            # Bank updates offer
```

**Database Schema:**
```sql
-- Rate Cards (configurable by bank)
CREATE TABLE rate_cards (
  id UUID PRIMARY KEY,
  organization_id UUID NOT NULL,
  loan_type VARCHAR(50) NOT NULL,
  min_amount DECIMAL(15, 2),
  max_amount DECIMAL(15, 2),
  min_term_months INT,
  max_term_months INT,
  base_apr_fixed DECIMAL(5, 3),
  base_apr_variable DECIMAL(5, 3),
  origination_fee_percent DECIMAL(5, 3),
  insurance_premium_percent DECIMAL(5, 3),
  processing_time_days INT,
  effective_from DATE,
  effective_to DATE,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Preliminary Offers (auto-calculated)
CREATE TABLE preliminary_offers (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  organization_id UUID NOT NULL,
  loan_amount DECIMAL(15, 2) NOT NULL,
  loan_term_months INT NOT NULL,
  apr DECIMAL(5, 3) NOT NULL,
  monthly_payment DECIMAL(15, 2) NOT NULL,
  total_cost DECIMAL(15, 2) NOT NULL,
  origination_fee DECIMAL(15, 2),
  insurance_cost DECIMAL(15, 2),
  processing_time_days INT,
  rate_type VARCHAR(20),
  required_documents JSONB,
  validity_days INT DEFAULT 14,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (application_id) REFERENCES loan_applications(id),
  FOREIGN KEY (organization_id) REFERENCES organizations(id),
  INDEX (application_id, created_at)
);
```

**Offer Calculation Algorithm (Preliminary):**
```
Given: Application (amount, term, loan_type, rate_type)
1. Load active RateCard for organization + loan_type
2. Validate amount within [min, max] range
3. Validate term within [min_term, max_term]
4. apr = base_apr (fixed/variable)
5. origination_fee = loan_amount × origination_fee_percent
6. insurance = loan_amount × insurance_premium_percent
7. monthly_payment = PMT formula (rate = apr/12, nper = term_months, pv = loan_amount)
8. total_cost = (monthly_payment × term_months) + origination_fee + insurance - loan_amount
9. Set expires_at = now + 14 days
10. Create PreliminaryOffer with status GENERATED
11. Auto-transition application to OFFERS_RECEIVED
12. Trigger notification to borrower
```

**Performance Target:** <500ms calculation (achieved via pre-loaded rate cards in Redis)

---

## 3. FRONTEND ARCHITECTURE

### 3.1 Borrower Portal

**Tech Stack:** React 18 + Next.js 13 + Tailwind CSS

**Key Pages:**
1. **Registration/Login** — Email/password signup, login, forgot password
2. **Dashboard** — Quick overview, saved applications, recent offers
3. **Application Form** — Multi-step form (loan details, optional income, consent)
4. **Offer Comparison** — Table with 8 metrics, side-by-side comparison
5. **Application Details** — Full view, status history, required documents
6. **Profile** — User info, KYC status, saved preferences

**State Management:** Redux or Zustand for global state

**Form Handling:** React Hook Form with Zod validation

**Mobile Responsiveness:** Breakpoints at 320px (mobile), 768px (tablet), 1024px (desktop)

### 3.2 Bank Admin Portal

**Tech Stack:** React 18 + Next.js 13 + Tailwind CSS

**Key Pages:**
1. **Application Queue** — Filterable table of submitted applications
2. **Application Detail** — Full borrower data
3. **Offer Submission** — Form to input 8 required fields
4. **Rate Card Configuration** — CRUD for rate cards
5. **Dashboard** — Metrics (applications received, offers submitted)
6. **Audit Logs** — View organization's transaction history

---

## 4. SECURITY ARCHITECTURE

### 4.1 Authentication & Authorization

**Authentication Flow:**
```
1. User submits email + password
2. Backend validates (bcrypt verify)
3. Generate JWT with claims: sub, role, org_id, exp (15 min)
4. Return access_token + refresh_token (7 days, stored in Redis)
5. Client includes access_token in Authorization header
6. API Gateway validates JWT signature (RS256) + expiration
```

**Authorization (RBAC):**
- Borrowers: View own applications & offers only
- Bank Admins: View organization's applications & offers
- Compliance Officers: View audit logs (read-only)

### 4.2 Data Protection

**Encryption Strategy:**

| Data | Method | Key Management |
|------|--------|-----------------|
| **In Transit** | HTTPS (TLS 1.3) | Let's Encrypt certs |
| **At Rest - Sensitive Fields** | AES-256 (column-level) | AWS KMS or Vault |
| **Passwords** | bcrypt (factor 12) | N/A (one-way) |
| **Sessions** | JWT (RS256) | Rotating key pairs |
| **Audit Logs** | Append-only + AES-256 | Same as above |
| **File Storage** | Server-side encryption (S3) | KMS managed |

### 4.3 API Security

**Rate Limiting:**
- Per-borrower: 10 requests/second
- Per-bank: 100 requests/second
- Burst limit: 50 req/sec for 5 seconds

**Input Validation:**
- JSON Schema validation for all POST/PUT payloads
- SQL injection prevention via parameterized queries (JPA)
- XSS prevention: HTML escaping in responses

---

## 5. INFRASTRUCTURE & DEPLOYMENT

### 5.1 Cloud Architecture

**Deployment Model:** Kubernetes on cloud (AWS EKS, GCP GKE, or DigitalOcean)

**Multi-Region:** Primary in Moldovan data center or EU region

**Containerization:**
- Docker images per service (user-service, application-service, offer-service)
- Base: Eclipse Temurin (OpenJDK 21)
- Multi-stage builds to minimize size
- Health check endpoints for k8s readiness probes

### 5.2 CI/CD Pipeline

**Tool:** GitHub Actions

**Stages:**
```
Commit → Lint → Unit Tests → Build → Security Scan → 
Deploy to Staging → Smoke Tests → Deploy to Prod
```

**Key Jobs:**
1. **Code Quality** — SonarQube scan, code coverage >80%
2. **Security** — OWASP dependency check, container scanning (Trivy)
3. **Build** — Maven build, Docker image push to registry
4. **Integration Tests** — Testcontainers with PostgreSQL, Redis
5. **Staging Deploy** — Blue-green deployment
6. **Production Deploy** — Canary deployment (10% → 50% → 100%)

---

## 6. PERFORMANCE & SCALABILITY

### 6.1 Performance Targets

| Metric | Target | Approach |
|--------|--------|----------|
| **Offer Calculation** | <500ms | Pre-loaded rate cards in Redis |
| **Page Load Time** | <3 seconds (borrower), <2s (admin) | Next.js SSR, compression, CDN |
| **Time-to-Offer** | <30 min avg | Async offer calculation, real-time notifications |
| **Uptime** | 99.5% | Multi-AZ deployment, auto-failover, monitoring |
| **API Response** | <200ms (p95) | Database indexing, caching, async processing |

### 6.2 Caching Strategy

**Cache Layers:**
1. **Browser Cache** — Static assets (JS, CSS, images) with 1-year max-age
2. **CDN** — CloudFlare for global distribution
3. **Redis Session Cache** — User sessions, refresh tokens (24-hour TTL)
4. **Redis Data Cache** — Rate cards (TTL: 1 hour), exchange rates (TTL: 1 hour)
5. **Database Query Cache** — JPA second-level cache for read-heavy queries

### 6.3 Database Performance

**Indexing Strategy:**
```sql
-- High-query paths
CREATE INDEX ix_applications_borrower ON loan_applications(borrower_id, created_at DESC);
CREATE INDEX ix_applications_status ON loan_applications(application_status, created_at DESC);
CREATE INDEX ix_offers_application ON preliminary_offers(application_id, created_at DESC);
CREATE INDEX ix_offers_org_status ON bank_offers(organization_id, offer_status, created_at DESC);
CREATE INDEX ix_audit_entity ON audit_logs(entity_type, entity_id, created_at DESC);

-- Partial index for active records
CREATE INDEX ix_active_rate_cards ON rate_cards(organization_id) WHERE is_active = true;

-- JSONB index for semi-structured data
CREATE INDEX ix_required_docs ON preliminary_offers USING gin(required_documents);
```

---

## 7. MONITORING & OBSERVABILITY

### 7.1 Metrics (Prometheus)

**Key Metrics:**
- **Application Performance:** Request latency (p50, p95, p99), request count, error rate
- **Business Metrics:** Applications submitted, offers generated, time-to-offer distribution
- **Infrastructure:** CPU/memory/disk usage, database connection pool, Redis cache hit rate

**Alerts:**
- API error rate >5% → Page on-call
- Uptime <99% (rolling 1 hour) → Warning
- Offer calculation >600ms → Investigation
- Database connection pool >90% → Scale

### 7.2 Logging (ELK Stack)

**Log Collection:**
- All application logs to stdout/stderr
- Fluentd/Logstash ships to Elasticsearch
- Kibana for visualization

**Log Retention:**
- 30 days for debug/info logs
- 90 days for warn/error logs
- 7 years for audit logs (separate index)

---

## 8. COMPLIANCE & REGULATORY

### 8.1 Data Residency

**Requirement:** All personal data stored in Moldova or EU (NFR5)

**Implementation:**
- PostgreSQL primary DB deployed in Moldovan data center or EU-compliant cloud region
- S3 backup bucket in same region
- No cross-border data transfers without explicit consent

### 8.2 Audit & Compliance

**Key Features:**
- **Audit Trail:** Every transaction logged with timestamp, actor, action, before/after state
- **Immutability:** Database constraints prevent deletion; append-only design
- **Retention:** 7-year retention for loan documents; 3-year for transactional logs
- **Encryption:** Sensitive fields encrypted with AES-256

**Schema:**
```sql
CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  entity_type VARCHAR(100) NOT NULL,
  entity_id UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  actor_id UUID,
  actor_role VARCHAR(50),
  old_values JSONB,
  new_values JSONB,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX (entity_type, entity_id, created_at)
);
```

### 8.3 Consent Management

**Consent Types:**
1. **Data Collection Consent** — Personal data collection
2. **Bank Sharing Consent** — Share application with banks
3. **E-Signature Consent** — Electronic document signing (Phase 2)

**Implementation:**
- Pop-up modal at application submission with checkboxes
- Consent recorded with timestamp, IP, user agent
- Immutable audit log
- Verification: Borrower can request proof anytime

---

## 9. COST OPTIMIZATION

**Infrastructure Costs (Monthly Estimate for MVP):**

| Component | Usage | Cost |
|-----------|-------|------|
| **EKS Cluster** | 2 nodes (on-demand) | $150 |
| **RDS PostgreSQL** | db.t3.small (multi-AZ) | $250 |
| **ElastiCache Redis** | cache.t3.small (cluster) | $100 |
| **S3 Storage** | 50 GB | $10 |
| **Data Transfer** | 500 GB/month | $50 |
| **CloudFront CDN** | Static assets | $20 |
| **Other** | Route53, backup, monitoring | $70 |
| **TOTAL** | | **~$650/month** |

**Cost Optimization Levers:**
- Reserved Instances (Phase 2): Save 40% on compute
- S3 Lifecycle: Move audit logs to Glacier after 30 days
- Right-sizing: Monitor and adjust instance types quarterly
- Caching: Reduce database query count by 60%

---

## 10. DISASTER RECOVERY

### 10.1 Backup & Recovery

**Database Backups:**
- Automated daily snapshots to S3 (retain 30 days)
- Point-in-time recovery enabled (retain 7 days of WAL logs)
- Monthly full backup to cold storage (Glacier)

**Recovery Time Objective (RTO):** <1 hour  
**Recovery Point Objective (RPO):** <15 minutes

### 10.2 High Availability

**Multi-AZ Deployment:**
- Primary database in us-east-1a
- Standby in us-east-1b (synchronous replication)
- Automatic failover (RDS Failover) <2 minutes

**No Single Point of Failure:**
- API Gateway: 2+ replicas
- Each microservice: 3+ replicas
- Database: Multi-AZ active-passive
- Cache (Redis): Cluster mode with sharding

---

## 11. MIGRATION & ROLLOUT STRATEGY

### 11.1 Phased Rollout

**Phase 1 (Week 1-2): Staging Validation**
- Deploy full stack to staging cluster
- Smoke tests, security scans, load testing
- Compliance review with legal/NBM
- Bank integration testing (sandboxes)

**Phase 2 (Week 3): Soft Launch**
- Production deployment (single AZ initially)
- Internal team testing
- Canary deployment: 10% → 50% → 100%
- Monitor error rates, latency, database performance

**Phase 3 (Week 4): Public Launch**
- Multi-AZ production deployment
- Marketing campaign, pilot bank enrollment
- Real user traffic ramping
- Daily monitoring & support rotations

---

## 12. FUTURE ROADMAP (Phase 2+)

### Phase 2: Scale & Integration
- **Bank API Integrations:** Real-time offers from bank systems
- **E-Signature:** DocuSign / local provider integration
- **Advanced Risk Scoring:** Machine learning model to predict default risk
- **AML/KYC Integration:** Third-party screening services

### Phase 3: Expansion
- **Multi-Currency:** Full support for EUR, USD alongside MDL
- **Additional Products:** Credit cards, business loans, insurance
- **Borrower Marketplace:** Offers from non-bank lenders
- **Mobile Apps:** Native iOS/Android (Flutter or React Native)

### Phase 4: Monetization
- **Commission Model:** % of originated loan amount
- **Premium Features:** Fast-track processing, co-lending
- **Data Services:** Aggregated borrower insights (anonymized)
- **White-Label:** Other markets (Romania, Georgia)

---

## APPENDIX

### A. Team Structure

| Role | Responsibility | Count |
|------|---|---|
| **Product Manager** | Roadmap, requirements | 1 |
| **Backend Engineer** | Spring microservices | 2 |
| **Frontend Engineer** | React portals | 2 |
| **DevOps Engineer** | Infrastructure, CI/CD | 1 |
| **QA Engineer** | Testing, compliance validation | 1 |
| **Data/Security Officer** | Compliance, audits | 1 (contract) |

### B. Success Criteria

✅ **Technical:**
- All services deploy and scale to 1000 req/sec
- Database queries <200ms (p95)
- Offer calculation <500ms
- Zero data breaches in Year 1

✅ **Business:**
- 70% borrower application submission rate
- 95% preliminary offer generation success
- <30 min average time-to-offer
- 2-3 pilot banks onboarded and actively offering

✅ **Compliance:**
- 100% consent audit trail
- Zero regulatory violations
- NBM/CNPF written approval for Phase 2

---

## Document Version

**Version:** 1.0  
**Author:** Winston (Architect)  
**Created:** 2026-01-13  
**Last Updated:** 2026-01-13  

**Change Log:**
| Date | Version | Change | Author |
|------|---------|--------|--------|
| 2026-01-13 | 1.0 | Initial full-stack architecture | Winston |

---

**Next Steps:**
1. Share architecture with engineering team for review & feedback
2. Create detailed service specifications (API contracts, DB schemas)
3. Set up development environments locally (Docker Compose)
4. Begin Phase 1 implementation sprints
