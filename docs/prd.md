# Credit Aggregator MVP Product Requirements Document (PRD)

**Project Name:** Credit Aggregator MVP  
**Version:** 1.1  
**Date:** 2026-01-14  
**Status:** Approved for Story Creation  
**Prepared by:** John (PM) | **Validated by:** Sarah (PO)  

---

## Goals and Background Context

### Goals

1. Enable borrowers to submit a single loan application and receive comparable credit offers from multiple Moldovan banks in real-time
2. Demonstrate measurable value to pilot banks to justify participation and integration effort
3. Establish regulatory compliance foundation with NBM/CNPF clarity
4. Build two-sided marketplace infrastructure with dual account systems (borrower + bank)
5. Achieve 70%+ application submission rate from signup and 95%+ preliminary offer generation for system reliability validation

### Background Context

The Moldovan lending market currently lacks a centralized credit comparison platform. Borrowers face friction by visiting multiple banks or websites to compare loan terms, while banks struggle to acquire customers cost-effectively through traditional in-branch channels. This platform eliminates that friction by enabling one standardized application to generate preliminary offers from 2-3 Moldovan banks with transparent comparison metrics.

The MVP focuses on validating product-market fit with 2-3 pilot banks before scaling. Success hinges on three pillars: (1) seamless borrower experience that captures application volume, (2) operational efficiency for banks that justifies their participation, and (3) regulatory clarity with Moldova's financial authorities (NBM/CNPF) to de-risk Phase 2 scaling.

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-13 | 1.0 | Initial PRD creation from project brief and brainstorming | John (PM) |
| 2026-01-14 | 1.1 | PO validation improvements: Added 6 new stories (1.0, 1.9-1.11, 2.4b, 2.10), enhanced Stories 1.1 & 2.5, moved consent management from Epic 5 to Epic 2 for proper sequencing | Sarah (PO) |

---

## Requirements

### Functional Requirements

**FR1:** Borrowers can register and create an account with minimal KYC data (name, email, phone)

**FR2:** Borrowers can submit a credit application with Tier 1+2 fields: loan type, amount, term, optional income, currency, and rate type preference

**FR3:** System calculates preliminary offers in real-time based on configurable bank rate cards stored in the platform (simulated calculations using bank-provided formulas until bank APIs become available in Phase 2)

**FR4:** Borrowers can view side-by-side offer comparison table displaying 8 standardized metrics (APR, monthly payment, total cost, fees, term, rate type, down payment, processing time)

**FR5:** Borrowers can select an offer and submit non-binding intent indicating bank preference

**FR6:** Borrowers can track application status progression (Submitted → Viewed → Offer Received → Offer Accepted) in real-time

**FR7:** Borrowers can view saved application history and re-apply using previous application templates

**FR8:** Borrowers can access a scenario calculator for what-if analysis (different loan amounts/terms)

**FR9:** Bank administrators can create organizational accounts and invite team members with role-based access

**FR10:** Bank administrators can view application queue dashboard displaying all borrower applications submitted to their bank

**FR11:** Bank administrators can review complete borrower application details including all submitted information and supporting data

**FR12:** Bank administrators can submit binding preliminary offers with 8 required fields (APR, monthly payment, total cost, origination fee, insurance cost, processing time, required documents, validity period)

**FR13:** Banks can track offer status and borrower selection decisions from bank perspective

**FR14:** Platform generates and maintains audit logs for all transactions (consent, offers, selections) with timestamps

**FR15:** System sends email notifications for key workflow events (application submitted, offer received, offer accepted, application expired)

**FR16:** Borrowers explicitly consent to data collection, processing, and sharing with participating banks before application submission

**FR17:** System supports optional e-signature capability for future document signing workflows (Phase 2 readiness)

### Non-Functional Requirements

**NFR1:** All user data must be encrypted at rest and in transit (HTTPS for all endpoints, AES-256 for sensitive data)

**NFR2:** System must maintain 99.5% uptime availability during pilot phase (max 3.6 hours downtime/month)

**NFR3:** Preliminary offer calculation must complete within <500ms from application submission to display

**NFR4:** Platform must achieve average time-to-offer of <30 minutes from borrower application to preliminary offer display

**NFR5:** All personal data must be stored in Moldova or EU (GDPR-aligned data residency compliance)

**NFR6:** System must implement password requirements: minimum 12 characters, mixed case, numbers, special characters

**NFR7:** Platform must maintain immutable audit logs with tamper-proof timestamps for regulatory compliance and disputes

**NFR8:** Bank API integrations must be rate-limited to 100 requests/second per bank to prevent abuse

**NFR9:** System must support multi-currency display for loan comparisons (MDL, EUR, USD) with real-time exchange rates

**NFR10:** Mobile responsiveness required for borrower web interface (iOS Safari, Android Chrome minimum)

**NFR11:** All regulatory compliance documentation and privacy policies must be reviewed by Moldovan legal counsel before launch

**NFR12:** System must comply with GDPR-aligned personal data protection law effective 2026 in Moldova

---

## User Interface Design Goals

### Overall UX Vision

