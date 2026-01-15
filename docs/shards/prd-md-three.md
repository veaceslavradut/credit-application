 # Credit Aggregator MVP — PRD Shard (md-three)

 Source: [docs/prd.md](docs/prd.md)
 Date: 2026-01-13
 Status: Draft

 ---

 ## Goals and Background Context

 ### Goals
 - Single application → multiple bank offers in real time
 - Pilot bank value → measurable benefits to justify integration
 - Regulatory baseline → NBM/CNPF clarity established
 - Two-sided marketplace → borrower + bank accounts
 - Reliability targets → 70%+ submit rate; 95%+ preliminary offers

 ### Background Context
 - Moldova lacks centralized loan comparison; borrowers face friction and banks rely on costly acquisition.
 - MVP validates fit with 2–3 pilot banks, balancing borrower UX, bank ops value, and regulatory clarity.

 ### Change Log
 - 2026-01-13 v1.0 — Initial PRD from brief and brainstorming (John, PM)

 ---

 ## Requirements

 ### Functional Requirements
 - Borrower registration and account creation with minimal KYC
 - Tiered application submission (loan type, amount, term, etc.)
 - Real-time simulated preliminary offers from bank rate cards
 - Side-by-side comparison across 8 standardized metrics
 - Offer selection with non-binding intent; status tracking
 - Application history; re-apply via templates; scenario calculator
 - Bank org accounts; role-based access; application queue dashboard
 - Full application review; submit binding preliminary offers (8 fields)
 - Offer tracking from bank perspective; immutable audit logs; email notifications
 - Explicit consent pre-submission; e-sign readiness (Phase 2)

 ### Non-Functional Requirements
 - Encryption at rest/in transit; 99.5% uptime; <500ms calc; <30m time-to-offer
 - GDPR-aligned residency (EU/MD); password policy; immutable audit logs
 - API rate limiting; multi-currency; mobile responsiveness; legal review

 ---

 ## User Interface Design Goals

 ### Overall UX Vision
 - Speed and transparency; <5-minute application; trust via clarity; minimal yet complete.

 ### Key Interaction Paradigms
 - Progressive wizard; side-by-side comparison; intent-based next steps
 - Admin queue; real-time notifications

 ### Core Screens and Views
 - Borrower: Login/Registration; Dashboard; Application Wizard; Offer Comparison; Offer Detail; Status Tracker; Scenario Calculator; Settings
 - Bank: Login; Admin Dashboard; Application Queue; Review Panel; Offer Submission; Offer History & Analytics

 ### Accessibility Requirements
 - WCAG AA: keyboard navigation, contrast, labels, screen reader compatibility.

 ### Branding
 - Fintech palette (navy/green/white); sans-serif (Inter/Roboto); minimalist, clarity-first.

 ### Target Device and Platforms
 - Responsive web: Desktop (Chrome/Firefox/Safari), Mobile (iOS Safari 14+, Android Chrome 90+), Tablet.

 ---

 ## Technical Assumptions

 ### Repository Structure
 - Monorepo with Spring Boot backend, React/TS frontend, docs, scripts.

 ### Service Architecture
 - Monolith with domain boundaries: auth, borrower, bank, offers, notifications, compliance, shared.

 ### Testing Requirements
 - Pyramid: unit (80%+), integration, E2E; manual regression & compliance.

 ### Additional Assumptions
 - PostgreSQL + Redis; JWT auth; TLS 1.3; SendGrid/SMTP; event bus (Kafka/RabbitMQ); mock bank calc in MVP, API later.

 ### Deployment
 - Docker; Kubernetes/ECS optional; Terraform; AWS EU/Moldova residency.

 ### Offer Calculation Engine
 - Configurable rate cards; amortization calc; in-memory cache; <500ms.

 ### Monitoring & Observability
 - JSON logging; Prometheus/Grafana; optional tracing (Jaeger).

 ### Regulatory & Compliance
 - Encryption; PII key separation; immutable audit; GDPR rights (export/deletion).

 ---

 ## Epic List

 ### Epic 1: Foundation & User Authentication
 - Goal: Infra, registration, auth, RBAC, audit logging; deploy to staging.

 ### Epic 2: Borrower Application Workflow
 - Goal: Draft/submit, history, status updates, templates, listing.

 ### Epic 3: Preliminary Offer Calculation & Comparison
 - Goal: Configurable rate cards, mock calc, retrieval, comparison, selection, expiration.

 ### Epic 4: Bank Admin Portal & Offer Management
 - Goal: Dashboard, queue, review, submit/override offers, history, notifications, settings, analytics.

 ### Epic 5: Regulatory Compliance & Data Governance
 - Goal: Consent, privacy/terms, data export/deletion, immutable audit, e-sign readiness, encryption, compliance checklist.

 ---

 ## Checklist Summary
 - Completeness: 95%; Ready for Architecture; High-priority items: data residency, e-sign provider, legal review.

 ---

 ## Next Steps (Architecture Prompt)
 - Design DB schema (users, applications, offers, banks, compliance)
 - Define borrower/bank API contracts
 - Spring Boot services by domain; React component structure
 - Security: JWT, encryption, audit logging
 - Deployment: AWS + containers; evaluate event bus, encryption strategy, Redis caching

 ---

 (Shard generated from H1–H3 structure; detailed content remains in the source PRD.)