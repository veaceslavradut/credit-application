# 1. TECHNOLOGY STACK

### 1.1 Core Framework & Language

| Technology | Version | Rationale |
|-----------|---------|-----------|
| **Next.js** | 13.5.6 | Server-side rendering (SEO), API routes, built-in optimization, App Router for modern routing patterns |
| **React** | 18.2.0 | Component-based UI, large ecosystem, familiar to most teams |
| **TypeScript** | 5.3.3 | Type safety, better IDE support, catches errors at compile time |
| **Node.js** | 20.11.0 LTS | Runtime environment, stable long-term support |

### 1.2 Styling & UI

| Technology | Version | Rationale |
|-----------|---------|-----------|
| **Tailwind CSS** | 3.4.0 | Utility-first approach, minimal CSS payload, consistent design system |
| **Headless UI** | 1.7.17 | Accessible, unstyled component primitives (Dialog, Popover, Menu) |
| **Heroicons** | 2.0.18 | Consistent icon set, Tailwind-native, 286 icons |

**Design Token System:**
```css
/* Tailwind Configuration (tailwind.config.ts) */
Color Palette:
  - Primary (Trust): #1e40af (blue-800) - Main actions, headers
  - Success: #16a34a (green-600) - Positive outcomes, offers
  - Warning: #d97706 (amber-500) - Alerts, expiring offers
  - Danger: #dc2626 (red-600) - Errors, rejections
  - Neutral: #6b7280 (gray-500) - Text, borders

Typography:
  - Heading 1: 32px, bold (2rem)
  - Heading 2: 24px, bold (1.5rem)
  - Body: 16px, regular (1rem)
  - Small: 14px, regular (0.875rem)
  - Breakpoints: mobile 320px, tablet 768px, desktop 1024px
```

### 1.3 State Management & Data Fetching

| Technology | Version | Use Case |
|-----------|---------|----------|
| **Zustand** | 4.4.7 | Global state (user session, notifications, UI state) |
| **React Query (TanStack Query)** | 5.28.0 | Server state (applications, offers, user data) with caching |
| **React Hook Form** | 7.49.2 | Form state management with minimal re-renders |
| **Zod** | 3.22.4 | Runtime schema validation for forms and API responses |

**Decision Rationale:**
- **Zustand over Redux** - Simpler API, smaller bundle size (2.8KB vs Redux 4.3KB + middleware)
- **React Query over SWR** - Better mutation support, cache invalidation, background refetching
- **React Hook Form over Formik** - Smaller footprint (8.5KB vs Formik 26KB), better performance with large forms
- **Zod over Yup** - Better TypeScript inference, smaller bundle (24KB vs Yup 32KB)

### 1.4 HTTP Client & API Communication

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Axios** | 1.6.5 | HTTP client with interceptors for auth, error handling |
| **OpenAPI TypeScript Codegen** | 11.0.1 | Generate TypeScript types from backend OpenAPI specs |

### 1.5 Testing

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Jest** | 29.7.0 | Unit test runner, snapshot testing |
| **React Testing Library** | 14.1.2 | Component testing (prefer testing behavior over implementation) |
| **jest-axe** | 8.0.0 | Accessibility testing |
| **MSW (Mock Service Worker)** | 1.3.4 | Mock API responses for tests |
| **Playwright** | 1.40.1 | E2E testing across browsers |

### 1.6 Developer Tools

| Technology | Version | Purpose |
|-----------|---------|---------|
| **ESLint** | 8.56.0 | Code quality and style enforcement |
| **Prettier** | 3.1.1 | Code formatting consistency |
| **Husky** | 8.0.3 | Git hooks (run tests before commit) |
| **lint-staged** | 15.2.0 | Run linters on staged files |

---