The platform prioritizes **speed and transparency** as core UX principles. Borrowers complete loan applications in <5 minutes with clear, progressive disclosure of fields (only ask what's needed). Banks receive organized queues with one-click offer submission. The entire experience emphasizes **trust through clarity** — borrowers see exactly what documents each offer requires, and banks see structured application data without manual parsing.

Design philosophy: **Minimal but complete** — eliminate friction without sacrificing data quality or regulatory compliance.

### Key Interaction Paradigms

1. **Progressive Application Flow** — Multi-step wizard with clear progress indicators; borrowers can save and resume applications
2. **Side-by-Side Comparison** — Standardized offer table enables instant visual comparison across banks
3. **Intent-Based Next Steps** — After offer selection, next actions are contextual (review documents, proceed to bank, accept terms)
4. **Admin Dashboard Pattern** — Bank administrators use a queue-based system: applications appear, review details, submit offer in one workflow
5. **Real-Time Notifications** — Event-driven UX (offer arrived, application viewed, status changed) keeps users engaged without page refreshes

### Core Screens and Views

**Borrower Journey:**
1. **Login/Registration Screen** — Email/phone registration with minimal KYC
2. **Borrower Dashboard** — View saved applications and quick-start new application
3. **Credit Application Wizard** (4-5 screens) — Step through: loan type → amount → term → optional details → review & submit
4. **Offer Comparison Page** — Side-by-side table of all offers with 8 metrics; bank logos and offer validity displayed
5. **Offer Detail Page** — Single offer expanded view showing required documents, terms, and selection button
6. **Application Status Tracker** — Real-time progression view (Submitted → Viewed → Offer Received → Offer Accepted)
7. **Scenario Calculator** — What-if tool: adjust amount/term and see offer impact
8. **Account Settings** — Profile, password, consent preferences, data sharing audit trail

**Bank Journey:**
1. **Bank Login Screen** — Organization account login
2. **Bank Admin Dashboard** — Summary: applications received today, offers submitted, conversion rate
3. **Application Queue** — Sortable/filterable list of borrower applications pending review
4. **Application Review Panel** — Full borrower details: loan request, personal info (with consent checkmarks), calculated preliminary offer
5. **Offer Submission Form** — 8 fields: APR, monthly payment, total cost, origination fee, insurance, processing time, required documents, validity period
6. **Offer History & Analytics** — View past offers submitted and borrower selection rates

### Accessibility Requirements

**WCAG AA** standard (Level AA) to ensure accessibility for borrowers and bank staff with disabilities. This includes:
- Keyboard navigation for all interactive elements
- Color contrast minimum 4.5:1 for text
- Form labels and error messages clearly associated with inputs
- Screen reader compatibility for all borrower-facing content

### Branding

**Neutral/Professional:**
- **Color Palette:** Professional fintech colors — navy/dark blue (trust), green (positive outcomes), white/light gray (clarity)
- **Typography:** Clean sans-serif (e.g., Inter, Roboto) for readability on mobile and desktop
- **Visual Language:** Minimalist design; emphasis on data clarity over decoration; icons for key actions (submit, compare, save)
- **Tone:** Professional but approachable; use clear, jargon-free language in all UX copy

### Target Device and Platforms

**Web Responsive + Mobile-Optimized** Platform must be **fully responsive** and work seamlessly on:
- **Desktop:** Chrome, Firefox, Safari (Windows/macOS)
- **Mobile:** iOS (Safari 14+), Android (Chrome 90+)
- **Tablet:** iPad (responsive layout adjustment)

---

## Technical Assumptions

### Repository Structure

**Monorepo** structure with clearly separated backend and frontend packages:

```
/credit-aggregator
  /backend (Spring Boot)
    /src
    /tests
    pom.xml
  /frontend (React/TypeScript)
    /src
    /tests
    package.json
  /docs
  /scripts (deployment, migrations)
```

### Service Architecture

**Monolith with Clear Boundaries** - Start with a **monolithic Spring Boot backend** organized by business domains:

```
/backend
  /auth (authentication, authorization)
  /borrower (borrower accounts, applications)
  /bank (bank accounts, offers, queue)
  /offers (calculation engine, comparison logic)
  /notifications (email, SMS events)
  /compliance (audit logs, consent management)
  /shared (common utilities, database models)
```

**Frontend Technology:** React with TypeScript for type safety and developer experience. Vite for build tooling.

### Testing Requirements

**Full Testing Pyramid:**
- **Unit Tests:** 80%+ code coverage (Jest for frontend, JUnit for backend)
- **Integration Tests:** Coverage of critical workflows (application submission → offer generation → selection)
- **E2E Tests:** Key user journeys (borrower signup → apply → compare offers; bank review → submit offer)
- **Manual Testing:** Pre-launch regression testing and compliance validation by QA team

### Additional Technical Assumptions and Requests

**Database:**
- PostgreSQL for primary OLTP database (reliability, ACID compliance, strong JSON support for flexible application schemas)
- Redis for session management and real-time offer calculations
- Audit trail stored in dedicated schema (immutable logs per NFR7)

**Authentication & Security:**
- JWT-based stateless authentication with refresh tokens
- Spring Security for authorization (role-based: Borrower, Bank Admin, Compliance Officer)
- TLS 1.3 for all endpoints
- Password hashing: bcrypt with salt
- CSRF protection for all state-changing operations

**Email & Notifications:**
- SendGrid or local SMTP for transactional emails
- Event-driven architecture: offer submission triggers email to borrower (Kafka or RabbitMQ for event bus)
- SMS optional in MVP (Phase 2 if adoption metrics justify)

**Bank Integration (MVP Phase):**
- **MVP approach:** Banks configure their loan calculator formulas (rate cards) via admin panel; platform simulates calculations locally without calling bank APIs
- **Rationale:** Bank websites have calculators but no available endpoints for external requests; this mock approach allows MVP validation while banks develop APIs
- **Phase 2:** Replace simulated calculations with real-time bank API calls when endpoints become available; API contracts designed in MVP to minimize rework

**Deployment:**
- Docker containerization for backend and frontend
- Kubernetes for orchestration (optional: AWS ECS for simplicity)
- Infrastructure-as-Code (Terraform) for repeatable deployments
- Cloud Platform: AWS recommended
- Data Residency: EU/Moldova region (AWS eu-central-1 or on-premise)

**Offer Calculation Engine:**
- Configurable rules engine allowing bank admins to set rate cards and formulas
- **MVP:** Hard-coded formulas for 2-3 pilot banks; parameterized in admin panel
- Real-time calculation: <500ms requirement drives in-memory caching of rate tables

**Monitoring & Observability:**
- Structured logging (JSON format) with ELK stack or CloudWatch
- Application metrics: Prometheus + Grafana for dashboard visibility
- Distributed tracing: Optional (Jaeger) for Phase 2

**Regulatory & Compliance:**
- All sensitive data encrypted at rest (AES-256) and in transit (TLS 1.3)
- PII handling: Separate encryption keys for PII vs. application data
- Audit logging: Immutable event log capturing all user actions, consent records, offer submissions
- GDPR compliance: Right to export data, right to deletion, consent audit trail

---

## Epic List

### Epic 1: Foundation & User Authentication (12 stories)
**Goal:** Establish complete production-ready infrastructure, user registration system, and authentication for both borrower and bank roles. Enable login, account creation, role-based access control, notifications, monitoring, and developer documentation from day one.

**Stories:** Infrastructure Provisioning (1.0), Project Setup & CI/CD (1.1), Database Schema (1.2), User Registration (1.3), Bank Account Creation (1.4), JWT Authentication (1.5), RBAC (1.6), Audit Logging (1.7), User Profile Management (1.8), Notification Service (1.9), Developer Documentation (1.10), Monitoring & Alerting (1.11)

### Epic 2: Borrower Application Workflow (11 stories)
**Goal:** Enable borrowers to create, submit, save, and track loan applications with explicit consent management and real-time status visibility. Borrowers can view application history, re-apply using templates, and access helpful guidance throughout the process.

**Stories:** Application Data Model (2.1), Create Application API (2.2), Update Application (2.3), Retrieve Application (2.4), Consent Management (2.4b), Submit Application (2.5), List Applications (2.6), Status Tracking (2.7), Re-Application Templates (2.8), Scenario Calculator (2.9), User Help Content (2.10)

### Epic 3: Preliminary Offer Calculation & Comparison (9 stories)
**Goal:** Implement offer calculation engine with configurable bank formulas. Display real-time preliminary offers and side-by-side comparison table. Add scenario calculator for what-if analysis.

**Stories:** Offer Data Model (3.1), Bank Rate Card Configuration (3.2), Offer Calculation Engine (3.3), Offer Retrieval & Comparison (3.4), Offer Selection (3.5), Offer Expiration (3.6), Scenario Calculator Integration (3.7), Comparison Table UI (3.8), Bank Offer Override (3.9)

### Epic 4: Bank Admin Portal & Offer Management (9 stories)
**Goal:** Build bank account setup, application queue dashboard, offer submission workflow, and offer tracking. Enable banks to review applications and submit binding preliminary offers.

**Stories:** Bank Dashboard (4.1), Application Queue (4.2), Application Review Panel (4.3), Offer Submission Form (4.4), Offer History & Tracking (4.5), Offer Expiration Notification (4.6), Bank Settings & Profile (4.7), Offer Analytics (4.8), Offer Decline & Withdrawal (4.9)

### Epic 5: Regulatory Compliance & Data Governance (8 stories)
**Goal:** Implement advanced compliance features including privacy policy management, data export/deletion capabilities, e-signature integration readiness, and GDPR-aligned data handling. Note: Basic consent management moved to Epic 2 for proper sequencing.

**Stories:** Privacy Policy & Terms (5.2), Data Export (5.3), Data Deletion (5.4), Audit Trail Immutability (5.5), E-Signature Readiness (5.6), Data Encryption (5.7), GDPR Compliance Checklist (5.8), Consumer Protection Disclosures (5.9)

---

## Epic 1: Foundation & User Authentication

**Expanded Goal:**
Establish the platform's foundational infrastructure and enables both borrowers and bank administrators to create accounts and log in. By the end of this epic, the application is deployed to a production-like environment with secure authentication, role-based access control, and audit logging.

### Story 1.0: Infrastructure Provisioning

**As a** DevOps engineer,  
**I want** cloud infrastructure and external services provisioned before application deployment,  
**so that** the application has all required resources (database, storage, email service) ready from day one.

**Acceptance Criteria:**

1. AWS account configured with appropriate IAM roles and policies for application deployment
2. Terraform modules created for: VPC (3 availability zones), RDS PostgreSQL 15.4 (Multi-AZ), ElastiCache Redis 7.2.3 (cluster mode), S3 bucket (with versioning and encryption enabled), CloudFront distribution (for static assets)
3. Domain registered and DNS configured (Route53 or equivalent) pointing to load balancer
4. SSL/TLS certificates acquired (Let's Encrypt or AWS Certificate Manager) and configured for HTTPS
5. SendGrid account created, API key generated, and sender domain verified for transactional emails
6. All infrastructure provisioned in EU/Moldova region (AWS eu-central-1 or equivalent) for GDPR compliance
7. Terraform state stored securely (S3 backend with state locking via DynamoDB)
8. Infrastructure deployed to both staging and production environments
9. All services health-checked and accessible (database connection test, Redis ping, S3 write test, email send test)
10. Infrastructure documentation created: architecture diagram, resource inventory, access credentials storage location
11. Cost monitoring and budget alerts configured (alert if monthly spend exceeds threshold)
12. Integration test: Terraform apply succeeds, all resources created, health checks pass

### Story 1.1: Project Setup & CI/CD Pipeline

**As a** development team lead,  
**I want** a fully configured Spring Boot project with automated build, test, and deployment pipeline,  
**so that** all developers can commit code with confidence that tests run automatically and deployments are repeatable.

**Acceptance Criteria:**

1. Spring Boot 3.2.1 project initialized with Maven, directory structure organized by domain (auth, borrower, bank, shared)
2. Exact dependency versions pinned: Java OpenJDK 21.0.1, Spring Boot 3.2.1, PostgreSQL 15.4, Redis 7.2.3, React 18.2.0, Node.js 20.11.0 LTS, Vite 5.0.8
3. PostgreSQL database provisioned locally (docker-compose) and connected to staging RDS from Story 1.0
4. CI/CD pipeline (GitHub Actions or GitLab CI) configured to: run unit tests on every commit, build Docker image on main branch, run integration tests, deploy to staging
5. Blue-green deployment strategy configured with automated rollback capability if health checks fail
6. Logging configured: JSON structured logs sent to CloudWatch (or local ELK for development) with structured fields (timestamp, level, service, trace_id)
7. README documents: local development setup with docker-compose, deployment process, environment variable configuration, troubleshooting guide
8. Deployment runbook created documenting: staging deployment process, production deployment process, rollback procedure, incident response contacts
9. All tests pass in CI pipeline (initially placeholder tests to prove pipeline works)
10. Staging deployment is accessible and shows a "Health Check" endpoint returning 200 OK with system status (database: connected, redis: connected, version: 1.0.0)

### Story 1.2: Database Schema & ORM Setup

**As an** architect,  
**I want** a well-designed database schema with Hibernate ORM configured,  
**so that** developers can quickly persist and query entities without writing raw SQL.

**Acceptance Criteria:**

1. PostgreSQL schema created with tables for: users, user_roles, sessions, audit_logs
2. Hibernate JPA entities defined for User, UserRole, AuditLog with proper annotations
3. Database migrations framework (Flyway or Liquibase) integrated; migration scripts version-controlled
4. Connection pooling (HikariCP) configured for production performance
5. Audit logging trigger or JPA listener implemented to capture all user entity changes
6. Development database can be seeded with test data (seed script in `scripts/` folder)
7. Unit tests verify entity relationships and basic ORM queries work correctly
8. Local `docker-compose.yml` spins up PostgreSQL with initialized schema for developers

### Story 1.3: User Registration API & Borrower Account Creation

**As a** borrower,  
**I want** to sign up with email, password, and basic information (name, phone),  
**so that** I can create an account and start applying for loans.

**Acceptance Criteria:**

1. POST `/api/auth/register` endpoint accepts email, password, full name, phone number
2. Email validation: must be valid email format; uniqueness enforced in database
3. Password validation: minimum 12 characters, mixed case, numbers, special characters
4. Passwords hashed with bcrypt (salt rounds ≥ 12)
5. User automatically assigned "BORROWER" role
6. Confirmation email sent to borrower (optional activation link; MVP allows immediate login)
7. Response returns user ID and success message; no sensitive data exposed
8. Duplicate email registration rejected with clear error message
9. Integration test verifies full flow: register borrower, verify user exists in database with correct role
10. API rate-limited to 10 registration requests per IP per hour

### Story 1.4: Bank Account Creation & Admin Registration

**As a** bank administrator,  
**I want** to register my bank with organization details and create an admin account,  
**so that** my bank can participate in the marketplace and review borrower applications.

**Acceptance Criteria:**

1. POST `/api/auth/register-bank` endpoint accepts: bank name, registration number, contact email, admin name, admin password, phone
2. Bank organization created in database with unique registration number
3. Admin user created and assigned "BANK_ADMIN" role linked to the bank organization
4. Bank activation workflow: admin receives email with activation link
5. Bank status: PENDING_ACTIVATION → ACTIVE (after email confirmation)
6. Only ACTIVE banks can log in and access application queue
7. Bank cannot be deleted once created (soft delete for audit trail)
8. Integration test verifies bank registration and admin login flow
9. Error handling: duplicate bank registration number, invalid email, password validation

### Story 1.5: JWT Authentication & Login

**As a** borrower or bank administrator,  
**I want** to log in with email and password and receive a secure authentication token,  
**so that** subsequent API requests prove my identity without sending my password.

**Acceptance Criteria:**

1. POST `/api/auth/login` endpoint accepts email and password
2. Validates credentials against hashed password in database
3. On success: returns JWT token with claims (user_id, role, bank_id if bank admin)
4. JWT token includes: expiration (15 minutes), refresh token (7 days)
5. Token format: Bearer token sent in Authorization header for subsequent requests
6. Failed login attempts: no detailed error message ("Invalid email or password")
7. Account lockout after 5 failed login attempts (15-minute cooldown)
8. Refresh token endpoint: POST `/api/auth/refresh` extends session without re-entering password
9. Logout endpoint: POST `/api/auth/logout` invalidates refresh token
10. Integration test: login as borrower, verify token claims, use token to access protected endpoint

### Story 1.6: Role-Based Access Control (RBAC)

**As a** security architect,  
**I want** authorization rules enforced so borrowers can only access borrower APIs and bank admins only access bank APIs,  
**so that** users cannot accidentally or maliciously access another role's data.

**Acceptance Criteria:**

1. Spring Security configured with @PreAuthorize annotations on all API endpoints
2. Three roles defined: BORROWER, BANK_ADMIN, COMPLIANCE_OFFICER
3. Unauthorized access (wrong role) returns 403 Forbidden
4. Endpoints requiring multiple roles explicitly configured
5. JWT token validation checks user role on every request
6. Unit tests verify authorization
7. Integration test: login as borrower, verify `/api/bank/queue` returns 403
8. Documentation: list all protected endpoints and required roles

### Story 1.7: Audit Logging Infrastructure

**As a** compliance officer,  
**I want** all user actions logged immutably with timestamps and user context,  
**so that** I can audit who did what and when for regulatory compliance.

**Acceptance Criteria:**

1. Audit log table: user_id, action, resource, timestamp, IP address, result
2. JPA event listener logs all user actions automatically
3. Audit logs are immutable: no delete or update operations
4. GET `/api/compliance/audit-logs` endpoint returns paginated audit logs
5. Audit logs contain: user_id, role, action, resource_id, timestamp, IP, user agent
6. Sensitive fields never logged
7. Retention policy: audit logs retained for minimum 3 years
8. Integration test: register user, verify audit log entry created
9. Documentation: audit event types

### Story 1.8: User Profile & Password Management

**As a** borrower or bank administrator,  
**I want** to view and update my profile information and change my password,  
**so that** I can keep my account details current and secure.

**Acceptance Criteria:**

1. GET `/api/profile` returns current user's profile
2. PUT `/api/profile` accepts name and phone updates
3. PUT `/api/profile/change-password` accepts current password and new password
4. New password validated same as registration
5. Current password must be correct
6. Successful password change invalidates all active refresh tokens
7. Password change email notification sent to user
8. Borrower profile includes loan preference history
9. Bank admin profile includes bank name and admin flag
10. Integration test: login, view profile, update name, change password, verify changes

### Story 1.9: Notification Service Setup

**As a** backend developer,  
**I want** a centralized notification service configured with email and event queue infrastructure,  
**so that** all application events can trigger appropriate user notifications reliably.

**Acceptance Criteria:**

1. SendGrid API integration configured using API key from Story 1.0 infrastructure setup
2. Email templates created for all notification types: user registration confirmation, application submitted, application viewed by bank, preliminary offer received, offer accepted, offer expired, password reset
3. Notification service implements: sendEmail(recipient, template, variables), queueNotification(event, recipient), retryFailedNotifications()
4. Event-driven architecture: RabbitMQ or Kafka message queue configured for async notification processing
5. Notification queue consumers process events and trigger email sends (non-blocking)
6. Email delivery tracking: log all sent emails with status (sent, delivered, bounced, failed)
7. Rate limiting: max 100 emails per minute to prevent SendGrid throttling
8. Fallback mechanism: if SendGrid fails, queue notification for retry (exponential backoff: 1min, 5min, 15min)
9. Environment-specific configuration: use real SendGrid in production, mock/log emails in development
10. Integration test: trigger application submission event, verify email queued, verify email sent via SendGrid API, verify delivery status logged
11. Health check endpoint: GET `/api/health/notifications` returns SendGrid connectivity status

### Story 1.10: Developer Documentation & Conventions

**As a** new developer joining the team,  
**I want** comprehensive documentation of code conventions, architecture decisions, and API contracts,  
**so that** I can contribute effectively without tribal knowledge.

**Acceptance Criteria:**

1. API documentation auto-generated from code using Swagger/OpenAPI annotations on all endpoints
2. Swagger UI accessible at `/api/docs` in development and staging environments
3. Coding conventions documented: Java code style (Google Java Style Guide), naming conventions (controllers, services, repositories), package organization by domain
4. Architecture Decision Records (ADRs) created for key decisions: monolith-first approach, JWT authentication choice, PostgreSQL selection, React + Vite frontend stack
5. Database schema documentation: ER diagram generated, all tables/columns documented with descriptions
6. Git workflow documented: branch naming (feature/, bugfix/, hotfix/), commit message format (Conventional Commits), PR review process
7. Development workflow guide: how to run locally, how to run tests, how to debug, how to deploy to staging
8. Troubleshooting guide: common errors and solutions (database connection failed, Redis not starting, email not sending)
9. API contract examples: request/response samples for all major endpoints (register, login, submit application, create offer)
10. Documentation stored in `/docs/dev/` folder and linked from main README
11. Documentation reviewed by at least 2 team members for clarity and completeness

### Story 1.11: Monitoring & Alerting Setup

**As a** site reliability engineer,  
**I want** comprehensive monitoring and alerting infrastructure configured,  
**so that** production issues are detected and escalated before users are significantly impacted.

**Acceptance Criteria:**

1. Prometheus deployed and configured to scrape metrics from all application services every 15 seconds
2. Application metrics exposed: HTTP request count, request latency (p50, p95, p99), error rate, active users, database connection pool usage, Redis cache hit rate
3. Business metrics exposed: applications submitted (count), offers generated (count), time-to-offer (histogram), user registrations (count)
4. Grafana dashboards created: System Health (CPU, memory, disk, network), Application Performance (latency, error rate, throughput), Business Metrics (applications, offers, conversions)
5. Infrastructure metrics collected: RDS performance (connections, queries, latency), Redis performance (commands/sec, memory usage), S3 usage
6. Alerting rules configured: API error rate >5% for 5min → Page on-call, API latency p95 >500ms for 5min → Warning, Database connections >90% → Critical, Uptime <99% (rolling 1 hour) → Warning
7. Alerting channels configured: email for warnings, PagerDuty/Slack for critical alerts
8. Log aggregation: Fluentd/Logstash ships logs to Elasticsearch, Kibana configured for log search and visualization
9. Distributed tracing (optional): Jaeger or Zipkin configured for request tracing across services
10. Health check endpoints for all critical services return detailed status (not just 200 OK)
11. Monitoring dashboard accessible to all team members with appropriate access controls
12. Integration test: trigger metric update, verify Prometheus scrapes it, verify Grafana displays it, trigger alert condition, verify alert fires

---

## Epic 2: Borrower Application Workflow

**Expanded Goal:**
Enable borrowers to create, save, submit, and track loan applications with real-time status visibility. Borrowers gain immediate value (status tracking) even before offers arrive.

### Story 2.1: Application Data Model & Database Schema

**As an** architect,  
**I want** a well-designed database schema for borrower applications,  
**so that** subsequent stories can persist and query application data reliably.

**Acceptance Criteria:**

1. Application table with fields: id, borrower_id, loan_type, loan_amount, loan_term_months, currency, rate_preference, status, created_at, submitted_at, updated_at
2. ApplicationDetails table (optional fields): annual_income, employment_status, down_payment_amount
3. ApplicationHistory table tracking status transitions
4. Indexes on: borrower_id, status, created_at
5. Cascade delete rules: deleting borrower cascades soft-delete to applications
6. Hibernate entities defined with relationships
7. Database migration script
8. Unit tests verify entity relationships
9. No sensitive data stored unencrypted

### Story 2.2: Create New Application API

**As a** borrower,  
**I want** to start a new loan application by specifying loan type, amount, and term,  
**so that** I can begin exploring available credit options.

**Acceptance Criteria:**

1. POST `/api/borrower/applications` creates new application in DRAFT status
2. Request body: loan_type, loan_amount (100-1000000), loan_term_months (6-360), currency
3. Optional fields: rate_preference (default VARIABLE)
4. Validation: loan_amount ≥ 100, loan_term_months ≥ 6
5. Application created with status DRAFT
6. Response returns application ID, status, created_at
7. Only authenticated BORROWER role can access
8. Rate limiting: 1 new application per borrower per minute
9. Integration test: create application, verify it appears with DRAFT status

### Story 2.3: Update Application (Save Draft)

**As a** borrower,  
**I want** to save changes to my application without submitting it,  
**so that** I can fill out the form over multiple sessions.

**Acceptance Criteria:**

1. PUT `/api/borrower/applications/{applicationId}` accepts partial updates
2. Updatable fields in DRAFT status: loan_amount, loan_term_months, currency, rate_preference
3. Cannot update loan_type after creation
4. Same validation rules as Story 2.2
5. Status remains DRAFT
6. Response includes updated application and timestamp
7. Cannot update SUBMITTED applications; returns 409 Conflict
8. Integration test: create application, update amount, verify persists

### Story 2.4: Retrieve Application & Application History

**As a** borrower,  
**I want** to view my application details and see the history of status changes,  
**so that** I understand where my application is in the process.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}` returns full application details
2. Response includes created_at, submitted_at, updated_at timestamps
3. GET `/api/borrower/applications/{applicationId}/history` returns status change timeline
4. Borrower can only view their own applications
5. History entries human-readable
6. Timestamps formatted in ISO 8601 UTC
7. Integration test: create application, retrieve history
8. Performance: API responds in <100ms

### Story 2.4b: Consent Management Framework

**As a** borrower,  
**I want** to explicitly consent to data collection and sharing before application submission,  
**so that** I understand how my data will be used and maintain control over my personal information.

**Acceptance Criteria:**

1. Consent types defined: DATA_COLLECTION, BANK_SHARING, MARKETING, ESIGNATURE (Phase 2 readiness)
2. Consent table created: borrower_id, consent_type, consented_at, withdrawn_at, ip_address, user_agent, policy_version
3. POST `/api/borrower/consent` endpoint accepts array of consent_type values
4. Borrower cannot submit application without DATA_COLLECTION and BANK_SHARING consent (validation in Story 2.5)
5. Consent checkboxes displayed during application flow with links to privacy policy and terms
6. Consent withdrawal capability: PUT `/api/borrower/consent/withdraw` triggers data anonymization workflow
7. Immutable consent audit log: original consent record preserved even after withdrawal
8. GET `/api/borrower/consent` returns current consent status for all types with timestamps
9. Privacy policy version tracking: consent linked to specific policy version
10. Consent display includes clear explanations: "I consent to Credit Aggregator collecting my loan application data", "I consent to sharing my application with participating banks"
11. Audit log entries: "CONSENT_GRANTED" and "CONSENT_WITHDRAWN" with full context
12. Integration test: attempt application submission without consent (should fail), provide consent, verify submission succeeds

### Story 2.5: Submit Application

**As a** borrower,  
**I want** to finalize and submit my application to banks,  
**so that** the process of receiving preliminary offers begins.

**Acceptance Criteria:**

1. POST `/api/borrower/applications/{applicationId}/submit` transitions from DRAFT to SUBMITTED
2. Validation before submit: all required fields present AND borrower has granted DATA_COLLECTION and BANK_SHARING consent (from Story 2.4b)
3. If consent not granted: return 400 Bad Request with message "You must consent to data collection and bank sharing before submitting"
4. After submission: application locked (no further edits allowed)
5. submitted_at timestamp recorded
6. Audit log entry: "APPLICATION_SUBMITTED" with application_id and borrower_id
7. Email notification sent to borrower (via Notification Service from Story 1.9) confirming submission
8. Status immediately visible: application.status = "SUBMITTED"
9. Banks notified (async via event queue): new application added to their review queue
10. Idempotent: submitting same application twice returns success without duplicate processing
11. Integration test: create draft application, attempt submit without consent (should fail), grant consent, submit successfully, verify banks notified
8. Banks notified (async): new application added to queue
9. Idempotent: submitting twice doesn't create duplicate
10. Integration test: create draft, submit, verify status SUBMITTED

### Story 2.6: List Borrower Applications (Dashboard)

**As a** borrower,  
**I want** to see all my past and current applications in a list,  
**so that** I can quickly navigate to the application I want.

**Acceptance Criteria:**

1. GET `/api/borrower/applications` returns paginated list
2. List includes: application ID, loan_type, loan_amount, status, created_at, submitted_at
3. Pagination: default 10 results per page
4. Sorting: by created_at (newest first), by status, by amount
5. Filtering: by status, by loan type
6. Response includes total count, page number, has_more flag
7. Performance: <200ms even with 100+ applications
8. Integration test: create 3 applications, verify all appear with correct sorting

### Story 2.7: Application Status Tracking & Real-Time Updates

**As a** borrower,  
**I want** to see live status updates as my application moves through the bank review process,  
**so that** I know what's happening.

**Acceptance Criteria:**

1. Application status values: DRAFT → SUBMITTED → VIEWED → OFFER_RECEIVED → OFFER_ACCEPTED
2. GET `/api/borrower/applications/{applicationId}` returns current status
3. Optional: WebSocket endpoint for real-time updates
4. Email notification when: application viewed by bank, offer received
5. Status progression timeline displayed
6. Borrower sees SLA: "Expect preliminary offers within 3 hours"
7. If no offers after 24 hours: trigger follow-up status
8. Immutable status history
9. Integration test: submit application, update status, verify change reflected

### Story 2.8: Re-Application & Application Templates

**As a** borrower,  
**I want** to quickly create a new application by reusing fields from a previous application,  
**so that** I don't have to re-enter the same information.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}/use-as-template` creates new DRAFT
2. Reusable fields: loan_type, currency, rate_preference, annual_income
3. Reset fields: loan_amount, loan_term_months (borrower must set), status (DRAFT)
4. New application gets new ID; previous unchanged
5. Audit log: "APPLICATION_CREATED_FROM_TEMPLATE"
6. Integration test: create application, create from template, verify new has same loan_type but different ID

### Story 2.9: Scenario Calculator (What-If Analysis)

**As a** borrower,  
**I want** to explore different loan scenarios and see estimated monthly payments,  
**so that** I can decide what loan configuration best fits my budget.

**Acceptance Criteria:**

1. POST `/api/borrower/scenarios` accepts: loan_type, loan_amount, loan_term_months, currency
2. Response: estimated monthly payment
3. Calculation assumes average market rate
4. No application created; pure "what-if" tool
5. Real-time response: <200ms
6. Borrower can run unlimited scenarios
7. Calculation transparent: show formula used
8. Integration test: call endpoint with various amounts, verify reasonable calculations

### Story 2.10: User Help Content & Error Messages

**As a** borrower or bank administrator,  
**I want** clear help text, tooltips, and error messages throughout the application,  
**so that** I can understand what information is required and resolve issues without frustration.

**Acceptance Criteria:**

1. Error message library created with standardized format: clear problem description, specific action to resolve, contact support link if needed
2. Field-level tooltips for all non-obvious form fields: loan term (tooltip: "Number of months to repay loan, typically 12-360"), rate preference (tooltip: "Fixed rate stays same, variable rate changes with market")
3. Help content for each major screen: Application Form (what information you need), Offer Comparison (how to compare offers), Bank Queue (how to review applications)
4. Inline validation messages display immediately: "Email format invalid", "Password must be at least 12 characters", "Loan amount must be between 100 and 1,000,000"
5. Error message copy reviewed for clarity and tone (professional, helpful, no jargon)
6. Success messages provide next steps: "Application submitted! You'll receive offers within 30 minutes. Check your email for updates."
7. Empty states with helpful guidance: "No applications yet. Start by creating your first loan application."
8. 404 and error pages with helpful content and navigation options
9. Contextual help links throughout UI pointing to relevant documentation sections
10. Multi-language preparation: all user-facing text externalized to i18n files (Romanian and Russian for Phase 2)
11. User testing: 3 users test help content and provide feedback on clarity
12. Integration test: trigger various error conditions, verify correct error messages display

---

## Epic 3: Preliminary Offer Calculation & Comparison

**Expanded Goal:**
Transform loan applications into comparable preliminary offers by implementing a configurable calculation engine. Borrowers see the core value proposition: multiple offers from multiple banks for transparent comparison shopping.

### Story 3.1: Offer Data Model & Database Schema

**As an** architect,  
**I want** a database schema for offers, bank rate cards, and calculations,  
**so that** subsequent stories can persist preliminary and formal offers.

**Acceptance Criteria:**

1. Offer table: id, application_id, bank_id, offer_status, apr, monthly_payment, total_cost, origination_fee, insurance_cost, processing_time_days, validity_period_days, required_documents, created_at, expires_at
2. BankRateCard table: id, bank_id, loan_type, currency, min/max_loan_amount, base_apr, adjustment_range
3. OfferCalculationLog table: id, application_id, bank_id, calculation_method, input_parameters, calculated_values, timestamp
4. Indexes: application_id, bank_id, expires_at
5. Relationship: one application has many offers
6. Cascade rules: deleting application soft-deletes related offers
7. Hibernate entities defined
8. Database migration script
9. Unit tests verify relationships

### Story 3.2: Bank Rate Card Configuration API

**As a** bank administrator,  
**I want** to configure my bank's loan calculator formulas in the admin panel,  
**so that** the platform can simulate what my bank's calculator would return for borrower applications.

**Acceptance Criteria:**

1. POST `/api/bank/rate-cards` creates rate card for loan type/currency
2. Request body: loan_type, currency, min/max_loan_amount, base_apr, apr_adjustment_range, default_processing_time_days, origination_fee_percent, insurance_percent (optional)
3. **Rate card represents bank's calculator formula:** Banks manually enter the same parameters their website calculators use
4. Validation: min < max, APR ranges realistic (0.5-50%), processing_time 1-30 days
5. Rate cards versioned: new card marks previous inactive
6. GET `/api/bank/rate-cards` returns all active rate cards
7. PUT `/api/bank/rate-cards/{rateCardId}` updates existing
8. Soft-delete preserves history
9. Effective dating: valid_from and valid_to
10. **UI helper:** Admin panel provides form fields matching common calculator parameters to simplify bank configuration
11. Integration test: create rate card, verify appears, update, verify new values apply; verify calculations use updated rates

### Story 3.3: Offer Calculation Engine (Mock/Simulated)

**As a** system,  
**I want** to calculate preliminary offers automatically when borrower submits application using bank rate cards,  
**so that** offers appear immediately simulating what banks' calculators would return (without calling external bank APIs).

**Acceptance Criteria:**

1. Service: `OfferCalculationService` triggered by application submission
2. **Mock/Simulated approach:** For each participating bank, retrieve matching rate card from database and perform calculation locally (NO external API calls to banks)
3. **Calculation logic:** Apply bank's configured formula from rate card: 
   - APR = base_apr + adjustment (based on loan_amount/term from rate card parameters)
   - Monthly Payment = standard amortization formula
   - Total Cost = (monthly_payment × months) - principal
   - Origination Fee = loan_amount × origination_fee_percent
   - Insurance Cost = loan_amount × insurance_percent (if applicable)
4. **Simulates bank calculator:** Result should match what bank's website calculator would show for same inputs
5. Validation: if rate card missing for bank/loan_type/currency, skip that bank with logged warning
6. Offer status set to CALCULATED (indicates simulated/preliminary, not formally submitted by bank)
7. Validity period: 24 hours (configurable per bank in rate card)
8. Required documents: from bank template or default list configured in rate card
9. Async execution: non-blocking; calculations happen in background
10. Audit log: each calculation logged with "MOCK_CALCULATION" indicator and input parameters
11. Calculation result stored immediately in Offer table
12. Error handling: if one bank's calculation fails (e.g., invalid formula), others proceed; failed bank logged
13. **Phase 2 preparation:** Code structured to easily replace with real bank API calls when endpoints available
14. Integration test: submit application, verify offers created for all banks with rate cards, verify calculations mathematically correct, verify no external API calls made

### Story 3.4: Offer Retrieval & Comparison API

**As a** borrower,  
**I want** to view all preliminary offers in side-by-side comparison,  
**so that** I can easily compare terms and choose the best offer.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}/offers` returns all offers
2. Response includes: bank_name, logo_url, apr, monthly_payment, total_cost, fees, processing_time, required_documents, validity_period
3. Offers sorted by APR (ascending)
4. Response schema standardizes 8 core comparison fields
5. Only borrower who owns application can view
6. Offers only returned if application status SUBMITTED or later
7. Return 200 with empty array if no offers yet
8. Include "Preliminary Offers" disclaimer
9. Performance: <100ms response
10. Integration test: submit application, retrieve offers, verify all banks, verify sorting

### Story 3.5: Offer Selection & Intent Submission

**As a** borrower,  
**I want** to indicate which preliminary offer I'm interested in,  
**so that** the bank knows I'm a qualified borrower.

**Acceptance Criteria:**

1. POST `/api/borrower/applications/{applicationId}/select-offer` accepts offer_id
2. Validation: offer_id must belong to application, not expired
3. Sets borrower_selected_at timestamp; status to ACCEPTED
4. Only one offer can be selected per application
5. Borrower can change selection until expires
6. Application status updates to OFFER_ACCEPTED
7. Email sent to borrower and bank
8. Audit log: "OFFER_SELECTED"
9. Response includes selected offer + next steps
10. Integration test: retrieve offers, select one, verify status ACCEPTED, verify email sent

### Story 3.6: Offer Expiration & Cleanup

**As a** system,  
**I want** to expire offers that are no longer valid,  
**so that** borrowers don't accidentally select stale offers.

**Acceptance Criteria:**

1. Offers expire after validity_period (24 hours default)
2. Batch job: sets offer.offer_status = EXPIRED where expires_at < now()
3. Borrower sees: "This offer expires in X hours"
4. Expired offers still visible but marked "Expired" and unselectable
5. If borrower tries to select expired, returns 410 Gone
6. System can recalculate offers if borrower requests
7. Audit log: "OFFER_EXPIRED"
8. Data retention: expired offers kept for audit
9. Integration test: create offer with 1-hour expiration, verify marked expired

### Story 3.7: Scenario Calculator Integration

**As a** borrower,  
**I want** the scenario calculator to use actual bank rate cards,  
**so that** my "what-if" estimates match the simulated offers I'll receive.

**Acceptance Criteria:**

1. POST `/api/borrower/scenarios` calculates estimates for each participating bank using their configured rate cards
2. **Same mock approach as Story 3.3:** Uses bank rate cards from database to perform local calculations (no external API calls)
3. Response returns array: one per bank with estimated payment, APR, total cost based on their rate card formulas
4. Sorted by monthly_payment (ascending)
5. Response includes disclaimer: "Estimates based on current bank rates configured in system; actual offers may vary after bank review"
6. Same loan parameters produce consistent estimates (deterministic calculation)
7. Uses same calculation formula as Story 3.3 offer calculation engine (ensures consistency)
8. No storage: transient calculations for exploration only
9. Performance: <500ms with 5+ banks (local calculation is fast)
10. Integration test: run scenario, verify estimates match offers for equivalent application; verify calculation consistency

### Story 3.8: Offer Comparison Table (UI Endpoint)

**As a** frontend developer,  
**I want** a standardized endpoint that returns offers in comparison table format,  
**so that** rendering side-by-side comparison is straightforward.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}/offers/comparison` returns table-ready format
2. Response: array with: bank_id, bank_name, apr, monthly_payment, total_cost, fees, processing_time, required_documents_list, validity_days_remaining, best_offer_badge
3. All monetary values formatted to 2 decimals
4. Processing time and validity as integers (days)
5. Required documents as array of strings
6. Sorting: by monthly_payment ascending
7. Missing values handled gracefully ("N/A")
8. Include summary stats: average_apr, lowest_monthly_payment
9. Integration test: verify response structure matches table format

