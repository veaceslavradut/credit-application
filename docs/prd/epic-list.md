# Epic List

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
