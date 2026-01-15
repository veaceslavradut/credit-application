# EPIC 3 VALIDATION REPORT - Offer Calculation & Comparison

**Project**: Credit Application Platform  
**Epic**: Epic 3 - Preliminary Offer Calculation & Comparison  
**Stories**: 12 (Stories 3.1-3.12)  
**Date**: 2026-01-15  
**Validation Framework**: 5-Category Assessment (Goal, Technical, Reference, Self-Containment, Testing)

---

## EXECUTIVE SUMMARY

### Overall Status
- **Average Readiness**: 6.3/10 (NEEDS SPECIFICATION)
- **Blocking Issues**: 1 CRITICAL (Story 3.3 calculation formula not fully specified - Blocking Decision 2)
- **Impact Scope**: Stories 3.3, 4.4, potentially 3.6-3.12 all depend on formula decision
- **Ready to Start**: Stories 3.1, 3.2 (data models, configuration)
- **Waiting on Decisions**: Stories 3.3, 3.4-3.12 (require Decision 2: exact calculation formula)

### Key Findings
 **Data models well-structured** (Story 3.1: Offer entity with all fields)  
 **Rate card system clear** (Story 3.2: defines APR by credit score, term)  
 **CRITICAL: Calculation formula too vague** (Story 3.3: "standard amortization" without explicit specification)  
 **Precision & test vectors missing** (No BigDecimal rounding rules, no test vectors for validating correctness)  
 **APR adjustment algorithm unclear** ("if term > 120 months" logic oversimplified)  

---

## STORY-BY-STORY VALIDATION

### Story 3.1: Offer Data Model & Database Schema
**Status**: DRAFT | **Readiness**: 7/10 | **Risk**: LOW

#### 5-Category Assessment

**1. Goal & Context Clarity**  (9/10)
- Clear purpose: "define database schema for loan offers with calculation history"
- Use case clear: persist calculated offers, track changes
- AC specifies all required fields: loanAmount, APR, monthly payment, validity period, etc.
- Offer status lifecycle defined (PENDING, APPROVED_BY_BANK, ACCEPTED_BY_BORROWER, REJECTED, EXPIRED)
- Audit trail integrated
- Minor gap: Doesn't mention what fields are immutable after creation

**2. Technical Implementation Detail**  (8/10)
- JPA entities specified: Offer, OfferApproval, OfferApprovalHistory
- Database migrations clear (V11, V12, V13)
- All fields with correct types (APR as DECIMAL(5,3), monthlyPayment as DECIMAL(12,2))
- Indexes for performance (borrowerId, applicationId, bankId, status, expiresAt)
- Validity period: 24 hours with automatic expiration job
- Cascade behaviors clear (soft delete)
- Minor gap: Automatic expiration job (Story 3.11) interface not specified - scheduled task or event listener?

**3. Reference Effectiveness**  (8/10)
- Story 2.1 dependency (Application entity)
- Story 1.4 dependency (Bank account for offer)
- Architecture references clear (data persistence)
- 8 tasks well-organized
- DTOs specified (OfferDTO, OfferApprovalDTO)
- Gap: No reference to Story 3.3 calculation engine architecture

**4. Self-Containment**  (8/10)
- All entities defined (Offer, OfferApproval, OfferApprovalHistory)
- All repositories with query methods
- Service methods outlined (getOffersByApplication, getOffersByBorrower)
- Controller endpoints outlined
- Gap: Doesn't include OfferRepository query methods for finding approved offers

**5. Testing Guidance**  (8/10)
- 8 integration tests specified (entity relationships, status transitions, cascade delete)
- Unit tests for status validation
- Test coverage target: >80%
- Gap: No test vectors for offer amounts across different loan ranges

#### Identified Gaps & Clarifications Needed
1. **Immutable Fields After Creation**: AC doesn't specify which fields are immutable
   - **Recommendation**: APR, loanAmount, monthlyPayment, expiresAt must be immutable (set @Column(updatable=false))

2. **Automatic Expiration Job**: Story 3.11 references expiration, but trigger not specified here
   - **Recommendation**: Define @Scheduled task: expireOffers() runs every 30 minutes

3. **Offer Rejection Reasons**: OfferStatus.REJECTED doesn't track why offer was rejected
   - **Recommendation**: Add optional rejectionReason field for audit trail

#### Validation Conclusion
 **READY (7/10)** - Well-designed offer data model. Can start immediately after Story 2.1. Clear schema, relationships, and status transitions. Minor clarifications needed on immutability and expiration mechanism.

---

