# Requirements

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
