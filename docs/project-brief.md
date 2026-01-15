# PROJECT BRIEF: Credit Aggregator App for Moldovan Market

**Project Name:** Credit Aggregator MVP  
**Version:** 1.0  
**Date Created:** 2026-01-12  
**Status:** Scoping Phase  
**Prepared by:** Business Analyst Mary  

---

## Executive Summary

The **Credit Aggregator App** is a fintech marketplace platform that enables Moldovan borrowers to submit a single loan application and receive comparable credit offers from multiple banks in real-time. The platform bridges a significant gap in the Moldovan lending market by eliminating the friction of visiting multiple banks while providing banks with an efficient customer acquisition channel.

**Core Value Proposition:**
- **For Borrowers:** One application → Multiple comparable offers + transparent documents required + optional e-signature
- **For Banks:** Qualified borrower leads at competitive acquisition cost with minimal operational burden (MVP: manual review portal)
- **For Platform:** Commission model based on completed loan originations (TBD in Phase 2)

---

## 1. PROJECT OBJECTIVES

### Primary Goals (MVP Phase)

1. **Enable borrower comparison shopping** — Allow one credit application to generate preliminary offers from 2-3 Moldovan banks, with standardized comparison metrics
2. **Demonstrate bank value proposition** — Prove that qualified lead volume justifies bank participation and integration effort
3. **Establish regulatory compliance foundation** — Achieve clarity on licensing, data protection, and e-signature requirements per NBM/CNPF guidelines
4. **Build two-sided marketplace infrastructure** — Create dual account system (borrower + bank) with clear role-based workflows
5. **Launch pilot with 2-3 banks** — Validate product-market fit with early adopters before scaling

### Success Metrics (MVP v1.0)

| Metric | Target | Rationale |
|--------|--------|-----------|
| **Borrower Signup Rate** | 50+ pilot users | Validates user acquisition model |
| **Application Submission Rate** | 70%+ of signups | Core platform adoption |
| **Preliminary Offer Generation** | 95%+ of applications | System reliability |
| **Bank Offer Submission** | 60%+ of applications receive offers | Bank participation engagement |
| **Offer Selection Rate** | 50%+ of borrowers select an offer | Conversion to handoff stage |
| **Time-to-Offer** | <30 mins (avg) | Competitive advantage vs. in-branch |
| **Regulatory Clarity** | NBM/CNPF written response | De-risks Phase 2 scaling |

---

## 2. KEY STAKEHOLDERS

### Primary Users

| Stakeholder | Role | Needs |
|---|---|---|
| **Borrower** | End-user submitting loan requests | Speed, comparison clarity, low friction, trust, transparency |
| **Bank Administrator** | Bank user reviewing applications & submitting offers | Simple interface, clear application info, fast workflow |
| **Compliance Officer (Platform)** | Internal role ensuring regulatory adherence | Audit trails, consent logs, data protection, transparency |
| **NBM / CNPF (Regulator)** | Moldovan financial regulator | Licensing clarity, consumer protection compliance, AML/KYC |

### Strategic Stakeholders

- **Platform Product Team** — Day-to-day roadmap decisions
- **Technology Lead** — Spring backend architecture and bank integration strategy
- **Pilot Banks (2-3)** — Early adopters; user feedback; operational insights
- **Legal/Compliance Advisor** — Data protection, e-signature, consumer protection framework

---

## 3. SCOPE & DELIVERABLES

### MVP Phase 1.0 Scope — IN SCOPE ✅

#### Borrower Journey
- [x] User registration and account creation
- [x] Profile setup (minimal KYC: name, email, phone)
- [x] Credit application form (Tier 1+2 fields: loan type, amount, term, optional income/currency/rate type)
- [x] Real-time preliminary offer calculation based on bank public calculator formulas
- [x] Side-by-side offer comparison table with 8 standardized metrics
- [x] Offer selection and non-binding intent submission
- [x] Application status tracking (Submitted → Viewed → Offer Received → Offer Accepted)
- [x] Saved applications history and re-application capability
- [x] Scenario calculator (what-if: different amounts/terms)

#### Bank Journey
- [x] Bank account creation and organizational setup
- [x] Application queue dashboard (manual review portal)
- [x] Borrower application details display with all submitted info
- [x] Offer submission form with 8 required fields
- [x] Offer status tracking from bank perspective
- [x] Bank admin console for product/rate rules configuration (future: deprecate in favor of API)