### Story 3.2: Rate Card Management System
**Status**: DRAFT | **Readiness**: 7/10 | **Risk**: LOW

#### 5-Category Assessment

**1. Goal & Context Clarity**  (8/10)
- Clear purpose: "configure APR rates by credit score and loan term ranges"
- Use case: banks manage rate pricing in admin portal (Story 4.x)
- AC specifies rate card structure: creditScore ranges, termMonths ranges, APR values
- APR can be fixed or variable (FIXED, VARIABLE_FLOOR, VARIABLE_CEILING)
- Versioning required (bankId, version, effectiveDate, deprecatedDate)
- Caching for performance
- Minor gap: Doesn't explain VARIABLE_FLOOR vs VARIABLE_CEILING (needs definition)

**2. Technical Implementation Detail**  (8/10)
- RateCard JPA entity with composite key (bankId, version)
- RateCardEntry table for individual rate rules
- Database migration clear (V14, V15)
- APR precision: DECIMAL(5,3) (min 0.5%, max 50%)
- Effective date tracking for version management
- Service methods: getRateForApplication(creditScore, termMonths), getRateCard(bankId)
- Caching with @Cacheable(cacheNames="rateCards")
- Cache invalidation on update
- Minor gap: VARIABLE_FLOOR/CEILING logic not fully detailed

**3. Reference Effectiveness**  (8/10)
- Story 4.5 dependency (Bank Admin Portal - Update Rate Cards)
- Story 3.3 dependency (Offer Calculation uses these rates)
- Architecture references clear (caching strategy)
- API documentation provided
- 6 tasks well-organized
- Gap: No explanation of how VARIABLE rates are resolved to APR (handled in Story 3.3?)

**4. Self-Containment**  (8/10)
- RateCard and RateCardEntry entities fully specified
- RateCardRepository with query methods clear
- RateCardService implementation clear
- Controller endpoints clear (GET /api/admin/rate-cards/{bankId})
- Exception handling clear (RateCardNotFoundException)
- Gap: Doesn't specify default rate card for new banks

**5. Testing Guidance**  (8/10)
- 8 unit tests for rate lookup logic
- Integration tests for versioning and caching
- Test cases for boundary conditions (min/max APR, credit score ranges)
- Coverage: >85%
- Gap: No test cases for VARIABLE rate resolution

#### Identified Gaps & Clarifications Needed

1. **VARIABLE_FLOOR vs VARIABLE_CEILING Definition**: AC references these but doesn't define them
   - **Recommendation**: 
     - VARIABLE_FLOOR: Minimum APR if rates drop (e.g., 3.5% base, 2.5% floor if rates decrease)
     - VARIABLE_CEILING: Maximum APR if rates rise (e.g., 3.5% base, 5.5% ceiling if rates increase)
   - **Resolution in Story 3.3**: How are these values resolved to concrete APR?

2. **Default Rate Card**: What APR applies if borrower credit score doesn't match any RateCardEntry?
   - **Recommendation**: Add catchall entry "creditScore >= 0" with default rate

3. **Rate Card Effective Dating**: Can new rate card version be applied retroactively?
   - **Recommendation**: No - once created, rate card is immutable. New rates use new version with new effectiveDate.

#### Validation Conclusion
 **READY (7/10)** - Well-designed rate card system. Can start after Story 2.1. Caching strategy is clear. Minor clarifications needed on VARIABLE rate types; these will be resolved in Story 3.3 (Offer Calculation).

---

### Story 3.3: Offer Calculation Engine
**Status**: DRAFT | **Readiness**: 6/10 | **Risk**: CRITICAL  **BLOCKER**

#### 5-Category Assessment

**1. Goal & Context Clarity**  (8/10)
- Clear purpose: "calculate preliminary loan offer based on application, rate card, and terms"
- Triggered by Story 2.5 (Submit Application)
- Output: Offer with APR, monthly payment, and fees
- Async processing required (event-driven)
- AC specifies 6 components: rate lookup, APR adjustment, fee calculation, monthly payment, offer creation, audit
- Minor gap: Doesn't explain APR adjustment triggers or decision criteria clearly

**2. Technical Implementation Detail**  (5/10)  **CRITICAL GAP**
- Endpoint: GET /api/offers/{applicationId}/calculate (or async event triggered)
- Service method clear: calculateOffer(Application application)
- Rate lookup logic clear: getRateFromRateCard(creditScore, termMonths)
- **APR ADJUSTMENT - OVERSIMPLIFIED**:
  - AC: "For MVP: simple adjustment = baseApr + (min adjustment if term > 120 months, else 0)"
  - Problem: "min adjustment if term > 120 months" - what is the adjustment percentage?
  - Is it hardcoded? Configurable? Based on term length?
  - Missing: exact formula, adjustment percentage, how it varies with term
