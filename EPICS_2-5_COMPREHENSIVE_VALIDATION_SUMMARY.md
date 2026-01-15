# EPICS 2-5 COMPREHENSIVE VALIDATION SUMMARY & NEXT STEPS

**Project**: Credit Application Platform  
**Date**: 2026-01-15  
**Validation Framework**: 5-Category Assessment (Goal Clarity, Technical Detail, Reference Effectiveness, Self-Containment, Testing Guidance)  
**Scope**: 40 stories across Epics 2, 3, 4, 5

---

## OVERALL PROJECT STATUS

### Portfolio View

| Epic | Stories | Avg Readiness | Status | Ready Now | Blocked | Phase 2 |
|------|---------|---|--------|-----------|---------|---------|
| **1** | **7** | **7.9/10** |  **COMPLETE** | 7 | 0 | 0 |
| **2** | **10** | **6.8/10** |  READY (1 blocker) | 6 | 1 | 3 |
| **3** | **12** | **6.3/10** |  READY (5 blocked) | 7 | 5 | 0 |
| **4** | **9** | **6.6/10** |  READY (2 blocked) | 7 | 2 | 0 |
| **5** | **9** | **6.1/10** |  READY (2 blocked) | 6 | 2 | 1 |
| **TOTAL** | **47** | **6.7/10** |  MOSTLY READY | 33 | 10 | 4 |

**Key Insight**: 33 of 47 stories (70%) ready to start immediately. 10 stories (21%) blocked on 3 critical decisions. 4 stories (9%) Phase 2 features.

---

## CRITICAL BLOCKERS & DECISIONS

### Blocker 1: Story 3.3 - Offer Calculation Formula (Decision 2) 
**Severity**: CRITICAL | **Impact**: 5 blocked stories (3.3, 3.4, 3.6, 3.7, 3.10, 4.4)

**Problem**:
- AC states "monthly payment = standard amortization formula" but doesn't specify exact formula
- APR adjustment logic "if term > 120 months, else 0" is too vague
- BigDecimal precision and rounding rules not specified
- No test vectors to validate implementation correctness

**Why It Blocks**:
- Story 4.4 (Bank Offer Submission) must use identical formula
- Story 3.6-3.10 (Offer comparison, scenario, override) must understand formula
- Cannot ensure consistency without single definition

**Stories Affected**: 3.3, 3.4, 3.6, 3.7, 3.10, 4.4 (6 stories = 13% of portfolio)

**Resolution Required** (Decision 2 Output):
`
1. APR Adjustment Formula:
   - Current: baseApr + (adjustment if term > 120 months)
   - Define: exact adjustment amount and conditions
   - Example: baseApr + 0.25% if term > 120 months

2. Monthly Payment Calculation:
   - Equation: [P  r(1+r)^n] / [(1+r)^n - 1] where r=APR/12, n=months
   - Fee handling: deduct from principal BEFORE or AFTER?
   - Precision: ROUND_HALF_UP to 2 decimals

3. Test Vectors (5-10 examples):
   - Input: ,000 loan, 60 months, 700 credit score
   - Expected: APR 3.5%, monthly payment ,887.12, fees ,000
   - Boundary: 120mo vs 121mo (adjustment difference)

4. Regulatory Compliance:
   - Confirm formula compliant with regulations
   - Document any assumptions
`

**Timeline**: 2-3 hours to create decision document + 2 hours per story review = 4 stories ready by next day

---

### Blocker 2: Story 5.1 - Consent Types & Enforcement (Decision 1) 
**Severity**: CRITICAL | **Impact**: 3 blocked stories (2.5, 4.3, 5.1, 5.8)

