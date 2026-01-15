# Epic 3: Preliminary Offer Calculation & Comparison

**Expanded Goal:**
Transform loan applications into comparable preliminary offers by implementing a configurable calculation engine. Borrowers see the core value proposition: multiple offers from multiple banks for transparent comparison shopping.

### Story 3.1: Offer Data Model & Database Schema

**As an** architect,  
**I want** a database schema for offers, bank rate cards, and calculations,  
**so that** subsequent stories can persist preliminary and formal offers.

**Acceptance Criteria:**

1. Offer table: id, application_id, bank_id, offer_status, apr, monthly_payment, total_cost, origination_fee, insurance_cost, processing_time_days, validity_period_days, required_documents, created_at, expires_at
2. BankRateCard table: id, bank_id, loan_type, currency, min/max_loan_amount, base_apr, adjustment_range
3. OfferCalculationLog table: id, application_id, bank_id, calculation_method, input_parameters, calculated_values, timestamp
4. Indexes: application_id, bank_id, expires_at
5. Relationship: one application has many offers
6. Cascade rules: deleting application soft-deletes related offers
7. Hibernate entities defined
8. Database migration script
9. Unit tests verify relationships

### Story 3.2: Bank Rate Card Configuration API

**As a** bank administrator,  
**I want** to configure my bank's loan calculator formulas in the admin panel,  
**so that** the platform can simulate what my bank's calculator would return for borrower applications.

**Acceptance Criteria:**

1. POST `/api/bank/rate-cards` creates rate card for loan type/currency
2. Request body: loan_type, currency, min/max_loan_amount, base_apr, apr_adjustment_range, default_processing_time_days, origination_fee_percent, insurance_percent (optional)
3. **Rate card represents bank's calculator formula:** Banks manually enter the same parameters their website calculators use
4. Validation: min < max, APR ranges realistic (0.5-50%), processing_time 1-30 days
5. Rate cards versioned: new card marks previous inactive
6. GET `/api/bank/rate-cards` returns all active rate cards
7. PUT `/api/bank/rate-cards/{rateCardId}` updates existing
8. Soft-delete preserves history
9. Effective dating: valid_from and valid_to
10. **UI helper:** Admin panel provides form fields matching common calculator parameters to simplify bank configuration
11. Integration test: create rate card, verify appears, update, verify new values apply; verify calculations use updated rates

### Story 3.3: Offer Calculation Engine (Mock/Simulated)

**As a** system,  
**I want** to calculate preliminary offers automatically when borrower submits application using bank rate cards,  
**so that** offers appear immediately simulating what banks' calculators would return (without calling external bank APIs).

**Acceptance Criteria:**

1. Service: `OfferCalculationService` triggered by application submission
2. **Mock/Simulated approach:** For each participating bank, retrieve matching rate card from database and perform calculation locally (NO external API calls to banks)
3. **Calculation logic:** Apply bank's configured formula from rate card: 
   - APR = base_apr + adjustment (based on loan_amount/term from rate card parameters)
   - Monthly Payment = standard amortization formula
   - Total Cost = (monthly_payment × months) - principal
   - Origination Fee = loan_amount × origination_fee_percent
   - Insurance Cost = loan_amount × insurance_percent (if applicable)
4. **Simulates bank calculator:** Result should match what bank's website calculator would show for same inputs
5. Validation: if rate card missing for bank/loan_type/currency, skip that bank with logged warning
6. Offer status set to CALCULATED (indicates simulated/preliminary, not formally submitted by bank)
7. Validity period: 24 hours (configurable per bank in rate card)
8. Required documents: from bank template or default list configured in rate card
9. Async execution: non-blocking; calculations happen in background
10. Audit log: each calculation logged with "MOCK_CALCULATION" indicator and input parameters
11. Calculation result stored immediately in Offer table
12. Error handling: if one bank's calculation fails (e.g., invalid formula), others proceed; failed bank logged
13. **Phase 2 preparation:** Code structured to easily replace with real bank API calls when endpoints available
14. Integration test: submit application, verify offers created for all banks with rate cards, verify calculations mathematically correct, verify no external API calls made

### Story 3.4: Offer Retrieval & Comparison API