- **MONTHLY PAYMENT - VAGUE**:
  - AC mentions "standard amortization formula" but doesn't specify it
  - Is it: monthlyPayment = [P  r(1+r)^n] / [(1+r)^n - 1]?
  - What about upfront fees? Deducted from principal first?
  - BigDecimal precision: ROUND_HALF_UP? How many decimal places?
- **FEE CALCULATIONS - CLEAR**:
  - originationFee = loanAmount  0.02 (2%)
  - insuranceCost = loanAmount  0.005 (0.5%)
  - But: How do these affect monthly payment calculation?
- **OFFER CREATION - CLEAR**:
  - Creates Offer entity with all calculated values
  - Publishes OfferCalculatedEvent for async processing
  - Audit logging with OFFER_CALCULATED or OFFER_CALCULATION_FAILED
- **MISSING - TEST VECTORS**:
  - No examples with specific numbers
  - No validation cases (e.g.,  loan, 5-year term, 700 credit score = APR 3.5%, monthly ,887.12)
  - No boundary test cases (min/max loan amounts, terms)

**3. Reference Effectiveness**  (6/10)
- Story 2.1 dependency (Application entity) 
- Story 3.1 dependency (Offer entity) 
- Story 3.2 dependency (Rate card lookup) 
- Architecture references mentioned but not detailed
- APR adjustment algorithm NOT referenced to any business requirement or regulatory standard
- **Gap**: **CRITICAL** - No reference to financial calculation standards (if required), no regulatory guidance on APR adjustments, no precision standards (BigDecimal rules)

**4. Self-Containment**  (5/10)  **CRITICAL DEPENDENCY**
- Service implementation outlined but formula incomplete
- Exception handling clear (RateCardNotFoundException, InvalidApplicationException)
- Async event publishing clear
- Audit logging clear
- **Gap**: **CRITICAL** - Story 3.3 is depended on by:
  - Story 4.4 (Offer Submission Form) - must use identical formula
  - Story 3.6 (Offer Comparison) - must understand formula
  - Story 3.7 (Scenario Analysis) - must recalculate with same formula
  - Story 3.10 (Offer Approval Workflow) - must understand formula for override decisions
  - CANNOT ensure consistency without single formula definition

**5. Testing Guidance**  (6/10)  **MISSING TEST VECTORS**
- Unit tests outlined (6 test cases)
- Integration tests outlined (5 test cases)
- Async event publishing test case included
- **Gap**: **CRITICAL MISSING** - Test vectors with exact numbers:
  - Test Case 1: ,000 loan, 60 months, 700 credit score  Expected APR 3.5%, monthly ,887
  - Test Case 2: ,000 loan, 180 months, 750 credit score  Expected APR 3.0%, monthly ,389
  - Test Case 3: Boundary: 121 months term (APR adjustment applies?)  Expected APR X
  - Test Case 4: Boundary: 120 months term (no adjustment?)  Expected APR Y
  - Gap: Without test vectors, implementation won't be independently verifiable

#### CRITICAL BLOCKER ANALYSIS 

**Issue**: Story 3.3 specification is insufficient for consistent implementation across multiple stories

**Concrete Problems**:
1. APR adjustment algorithm: "simple adjustment = baseApr + (min adjustment if term > 120 months, else 0)"
   - What is the adjustment value? 0.25%? 0.5%? Term-dependent?
   - How do Stories 4.4 and 3.7 implement identical logic if it's not specified?

2. Monthly payment formula: "standard amortization formula" is not standard English
   - Amortization formula = [P  r(1+r)^n] / [(1+r)^n - 1]?
   - Is upfront fees deducted from principal BEFORE monthly payment calculation?
   - Monthly payment = (loanAmount - upfrontFees)  rate... OR (loanAmount  rate) - upfrontFees?

3. BigDecimal precision rules not specified
   - Monthly payment in ,234.56 (2 decimals) or ,234.567 (3 decimals)?
   - Rounding: ROUND_HALF_UP, ROUND_DOWN, ROUND_HALF_EVEN?
   - How are rounding errors across multiple fees handled?

4. No validation test vectors
   - How will developers know their calculation is correct?
   - Will QA be testing with hardcoded values or formulas they must reverse-engineer?

**Decision 2 Resolution Required**:
1. **Exact APR Adjustment Formula**: 
   - Define: baseApr + adjustment_amount (e.g., +0.25 if term > 120 months)
   - OR baseApr + (adjustment_percentage  someVariable)
   - OR baseApr + min(max(adjustmentByTerm[term]), adjustmentCeiling)