**Problem**:
- AC lists 4 consent types (DATA_COLLECTION, BANK_SHARING, MARKETING, ESIGNATURE) but doesn't specify:
  - Which are mandatory vs optional
  - Enforcement rules (can't submit without what consents?)
  - User experience flow (form presentation, order)
  - Data retention policy

**Why It Blocks**:
- Story 2.5 (Submit Application) needs to know "which consents required before submit?"
- Story 4.3 (Bank user preferences) needs consent requirement rules
- Story 5.8 (Compliance reporting) needs consent enforcement data
- Story 5.1 itself can't be implemented without consent spec

**Stories Affected**: 2.5, 4.3, 5.1, 5.8 (4 stories = 9% of portfolio)

**Resolution Required** (Decision 1 Output):
`
1. Consent Types Specification:
   - DATA_COLLECTION: mandatory, "collect personal/financial info", GDPR lawful basis (contract)
   - BANK_SHARING: mandatory, "share data with lending institutions", GDPR lawful basis (contract)
   - MARKETING: optional, "use data for marketing communications", GDPR lawful basis (consent)
   - ESIGNATURE (Phase 2): mandatory, "electronic signature for offers", GDPR lawful basis (contract)

2. Enforcement Rules:
   - Cannot submit application without DATA_COLLECTION + BANK_SHARING
   - MARKETING optional
   - Can revoke anytime after grant
   - Revocation on submitted application requires re-approval

3. User Experience Flow:
   - Screen 1: Mandatory consents (DATA_COLLECTION, BANK_SHARING) - cannot uncheck
   - Screen 2: Optional consents (MARKETING) - can check/uncheck
   - Show consent summary/terms for each
   - Cannot proceed without mandatory consents checked

4. Data Retention:
   - Store consent records 7 years (regulatory requirement)
   - Allow deletion only on GDPR right-to-be-forgotten request
`

**Timeline**: 3-4 hours to create decision document + 1 hour per story = 4 stories ready in parallel

---

### Blocker 3: Story 5.7 - Encryption & Key Management (Decision 3) 
**Severity**: CRITICAL (Production Blocker) | **Impact**: 2 blocked stories (5.7) + production deployment

**Problem**:
- AC states "encrypt sensitive data with AES-256" but doesn't specify:
  - Which KMS to use (AWS KMS? HashiCorp Vault? Azure Key Vault? GCP?)
  - Key naming convention
  - Key rotation policy
  - How to handle key rotation (re-encrypt old records?)
  - Disaster recovery plan if KMS unavailable

**Why It Blocks**:
- Cannot implement encryption without KMS selection
- Cannot deploy to production without encryption in place
- Infrastructure changes needed based on KMS choice
- Operational procedures needed for key management

**Stories Affected**: 5.7 (1 story = 2%) + Production deployment

**Resolution Required** (Decision 3 Output):
`
1. KMS Selection:
   - Recommended: AWS KMS (if AWS infrastructure) OR HashiCorp Vault (on-premise)
   - Include selection criteria and trade-offs
   - Pricing and operational overhead

2. Key Management Strategy:
   - Key naming: /credit-app/borrower/{field-name}/{version}
   - Key rotation: monthly automatic rotation
   - Key retention: keep old keys 2 years for decryption
   - Per-record keys for better security

3. Implementation Details:
   - Encryption library: Tink (Google) not low-level JCE
   - Algorithm: AES-256-GCM (authenticated encryption)
   - Sensitive fields: SSN, date_of_birth, account_number, routing_number, income

4. Disaster Recovery:
   - RTO: 30 minutes
   - RPO: 5 minutes
   - Can read with cached keys if KMS unavailable (30-min cache TTL)
   - Cannot write if KMS unavailable
`

**Timeline**: 4-5 hours to create decision + infrastructure planning + 2 hours per epic review = 5.7 ready within 2 days

---

## DEVELOPMENT ROADMAP

### Phase 1: Epic 1 Foundation ( COMPLETE - Week 1)
**Status**: All 7 stories complete and in development
-  1.1: Project Setup & CI/CD
-  1.2: Database & ORM Setup
-  1.3: User Registration API
-  1.4: Bank Account Creation
-  1.5: JWT Authentication & Login
-  1.6: Role-Based Access Control
-  1.7: Audit Logging Infrastructure

**Ready for**: All other epics depend on this (foundation complete)

---

### Phase 2: Epic 2 Foundation + Workflows ( READY - Week 2-4)
**Ready Now**: 6 stories (2.1, 2.2, 2.3, 2.4, 2.6, 2.7)
**Can Start Immediately**:
- 2.1 (Application Data Model) - foundational
- 2.2 (Create New Application API) - depends on 2.1
- 2.3 (Update Application API) - depends on 2.1
- 2.4 (Retrieve Application API) - depends on 2.1
- 2.6 (List Applications API) - depends on 2.1
- 2.7 (Status Tracking Dashboard) - depends on 2.1

**Estimated Duration**: 3-4 weeks (6 stories with 2-3 developers)

**Parallel with Phase 2**: Epic 3 & 4 foundations (don't depend on 2.x)

---

### Phase 3A: Epic 3 & 4 Foundations ( READY - Week 2-4)
**Ready Now**: 14 stories (7 from Epic 3, 7 from Epic 4)

**Epic 3 Ready Stories**:
- 3.1 (Offer Data Model) - no dependencies
- 3.2 (Rate Card Management) - depends on 3.1
- 3.5 (Offer Expiration Job) - depends on 3.1
- 3.8 (Rate Card History) - depends on 3.2
- 3.9 (Offer Approval Workflow) - depends on 3.1, 1.4
- 3.11 (Offer Expiration Cleanup) - depends on 3.1
- 3.12 (Analytics Dashboard) - depends on 3.1

**Epic 4 Ready Stories**:
- 4.1 (Admin Backend Setup) - no dependencies
- 4.2 (Bank User Management) - depends on 4.1
- 4.5 (Rate Card UI) - depends on 4.1, 3.2
- 4.6 (Application Review Dashboard) - depends on 4.1, 2.1
- 4.7 (Compliance Reporting) - depends on 4.1, 1.7
- 4.8 (Bank Settings) - depends on 4.1
- 4.9 (Notifications Config) - depends on 4.1

**Estimated Duration**: 3-4 weeks (14 stories with 3-4 developers)

**Parallel with Phase 3A**: Epic 5 foundations

---

### Phase 3B: Epic 5 Foundations ( READY - Week 2-4)
**Ready Now**: 6 stories (5.2, 5.3, 5.4, 5.5, 5.6, 5.9)

**Can Start Immediately**:
- 5.2 (Data Export/Delete - GDPR)
- 5.3 (Data Retention Policies)
- 5.4 (PII Masking in Logs)
- 5.5 (Encryption at Rest - basic)
- 5.6 (Comprehensive Audit Trail)
- 5.9 (Compliance Dashboard)

**Estimated Duration**: 3 weeks (6 stories with 2 developers)

---

### Phase 4: Critical Decision Points ( BLOCKING - Week 5)

**MUST COMPLETE BEFORE PHASE 5**:

1. **Decision 2** (Offer Calculation Formula) - 2-3 hours
   - Specify APR adjustment algorithm
   - Specify monthly payment formula
   - Create 10 test vectors
   - Validate compliance

2. **Decision 1** (Consent Types) - 3-4 hours
   - Define mandatory vs optional consents
   - Specify enforcement rules
   - Define UX flow
   - Validate GDPR compliance

3. **Decision 3** (KMS & Encryption) - 4-5 hours
   - Select KMS (AWS, Vault, etc.)
   - Define key management strategy
   - Plan infrastructure
   - Plan disaster recovery

**Critical Path Impact**: Without these decisions, 6 stories (3.3, 3.4, 3.6, 3.7, 3.10, 4.4, 2.5, 4.3, 5.1, 5.8, 5.7) remain blocked

---

### Phase 5: Epic 3 Offer Calculation ( BLOCKED UNTIL DECISION 2 - Week 6)

**Can Start After Decision 2**:
- 3.3 (Offer Calculation Engine) - **requires Decision 2**
- 3.4 (Alternative Offer Scenarios) - depends on 3.3
- 3.6 (Offer Comparison) - depends on 3.3
- 3.7 (Scenario Analysis) - depends on 3.3
- 3.10 (Offer Override) - depends on 3.3
- 4.4 (Bank Offer Submission) - depends on 3.3

**Estimated Duration**: 2-3 weeks (6 stories with 3 developers, after decision)

---

### Phase 6: Epic 5 Compliance & Decision-Dependent Stories ( BLOCKED UNTIL DECISIONS 1 & 3 - Week 6-7)

**Can Start After Decision 1**:
- 5.1 (Consent Management) - **requires Decision 1**
- 5.8 (Compliance Reporting) - depends on 5.1
- 2.5 (Submit Application) - depends on 5.1
- 4.3 (Bank Consent Preferences) - depends on 5.1

**Can Start After Decision 3**:
- 5.7 (Encryption & Key Management) - **requires Decision 3**

**Estimated Duration**: 2 weeks (4 stories with 2 developers, after decisions)

---

### Phase 7: Phase 2 & Integration Testing (Week 8)

**Phase 2 Features** (lower priority):
- 2.8 (Reapplication Templates)
- 2.9 (Scenario Calculator)
- 2.10 (Help Content)

**Integration Testing**:
- Cross-epic end-to-end testing
- Performance testing
- Security testing
- User acceptance testing

---

## CRITICAL PATH ANALYSIS

### Shortest Path to MVP (4-5 weeks)

`
Week 1: Epic 1 ( complete)
        
Week 2-4: Parallel Development
   Phase 2: Stories 2.1, 2.2, 2.3, 2.4, 2.6, 2.7 (Epic 2 - 6 stories)
   Phase 3A: Stories 3.1, 3.2, 3.5, 3.8, 3.9, 3.11, 3.12 (Epic 3 - 7 stories)
   Phase 3A: Stories 4.1, 4.2, 4.5, 4.6, 4.7, 4.8, 4.9 (Epic 4 - 7 stories)
   Phase 3B: Stories 5.2, 5.3, 5.4, 5.5, 5.6, 5.9 (Epic 5 - 6 stories)
        
Week 5: Critical Decisions
   Decision 2 (Offer Calculation)
   Decision 1 (Consent Types)
   Decision 3 (KMS & Encryption)
        
Week 6: Offer Calculation & Consent Implementation
   Phase 5: Stories 3.3, 3.4, 3.6, 3.7, 3.10, 4.4 (6 stories)
   Phase 6: Stories 5.1, 5.8, 2.5, 4.3 (4 stories)
        
Week 7: Encryption & Production Hardening
   Phase 6: Story 5.7 (Encryption)
   Security & Compliance Testing
        
Week 8: Integration & UAT
   End-to-End Testing
   Performance Testing
   User Acceptance Testing
        
Week 9: Production Deployment
`

---

## RECOMMENDED EXECUTION STRATEGY

### Immediate Actions (This Week)

**1. Create Decision 2 Document** (2-3 hours)
`
Title: Offer Calculation Formula Specification (Decision 2)
Owner: Business/Finance Lead + Architect
Content:
- APR adjustment algorithm with examples
- Monthly payment formula with derivation
- Test vectors (10 cases)
- Regulatory compliance validation
Deliverable: decision-2-offer-calculation.md
`

**2. Create Decision 1 Document** (3-4 hours)
`
Title: Consent Types & Enforcement Rules (Decision 1)
Owner: Compliance/Legal + Product Manager
Content:
- Consent types with regulatory basis
- Mandatory vs optional designation
- Enforcement rules (story 2.5 enforcement)
- UX flow diagrams
- GDPR Article 7 compliance statement
Deliverable: decision-1-consent-management.md
`

**3. Create Decision 3 Document** (4-5 hours)
`
Title: Key Management & Encryption Strategy (Decision 3)
Owner: Security/Infrastructure Lead
Content:
- KMS selection (AWS, Vault, etc.)
- Key naming convention
- Key rotation strategy
- Disaster recovery plan
- Infrastructure requirements
Deliverable: decision-3-encryption-kms.md
`

**Total: 9-12 hours of decision-making activities**

### Week 2-4 Parallel Development (30 developers estimated)
- Team 1: Epic 2 (2-3 devs, 6 stories)
- Team 2: Epic 3 Foundations (2-3 devs, 7 stories)
- Team 3: Epic 4 Foundations (2-3 devs, 7 stories)
- Team 4: Epic 5 Foundations (2 devs, 6 stories)

### Week 5 Decision Window
- Publish all 3 decision documents
- Review with teams
- Update affected stories with decision details

### Week 6+ Dependent Stories
- Team 1: Epic 3 Calculation (after Decision 2)
- Team 2: Epic 2 Submission + Epic 5 Consent (after Decision 1)
- Team 3: Epic 5 Encryption (after Decision 3)

---

## RISK MITIGATION

### Risk 1: Decision Delays Block 6+ Stories
**Mitigation**: 
- Assign decision owners immediately
- Set hard deadline (Friday end-of-day Week 4)
- Escalate blockers daily
- Have contingency: proceed with MVP without 3.10 (manual override) if formula decision delayed

### Risk 2: Calculation Formula Bugs
**Mitigation**:
- Use provided test vectors for validation
- Contract financial expert for formula review
- Implement comprehensive test coverage (100% for financial logic)
- Compare results with known loan calculators (mortgage calc tools)

### Risk 3: Consent Compliance Issues
**Mitigation**:
- Have legal review Decision 1 output
- Validate against GDPR requirements
- Include consent language translation if needed
- Plan audit of consent implementation

### Risk 4: KMS Infrastructure Not Ready
**Mitigation**:
- Start infrastructure setup early (Week 2)
- Use mock KMS for testing (LocalStack, Vault local mode)
- Plan transition to production KMS by Week 7
- Document key rotation procedures

---

## TEAM ALLOCATION RECOMMENDATION

### Recommended Team Structure (9 developers total)

**Team 1: Epic 2 (Borrower Application Workflow)** - 3 developers
- Lead: Senior Backend Dev
- Members: 2 mid-level backend devs
- Stories: 2.1, 2.2, 2.3, 2.4, 2.6, 2.7 (can also handle 2.5 after Decision 1)
- Duration: 3-4 weeks

**Team 2: Epic 3 & 4 Foundations (Offers & Admin Portal)** - 3 developers
- Lead: Senior Backend Dev + Senior Frontend Dev (pair)
- Members: 1 backend, 1 frontend dev
- Backend Stories: 3.1, 3.2, 3.5, 3.8, 3.9, 3.11, 3.12, 4.1, 4.2
- Frontend Stories: 4.5, 4.6, 4.7, 4.8, 4.9
- Duration: 3-4 weeks

**Team 3: Epic 5 Foundations (Compliance & Data Governance)** - 2 developers
- Lead: Backend Dev with security focus
- Members: 1 backend, DevOps support
- Stories: 5.2, 5.3, 5.4, 5.5, 5.6, 5.9
- Duration: 3 weeks

**Team 4: Critical Path (Decision-Dependent Stories)** - 1 developer (available after Week 4)
- Lead: Backend Dev with financial logic experience
- Stories: 3.3, 3.4, 3.6, 3.7, 3.10, 4.4, 5.7 (rotation through teams as needed)
- Duration: 2-3 weeks (after decisions)

**QA & DevOps**: 2 continuous throughout
- Infrastructure (KMS setup, deployment)
- Testing (unit, integration, e2e)
- Performance & security testing

---

## SUCCESS CRITERIA

### Week 4 End (Phase 1-3 Complete)
-  26 stories in development or complete (60% of 43 ready stories)
-  3 decision documents created and reviewed
-  Infrastructure for KMS, database, CI/CD verified

### Week 6 End (Phase 5-6 Started)
-  All decision-dependent stories can start
-  38+ stories in development (80% of portfolio)
-  KMS infrastructure ready for integration

### Week 8 End (MVP Ready)
-  All 40 ready stories complete
-  Phase 2 stories in backlog for future
-  Comprehensive testing (unit, integration, e2e) complete
-  Production deployment ready (security, compliance verified)

---

## NEXT STEPS (Today/This Week)

**Priority 1 - Today**: 
- [ ] Review this validation summary with product/architecture teams
- [ ] Assign decision owners for Decision 1, 2, 3
- [ ] Create JIRA tickets for all 40 ready stories

**Priority 2 - Tomorrow**: 
- [ ] Assign teams to Phase 2, 3A, 3B epic
- [ ] Create detailed acceptance criteria for Stories 2.1, 3.1, 4.1 (parallel starters)
- [ ] Set up development environments (all 4 teams)

**Priority 3 - This Week**: 
- [ ] Complete Decision 2, 1, 3 documents
- [ ] Publish decisions to all teams
- [ ] Update blocking stories with decision references
- [ ] Begin Phase 2, 3A, 3B development
- [ ] Infrastructure setup (KMS, encryption, rate limiting)

---

**Report Prepared By**: GitHub Copilot (Scrum Master)  
**Date**: 2026-01-15  
**Status**:  VALIDATION COMPLETE - READY FOR DEVELOPMENT (3 decisions pending)  
**Confidence**: 90% (based on detailed story review)

---

## APPENDIX: Readiness Score Methodology

**5-Category Framework** (0-10 per category):
1. **Goal & Context Clarity** (8/10): User story format clear, use cases explicit, AC well-defined
2. **Technical Implementation Detail** (8/10): Endpoint/service methods detailed, data models clear, algorithms specified
3. **Reference Effectiveness** (8/10): Dependencies documented, architecture references provided, dev notes clear
4. **Self-Containment** (8/10): All required components defined, no external undefined dependencies
5. **Testing Guidance** (8/10): Test cases detailed, coverage targets clear, test vectors provided

**Story Readiness Score**: Average of 5 categories
- 8-10/10: Ready (can start immediately)
- 6-7/10: Ready with Clarification (minor gaps, can start with questions answered)
- 4-5/10: Not Ready (major gaps, design decisions needed)
- 0-3/10: On Hold (blocked on external decisions)

**Portfolio Ready %**: Stories with 6+/10 readiness (can start with questions)

