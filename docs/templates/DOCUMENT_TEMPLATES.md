# E-Signature Document Templates

## LOAN_AGREEMENT Template

**Document Name:** Loan Agreement
**Purpose:** Primary lending contract defining loan terms, interest rate, repayment schedule, and conditions.
**Placeholders:**
- {{APPLICANT_NAME}}
- {{APPLICANT_ADDRESS}}
- {{LOAN_AMOUNT}}
- {{INTEREST_RATE}}
- {{REPAYMENT_TERM_MONTHS}}
- {{MONTHLY_PAYMENT}}
- {{LENDER_NAME}}
- {{EFFECTIVE_DATE}}

**Signature Requirements:** Borrower and Lender (2 signatures)
**Retention:** Permanent (7+ years per regulation)

---

## OFFER_LETTER Template

**Document Name:** Loan Offer Letter
**Purpose:** Formal offer from lender with loan terms before borrower commitment.
**Placeholders:**
- {{APPLICANT_NAME}}
- {{OFFERED_AMOUNT}}
- {{APR}}
- {{MONTHLY_PAYMENT}}
- {{OFFER_EXPIRATION_DATE}}
- {{TERMS_SUMMARY}}

**Signature Requirements:** Borrower (1 signature to accept)
**Retention:** 3 years

---

## TERMS_CONDITIONS Template

**Document Name:** Terms and Conditions
**Purpose:** General service terms, user obligations, disclaimer, limitation of liability.
**Content:** Standard legal T&Cs (not customized per applicant)

**Signature Requirements:** Borrower (1 signature)
**Retention:** For term of service + 1 year

---

## PRIVACY_POLICY Template

**Document Name:** Privacy Policy
**Purpose:** Data handling, processing, retention, user rights under GDPR/local law.
**Content:** GDPR-compliant privacy policy with:
- Data collection purposes
- Processing legal basis
- Recipient information
- Retention periods
- User rights

**Signature Requirements:** Borrower (1 signature for consent)
**Retention:** For service duration + 1 year

---

## CONSENT_FORM Template

**Document Name:** EIDAS Consent Form
**Purpose:** Explicit consent for electronic signatures per eIDAS regulation.
**Content:**
- Declaration of understanding electronic signatures are legally equivalent
- Consent to use DocuSign as qualified trust service provider
- Acknowledgment of risks/responsibilities
- Data processing consent for signature service

**Signature Requirements:** Borrower (1 signature)
**Retention:** 3+ years (compliance requirement)

---

## Implementation Notes

- Phase 1: Placeholder templates stored in `docs/templates/`
- Phase 2: Template management UI, customization per application type
- Phase 3: Dynamic template rendering with variable substitution
- All templates must be compliant with eIDAS Regulation (EU) 910/2014