#### Platform Infrastructure
- [x] Two-sided user registration system (Borrower + Bank roles)
- [x] Application database and offer management system
- [x] Preliminary offer calculation engine with configurable rules per bank
- [x] Audit logs for all transactions (consent, offers, selections)
- [x] Data residency and protection compliance framework (GDPR-aligned)
- [x] Email notifications for key workflow events
- [x] Basic security: password requirements, data encryption, HTTPS

#### Regulatory & Compliance
- [x] Privacy policy aligned with Moldovan data protection law
- [x] Explicit consent framework (data collection, bank sharing, e-signature)
- [x] Immutable audit trails and consent logs
- [x] Consumer protection disclosures (APR, fees, total cost clarity)
- [x] Complaint handling workflow (documented procedure)
- [x] Cooling-off period acknowledgment (if applicable per Moldovan law)

#### Documentation & Launch
- [x] Technical architecture documentation (Spring backend design)
- [x] Bank integration guide (manual portal usage; future API spec)
- [x] Compliance documentation (audit trail, data governance)
- [x] Borrower user guide and FAQs
- [x] Bank administrator guide
- [x] Go-live checklist and launch plan

---

### MVP Phase 1.0 Scope — OUT OF SCOPE ❌

- [ ] E-signature integration (Deferred to Phase 2 — binding offer finalization)
- [ ] API integration for banks (Phase 2 — portal-first for MVP)
- [ ] Advanced income verification (payroll/tax system pulls — Phase 2)
- [ ] Loan insurance options (Phase 2 — adds comparison complexity)
- [ ] Bank ratings/reviews and service metrics (Phase 2)
- [ ] Post-disbursement loan management (payment tracking, refinancing — Phase 3+)
- [ ] Regional expansion beyond Moldova (Phase 2+)
- [ ] Machine learning / automated underwriting (Phase 2+)
- [ ] Mobile app (MVP web-only; mobile Phase 2)
- [ ] Multi-currency support (MDL primary; EUR/USD secondary Phase 2)

---

## 4. TIMELINE & CONSTRAINTS

### Project Timeline (Estimated)

| Phase | Duration | Deliverables | Status |
|-------|----------|--------------|--------|
| **Discovery & Validation (Current)** | 2 weeks | Finalize regulatory requirements, bank commitment letters, technical architecture | In Progress |
| **MVP Development** | 8-10 weeks | Core platform features, bank integration testing, compliance setup | Not Started |
| **Pilot Testing** | 2 weeks | Live testing with 2-3 pilot banks; borrower feedback; UAT | Not Started |
| **Regulatory Clearance** | Parallel | NBM/CNPF response on licensing; minor adjustments as needed | Not Started |
| **Launch** | 1 week | Soft launch with pilot banks; go-live monitoring | Not Started |
| **Post-Launch (30 days)** | 1 month | Bug fixes, performance tuning, bank feedback integration | Not Started |

**Estimated MVP Full Launch:** Q1 2026 (March 2026)

### Key Constraints

| Constraint | Impact | Mitigation |
|-----------|--------|-----------|
| **Regulatory Uncertainty** | Potential delays if NBM/CNPF requires formal licensing | Initiate regulatory inquiry immediately (Week 1); design for compliance flexibility |
| **Bank Commitment** | MVP needs 2-3 participating banks; no banks = no platform value | Secure 2-3 pilot bank agreements with clear value prop and minimal integration burden |
| **Technical Integration** | Bank data quality and integration delays (even with manual portal) | Detailed bank integration testing plan; failover procedures; clear SLAs |
| **Compliance Timeline** | Data protection framework, e-signature provider setup | Begin compliance framework build in parallel with feature development |
| **Resource Availability** | Small team (platform + backend + compliance lead) | Prioritize ruthlessly; defer Phase 2 features; outsource non-core items (e-signature provider) |

### Budget & Resource Assumptions (To Be Defined)

- Development team: 1 Tech Lead (Spring backend) + 1 Frontend Engineer + 1 QA
- Compliance: 0.5 FTE internal or external legal advisor
- Infrastructure: Cloud hosting (AWS/Azure), e-signature provider (commercial), email service
- Pilot bank incentives: TBD (revenue share model to be finalized Phase 2)

