# Epic 5: Regulatory Compliance & Data Governance

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