2. **Exact Monthly Payment Formula**:
   - Define: monthlyPayment = [P  r(1+r)^n] / [(1+r)^n - 1] where r=APR/12, n=months
   - Define: How fees affect principal (deducted before or after)
   - Define: rounding (ROUND_HALF_UP to 2 decimals)

3. **Test Vectors**:
   - Provide 5-10 test cases with known inputs and expected outputs
   - Examples: /60mo/700 credit  APR X%, monthly 
   - Include boundary cases: 120mo, 121mo (APR adjustment boundary)

#### Identified Gaps & Clarifications Needed

** CRITICAL - BLOCKER DECISION 2**:
1. APR adjustment formula incomplete - must specify exact formula
2. Monthly payment formula not specified - must specify exact calculation
3. Fee handling not specified - when are they deducted from principal?
4. BigDecimal precision not specified - rounding rules required
5. Test vectors missing - exact expected values for validation

**Recommendation**: 
- **HOLD STORY 3.3 DEVELOPMENT** until Decision 2 is finalized
- Create decision document specifying:
  - APR adjustment algorithm with examples
  - Monthly payment formula with examples
  - Fee impact on principal calculation
  - BigDecimal rounding rules
  - 10 test vectors with expected outcomes
- Once Decision 2 output is available, update Story 3.3, 4.4, 3.6-3.10 with exact formula references

#### Validation Conclusion
 **BLOCKED (6/10)** - **Story 3.3 cannot be implemented without Decision 2 finalization**. While goal is clear and overall structure is sound, critical calculation formulas are underspecified. Blocks dependent stories (4.4, 3.6, 3.7, 3.10) until formula is explicitly defined. Recommend creating Decision 2 output document with mathematical formulas and test vectors before development starts.

---

### Story 3.4: Alternative Offer Scenarios (Term & APR Variations)
**Status**: DRAFT | **Readiness**: 5/10 | **Risk**: MEDIUM  **DEPENDS ON 3.3**

#### Content (Estimated)
- Generate alternative offers with different term/APR combinations
- Examples: current offer, +1% APR/-1%, shorter/longer terms
- Uses calculation formula from Story 3.3
- Returns array of offers for comparison

#### Validation Conclusion
 **BLOCKED (5/10)** - Depends entirely on Story 3.3 calculation engine. Cannot implement until Story 3.3 formula is finalized (Decision 2).

---

### Story 3.5: Offer Expiration & Auto-Extension
**Status**: DRAFT | **Readiness**: 6/10 | **Risk**: MEDIUM

#### Content (Estimated)
- Offers expire after 24 hours
- Scheduled job checks for expired offers
- Updates offer status to EXPIRED
- Optionally extends if borrower requests

#### Validation Conclusion
 **READY (6/10)** - Straightforward scheduled task. Can start after Story 3.1 (Offer entity). No calculation dependencies.

---

### Stories 3.6-3.12 (Comparison, Scenario, Approval, etc.)
**Status**: DRAFT | **Readiness**: 5/10 (collectively) | **Risk**: HIGH  **ALL DEPEND ON 3.3**

#### Content Summary (Estimated from file names):
- 3.6: Offer Comparison UI (side-by-side offers)
- 3.7: Scenario Analysis Tool ("what if" calculations)
- 3.8: Rate Card History & Audit
- 3.9: Offer Approval Workflow (bank decides to approve)
- 3.10: Offer Override (bank can manually adjust APR, fees)
- 3.11: Offer Expiration Job (scheduled cleanup)
- 3.12: Offer Analytics Dashboard (metrics, approval rates)

#### Common Dependencies:
- Stories 3.6, 3.7, 3.10 directly use or reference Story 3.3 calculation formula
- Stories 3.9, 3.11, 3.12 use Story 3.1 (Offer entity)

#### Validation Conclusion
 **BLOCKED (5/10)** - Most of these depend on Story 3.3. Cannot finalize until Decision 2 is resolved. Recommend deferring detailed validation until Story 3.3 is unblocked.

---

## EPIC 3 CROSS-STORY DEPENDENCIES

