# Epic 2: Borrower Application Workflow

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