**As a** borrower,  
**I want** to view all preliminary offers in side-by-side comparison,  
**so that** I can easily compare terms and choose the best offer.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}/offers` returns all offers
2. Response includes: bank_name, logo_url, apr, monthly_payment, total_cost, fees, processing_time, required_documents, validity_period
3. Offers sorted by APR (ascending)
4. Response schema standardizes 8 core comparison fields
5. Only borrower who owns application can view
6. Offers only returned if application status SUBMITTED or later
7. Return 200 with empty array if no offers yet
8. Include "Preliminary Offers" disclaimer
9. Performance: <100ms response
10. Integration test: submit application, retrieve offers, verify all banks, verify sorting

### Story 3.5: Offer Selection & Intent Submission

**As a** borrower,  
**I want** to indicate which preliminary offer I'm interested in,  
**so that** the bank knows I'm a qualified borrower.

**Acceptance Criteria:**

1. POST `/api/borrower/applications/{applicationId}/select-offer` accepts offer_id
2. Validation: offer_id must belong to application, not expired
3. Sets borrower_selected_at timestamp; status to ACCEPTED
4. Only one offer can be selected per application
5. Borrower can change selection until expires
6. Application status updates to OFFER_ACCEPTED
7. Email sent to borrower and bank
8. Audit log: "OFFER_SELECTED"
9. Response includes selected offer + next steps
10. Integration test: retrieve offers, select one, verify status ACCEPTED, verify email sent

### Story 3.6: Offer Expiration & Cleanup

**As a** system,  
**I want** to expire offers that are no longer valid,  
**so that** borrowers don't accidentally select stale offers.

**Acceptance Criteria:**

1. Offers expire after validity_period (24 hours default)
2. Batch job: sets offer.offer_status = EXPIRED where expires_at < now()
3. Borrower sees: "This offer expires in X hours"
4. Expired offers still visible but marked "Expired" and unselectable
5. If borrower tries to select expired, returns 410 Gone
6. System can recalculate offers if borrower requests
7. Audit log: "OFFER_EXPIRED"
8. Data retention: expired offers kept for audit
9. Integration test: create offer with 1-hour expiration, verify marked expired

### Story 3.7: Scenario Calculator Integration

**As a** borrower,  
**I want** the scenario calculator to use actual bank rate cards,  
**so that** my "what-if" estimates match the simulated offers I'll receive.

**Acceptance Criteria:**

1. POST `/api/borrower/scenarios` calculates estimates for each participating bank using their configured rate cards
2. **Same mock approach as Story 3.3:** Uses bank rate cards from database to perform local calculations (no external API calls)
3. Response returns array: one per bank with estimated payment, APR, total cost based on their rate card formulas
4. Sorted by monthly_payment (ascending)
5. Response includes disclaimer: "Estimates based on current bank rates configured in system; actual offers may vary after bank review"
6. Same loan parameters produce consistent estimates (deterministic calculation)
7. Uses same calculation formula as Story 3.3 offer calculation engine (ensures consistency)
8. No storage: transient calculations for exploration only
9. Performance: <500ms with 5+ banks (local calculation is fast)
10. Integration test: run scenario, verify estimates match offers for equivalent application; verify calculation consistency

### Story 3.8: Offer Comparison Table (UI Endpoint)

**As a** frontend developer,  
**I want** a standardized endpoint that returns offers in comparison table format,  
**so that** rendering side-by-side comparison is straightforward.

**Acceptance Criteria:**

1. GET `/api/borrower/applications/{applicationId}/offers/comparison` returns table-ready format
2. Response: array with: bank_id, bank_name, apr, monthly_payment, total_cost, fees, processing_time, required_documents_list, validity_days_remaining, best_offer_badge
3. All monetary values formatted to 2 decimals
4. Processing time and validity as integers (days)
5. Required documents as array of strings
6. Sorting: by monthly_payment ascending
7. Missing values handled gracefully ("N/A")
8. Include summary stats: average_apr, lowest_monthly_payment
9. Integration test: verify response structure matches table format

### Story 3.9: Bank Offer Override (Prepared in MVP)

**As a** bank administrator,  
**I want** to review the simulated calculated offer and optionally override with custom terms,  
**so that** I can submit binding offers reflecting my actual underwriting decision (since MVP uses simulated calculations, not my real calculator API).

**Acceptance Criteria:**

1. Bank admin sees simulated/calculated offer in application review panel (generated from rate card formula)
2. **Override capability critical for MVP:** Since calculations are simulated (not calling real bank systems), banks must be able to override with their actual offer terms
3. Bank can override: apr, monthly_payment, total_cost, origination_fee, insurance_cost, processing_time, required_documents, validity_period
4. Validation: overridden values within reasonable bounds (APR 0.5-50%, processing 1-30 days)
5. PUT `/api/bank/offers/{offerId}/override` accepts override data (implemented in Epic 4)
6. Original calculated values preserved in OfferCalculationLog for audit
7. Offer status changes CALCULATED → SUBMITTED when bank submits
8. timestamp: offer_submitted_at recorded
9. Email to borrower: "Bank [Name] has submitted a preliminary offer based on your application"
10. Audit log: "OFFER_SUBMITTED_BY_BANK" with original calculated vs. submitted values for comparison
11. **Workflow:** Simulated offer provides baseline; bank reviews and either accepts as-is or overrides

**Note:** Data model prepared in Epic 3; full implementation in Epic 4 Story 4.4.

---
