# Epic 1: Foundation & User Authentication

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