### Story 3.9: Bank Offer Override (Prepared in MVP)

**As a** bank administrator,  
**I want** to review the simulated calculated offer and optionally override with custom terms,  
**so that** I can submit binding offers reflecting my actual underwriting decision (since MVP uses simulated calculations, not my real calculator API).

**Acceptance Criteria:**

1. Bank admin sees simulated/calculated offer in application review panel (generated from rate card formula)
2. **Override capability critical for MVP:** Since calculations are simulated (not calling real bank systems), banks must be able to override with their actual offer terms
3. Bank can override: apr, monthly_payment, total_cost, origination_fee, insurance_cost, processing_time, required_documents, validity_period
4. Validation: overridden values within reasonable bounds (APR 0.5-50%, processing 1-30 days)
5. PUT `/api/bank/offers/{offerId}/override` accepts override data (implemented in Epic 4)
6. Original calculated values preserved in OfferCalculationLog for audit
7. Offer status changes CALCULATED → SUBMITTED when bank submits
8. timestamp: offer_submitted_at recorded
9. Email to borrower: "Bank [Name] has submitted a preliminary offer based on your application"
10. Audit log: "OFFER_SUBMITTED_BY_BANK" with original calculated vs. submitted values for comparison
11. **Workflow:** Simulated offer provides baseline; bank reviews and either accepts as-is or overrides