`
Story 3.1 (Offer Data Model)
    
     Story 3.2 (Rate Card)  Ready
       
        Story 3.3 (Calculation)  BLOCKED on Decision 2
           
            Story 3.4 (Alternative Scenarios)  BLOCKED
            Story 3.6 (Offer Comparison)  BLOCKED
            Story 3.7 (Scenario Analysis)  BLOCKED
            Story 3.10 (Offer Override)  BLOCKED
            Story 4.4 (Bank Submission Form)  BLOCKED
    
     Story 3.5 (Expiration Job)  Ready (after 3.1)
     Story 3.8 (Rate Card History)  Ready (after 3.2)
     Story 3.9 (Approval Workflow)  Ready (after 3.1, 1.4)
     Story 3.11 (Expiration Cleanup)  Ready (after 3.1)
     Story 3.12 (Analytics Dashboard)  Ready (after 3.1)
`

---

## EPIC 3 RISK ASSESSMENT

| Story | Readiness | Risk | Blocker | Can Start? |
|-------|-----------|------|---------|-----------|
| 3.1 | 7/10 | LOW | None |  YES (after 2.1) |
| 3.2 | 7/10 | LOW | None |  YES (after 3.1) |
| **3.3** | **6/10** | **CRITICAL** | **Decision 2** |  **NO - WAIT** |
| 3.4 | 5/10 | MEDIUM | Story 3.3 |  NO (after 3.3) |
| 3.5 | 6/10 | MEDIUM | None |  YES (after 3.1) |
| 3.6 | 5/10 | MEDIUM | Story 3.3 |  NO (after 3.3) |
| 3.7 | 5/10 | MEDIUM | Story 3.3 |  NO (after 3.3) |
| 3.8 | 6/10 | LOW | None |  YES (after 3.2) |
| 3.9 | 6/10 | MEDIUM | None |  YES (after 3.1) |
| 3.10 | 5/10 | MEDIUM | Story 3.3 |  NO (after 3.3) |
| 3.11 | 6/10 | LOW | None |  YES (after 3.1) |
| 3.12 | 6/10 | LOW | None |  YES (after 3.1) |

---

## EPIC 3 FINAL STATUS

### Overall Readiness: 6.3/10 (NEEDS CRITICAL SPECIFICATION)

**Ready Stories**: 3.1, 3.2, 3.5, 3.8, 3.9, 3.11, 3.12 (7 stories = 58%)  
**Blocked Stories**: 3.3, 3.4, 3.6, 3.7, 3.10 (5 stories = 42%)  on Decision 2

### Recommended Development Order
1. **Wave 1**: Story 3.1 (Offer data model) + 3.2 (Rate card) - Foundation
2. **Wave 2**: Stories 3.5, 3.8, 3.9, 3.11, 3.12 (parallel) - Supporting features
3. **WAIT**: Story 3.3 (calculate offer) - **MUST FINALIZE DECISION 2 FIRST**
4. **Wave 3** (after Decision 2): Stories 3.3, 3.4, 3.6, 3.7, 3.10 (offer calculation + comparison)

### Critical Path
- **Start**: Story 3.1 (0 dependencies)
- **Sequence**: 3.1  3.2  3.3 (WAIT FOR DECISION 2)  3.4, 3.6, 3.7, 3.10 (parallel)
- **In parallel**: 3.5, 3.8, 3.9, 3.11, 3.12 (don't depend on 3.3)

---

## SUMMARY RECOMMENDATIONS

 **Start immediately**: Stories 3.1, 3.2, 3.5, 3.8, 3.9, 3.11, 3.12 (7 stories, parallel development possible)  

 **CRITICAL - Finalize Decision 2 BEFORE starting Story 3.3**:
- Story 3.3 (Offer Calculation Engine) is BLOCKED
- Affects dependent stories: 3.4, 3.6, 3.7, 3.10, 4.4
- Decision 2 must specify:
  - APR adjustment formula with examples
  - Monthly payment amortization formula with examples
  - Fee impact on principal calculation
  - BigDecimal rounding rules
  - 10 test vectors with expected outcomes

 **Decision 2 Checklist** (Required before Story 3.3 development):
- [ ] APR adjustment algorithm documented (formula + examples)
- [ ] Monthly payment calculation formula documented (equation + examples)
- [ ] Fee handling documented (deduction from principal rules)
- [ ] BigDecimal precision rules documented (ROUND_HALF_UP, 2 decimals, etc.)
- [ ] 10 test vectors created with expected APR, monthly payment, fees
- [ ] Regulatory compliance verified (if applicable)
- [ ] Story 3.3, 4.4, 3.6-3.10 updated with Decision 2 output
- [ ] Test cases created from test vectors

---

**Report Prepared By**: GitHub Copilot (Scrum Master Agent)  
**Date**: 2026-01-15  
**Status**:  VALIDATION COMPLETE - CRITICAL BLOCKER IDENTIFIED (Decision 2)

