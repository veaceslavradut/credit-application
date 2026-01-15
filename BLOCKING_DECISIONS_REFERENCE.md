# BLOCKING DECISIONS & RESOLUTION REQUIREMENTS

**Project**: Credit Application Platform  
**Date**: 2026-01-15  
**Status**: 3 CRITICAL DECISIONS REQUIRED FOR FULL DEVELOPMENT

---

## BLOCKER SUMMARY

| Decision | Title | Impact | Stories Blocked | Priority | Est. Resolution |
|----------|-------|--------|-----------------|----------|-----------------|
| **Decision 2** | Offer Calculation Formula | 6 stories (13%) | 3.3, 3.4, 3.6, 3.7, 3.10, 4.4 | CRITICAL | 2-3 hours |
| **Decision 1** | Consent Types & Rules | 4 stories (9%) | 2.5, 4.3, 5.1, 5.8 | CRITICAL | 3-4 hours |
| **Decision 3** | KMS & Encryption | 1 story (2%) + Prod Deploy | 5.7 | CRITICAL | 4-5 hours |

**Total Blocked Stories**: 10 of 40 (25%)  
**Total Ready Stories**: 30 of 40 (75%)  
**Total Phase 2**: 4 of 40 (10%)

---

## DECISION 2: OFFER CALCULATION FORMULA SPECIFICATION

### Current Problem

**Story 3.3 AC (Vague)**:
`
"Monthly Payment = standard amortization formula"
"For MVP: simple adjustment = baseApr + (min adjustment if term > 120 months, else 0)"
`

**Story 4.4 AC (Vague)**:
`
"monthlyPayment recalculation when APR changes uses standard amortization formula"
`

**Issues**:
1. "Standard amortization formula" is undefined in AC
2. "Min adjustment if term > 120 months" - what IS the adjustment value?
3. Fee handling: are they deducted from principal BEFORE monthly payment calc or AFTER?
4. BigDecimal precision: how many decimals? ROUND_HALF_UP or other?
5. No test vectors to validate implementation

### Stories Affected

| Story | Dependency | Impact |
|-------|-----------|--------|
| 3.3 | **PRIMARY** | Core calculation logic - needs exact formula |
| 4.4 | 3.3 | Bank submission form - must use identical formula |
| 3.4 | 3.3 | Alternative scenarios - must recalculate with same formula |
| 3.6 | 3.3 | Offer comparison - must compare offers from same formula |
| 3.7 | 3.3 | Scenario analysis - must show what-if scenarios |
| 3.10 | 3.3 | Offer override - must recalculate monthly payment |

### Required Resolution

**Deliverable**: Decision 2 Document (1-2 pages)

**Content Required**:

#### 1. APR Adjustment Algorithm
`
Current State (Vague):
  baseApr + (adjustment if term > 120 months, else 0)

Required Definition:
  Example: baseApr + 0.25% if term > 120 months
  OR:      baseApr + (termMonths - 120)  0.01% if term > 120 months
  OR:      baseApr + min_adjustment (e.g., 0.25%) if term > 120 months

Output Should Include:
  - Exact formula with variables defined
  - Example calculations (e.g., 5-year term = 3.5%, 10-year term = 3.75%)
  - Adjustment percentage/algorithm
  - Why this algorithm (business rationale)
`

#### 2. Monthly Payment Formula
`
Loan Calculation Parameters:
  P = Principal (loanAmount after fee deduction)
  r = Monthly Interest Rate (APR / 12 / 100)
  n = Number of Months (termMonths)

Formula Options:
  Option A: M = [P  r  (1+r)^n] / [(1+r)^n - 1]
  Option B: M = P  [r(1+r)^n] / [(1+r)^n - 1]
  (Same formula, different notation)

Fee Handling:
  Option 1: Deduct fees from principal BEFORE calculation
    Effective Principal = loanAmount - originationFee - insuranceCost
    Monthly Payment = CALC(Effective Principal, APR, term)
  
  Option 2: Add fees to monthly payment
    Monthly Payment = CALC(loanAmount, APR, term) + (fees / term)

Output Should Include:
  - Chosen formula with full equation
  - Fee handling decision + rationale
  - Example: ,000 loan, 3.5% APR, 60 months, 2% orig fee, 0.5% insurance fee
    Expected Monthly Payment = $\_ (calculated value)
  - Example: ,000 loan, 3.0% APR, 180 months (same fees)
    Expected Monthly Payment = $\_ (calculated value)
`