**Note:** Data model prepared in Epic 3; full implementation in Epic 4 Story 4.4.

---

## Epic 4: Bank Admin Portal & Offer Management

**Expanded Goal:**
Activate the two-sided marketplace by building bank administration interface. Banks review borrower applications, submit formal offers, and track borrower selections. Demonstrates bank value proposition for pilot expansion.

### Story 4.1: Bank Admin Dashboard & Queue Overview

**As a** bank administrator,  
**I want** to see a summary dashboard showing applications, offers, and conversion metrics,  
**so that** I understand my bank's activity at a glance.

**Acceptance Criteria:**

1. GET `/api/bank/dashboard` returns summary metrics for authenticated bank
2. Metrics: Applications Received Today, Applications Received (All), Offers Submitted, Offers Accepted by Borrowers, Conversion Rate, Average Time to Offer
3. Time period filters: Today, Last 7 days, Last 30 days, Custom
4. Charts (optional): trend line, conversion funnel
5. Quick links: View Application Queue, View Rate Cards, Submit Offer
6. Last updated timestamp
7. Authenticated bank sees only their own metrics
8. Response time: <500ms
9. Integration test: submit applications, verify dashboard count matches

### Story 4.2: Application Queue Dashboard

**As a** bank administrator,  
**I want** to see all borrower applications assigned to my bank in organized queue,  
**so that** I can prioritize which to review first.

