# Epic 4: Bank Admin Portal & Offer Management

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
