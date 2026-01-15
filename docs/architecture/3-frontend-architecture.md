# 3. FRONTEND ARCHITECTURE

## 3.1 Borrower Portal

**Tech Stack:** React 18 + Next.js 13 + Tailwind CSS

**Key Pages:**
1. **Registration/Login** — Email/password signup, login, forgot password
2. **Dashboard** — Quick overview, saved applications, recent offers
3. **Application Form** — Multi-step form (loan details, optional income, consent)
4. **Offer Comparison** — Table with 8 metrics, side-by-side comparison
5. **Application Details** — Full view, status history, required documents
6. **Profile** — User info, KYC status, saved preferences

**State Management:** Redux or Zustand for global state

**Form Handling:** React Hook Form with Zod validation

**Mobile Responsiveness:** Breakpoints at 320px (mobile), 768px (tablet), 1024px (desktop)

## 3.2 Bank Admin Portal

**Tech Stack:** React 18 + Next.js 13 + Tailwind CSS

**Key Pages:**
1. **Application Queue** — Filterable table of submitted applications
2. **Application Detail** — Full borrower data
3. **Offer Submission** — Form to input 8 required fields
4. **Rate Card Configuration** — CRUD for rate cards
5. **Dashboard** — Metrics (applications received, offers submitted)
6. **Audit Logs** — View organization's transaction history

---
