# DECISION 2: OFFER CALCULATION FORMULA SPECIFICATION

**Status**: APPROVED  
**Date**: January 15, 2026  
**Owner**: Product Manager + Finance Lead  
**Affected Stories**: 3.3, 3.4, 3.6, 3.7, 3.10, 4.4  

---

## EXECUTIVE SUMMARY

This document defines the exact calculation formulas for preliminary offer generation in the Credit Aggregator Platform MVP. All calculations must use these formulas to ensure consistency across Story 3.3 (offer calculation engine), Story 4.4 (bank submission), and related stories.

**Key Decisions**:
1. **APR varies by loan type** (6 types defined with examples)
2. **Fees are additional costs** (NOT deducted from principal before calculation)
3. **Monthly payment uses standard amortization formula**
4. **BigDecimal precision**: APR (4 decimals), amounts (2 decimals), ROUND_HALF_UP

---

## 1. APR ADJUSTMENT ALGORITHM

### Decision
**APR is VARIABLE per loan type** with the following defaults:

| Loan Type | Default APR | Notes |
|-----------|-------------|-------|
| **Fixed Rate Consumer Loan** | 10.58% | Standard consumer credit |
| **Floating Point Consumer Loan** | 12.76% | Variable rate consumer credit |
| **Consumer Credit with Collateral** | 12.76% | Secured consumer loan |
| **Fixed-Rate Mortgage** | 13.16% | Home loan with admin fees |
| **Adjustable Rate Mortgage** | 15.00% | Variable rate home loan |
| **Express Loan (Fixed Rate)** | 9.49% | Fast approval consumer loan |

### Implementation
`java
// Example: APR lookup by loan type
public BigDecimal getDefaultApr(LoanType loanType, RatePreference ratePreference) {
    return switch(loanType) {
        case PERSONAL -> ratePreference == FIXED ? 
            new BigDecimal("10.5800") : new BigDecimal("12.7600");
        case HOME -> ratePreference == FIXED ? 
            new BigDecimal("13.1600") : new BigDecimal("15.0000");
        case EXPRESS -> new BigDecimal("9.4900");
        case COLLATERAL_SECURED -> new BigDecimal("12.7600");
        default -> new BigDecimal("10.5800"); // fallback
    };
}
`

### APR Adjustment for Term Length
**For MVP**: No term-based adjustment  
**Future Enhancement**: Add adjustment for terms > 120 months (e.g., +0.25%)

---

## 2. MONTHLY PAYMENT FORMULA

### Standard Amortization Formula
`
M = P  [r(1 + r)^n] / [(1 + r)^n - 1]

Where:
  M = Monthly Payment
  P = Principal (loan amount) - NO FEE DEDUCTION
  r = Monthly Interest Rate (APR / 100 / 12)
  n = Number of Months (term)
`

### Fee Handling Decision
**CRITICAL**: Fees do NOT reduce the principal before calculation.

**Fees are additional costs** added to the total amount the borrower must repay:
- **Monthly Payment** = calculated from full principal (P = loanAmount)
- **Total Fees** = originationFee + insuranceCost + (monthlyAdminFee  months)
- **Total Cost of Loan** = (monthlyPayment  months) + totalFees

### Example Calculation
`
Loan Amount: 10,000 MDL
Term: 12 months
APR: 10.58% (Fixed Rate Consumer Loan)

Step 1: Calculate monthly rate
  r = 10.58 / 100 / 12 = 0.00881667

Step 2: Calculate monthly payment
  M = 10000  [0.00881667  (1.00881667)^12] / [(1.00881667)^12 - 1]
  M = 10000  [0.00881667  1.110822] / [0.110822]
  M = 10000  0.097944 / 0.110822
  M = 879.53 MDL

Step 3: Calculate total interest
  Total Paid = 879.53  12 = 10,554.36 MDL
  Total Interest = 10,554.36 - 10,000 = 554.36 MDL

Step 4: Add fees (if any)
  Total Cost of Loan = 10,554.36 + fees
  (In this example: fees = 0, so Total Cost = 10,554.36 MDL)
`

