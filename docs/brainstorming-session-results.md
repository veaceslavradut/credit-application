# Brainstorming Session Results

**Session Date:** 2026-01-11  
**Facilitator:** Business Analyst Mary 📊  
**Participant:** User

---

## Executive Summary

**Topic:** Credit Aggregator App for Moldovan Market

**Session Goals:** Define MVP that analyzes a user's loan request and returns comparable bank offers, with transparency on required documents and optional e-signature completion.

**Techniques Used:** Stakeholder Role Playing, First Principles Thinking (in progress)

**Key Constraints:**
- Geographic: Moldovan market only (banks, regulations, data laws)
- Technical: Spring framework backend
- MVP Focus: Basic aggregator with auto-calculation capability

---

## Technique Session 1: Stakeholder Role Playing

**Duration:** 25 minutes  
**Description:** Explored the platform from three stakeholder perspectives to uncover needs, pain points, and value propositions.

### Borrower Perspective — Key Needs & Value Propositions

1. **Speed and convenience** - One application instead of visiting multiple banks/websites
2. **Clear comparison** - List of offers to choose the best fit
3. **Transparency** - Upfront list of documents required for approval per offer
4. **Trust and low commitment** - Minimal personal data sharing, no obligations
5. **Optional completion** - Ability to apply and e-sign documents through the aggregator
6. **Auto-calculation option** - Get results immediately after submitting request for specific loan options

**Key Insight:** The platform reduces friction in the loan shopping process while maintaining user control and transparency.

---

### Bank Perspective — Motivations

1. **Increase loan volume** - Capture time-constrained borrowers who might otherwise postpone or choose the nearest bank
2. **Reduced in-branch queues** - Less physical operational load
3. **New client acquisition** - Access to customers they wouldn't reach otherwise

### Bank Perspective — Concerns & Challenges

1. **Offer confidentiality** - Terms shown to a borrower could be shared externally or with competitors
2. **Operational burden** - Preparing proposals for aggregator applications if automated offer preparation is lacking
3. **Integration effort** - Technical and process changes required to participate

**Key Insight:** Banks need value (volume) to justify participation, plus tools to minimize operational burden. One bank = one offer in MVP simplifies bank commitment.

---

### Regulator Perspective (Moldova) — Compliance Requirements

#### 1. Licensing / Permissions (NBM & CNPF Oversight)

**Regulatory Authority:** National Bank of Moldova (NBM) and National Commission for Financial Markets (CNPF)

**Key Considerations:**
- Determine whether credit marketplace/aggregation services fall under financial intermediation or brokerage that triggers registration/licensing
- If platform collects payments, holds funds, or acts as intermediary in consumer credit agreements, formal approval may be required
- Clear contractual and regulatory boundaries with licensed banks/lenders must be maintained

**Action Required:** Clarify platform classification and licensing requirements with NBM or CNPF before launch.

#### 2. Data Protection / Personal Data Requirements

**Legal Framework:** New national personal data protection law (GDPR-aligned, effective 2026)

**Key Constraints:**
- **Explicit consent** - Users must consent to collection, processing, and sharing of personal data with banks and third parties
- **Data residency/transfer** - Lawful handling of personal data including cross-border transfers and retention policies
- **Data protection governance** - Internal records, privacy impact assessments, compliance registers
- **Registration** - Notification to the data protection authority as data controller/processor

**Action Required:** Implement comprehensive consent management system and privacy compliance framework.

#### 3. E-Signature / Digital Consent Requirements

**Legal Framework:** Digital signatures and electronic consent legally recognized in Moldova

**Key Requirements:**
- **Immutable consent logs** - Timestamps and unalterable records of user approval for credit submissions and T&Cs
- **Legally acceptable e-signature** - Use reputable provider recognized in Moldova, tied to user identity
- **Document retention** - Align with legal audit requirements (retention periods, secure storage)
- **Evidence package** - Maintain complete audit trail of signing events

**Action Required:** Integrate qualified e-signature provider with full audit trail capability.

#### 4. Consumer Protection

**Regulatory Authority:** CNPF and NBM monitor compliance