**Acceptance Criteria:**

1. GET `/api/bank/applications/queue` returns paginated list for authenticated bank
2. List displays: application ID, borrower_id (anonymized), loan_type, loan_amount, loan_term, currency, submission_date, status
3. Status: SUBMITTED, VIEWED, OFFER_RECEIVED
4. Sorting: by submission_date (newest first), by loan_amount, by status
5. Filtering: by status, loan_type, date range, amount range
6. Pagination: default 20 per page
7. "New applications" badge highlights unreviewed
8. Search: by application ID or borrower email
9. Performance: <200ms even with 500+ applications
10. Integration test: create 5 applications, query queue, verify all appear with filtering

### Story 4.3: Application Review Panel

**As a** bank administrator,  
**I want** to view complete details of borrower's application,  
**so that** I can understand the loan request and assess fit.

**Acceptance Criteria:**

1. GET `/api/bank/applications/{applicationId}` returns full application details
2. Details: Loan Request (loan_type, amount, term, currency), Borrower Profile (name, email, phone), Optional Details (income, employment), Calculated Offer, Application Timeline
3. Consent checklist: verify borrower consented to data sharing
4. Mark as "VIEWED" when bank opens application
5. Display existing offer if submitted; if not, show calculated with "submit custom offer" button
6. "Offer Status" badge: CALCULATED or SUBMITTED
7. Notes field: bank can add internal notes (not visible to borrower)
8. Action buttons: View & Submit Offer, Decline Application, Request More Info
9. Audit log: "APPLICATION_VIEWED_BY_BANK"
10. Response time: <200ms
11. Integration test: open application, verify calculated offer visible, verify status transitions to VIEWED