---

## 5. SUCCESS CRITERIA

### Functional Requirements (MVP Must-Have)

| Feature | Success Criteria |
|---------|------------------|
| **Borrower Registration** | <2 min signup; 95%+ successful; email verification working |
| **Credit Application** | All required fields functional; form validation clear; <5 min completion |
| **Preliminary Offers** | Generated for 95%+ of valid applications; math correct per bank calculator |
| **Offer Comparison** | All 8 fields visible; sortable; side-by-side layout clear; no data loss |
| **Bank Portal** | Application queue loads <2 sec; offer submission form complete; no data loss |
| **Status Tracking** | Real-time updates; borrower sees status within 1 min of bank action |
| **Audit Logs** | All transactions logged with timestamp, user, action, data; immutable |
| **Email Notifications** | Sent for key events; <5 min delivery; no loss |

### Non-Functional Requirements (MVP Must-Have)

| Requirement | Target |
|---|---|
| **Uptime** | 99.5% (except planned maintenance) |
| **Response Time** | API calls <500ms; page loads <2 sec |
| **Data Security** | All passwords hashed; PII encrypted at rest; HTTPS everywhere |
| **Compliance** | Audit trail complete; consent logs immutable; data protection policy documented |
| **Scalability** | Support 100+ concurrent users without degradation |

### Business Success Criteria

| Metric | Target | Definition |
|--------|--------|-----------|
| **Regulatory Approval** | Formal response from NBM/CNPF (licensing clarity) | Written confirmation that platform structure is permissible |
| **Bank Participation** | 2-3 banks signed for pilot | Formal agreements with integration testing completed |
| **Borrower Traction** | 50+ pilot users; 70%+ submit applications | Validates market demand and product-market fit |
| **Platform Reliability** | 99%+ application completion rate | No abandonment due to bugs/downtime |
| **Time-to-Value** | <30 min average (submission to preliminary offers) | Competitive vs. in-branch experience |

---

## 6. RISKS & DEPENDENCIES

### Critical Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| **Regulatory Licensing Uncertainty** | Medium | Could block launch if formal license required | Initiate NBM/CNPF inquiry Week 1; design for flexible compliance; have legal advisor standby |
| **Bank Integration Delays** | High | Banks slow to provide rules, test manually portal, or require API early | Start bank engagement now; provide detailed integration guide; set clear SLAs |
| **Data Protection Compliance** | Medium | GDPR-aligned law new (2026); gaps could trigger fines | Hire external DPA advisor; build audit framework immediately; freeze feature creep |
| **E-Signature Provider Delays** | Low | Phase 2; not blocking MVP, but future feature risky | Research & contract e-signature provider now (even if unused until Phase 2) |
| **Borrower Trust/Transparency** | Medium | Clear disclaimers essential; if users think "offers are approvals," abandonment or legal risk | Ruthless messaging discipline: "Preliminary," "Subject to verification," "Not an approval" |
| **Team Capacity** | Medium | Small team; burnout risk if scope creeps | Strict scope management; defer Phase 2; clear priorities |

### Critical Dependencies

| Dependency | Owner | Risk |
|-----------|-------|------|
| **NBM/CNPF Regulatory Feedback** | External (Regulator) | 2-4 week response time; possible requirement for formal licensing review |
| **Pilot Bank Commitment** | External (Banks) | Banks may require API immediately; complex underwriting rules; late integration |
| **E-Signature Provider** | External (Vendor) | Choose vendor early; test audit trail capability; ensure Moldovan compliance |
| **Technology Stack Agreement** | Internal (Tech Lead) | Spring backend already chosen; confirm hosting (AWS/Azure); confirm database (PostgreSQL/MySQL) |
| **Compliance Legal Review** | External (Legal Advisor) | Need clarity on data protection requirements; consumer protection; complaint handling |

### Assumptions

1. **Banks will participate** — Pilot banks see value in lead volume and accept manual portal model
2. **Regulatory will clarify** — NBM/CNPF will respond to licensing inquiry within 4 weeks
3. **Moldovan market is ready** — Borrowers and banks willing to adopt aggregator model (validated in brainstorm)
4. **Spring backend is final** — Tech team committed to Spring framework (not changing mid-project)
5. **Preliminary offers are acceptable** — Borrowers understand auto-calculated offers are indicative, not binding
6. **Manual bank review is sufficient** — Banks can review applications within reasonable timeframe for MVP (target: <24 hrs)