#### 3. BigDecimal Precision Rules
`
Requirements:
  - APR storage: 5 decimals (e.g., 3.50000)
  - Monthly payment: 2 decimals (e.g., 1887.50)
  - Rounding rule: ROUND_HALF_UP (standard banking)
  - Intermediate calculations: preserve precision, round at final step

Output Should Include:
  - Rounding rules for each field
  - Example of rounding impact (e.g., 1887.4956 rounds to 1887.50)
`

#### 4. Test Vectors (Validation Cases)
`
Format: Input  Expected Output

Example Test Cases:
  1. ,000 loan, 60 months, 700 credit score
     Rate: 3.50% APR, Adjustment: 0% (< 120mo)
     Fees: ,000 origin,  insurance
     Expected: APR 3.50%, Monthly ,887.50, Total Cost ,850

  2. ,000 loan, 180 months, 750 credit score
     Rate: 3.00% APR, Adjustment: 0% (< 120mo)
     Fees: ,000 origin, ,250 insurance
     Expected: APR 3.00%, Monthly ,389.35, Total Cost ,083

  3. \,000 loan, 121 months (BOUNDARY), 650 credit score
     Rate: 4.50% APR, Adjustment: +0.25% ( 120mo)
     Fees: ,000 origin,  insurance
     Expected: APR 4.75%, Monthly .87, Total Cost ,385

  4. \,000 loan, 120 months (BOUNDARY), 650 credit score
     Rate: 4.50% APR, Adjustment: 0% (< 120mo)
     Fees: ,000 origin,  insurance
     Expected: APR 4.50%, Monthly .56, Total Cost ,147

Output Should Include:
  - 10 test vectors with expected values
  - Boundary cases (120/121 month adjustment)
  - Min/max loan amounts
  - Min/max credit scores
  - All APR adjustments visible in test results
`

#### 5. Regulatory Compliance Statement
`
Verify:
  - Formula compliant with lending regulations (if applicable)
  - APR calculation complies with TILA/RESPA (if US-based)
  - No discriminatory adjustments based on protected class
  - Documentation of any assumptions or simplifications

Output Should Include:
  - Legal review checklist
  - Compliance statement
  - Any regulatory exceptions or special handling
`

### Timeline
- **Owner**: Finance/Business Lead + Product Manager
- **Reviewers**: Architect, Lead Developer (3.3), Lead Developer (4.4)
- **Creation**: 2-3 hours
- **Review**: 1 hour
- **Total**: 3-4 hours
- **Start**: Today
- **Deadline**: Friday EOD (Week 4)

### Success Criteria
- [ ] APR adjustment algorithm fully specified with examples
- [ ] Monthly payment formula with fee handling clearly documented
- [ ] Precision rules defined for all numerical fields
- [ ] 10 test vectors created with expected outputs
- [ ] Legal/regulatory compliance verified
- [ ] Stories 3.3, 4.4 updated with formula references
- [ ] All 4 affected teams briefed on formula

---

## DECISION 1: CONSENT TYPES & ENFORCEMENT RULES

### Current Problem

**Story 5.1 AC (Incomplete)**:
`
"Consent types: DATA_COLLECTION, BANK_SHARING, MARKETING, ESIGNATURE (Phase 2)"
"Cannot submit application without certain consents"
`

