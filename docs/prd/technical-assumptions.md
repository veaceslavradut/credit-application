# Technical Assumptions

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