### Java Implementation
`java
public BigDecimal calculateMonthlyPayment(
    BigDecimal principal, 
    int termMonths, 
    BigDecimal annualApr
) {
    // Calculate monthly rate
    BigDecimal monthlyRate = annualApr
        .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
        .divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
    
    // Calculate (1 + r)^n
    BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
    BigDecimal power = onePlusRate.pow(termMonths, MathContext.DECIMAL128);
    
    // Calculate numerator: P  r  (1+r)^n
    BigDecimal numerator = principal
        .multiply(monthlyRate)
        .multiply(power);
    
    // Calculate denominator: (1+r)^n - 1
    BigDecimal denominator = power.subtract(BigDecimal.ONE);
    
    // Monthly payment = numerator / denominator
    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
}
`

---

## 3. FEE CALCULATIONS

### Origination Fee
`
Origination Fee = loanAmount  originationFeePercent / 100
`

**Default**: 0% for consumer loans (based on test vectors)  
**Range**: 0% - 10% (configurable per bank rate card)

### Insurance Cost
`
Insurance Cost = loanAmount  insurancePercent / 100
`

**Default**: 0% for consumer loans  
**Range**: 0% - 5% (configurable per bank rate card)

### Monthly Admin Fee
`
Monthly Admin Fee = fixed amount per month (e.g., 500 MDL for mortgages)
Total Admin Fees = monthlyAdminFee  termMonths
`

**Default**: 0 MDL for consumer loans, 500 MDL for mortgages  
**Application**: Mortgages only (home loans)

### Total Cost Calculation
`
Total Cost of Loan = (monthlyPayment  termMonths) + originationFee + insuranceCost + (monthlyAdminFee  termMonths)
`

---

## 4. BIGDECIMAL PRECISION RULES

### Field-Specific Precision
| Field | Scale | Rounding Mode | Example |
|-------|-------|---------------|---------|
| **APR** | 4 decimals | ROUND_HALF_UP | 10.5800 |
| **Monthly Payment** | 2 decimals | ROUND_HALF_UP | 879.53 |
| **Loan Amount** | 2 decimals | ROUND_HALF_UP | 10000.00 |
| **Total Interest** | 2 decimals | ROUND_HALF_UP | 554.39 |
| **Total Cost** | 2 decimals | ROUND_HALF_UP | 10554.39 |
| **Fees** | 2 decimals | ROUND_HALF_UP | 500.00 |
| **Intermediate Calculations** | 6 decimals | ROUND_HALF_UP | 0.008817 (monthly rate) |

### Implementation Rules
1. **All monetary amounts**: Use BigDecimal, NEVER Double or Float
2. **Intermediate calculations**: Preserve 6 decimal precision
3. **Final output**: Round to 2 decimals for display
4. **Rounding mode**: RoundingMode.HALF_UP (standard banking practice)

### Java Example
`java
// Correct BigDecimal usage
BigDecimal loanAmount = new BigDecimal("10000.00");
BigDecimal apr = new BigDecimal("10.5800");
BigDecimal monthlyPayment = calculateMonthlyPayment(loanAmount, 12, apr);
// Result: 879.53 (rounded to 2 decimals)

// INCORRECT - never use Double
double apr = 10.58; // WRONG - precision loss
`

---

## 5. TEST VECTORS (VALIDATION CASES)

### Test Vector 1: Fixed Rate Consumer Loan
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 12 months |
| **APR** | 10.58% |
| **Monthly Payment** | 879.53 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 554.39 MDL |
| **Total Cost of Loan** | 10,554.39 MDL |
| **Effective APR** | 10.58% |

### Test Vector 2: Floating Point Consumer Loan
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 12 months |
| **APR** | 12.76% |
| **Monthly Payment** | 888.70 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 664.46 MDL |
| **Total Cost of Loan** | 10,664.46 MDL |
| **Effective APR** | 12.76% |

### Test Vector 3: Consumer Credit with Collateral
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 12 months |
| **APR** | 12.76% |
| **Monthly Payment** | 888.70 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 664.46 MDL |
| **Total Cost of Loan** | 10,664.46 MDL |
| **Effective APR** | 12.76% |

### Test Vector 4: Fixed-Rate Mortgage
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 24 months |
| **APR** | 13.16% |
| **Monthly Payment** | 451.77 MDL |
| **Monthly Admin Fee** | 500 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 842.51 MDL |
| **Total Admin Fees** | 12,000 MDL (500  24) |
| **Total Cost of Loan** | 11,342.51 MDL |
| **Effective APR** | 13.16% |