### Story 4.4: Offer Submission Form

**As a** bank administrator,  
**I want** to submit preliminary offer with custom terms (or accept calculated),  
**so that** borrowers see my bank's official preliminary terms.

**Acceptance Criteria:**

1. POST `/api/bank/offers/{offerId}/submit` accepts submission with optional overrides
2. Form displays: current calculated values (apr, monthly_payment, total_cost, fees, processing_time, required_documents, validity_period)
3. Bank can: Accept As-Is (Submit without changes) or Override specific fields
4. Validation: APR 0.5-50%, processing_time 1-30 days, validity 1-30 days, total cost within 5% of calculated
5. Recalculation: if bank changes APR, auto-calculate new monthly_payment
6. Required documents: bank can override default list
7. Offer status set to SUBMITTED
8. offer_submitted_at timestamp
9. Email to borrower: "Bank has submitted preliminary offer"
10. Audit log: "OFFER_SUBMITTED_BY_BANK" with calculated vs. submitted values
11. Idempotent: resubmitting returns 200
12. Integration test: open offer, accept as-is, verify submission; override APR, verify monthly payment updates

### Story 4.5: Bank Offer History & Tracking

**As a** bank administrator,  
**I want** to see all offers my bank has submitted and their status,  
**so that** I can track which borrowers chose my bank.

**Acceptance Criteria:**

1. GET `/api/bank/offers` returns paginated list for authenticated bank
2. List displays: offer ID, application ID, loan_amount, loan_type, apr, monthly_payment, submission_date, borrower_status (accepted/not selected/expired), days_until_expiration
3. Sorting: by submission_date, by status, by apr, by monthly_payment
4. Filtering: by status, date range, loan_type
5. "Accepted" indicator: shows which offers borrowers selected
6. Click to view: links to full application
7. Bulk export (optional): download as CSV
8. Conversion funnel (optional): progression
9. Performance: <200ms even with 1000+ offers
10. Borrower contact info: show if accepted (for follow-up)
11. Integration test: submit 5 offers, retrieve history, verify all appear, select one, verify status updates

### Story 4.6: Offer Expiration Notification

**As a** bank administrator,  
**I want** to be notified when offer is about to expire or has expired,  
**so that** I can resubmit or follow up.

**Acceptance Criteria:**

1. Batch job: checks for offers expiring in next 24 hours
2. Email notification: "Your offer expires in 24 hours. Borrower has not selected."
3. For expired: email sent once: "Your preliminary offer has expired"
4. In portal: offers expiring soon highlighted orange; expired show red "EXPIRED"
5. Action: "Resubmit Offer" button allows quick update with new validity
6. Audit log: "OFFER_EXPIRATION_NOTIFICATION_SENT"
7. Borrower also notified: "Preliminary offer from [Bank] has expired"
8. Resubmit flow: pre-fills previous terms
9. Integration test: create offer with 1-day validity, verify notification sent

### Story 4.7: Bank Settings & Profile Management

**As a** bank administrator,  
**I want** to manage my bank's profile, account settings, and team members,  
**so that** my bank's information is current.

**Acceptance Criteria:**

1. GET `/api/bank/profile` returns bank details: bank_name, registration_number, contact_email, phone, address, website
2. PUT `/api/bank/profile` allows updating: bank_name, contact_email, phone, address, website
3. GET `/api/bank/team` returns list of team members
4. POST `/api/bank/team/invite` sends invitation to new team member
5. Team member roles (prepared for Phase 2): ADMIN, REVIEWER, ANALYST
6. Currently MVP supports single ADMIN role
7. PUT `/api/bank/profile/update-rate-cards` quick link
8. Bank logo upload (optional): bank can upload logo for borrower view
9. Notification preferences: opt-in/out of email notifications
10. Audit log: all profile changes logged
11. Integration test: update profile, verify persisted; invite team member, verify email sent

### Story 4.8: Offer Analytics & Conversion Reports

**As a** bank administrator,  
**I want** to see detailed analytics on offer performance,  
**so that** I can optimize my participation strategy.