**Issues**:
1. Which consents are "certain" (mandatory)?
2. Can borrower proceed without MARKETING?
3. Can they revoke consent after submitting application?
4. What about ESIGNATURE (Phase 2) - required for initial application?
5. Form UX not specified (order, presentation, required vs optional indication)
6. GDPR compliance not verified

### Stories Affected

| Story | Dependency | Impact |
|-------|-----------|--------|
| 5.1 | **PRIMARY** | Consent management - needs spec of types and rules |
| 2.5 | 5.1 | Submit application - must verify required consents |
| 4.3 | 5.1 | Bank preferences - must define which consents required |
| 5.8 | 5.1 | Compliance reporting - must report consent metrics |

### Required Resolution

**Deliverable**: Decision 1 Document (2-3 pages)

**Content Required**:

#### 1. Consent Types Definition
`
Data Element for Each Type:
  Name
  GDPR Lawful Basis (Contract/Consent/Legitimate Interest)
  Required/Optional (for initial application)
  Description (user-facing)
  Retention Period
  Revocation Handling

Template:
  DATA_COLLECTION
     Lawful Basis: Contract (necessary for loan application)
     Required: YES (mandatory for application submission)
     Description: "Collect personal and financial information to process your application"
     Retention: 7 years (regulatory requirement)
     Revocation: Cannot revoke if application submitted

  BANK_SHARING
     Lawful Basis: Contract (necessary for bank to review application)
     Required: YES (mandatory for application submission)
     Description: "Share your application data with lending institutions for underwriting"
     Retention: 7 years (regulatory requirement)
     Revocation: Cannot revoke if application submitted

  MARKETING
     Lawful Basis: Consent (optional, for marketing communications)
     Required: NO (optional)
     Description: "Send promotional materials and service updates"
     Retention: Until revoked (GDPR right to withdraw)
     Revocation: Can revoke anytime

  ESIGNATURE (Phase 2)
     Lawful Basis: Contract (necessary for electronic signature)
     Required: YES (for offer acceptance phase)
     Description: "Use electronic signature for loan documents"
     Retention: 7 years (regulatory requirement)
     Revocation: Cannot revoke after offer accepted
`

#### 2. Enforcement Rules
`
Submission Rules (Story 2.5):
  Required Consents Before Submission: DATA_COLLECTION + BANK_SHARING
  Optional Consents: MARKETING (can be unchecked)
  Can Proceed Without Optional?: YES

Revocation Rules:
  DATA_COLLECTION + BANK_SHARING: Cannot revoke after SUBMITTED status
  MARKETING: Can revoke anytime
  ESIGNATURE: Cannot revoke after ACCEPTED status

Example Enforcement Code:
  if (!consentService.isConsentGiven(borrowerId, DATA_COLLECTION)) {
    throw ValidationException("DATA_COLLECTION consent required")
  }
  if (!consentService.isConsentGiven(borrowerId, BANK_SHARING)) {
    throw ValidationException("BANK_SHARING consent required")
  }
  // MARKETING optional, no check needed
  // ESIGNATURE Phase 2, skip for now
`

#### 3. User Experience Flow
`
Screen 1: Mandatory Consents
  Title: "Required Consents"
  Description: "These consents are required to process your loan application"
  
  [  ] DATA_COLLECTION (CHECKED, CANNOT UNCHECK)
        "Collect personal and financial information..."
  
  [  ] BANK_SHARING (CHECKED, CANNOT UNCHECK)
        "Share your data with lending institutions..."
  
  Buttons: [Previous] [Next ]

Screen 2: Optional Consents
  Title: "Communication Preferences"
  Description: "We'd like to send you promotions and updates (optional)"
  
  [ ] MARKETING (UNCHECKED BY DEFAULT, CAN CHECK)
      "Send promotional materials and service updates"
  
  Buttons: [ Back] [Submit Application]

Compliance Notes:
  - Mandatory consents pre-checked and disabled
  - Optional consents unchecked by default
  - Each consent shows description/summary
  - "Submit Application" button disabled until mandatory consents shown
`