**Key Constraints:**
- **Transparent disclosures** - Clear presentation of APR, fees, and full cost of credit
- **Responsible lending** - Mechanisms for assessing consumer creditworthiness before presenting offers
- **Complaint handling & cooling-off** - Defined procedures for disputes, queries, and possible cancellation rights
- **Unfair practices prevention** - Safeguards against misleading terms or automated offers misrepresenting cost/risk
- **Total cost limits** - Compliance with national caps on interest and fees

**Action Required:** Build transparent disclosure system with standardized APR calculations and complaint handling workflow.

---

## Insights Discovered

1. **Trust Triangle:** Success depends on balancing borrower convenience, bank value proposition, and regulatory compliance
2. **Operational Burden is Key:** Banks without automated offer systems may struggle; platform must provide tools to minimize manual work
3. **Data Minimization Strategy:** Collect only essential data for preliminary offers; request sensitive docs after shortlisting
4. **Regulatory Clarity First:** Platform classification must be resolved with NBM/CNPF before launch to avoid compliance issues
5. **One Offer Per Bank:** Simplifies MVP scope and reduces bank operational complexity

---

## Technique Session 2: First Principles Thinking

**Duration:** In Progress  
**Description:** Breaking down credit aggregation to core fundamentals to define MVP scope and data model.

### 1. Minimal Data for Preliminary Offers

**Core Insight:** Progressive disclosure model with three tiers

#### Tier 1: Minimum Required Fields (Abstract Quote)
1. **Loan Type** - Consumer loan, mortgage, auto loan, etc.
2. **Amount** - Requested loan amount
3. **Term** - Repayment period (months/years)

**Output:** Abstract/approximate loan estimate from banks

#### Tier 2: Optional Enrichment Fields (Refined Quote)
4. **Income** - Monthly gross income
5. **Currency** - MDL, EUR, USD
6. **Interest Rate Type** - Fixed or adjustable

**Output:** More accurate, personalized preliminary offer

#### Tier 3: Final Offer Data (Post-Commitment)
- Full personal details, employment verification, credit history, collateral info
- Collected only after borrower selects a specific bank offer

**Key Design Decision:** 
- **Preliminary offer** = based on user-provided minimal or enriched data (no KYC yet)
- **Final offer** = sent after borrower confirms interest in specific bank, triggers full underwriting

**Implications:**
- Banks can provide instant preliminary quotes with minimal data
- Reduces data sharing concerns (sensitive docs only after commitment)
- Lower friction for user experimentation ("what if" scenarios)
- Clear expectation management (preliminary vs final approval)

---

### 2. Offer Comparability — Standardized Offer Format

**Core Insight:** Every bank offer must include identical fields for fair comparison.

#### Must-Have Fields in Every Bank Offer
1. **Loan Amount** - Original requested amount
2. **Loan Term** - Repayment period (months)
3. **Monthly Payment** - Fixed amount due each month
4. **Loan Approval Fee** - One-time fee for loan approval
5. **Administration Fee** - Ongoing admin/servicing costs
6. **Total Interest Paid** - Sum of interest over full loan term
7. **Total Payments to Bank** - Loan amount + fees + interest
8. **Annual Interest Rate (APR)** - Standardized annual percentage rate

**Comparability Strategy:**
- These 8 fields create a complete financial picture per offer
- Banks can add optional differentiators (bonuses, special conditions, etc.)
- Simple comparison: sort by monthly payment, total cost, or APR
- Regulatory compliance: transparent disclosure of all material costs

**Key Design Implication:**
- Offer response schema must be standardized across all participating banks
- Bank API/portal must enforce these fields
- UI displays offers in sortable comparison table with these core metrics

---

### 3. Smallest Unit of Value for Each Stakeholder

**Core Insight:** Define the breakthrough moment for each party.

#### Borrower Minimum Viable Outcome
**Value:** One application → multiple comparable credit offers, with less effort and better terms than contacting a single bank directly.

**Breakthrough Metric:** Saves time (vs. visiting/calling banks) AND receives at least one offer with better terms than they could negotiate alone.

#### Bank Minimum Viable Outcome
**Value:** Incremental, qualified borrowers at a lower or equal cost than existing acquisition channels (branch visits, direct marketing, call centers).

**Breakthrough Metric:** Cost per qualified application ≤ bank's standard acquisition cost; conversion rate justifies API/integration effort.

