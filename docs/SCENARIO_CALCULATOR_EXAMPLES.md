# Scenario Calculator - Example Calculations

## Overview
This document provides example loan scenario calculations using the Scenario Calculator API to help borrowers understand different loan parameters and their impact on monthly payments and total costs.

## Example 1: $200,000 Personal Loan at 8.5% APR for 30 Years

**Input Parameters:**
- Loan Amount: $200,000
- Interest Rate (APR): 8.5%
- Term: 360 months (30 years)
- Origination Fee: 2.5%
- Insurance Cost: 0.5% annualized

**Calculation Breakdown:**

1. **Monthly Payment (Amortization Formula)**
   - Formula: M = P  [r(1+r)^n] / [(1+r)^n - 1]
   - Where:
     - P = $200,000 (principal)
     - r = 0.085/12 = 0.00708333 (monthly rate)
     - n = 360 (months)
   - Result: **$1,489.51/month**

2. **Origination Fee**
   - Fee = $200,000  2.5% = **$5,000.00**

3. **Insurance Cost**
   - Annual Insurance = $200,000  0.5% = $1,000
   - Prorated for 30 years = $1,000  30 = **$30,000.00**

4. **Total Cost**
   - Total Payments = $1,489.51  360 = $535,823.60
   - Total Interest = $535,823.60 - $200,000 = $335,823.60
   - Total Cost Including Fees = $335,823.60 + $5,000.00 + $30,000.00 = **$370,823.60**

**Response Example:**
```json
{
  "loanAmount": 200000,
  "termMonths": 360,
  "apr": 8.5,
  "monthlyPayment": 1489.51,
  "totalCost": 370823.60,
  "originationFee": 5000.00,
  "insuranceCost": 30000.00,
  "calculatedAt": "2026-01-17T10:30:00Z",
  "disclaimer": "This is a preliminary calculation based on current rates..."
}
```

---

## Example 2: $100,000 Personal Loan at 8.5% APR for 5 Years

**Input Parameters:**
- Loan Amount: $100,000
- Interest Rate (APR): 8.5%
- Term: 60 months (5 years)
- Origination Fee: 2.5%
- Insurance Cost: 0.5% annualized

**Calculation Breakdown:**

1. **Monthly Payment**
   - M = $100,000  [0.00708333(1.00708333)^60] / [(1.00708333)^60 - 1]
   - Result: **$2,002.48/month**

2. **Origination Fee**
   - Fee = $100,000  2.5% = **$2,500.00**

3. **Insurance Cost**
   - Annual Insurance = $100,000  0.5% = $500
   - Prorated for 5 years = $500  5 = **$2,500.00**

4. **Total Cost**
   - Total Payments = $2,002.48  60 = $120,148.80
   - Total Interest = $120,148.80 - $100,000 = $20,148.80
   - Total Cost Including Fees = $20,148.80 + $2,500.00 + $2,500.00 = **$25,148.80**

**Response Example:**
```json
{
  "loanAmount": 100000,
  "termMonths": 60,
  "apr": 8.5,
  "monthlyPayment": 2002.48,
  "totalCost": 25148.80,
  "originationFee": 2500.00,
  "insuranceCost": 2500.00,
  "calculatedAt": "2026-01-17T10:30:00Z",
  "disclaimer": "This is a preliminary calculation based on current rates..."
}
```

---

## Example 3: Comparing Loan Terms

**Scenario: $250,000 Loan at 8.5% APR - Different Terms**

| Term | Monthly Payment | Total Interest | Total Cost (with fees) |
|------|-----------------|-----------------|------------------------|
| 5 years (60 months) | $4,713.46 | $31,807.60 | $35,307.60 |
| 10 years (120 months) | $2,889.67 | $96,761.60 | $101,761.60 |
| 20 years (240 months) | $2,034.49 | $238,277.60 | $248,777.60 |
| 30 years (360 months) | $1,678.76 | $353,935.60 | $368,935.60 |

**Key Insight:** While monthly payments are lower with longer terms, the total interest paid increases significantly. A 5-year loan costs ~$21,428 more per month but saves ~$333,628 in total interest compared to a 30-year loan.

---

## Calculation Notes

1. **Precision:** All currency calculations use BigDecimal with scale=2 (two decimal places) and HALF_UP rounding mode for accuracy.

2. **APR vs Monthly Rate:** The API accepts annual percentage rate (APR). The monthly rate is calculated as APR / 100 / 12.

3. **Insurance Calculation:** Insurance is prorated based on the loan term. For example, 0.5% annual insurance on a $200,000 loan for 30 years = $30,000 total.

4. **Disclaimer:** All calculations are preliminary and based on current rate cards. Actual rates may vary based on underwriting, credit score, and other factors.

5. **No Persistence:** Calculations are performed in-memory and not stored. Each request must include all parameters.

---

## API Request Format

```bash
curl -X POST http://localhost:8080/api/borrower/scenario-calculator \
  -H "Content-Type: application/json" \
  -d '{
    "loanAmount": 200000,
    "termMonths": 360,
    "bankId": null
  }'
```

## Additional Resources

- **POST /api/borrower/scenario-calculator** - Full API documentation
- **Story 3.2** - Bank Rate Card Configuration
- **Story 3.3** - Offer Calculation Engine