#### 4. Regulatory Compliance Validation
`
GDPR Article 7 Checklist:
  [ ] Consent freely given (not coerced)
    - Mandatory consents: no choice given (required for contract)
    - Optional consents: explicit opt-in required
  
  [ ] Consent specific (not bundled)
    - Separate checkboxes for each type
    - Each has own description
    - Not bundled as single "I agree" checkbox
  
  [ ] Consent informed (clear information)
    - User sees what data is collected (Story 5.2)
    - User sees how data is used
    - User sees retention period
  
  [ ] Consent unambiguous (clear intent)
    - Clear checkbox = clear consent
    - Not hidden in T&C fine print
  
  [ ] Consent withdrawable (easy revocation)
    - Offer revocation option in account settings
    - For non-contract consents (MARKETING)

Output Should Include:
  - GDPR Article 7 compliance checklist (checked)
  - Privacy policy update requirements
  - Consent form legal review by counsel
`

#### 5. Data Retention & Deletion Rules
`
Consent Record Retention:
  Duration: 7 years (regulatory requirement for loan records)
  Deletion: Cannot delete until 7 years after loan closed
  GDPR Right-to-Be-Forgotten: Honored with 7-year retention exception

Example Handling:
  if (consentGrantedDate + 7_years < today) {
    allowDeletion = true; // Safe to delete old consent records
  }
  
  if (borrowerDeleteAccount && today < consentGrantedDate + 7_years) {
    softDelete consent records; // Mark deleted, keep data
    // Explain to borrower: "Consent records retained for 7 years per law"
  }
`

### Timeline
- **Owner**: Compliance Officer / Legal + Product Manager
- **Reviewers**: Architect, Privacy Officer, Customer Success
- **Creation**: 3-4 hours
- **Review**: 1-2 hours (legal review)
- **Total**: 4-6 hours
- **Start**: Today
- **Deadline**: Friday EOD (Week 4)

### Success Criteria
- [ ] Consent types defined with lawful basis
- [ ] Mandatory vs optional clearly designated
- [ ] Enforcement rules specified for all stories
- [ ] UX flow documented with wireframes
- [ ] Revocation rules clear
- [ ] GDPR Article 7 compliance verified
- [ ] Stories 2.5, 4.3, 5.1, 5.8 updated with consent references
- [ ] All 3 affected teams briefed on consent rules

---

## DECISION 3: KEY MANAGEMENT & ENCRYPTION STRATEGY

### Current Problem

**Story 5.7 AC (Incomplete)**:
`
"Encrypt sensitive data (SSN, bank accounts) with AES-256"
"Must use external key management service"
`

**Issues**:
1. Which KMS to use? (AWS, Vault, Azure, GCP?)
2. How to authenticate with KMS?
3. Key naming convention not specified
4. Key rotation policy not specified
5. How to handle key rotation (re-encrypt old records?)
6. Disaster recovery plan not defined
7. Infrastructure requirements not specified

### Stories Affected

| Story | Dependency | Impact |
|-------|-----------|--------|
| 5.7 | **PRIMARY** | Encryption - needs KMS selection and strategy |
| Prod Deploy | 5.7 | Production deployment blocked without encryption |

### Required Resolution

**Deliverable**: Decision 3 Document + Infrastructure Plan (3-4 pages)

**Content Required**:

#### 1. KMS Selection
`
Options:

AWS KMS:
   Pros: Integrated with AWS, automatic audit logging, HA
   Cons: AWS lock-in, cost per operation, requires AWS account
  Best For: AWS-native infrastructure

HashiCorp Vault:
   Pros: Cloud-agnostic, on-premise capable, excellent audit
   Cons: Operational overhead, self-hosted complexity, no automatic HA
  Best For: Multi-cloud or on-premise deployments

Azure Key Vault:
   Pros: Azure integration, good audit logging
   Cons: Azure lock-in, requires Azure AD authentication
  Best For: Azure-native infrastructure

GCP Cloud KMS:
   Pros: GCP integration, good audit logging
   Cons: GCP lock-in, cost per operation
  Best For: GCP-native infrastructure

RECOMMENDATION:
  [ ] AWS KMS (if using AWS infrastructure)
  [ ] HashiCorp Vault (if multi-cloud or on-premise)
  
Selection Criteria:
  1. Current infrastructure (AWS/Azure/GCP/On-premise)?
  2. Budget for KMS operations?
  3. Multi-cloud strategy?
  4. Operational expertise (Vault vs AWS)?
`

#### 2. Key Management Strategy
`
Key Naming Convention:
  /credit-app/{environment}/{entity-type}/{field-name}/{version}
  
  Examples:
    /credit-app/prod/borrower/ssn/1
    /credit-app/prod/borrower/date_of_birth/1
    /credit-app/prod/bank_account/account_number/1
    /credit-app/prod/bank_account/routing_number/1
    /credit-app/prod/borrower/income/1

Key Rotation Policy:
  Frequency: Monthly (automated)
  Trigger: Calendar-based or on-demand
  Process:
    1. New key created in KMS (/credit-app/prod/borrower/ssn/2)
    2. Application uses new key for all new encryptions
    3. Old key retained for decryption of old records
    4. Optionally: background job re-encrypts old records (expensive)

Key Retention:
  Active Key: Current version only
  Previous Keys: Keep for 2 years (data retention requirement)
  Purge: Delete keys older than 2 years (if no data uses them)

Per-Record Encryption:
  Strategy: Each record encrypted with unique key
  Benefit: Better security (compromise of one key doesn't expose all records)
  Implementation: Key ID stored with encrypted data (key_version field)
  
  Example:
    borrower_ssn = {
      encrypted_value: "...",
      key_version: 1,
      algorithm: "AES-256-GCM",
      iv: "..."
    }
`

#### 3. Implementation Details
`
Encryption Algorithm:
  Algorithm: AES-256-GCM (Authenticated Encryption with Associated Data)
  Why GCM: Built-in authentication, prevents tampering
  Not CBC: Avoid padding oracle attacks, less secure than GCM
  Library: Tink (Google) - don't use low-level JCE

Sensitive Fields to Encrypt:
  Borrower:
    - SSN
    - Date of Birth
    - Bank Account SSN (if captured separately)
  
  Bank Account:
    - Account Number
    - Routing Number
  
  Financial:
    - Income
    - Employment Info (if captured)
  
  Loan Application:
    - Co-borrower SSN (if applicable)

Example Code Pattern:
  // Encrypt on write
  String encryptedSSN = encryptionService.encrypt(
    plaintext: borrower.ssn,
    fieldName: "borrower_ssn",
    borrowerId: borrower.id
  );
  // encryptionService calls KMS to get encryption key,
  // encrypts plaintext, returns base64-encoded ciphertext
  
  // Decrypt on read
  String plainSSN = encryptionService.decrypt(
    ciphertext: dbRecord.ssn,
    fieldName: "borrower_ssn",
    borrowerId: borrower.id
  );
  // encryptionService calls KMS to get decryption key,
  // decrypts ciphertext, returns plaintext
`