#### Platform Minimum Viable Outcome
**Value:** Efficiently match one borrower application to one or more bank offers and enable a completed selection and handoff.

**Breakthrough Metric:** 
- Complete end-to-end flow (application → offers → selection → e-signature)
- Minimal data loss between borrower and banks
- Clear audit trail and consent trail
- Bank integration simple enough for MVP (2-3 banks)

---

### 4. Automation vs. Accuracy: Two-Step Offer Model

**Core Insight:** MVP uses tiered offers—fast preliminary calculations with clear disclaimers, validated by banks.

#### Design Principle: Treat All Auto-Calculations as Indicative, Not Final

**Non-binding rule:** Any automatically generated number is:
- **Non-binding** - Not a commitment from the bank
- **Subject to bank review** - Bank may adjust after verification
- **For comparison only** - Borrower tool, not legal offer

**Protection:** Banks (compliance), Platform (liability), Borrowers (expectation management)

---

#### What Can Be Auto-Calculated Safely in MVP

**Allowed (Low-Risk):**
- Monthly payment estimates (based on amount/term)
- Interest rate range (min–max by loan type/public rules)
- Term-based scenarios ("what if 24 months vs. 36 months?")

**Not Allowed (High-Risk in MVP):**
- Final approval decision
- Final APR (subject to credit assessment)
- Credit limits (require income verification)
- Risk-based pricing adjustments

---

#### Two-Step Offer Model (MVP Pattern)

**Step 1: Preliminary Offer (Instant, Automated)**
- **Trigger:** Borrower submits Tier 1 or 2 data
- **Calculation:** Based on loan type, amount, term, public product rules
- **Data used:** Declared income (if provided), requested amount, term
- **Output:** Monthly payment estimate, interest range, total cost estimate
- **Label:** ⚠️ "Preliminary estimate — final terms may change after bank review"
- **Purpose:** Engagement, comparison, platform value demonstration

**Step 2: Confirmed Offer (Manual, Verified)**
- **Trigger:** Bank user reviews application and submits confirmed offer
- **Calculation:** Bank's internal underwriting + risk assessment
- **Data used:** Full borrower data, credit checks, income verification
- **Output:** Binding preliminary offer with confirmed terms
- **Label:** ✓ "Confirmed offer from [Bank] — subject to final approval"
- **Purpose:** Accuracy, legal validity, borrower selection

---

#### Borrower Expectation Management (Critical)

**Messaging must clearly communicate:**
1. "This is not an approval from the bank"
2. "Final terms depend on the bank's verification of your information"
3. "Use these offers to compare options, not to make a commitment"
4. "If you select an offer, the bank will contact you with final terms and documents"

**Risk if messaging fails:** Loss of trust → Platform credibility destroyed

---

### 5. Auto-Calculation Rules Engine for MVP

**Core Insight:** Banks already publish calculation logic publicly via their website calculators. MVP mirrors this logic.

#### Data Source: Reverse-Engineer Bank Calculators

**Approach:**
- Each Moldovan bank publishes a public calculator on their website
- Calculator accepts: loan type, amount, term
- Calculator outputs: monthly payment, total interest, total cost
- **MVP strategy:** Extract the calculation formula/rules from each bank's public calculator and replicate in platform

#### MVP Rules Engine Design

**Step 1: Formula Extraction**
- Document each bank's calculation logic from their website calculator
- Identify parameters: base interest rate, fees, formulas for APR calculation
- Store as configurable rules per bank

**Step 2: Platform Calculation**
- User submits: loan type, amount, term (+ optional: income, currency, rate type)
- Platform looks up bank products and calculation rules
- Executes calculation to produce preliminary offer
- Output: Monthly payment, total interest, total cost, APR estimate

**Step 3: Bank Confirmation**
- When borrower selects an offer, submission goes to bank
- Bank reviews application data (if additional Tier 2/3 data provided)
- Bank submits confirmed offer (may differ slightly from preliminary due to verification)
- Confirmed offer replaces preliminary in UI

---

#### Scenario Engine (Optional for MVP v1)