**Acceptance Criteria:**

1. GET `/api/bank/analytics/conversions` returns: Total applications received, Total offers submitted, Conversion rate, Average time to offer, Average apr, Acceptance rate by loan type, Acceptance rate by amount
2. Trend charts (optional): daily/weekly/monthly trends
3. Leaderboard (optional): compare to platform average (anonymized)
4. Borrower demographics (Phase 2): age, location, income
5. Date range filtering: custom, presets (Today, Last 7 days, Last 30 days, Last 90 days)
6. Export: download as PDF or CSV
7. Refresh frequency: data updated daily
8. Performance: <1 second response
9. Permission: only bank admins for that bank
10. Integration test: submit applications/offers, accept some, verify conversion rate correct

### Story 4.9: Offer Decline & Withdrawal (Prepared for MVP)

**As a** bank administrator,  
**I want** to decline a borrower application or withdraw already-submitted offer,  
**so that** I can manage my portfolio.

**Acceptance Criteria:**

1. POST `/api/bank/applications/{applicationId}/decline` allows rejecting application
2. Optional decline reason
3. Data model prepared (notes field supports tracking)
4. Email to borrower: "Your application has been declined"
5. Bank can decline BEFORE submitting offer
6. DELETE `/api/bank/offers/{offerId}` allows withdrawing offer
7. Email to borrower: "Bank has withdrawn preliminary offer"
8. Audit log: "OFFER_WITHDRAWN_BY_BANK"
9. Data structures prepared; API endpoints not critical for MVP
10. Integration test: submit offer, withdraw, verify borrower notified

**Note:** Data structures prepared in MVP; full UX deferred to Phase 2 if timeline tight.

---

## Epic 5: Regulatory Compliance & Data Governance

**Expanded Goal:**
Establish regulatory and compliance foundation for operating fintech marketplace in Moldova. Comprehensive consent management, immutable audit trails, data export/deletion capabilities (GDPR-aligned), privacy policy enforcement, and e-signature integration readiness for Phase 2. Ensures all prior work meets Moldovan data protection law and NBM/CNPF requirements.

### Story 5.1: Consent Management Framework

**As a** borrower,  
**I want** to explicitly consent to data collection and sharing before application submission,  
**so that** I understand how my data will be used.

**Acceptance Criteria:**

1. Consent types: DATA_COLLECTION, BANK_SHARING, MARKETING, ESIGNATURE (Phase 2)
2. Consent table: borrower_id, consent_type, consented_at, withdrawn_at, ip_address, user_agent, version
3. POST `/api/borrower/consent` accepts consent_type array
4. Borrower cannot submit application without DATA_COLLECTION and BANK_SHARING consent
5. Consent checkboxes during application flow
6. Consent withdrawal: PUT `/api/borrower/consent/withdraw` triggers data deletion
7. Immutable consent log: original preserved even if withdrawn
8. GET `/api/borrower/consent` returns current status and timestamps
9. Privacy policy version tracking
10. Audit log: "CONSENT_GRANTED" and "CONSENT_WITHDRAWN"
11. Integration test: attempt submission without consent, verify rejection; provide consent, verify succeeds

### Story 5.2: Privacy Policy & Terms of Service

**As a** borrower or bank administrator,  
**I want** to view the platform's privacy policy and terms of service,  
**so that** I understand my rights and platform obligations.

**Acceptance Criteria:**

1. Privacy policy stored in database (content versioned)
2. GET `/api/legal/privacy-policy` returns current policy
3. GET `/api/legal/terms-of-service` returns current terms
4. Privacy policy includes: data collected, purpose, sharing, retention (3 years), rights (export, deletion, correction), contact info
5. Terms include: platform role (marketplace, not lender), obligations, dispute resolution, limitation of liability
6. Version tracking: increments with updates; borrowers see "Privacy policy updated, please review" banner
7. Forced re-acceptance: if material change, borrower must re-accept
8. Footer links: all pages include links
9. Moldovan law compliance: acknowledges GDPR-aligned law
10. Legal review: content reviewed by Moldovan counsel
11. Integration test: retrieve policy, verify version; update, verify new version; verify re-acceptance flow

### Story 5.3: Data Export (Right to Portability)

**As a** borrower,  
**I want** to export all my personal data held by the platform,  
**so that** I can review what's stored and exercise data portability rights.

**Acceptance Criteria:**

1. GET `/api/borrower/data-export` initiates export
2. Export includes: profile, all applications, all offers, consent history, audit log entries
3. Format: JSON (structured) and/or PDF (human-readable)
4. Sensitive data included: all PII borrower provided
5. Export generation: async (1-5 minutes); email with download link
6. Download link valid 24 hours; expires after first download or 24 hours
7. Audit log: "DATA_EXPORT_REQUESTED" and "DATA_EXPORT_DOWNLOADED"
8. Security: one-time-use token; only borrower can download
9. Performance: queued; supports up to 1000 applications
10. Compliance: meets GDPR Article 20
11. Integration test: request export, wait for email, download, verify JSON contains all data

### Story 5.4: Data Deletion (Right to Erasure)

**As a** borrower,  
**I want** to request deletion of all my personal data,  
**so that** I can exercise my right to be forgotten.

**Acceptance Criteria:**

1. POST `/api/borrower/data-deletion` initiates deletion request
2. Soft delete: borrower marked "deleted," PII anonymized, application history preserved (3-year retention)
3. Data anonymized: name → "Deleted User [hash]", email → "[hash]@deleted.local", phone → NULL
4. Application data retained: loan_amount, term, status (non-PII for audit)
5. Consent withdrawal: all consents marked WITHDRAWN
6. Email confirmation before deletion: "You've requested deletion. Confirm within 7 days."
7. Confirmation link: click to confirm (prevents accidental deletion)
8. Grace period: 7 days to cancel
9. After deletion: borrower cannot log in; can re-register with same email
10. Audit log: "DATA_DELETION_REQUESTED" and "DATA_DELETION_COMPLETED"
11. Regulatory compliance: honors 3-year audit retention
12. Integration test: request deletion, confirm, verify PII anonymized but history preserved

### Story 5.5: Audit Trail Immutability & Compliance Logging

**As a** compliance officer,  
**I want** all user actions logged immutably,  
**so that** regulatory audits can reconstruct activity.

**Acceptance Criteria:**

1. All audit log entries immutable: no UPDATE or DELETE
2. Database constraints: no foreign key cascades from audit_logs
3. Audit log retention: 3 years minimum; after 3 years, optional archival
4. GET `/api/compliance/audit-logs` endpoint (COMPLIANCE_OFFICER role only)
5. Filtering: by user_id, action type, date range, result
6. Export: download as CSV
7. Critical events logged: registration, login, application submission, consent, offer submission, selection, data export, data deletion
8. Context: user_id, role, IP, user agent, timestamp (UTC), action, resource_id, result
9. Sensitive data never logged
10. Tamper-proof: stored with cryptographic hash (optional: blockchain in Phase 2)
11. Integration test: perform 10 actions, retrieve logs, verify all captured

### Story 5.6: E-Signature Integration Readiness

**As a** borrower,  
**I want** infrastructure for e-signature prepared so Phase 2 can add document signing without rework,  
**so that** future loan document execution is seamless.

**Acceptance Criteria:**

1. E-signature provider selected (DocuSign, HelloSign, or Moldovan-qualified)
2. Database schema: documents table (id, application_id, document_type, file_url, created_at, signed_at, signature_id)
3. Signature log table: document_id, signer_id, signed_at, ip_address, signature_certificate, signature_status
4. API endpoints stubbed: POST `/api/borrower/documents/{documentId}/sign` (returns 501 Not Implemented)
5. Consent type ESIGNATURE added to consent framework
6. Document retention: secure S3 bucket, 3+ years
7. Audit log integration: "DOCUMENT_SIGNED" prepared
8. Legal validation: provider verified as recognized in Moldova
9. Document templates prepared: placeholder agreements
10. Integration test: verify schema supports storage; verify API returns "not implemented"; verify consent includes ESIGNATURE

### Story 5.7: Data Encryption at Rest & in Transit

**As a** security architect,  
**I want** all sensitive data encrypted at rest and API traffic secured with TLS,  
**so that** data breaches cannot expose PII.

**Acceptance Criteria:**

1. Database encryption: PostgreSQL with TDE or column-level encryption for PII
2. Encryption keys via AWS KMS (or Moldovan-compliant key management)
3. Separate keys for PII vs. application data
4. TLS 1.3 enforced for all HTTPS endpoints
5. HSTS headers sent to prevent downgrade attacks
6. Certificate management: valid SSL from trusted CA; auto-renewal
7. API keys and JWT secrets in environment variables or secrets manager
8. Passwords hashed with bcrypt
9. Audit logs don't contain sensitive data (PII redacted)
10. File uploads (Phase 2): encrypted in S3 with SSE-KMS
11. Compliance: meets Moldovan encryption requirements
12. Integration test: verify queries return decrypted data; verify TLS 1.3; verify passwords hashed