#### 4. Disaster Recovery Plan
`
Scenario 1: KMS Unavailable (network outage)
  RTO (Recovery Time Objective): 30 minutes
  RPO (Recovery Point Objective): 5 minutes
  Strategy:
    - Cache encryption keys in memory (5-min TTL)
    - Can read encrypted data with cached keys
    - Cannot write if KMS unavailable (fail fast)
    - Retry with exponential backoff

Scenario 2: Key Corruption/Loss
  Prevention:
    - KMS handles backup automatically (AWS, Azure, GCP)
    - Vault with automatic snapshots
  Recovery:
    - Restore from backup
    - Re-key application (assign new key version)
    - Timeline: < 1 hour

Scenario 3: Encryption Key Compromised
  Response:
    - Immediately rotate key (create new version)
    - Mark old key as compromised (audit log)
    - Optional: re-encrypt old data with new key (expensive, background job)
    - Set 30-day deadline for re-encryption

Backup Strategy:
  Encrypted Data: Standard database backups
  Encryption Keys: Handled by KMS (automatic)
  Key Backup Access: Only KMS service, not application

Testing:
  [ ] Test KMS unavailability (network partition)
  [ ] Test key rotation
  [ ] Test decryption of old keys
  [ ] Load test encryption performance (latency <50ms per operation)
`

#### 5. Infrastructure Requirements
`
For AWS KMS:
  - Requires AWS Account
  - IAM roles for application to call KMS
  - CloudWatch for monitoring
  - Cost: ~/key/month + .03 per 10k requests

For HashiCorp Vault:
  - Vault cluster (3+ nodes for HA)
  - PostgreSQL backend for storage
  - TLS certificates for all communication
  - Operational team to manage Vault updates
  - Cost: Self-hosted (only infra costs) or managed (Vault Cloud)

Common to Both:
  - Application code changes (EncryptionService)
  - Database schema change (add key_version field to encrypted records)
  - Monitoring & alerting (KMS failures)
  - Audit logging (all key access)
  - Key access policies (only app can read keys)
  - Compliance: FedRAMP, SOC2, PCI-DSS if needed
`

### Timeline
- **Owner**: Security Lead / Infrastructure Lead
- **Reviewers**: Architect, DevOps, Compliance Officer
- **Creation**: 4-5 hours
- **Review**: 1-2 hours (security review)
- **Infrastructure Setup**: 2-3 days (parallel with Week 2-4 development)
- **Total**: 5-7 hours + 2-3 days infra
- **Start**: Today
- **Deadline**: Friday EOD (Week 4)
- **Infrastructure Ready**: End of Week 5 (before Story 5.7 coding begins)

### Success Criteria
- [ ] KMS selected (AWS/Vault/Azure/GCP with justification)
- [ ] Key naming convention defined
- [ ] Key rotation policy documented
- [ ] Key retention rules specified
- [ ] Encryption algorithm specified (AES-256-GCM)
- [ ] Sensitive fields list defined
- [ ] Disaster recovery plan documented (RTO/RPO)
- [ ] Infrastructure requirements specified
- [ ] Cost estimates provided
- [ ] Story 5.7 updated with KMS references
- [ ] Infrastructure setup started (parallel with Phase 2-3)
- [ ] All teams briefed on encryption strategy

---

## ACTION ITEMS SUMMARY

### By End of Day Today
- [ ] Notify decision owners (Finance lead for Decision 2, Compliance for Decision 1, Security for Decision 3)
- [ ] Schedule kick-off meetings for each decision (1 hour each)
- [ ] Create decision tracking issue in JIRA

### By Tomorrow EOD
- [ ] Decision 1 first draft (consent types list)
- [ ] Decision 2 first draft (formula outline)
- [ ] Decision 3 first draft (KMS options)

### By Thursday EOD
- [ ] All 3 decisions reviewed by primary stakeholders
- [ ] Feedback incorporated
- [ ] Legal/Compliance review complete (Decision 1)
- [ ] Architect review complete (Decision 3)

### By Friday EOD
- [ ] All 3 decision documents final and published
- [ ] All teams briefed on decisions
- [ ] Affected stories updated with decision references
- [ ] Phase 2, 3A, 3B development begins (using ready stories)

---

**Report Prepared By**: GitHub Copilot (Scrum Master)  
**Date**: 2026-01-15  
**Status**:  3 CRITICAL DECISIONS PENDING - Development Team Ready to Start on 30 Ready Stories