**Recommended for v1.0:**
- Allow borrowers to recalculate with different term/amount
- Real-time calculation updates
- "Compare scenarios" table (3,000 MDL @ 24mo vs. 5,000 MDL @ 36mo, etc.)
- Each scenario queries same banks, shows preliminary offers side-by-side

**Why valuable:**
- Keeps borrower engaged on platform
- Demonstrates comparison value
- Justifies returning to platform (vs. using individual bank calculators)

---

#### Configuration Ownership

**MVP Model:**
- **Platform team** owns the initial rules extraction from each bank's public calculator
- **Bank admin portal** (future) allows banks to update their own rules/products
- **Initial setup:** Manual extraction + documented rules file per bank

**Technical Implementation:**
- Calculation rules stored in database or config (Spring-friendly: YAML or database table)
- Rules versioned and auditable
- Clear timestamp on when rules were last updated from bank source

---

## Summary: First Principles Framework

| Dimension | MVP Decision |
|-----------|---|
| **Data Collection** | Tier 1 (min): loan type, amount, term; Tier 2 (optional): income, currency, rate type; Tier 3 (post-commitment): full KYC |
| **Offer Comparability** | 8 standardized fields: amount, term, monthly payment, approval fee, admin fee, total interest, total cost, APR |
| **Offer Tiers** | Step 1: Preliminary (instant, auto-calculated, indicative only); Step 2: Confirmed (bank-verified, selectable) |
| **Auto-Calculation** | Based on reverse-engineered rules from each bank's public calculator |
| **Risk Management** | All auto-calculated offers labeled clearly as estimates subject to verification |
| **Scenario Support** | Real-time recalculation for different term/amount combinations |
| **Trust Mechanism** | Clear expectation management: "Not an approval," "For comparison," "Bank will verify" |

---

## Technique Session 3: SCAMPER Method

**Duration:** In Progress  
**Description:** Systematically expand MVP features by exploring Substitute, Combine, Adapt, Modify, Put to another use, Eliminate, Reverse.

### S - Substitute: What Can Be Swapped?

#### Decision 1: Loan Type Scope
**Question:** Single loan type per request vs. multiple loan types?  
**MVP Decision:** **One loan type per request** (stable constraint)
- Simplifies comparison logic
- Reduces calculation complexity
- Clearer user journey per request

#### Decision 2: Geographic Scope
**Question:** Moldovan banks only vs. regional expansion?  
**MVP Decision:** **Moldova-only for MVP, design for future expansion**
- Current focus: Moldovan market, laws, banks only
- Architecture: Region parameter in data model (enables EU expansion later)
- Regulatory: Simplifies compliance (one NBM, one data protection regime)
- Future: Romania, Bulgaria, EU banks as Phase 2

#### Decision 3: User Registration & Roles
**Question:** Anonymous access vs. mandatory registration?  
**MVP Decision:** **Two mandatory registration roles:**

1. **Borrower**
   - Creates account with email/password
   - Submits credit applications
   - Receives and compares offers
   - Selects offers and completes e-signature

2. **Bank**
   - Creates organizational account
   - Accesses application queue
   - Reviews applications
   - Submits confirmed offers
   - Future: API integration for automation

**Implication:** Platform is two-sided marketplace; both parties must be registered.

#### Decision 4: Bank Integration Approach
**Question:** Manual portal entry vs. API integration?  
**MVP Decision:** **Manual portal-first, API planned**

- **MVP Phase 1:** Bank users manually review applications and submit offers via web portal
- **Reasoning:** Faster to launch, no API integration burden on banks, manual review allows quality control
- **Blocker risk:** Acknowledged—slow bank responses could frustrate borrowers
- **Phase 2:** API integration for banks that want automation, but not blocking MVP launch
- **Phased approach:** Portal works for 3-5 pilot banks; API added when scale demands it

---

### C - Combine: What Can Be Merged?

**Question:** Which feature combinations add value to MVP without overcomplicating scope?

#### MVP Combinations — INCLUDED

✅ **Credit Application + Manual Bank Review**
- Core value; non-negotiable for two-sided marketplace
- Borrower submits application → bank users review → bank submits offer
- Maintains quality control; no integration burden on banks

✅ **Application + Preliminary (Indicative) Offers**
- Fast comparison with auto-calculated estimates
- Low risk (clearly marked as non-binding)
- Justifies platform value proposition