### Story 5.8: GDPR & Moldovan Data Protection Compliance Checklist

**As a** compliance officer,  
**I want** a comprehensive checklist documenting platform compliance,  
**so that** regulatory review can be completed efficiently.

**Acceptance Criteria:**

1. Compliance checklist: covers GDPR Articles 6, 7, 15, 16, 17, 20, 32
2. Moldovan-specific: data residency (EU/Moldova), registration with data protection authority, privacy policy in Romanian/Russian if required
3. Checklist items: ✅ Explicit consent, Privacy policy published, Data export, Data deletion, Audit logs immutable, Encryption, Data retention policy (3 years), DPO identified
4. GET `/api/compliance/checklist` returns status (COMPLIANCE_OFFICER role only)
5. Red/yellow/green status for each item
6. Regulatory submission package: downloadable PDF with all compliance documentation
7. NBM/CNPF communication: prepared template letter requesting licensing clarification
8. Annual review: refreshed annually or when regulations change
9. Audit log: "COMPLIANCE_CHECKLIST_REVIEWED"
10. Integration test: retrieve checklist, verify all items green; generate submission package, verify PDF includes all sections

### Story 5.9: Consumer Protection & Transparent Disclosures

**As a** borrower,  
**I want** clear, transparent disclosures of loan costs (APR, fees, total cost) and my rights,  
**so that** I can make informed borrowing decisions without hidden fees.

**Acceptance Criteria:**

1. All offers display APR prominently (font size ≥ 14pt, bold)
2. Standardized comparison metrics include: APR, monthly payment, total cost, all fees
3. No hidden fees: all costs disclosed upfront
4. "Effective APR" calculated: includes all fees
5. Tooltip explains each fee
6. Borrower rights section in privacy policy: right to compare, withdraw, data access/deletion
7. "Preliminary offer" disclaimer: "Offers are preliminary estimates. Final terms subject to review."
8. Bank contact info: each offer includes customer service email/phone
9. Comparison table includes "Total Cost of Credit" column
10. Moldovan consumer protection law compliance: all disclosures meet NBM/CNPF requirements
11. Integration test: view offers, verify APR prominent; verify total cost correct; verify disclaimers present

---

## Checklist Results Report

**Overall PRD Completeness:** 95%  
**MVP Scope Appropriateness:** Just Right  
**Readiness for Architecture Phase:** Ready  
**Most Critical Gaps:** E-signature provider selection, Moldovan legal review pending, Data residency final confirmation

### Category Analysis

| Category                         | Status  | Critical Issues                                      |
| -------------------------------- | ------- | ---------------------------------------------------- |
| 1. Problem Definition & Context  | PASS    | None - problem clearly defined with user research    |
| 2. MVP Scope Definition          | PASS    | Well-bounded with clear rationale for inclusions     |
| 3. User Experience Requirements  | PASS    | Comprehensive UX vision and core screens identified  |
| 4. Functional Requirements       | PASS    | 17 FRs cover all MVP features                        |
| 5. Non-Functional Requirements   | PASS    | 12 NFRs address security, performance, compliance    |
| 6. Epic & Story Structure        | PASS    | 5 epics, 44 stories, all sequentially logical        |
| 7. Technical Guidance            | PASS    | Clear architecture direction with Spring/React stack |
| 8. Cross-Functional Requirements | PARTIAL | Data residency needs final confirmation (EU vs. MD)  |
| 9. Clarity & Communication       | PASS    | Well-documented with rationale for all decisions     |

### Top Issues by Priority

**BLOCKERS:** None

**HIGH:**
1. **Data Residency Confirmation** — Verify with Moldovan regulators if EU (AWS eu-central-1) is acceptable or if on-premise Moldova required
2. **E-Signature Provider Selection** — Choose qualified provider recognized by Moldovan courts before Phase 2
3. **Legal Counsel Review** — Privacy policy and terms must be reviewed by Moldovan attorney before launch

**MEDIUM:**
4. **Multi-Language Support** — Clarify if privacy policy/terms need Romanian/Russian translations for MVP
5. **DPO Requirement** — Confirm if formal Data Protection Officer role required or if compliance officer sufficient
6. **Bank Rate Card Complexity** — Validate with pilot banks that hard-coded formulas for MVP are acceptable

**LOW:**
7. **Bank Logo Upload** — Optional for MVP but would enhance borrower UX
8. **WebSocket Real-Time Updates** — Polling is acceptable for MVP; WebSocket nice-to-have for Phase 2

### MVP Scope Assessment

**Features that might be cut for true MVP:**
- Story 2.9 (Scenario Calculator) — Valuable but not critical path; can defer if timeline tight
- Story 4.8 (Offer Analytics) — Banks can track manually in MVP; robust analytics Phase 2
- Story 4.9 (Offer Decline & Withdrawal) — Data structures prepared; full UX can defer

**Missing features that are essential:** None identified

**Complexity concerns:**
- Epic 5 (Compliance) has significant regulatory dependencies; NBM/CNPF clarification timing is external risk
- Offer calculation engine (Story 3.3) needs validation with pilot banks on formula accuracy

**Timeline realism:**
- 5 epics at 4-6 weeks per epic = 20-30 weeks for MVP (reasonable for fintech)
- Parallel workstreams possible: Frontend team on Epic 2 UI while backend builds Epic 1
- **Reduced risk:** Mock calculation approach eliminates bank API dependency; can launch MVP without waiting for banks to develop endpoints

### Technical Readiness

**Clarity of technical constraints:** High — Spring Boot, React/TypeScript, PostgreSQL, AWS clearly specified

**Identified technical risks:**
1. **Offer Calculation Formula Accuracy** — Needs validation with banks that simulated formulas match their website calculators; bank admins must configure rate cards accurately
2. **Bank Rate Card Configuration Complexity** — Banks may struggle to translate their calculator logic into rate card parameters; may need onboarding support
3. **Data Encryption Key Management** — AWS KMS setup requires DevOps expertise
4. **Email Deliverability** — SendGrid integration must be tested; Moldova ISPs may block transactional emails
5. **Database Performance at Scale** — PostgreSQL indexes critical for queue performance with 500+ applications

**Areas needing architect investigation:**
- Kafka vs. RabbitMQ for event bus (Story 3.3, 4.4) — architect should evaluate trade-offs
- Column-level encryption vs. application-level encryption (Story 5.7) — performance implications
- Redis caching strategy for offer calculations (Story 3.3) — architect should design caching layer

### Recommendations

**Actions to address blockers:** None (no blockers identified)

**Actions to address HIGH priority issues:**
1. **Schedule legal consultation** — Engage Moldovan attorney for privacy policy review (2-week timeline)
2. **Regulatory inquiry** — Submit NBM/CNPF inquiry letter requesting licensing clarification (template in Story 5.8)
3. **Data residency decision** — Stakeholder meeting to confirm EU vs. on-premise Moldova; impacts AWS setup

**Suggested improvements:**
1. **Add Story 0.0 (Epic 0):** "Bank Rate Card Onboarding Workshop" — Before Epic 3, conduct sessions with pilot banks to help them configure their calculator formulas as rate cards; validate simulated calculations match their website results
2. **Split Story 5.8:** Separate compliance checklist (automated) from regulatory submission package (manual) for clearer tracking
3. **Enhance Story 4.3:** Add "Bank can flag application for manual underwriting" to prepare Phase 2 formal approval workflow
4. **Add Story 3.2b (Optional):** "Rate Card Import Wizard" — Tool to help banks bulk-import rate configurations from spreadsheet if they have complex product matrices

**Next steps:**
1. ✅ PRD approved and ready for architect
2. → **Architect phase:** Design technical architecture, database schema, API contracts
3. → **Legal review:** Privacy policy and terms finalization (parallel with architect phase)
4. → **Regulatory inquiry:** Submit NBM/CNPF letter and await response (parallel, non-blocking)
5. → **Development kickoff:** Epic 1 begins after architecture complete

---

### Final Decision

**✅ READY FOR ARCHITECT** — The PRD and epics are comprehensive, properly structured, and ready for architectural design. Address HIGH priority items (legal review, regulatory inquiry) in parallel with architecture phase to avoid blocking development kickoff.

---

## Next Steps

### UX Expert Prompt

"Using the Credit Aggregator MVP PRD (docs/prd.md) as input, please initiate architecture design mode. Focus on:
1. Database schema design for all 5 epics (users, applications, offers, banks, compliance)
2. API contract design for borrower and bank endpoints
3. Spring Boot service architecture with domain boundaries
4. React component structure for borrower and bank portals
5. Security architecture (JWT, encryption, audit logging)
6. Deployment architecture (AWS, Docker, Kubernetes)

Address the technical investigation areas identified in the PRD checklist: event bus selection (Kafka vs. RabbitMQ), encryption strategy (column-level vs. application-level), and Redis caching design for offer calculations."