---

## 7. ROLES & RESPONSIBILITIES

| Role | Name/Title | Responsibilities |
|------|-----------|------------------|
| **Project Sponsor** | [TBD] | Executive alignment, budget approval, risk escalation |
| **Product Manager** | [TBD] | Feature prioritization, roadmap, stakeholder communication |
| **Tech Lead** | [TBD] | Architecture decisions, Spring backend design, performance |
| **Frontend Engineer** | [TBD] | Borrower & bank UI, responsive design, integration testing |
| **QA / Tester** | [TBD] | Test plan, bug tracking, UAT coordination |
| **Compliance Officer** | [TBD] | Data protection, audit trails, regulatory alignment, disclosures |
| **Business Analyst** | Mary 📊 | Discovery, stakeholder analysis, brainstorming, strategic insights |

---

## 8. APPROVAL & SIGN-OFF

**Document Owner:** Business Analyst Mary  
**Last Updated:** 2026-01-12  

**Approvals Required:**
- [ ] Project Sponsor
- [ ] Tech Lead
- [ ] Compliance Officer
- [ ] Product Manager (if assigned)

**Sign-Off Status:** Pending approval

---

## Appendix A: Brainstorming Session Reference

This brief is derived from detailed brainstorming conducted on **2026-01-11** with the following techniques:
- Stakeholder Role Playing (Borrower, Bank, Regulator perspectives)
- First Principles Thinking (data tiers, offer standardization, auto-calculation rules)
- SCAMPER Method (feature decisions: substitution, combination, adaptation)

**Full session details:** [brainstorming-session-results.md](brainstorming-session-results.md)

---

## Appendix B: Moldovan Regulatory Framework Summary

### Key Regulators & Laws
1. **National Bank of Moldova (NBM)** — Licensing, consumer protection, monetary policy
2. **National Commission for Financial Markets (CNPF)** — Credit marketplace oversight
3. **Data Protection Authority** — Enforcement of 2026 GDPR-aligned personal data law

### Critical Compliance Areas (MVP Phase)
1. **Licensing/Permissions** — Clarify whether aggregator service requires financial intermediation license
2. **Data Protection** — Implement consent framework, audit trails, secure storage
3. **E-Signature** — Moldovan law recognizes digital signatures; ensure immutable audit trail
4. **Consumer Protection** — APR transparency, fee disclosure, complaint handling, cooling-off period (if applicable)

### Next Steps
- [ ] Initiate formal inquiry with NBM/CNPF on licensing requirements (Week 1)
- [ ] Retain external legal advisor specializing in Moldovan financial regulation (Week 1)
- [ ] Draft detailed compliance framework aligned with 2026 data protection law (Week 2-3)
- [ ] Finalize e-signature provider selection and audit trail verification (Week 3-4)

---

## Appendix C: MVP Calculation Rules Engine (Preliminary)

The platform will reverse-engineer preliminary offer calculation logic from each pilot bank's public website calculator.

### Calculation Formula (General Pattern)

**Monthly Payment** = (Loan Amount × (Rate/12)) / (1 - (1 + Rate/12)^-Term)  
**Total Interest** = (Monthly Payment × Term) - Loan Amount  
**Total Cost** = Loan Amount + Total Interest + Fees  
**Annual Interest Rate (APR)** = Effective annual rate (adjusted for fees)

### Bank-Specific Rules (To Be Extracted)

| Bank | Loan Types | Min/Max Amount | Min/Max Term | Base Rate | Fees |
|------|-----------|----------------|--------------|-----------|------|
| [Bank A] | [To be populated] | [To be populated] | [To be populated] | [To be populated] | [To be populated] |
| [Bank B] | [To be populated] | [To be populated] | [To be populated] | [To be populated] | [To be populated] |
| [Bank C] | [To be populated] | [To be populated] | [To be populated] | [To be populated] | [To be populated] |

**Extraction Method:** Review each bank's public calculator on their website; document rules in database; version control for audit trail.

---

**END OF PROJECT BRIEF**