✅ **Offer Comparison + Selection (Non-binding)**
- Borrower compares 8 standardized offer fields
- Selects preferred offer (non-binding intent)
- Clear next step: bank finalizes offer and documents

#### Future Combinations — DEFERRED

❌ **Income Verification (Auto-pull from payroll/tax data)**
- Complex integration with Moldovan payroll/tax systems
- Security/privacy concerns
- Deferred to Phase 2; MVP collects self-reported income only

❌ **Loan Offer + Insurance Options**
- Adds configuration complexity for banks
- Increases offer variations; harder to compare
- Deferred; banks can offer insurance separately in Phase 2

❌ **Application + Bank Profile/Reviews**
- Requires reputation system buildout
- Out of scope for MVP
- Future: "Bank insights" feature showing service ratings, approval speed, etc.

❌ **Offer Selection + Immediate E-Signature**
- Premature; borrower hasn't confirmed final offer from bank yet
- E-signature only for binding final offer (Phase 2)
- Current flow: select → bank confirms → then e-sign

❌ **Platform Account + Digital Wallet/Loan Management**
- Post-disbursement; out of MVP scope
- Focus: getting borrower to loan origination, not ongoing servicing
- Future: loan dashboard, payment tracking, refinancing (Phase 2+)

---

### A - Adapt: What Patterns Can We Borrow?

**Question:** Which successful UX patterns from other industries would improve MVP borrower experience?

#### From E-Commerce (Amazon/eBay)

✅ **INCLUDE: Side-by-Side Comparison Table**
- **Borrower benefit:** Instantly see differences; reduces mental math
- **MVP scope:** Rate/APR, Monthly payment, Term (core metrics only)
- **ROI:** Very high value, very low implementation risk

✅ **INCLUDE: "Best Option" Highlight Badge**
- **Borrower benefit:** Decision support; reduces anxiety
- **MVP rule-based badges:**
   - "Lowest monthly payment"
   - "Lowest total cost"
- **Requirement:** Must be transparent and based on objective metrics

❌ **DEFER:** Wishlist / save-for-later; one-click purchase metaphors (too binding)

#### From Fintech Apps (Wise/Revolut)

✅ **INCLUDE: Application Status Tracking**
- **Borrower benefit:** Reduces uncertainty; builds trust
- **MVP statuses:** Submitted → Viewed by bank → Offer received → Offer accepted

⚠️ **Optional (Light MVP):** Simple in-app notifications (e.g., "New offer received"); no real-time push required

❌ **DEFER:** Document management, full activity timelines, real-time sync

#### From Job Aggregators (LinkedIn Jobs)

✅ **INCLUDE: Saved Applications History**
- **Borrower benefit:** Transparency and sense of control
- **MVP:** View past applications and outcomes

⚠️ **Optional:** Basic filters (amount / term) — only if multiple offers exist

❌ **DEFER:** Advanced search; alerts for "matching offers"

#### From Insurance Aggregators (Compare.com)

✅ **INCLUDE: Sorting**
- **Borrower benefit:** Immediate clarity
- **MVP sorting:** Lowest monthly payment; Lowest APR; Shortest term

✅ **INCLUDE: Feature-based comparison (limited)**
- **Borrower benefit:** Clear trade-offs
- **MVP:** Numeric values only; no fine print

❌ **DEFER:** Deep feature breakdowns; add-ons/riders/bundles

#### MVP-Approved UX Pattern Summary

✅ Strongly recommended
- Side-by-side comparison (E-commerce) — Core decision aid
- Sorting by cost/APR/term (Insurance) — Immediate value
- Status tracking (Fintech) — Reduces anxiety
- Offer highlights (E-commerce) — Faster decisions
- Application history (Job aggregators) — Transparency

❌ Explicitly avoid in MVP
- "Instant approval" metaphors
- One-click commitment flows
- Heavy personalization
- Real-time dashboards (creates false certainty)

---

## Next Techniques Planned

- **Assumption Reversal** - Challenge core assumptions to identify edge cases
- **Five Whys** - Deep-dive into critical challenges

---

## Ideas To Be Categorized

(Will be populated after remaining technique sessions)