### Test Vector 5: Adjustable Rate Mortgage
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 24 months |
| **APR** | 15.00% |
| **Monthly Payment** | 459.41 MDL |
| **Monthly Admin Fee** | 500 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 1,025.92 MDL |
| **Total Admin Fees** | 12,000 MDL (500  24) |
| **Total Cost of Loan** | 11,525.92 MDL |
| **Effective APR** | 15.00% |

### Test Vector 6: Express Loan (Fixed Rate)
| Parameter | Value |
|-----------|-------|
| **Loan Amount** | 10,000 MDL |
| **Term** | 12 months |
| **APR** | 9.49% |
| **Monthly Payment** | 874.90 MDL |
| **Origination Fee** | 0 MDL |
| **Insurance Cost** | 0 MDL |
| **Total Interest** | 498.75 MDL |
| **Total Cost of Loan** | 10,498.75 MDL |
| **Effective APR** | 9.49% |

---

## 6. VALIDATION REQUIREMENTS

### Developer Checklist
- [ ] Implement monthly payment calculation using exact formula
- [ ] DO NOT deduct fees from principal before calculation
- [ ] Use BigDecimal with correct precision (4 for APR, 2 for amounts)
- [ ] Implement RoundingMode.HALF_UP for all rounding
- [ ] Verify calculations match all 6 test vectors (tolerance: 0.01 MDL)
- [ ] Add unit tests for each test vector
- [ ] Log calculation inputs and outputs for debugging
- [ ] Handle edge cases (0% APR, very short/long terms)

### QA Validation Criteria
- [ ] All 6 test vectors produce expected results (within 0.01 MDL)
- [ ] Monthly payment calculated from FULL principal (no fee deduction)
- [ ] Total cost includes all fees correctly
- [ ] BigDecimal precision verified (no floating-point errors)
- [ ] Consistent results across Story 3.3, 4.4, and related stories

---

## 7. REGULATORY COMPLIANCE

### Moldovan Banking Regulations
- **APR Disclosure**: Must display effective annual percentage rate
- **Fee Transparency**: All fees must be itemized separately
- **Total Cost Disclosure**: Must show total amount borrower will repay
- **Calculation Method**: Standard amortization formula (industry standard)

### Compliance Notes
- Formulas align with standard banking practices
- Test vectors provided by domain experts
- All fees disclosed separately (not hidden in APR)
- Calculation method is auditable and verifiable

---

## 8. IMPLEMENTATION TIMELINE

### Story Updates Required
1. **Story 3.3** (Offer Calculation Engine):
   - Implement monthly payment formula
   - Add loan type APR lookup
   - Implement fee calculations
   - Add test vectors to unit tests

2. **Story 4.4** (Bank Offer Submission):
   - Use identical calculation formula
   - Validate officer-provided APR matches recalculation
   - Reference Decision 2 in dev notes

3. **Stories 3.4, 3.6, 3.7, 3.10**:
   - Update references to calculation formula
   - Ensure consistent fee handling

### Rollout Plan
- **Week 4, Day 1**: Update Story 3.3 with Decision 2 formulas
- **Week 4, Day 2**: Update Story 4.4 and dependent stories
- **Week 4, Day 3**: Team briefing on calculation formulas
- **Week 5**: Implementation begins with validated formulas

---

## 9. DECISION APPROVAL

| Role | Name | Approval | Date |
|------|------|----------|------|
| **Product Manager** | [Name] |  Approved | 2026-01-15 |
| **Finance Lead** | [Name] |  Approved | 2026-01-15 |
| **Lead Developer (Story 3.3)** | [Name] |  Pending Review | - |
| **Lead Developer (Story 4.4)** | [Name] |  Pending Review | - |
| **QA Lead** | [Name] |  Pending Review | - |

---

## 10. QUESTIONS & CLARIFICATIONS

### Q1: What if a bank has a different APR for the same loan type?
**A**: Banks configure their own APRs via rate cards (Story 3.2). These defaults are for MVP testing only.

### Q2: Can fees be negative (discounts)?
**A**: No. Fees must be >= 0. Discounts handled separately in Phase 2.

### Q3: What about early repayment or late fees?
**A**: Out of scope for MVP. Phase 2 feature.

### Q4: How to handle monthly admin fees for non-mortgages?
**A**: Set monthlyAdminFee = 0 for consumer loans. Only mortgages use this field.

---

## CHANGE LOG

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2026-01-15 | 1.0 | Initial decision document with 6 test vectors | Bob (Scrum Master) |

---

**END OF DECISION 2 DOCUMENT**
