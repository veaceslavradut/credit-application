# 14. TECHNOLOGY DECISION MATRIX

| Decision | Choice | Rationale | Alternatives Considered |
|----------|--------|-----------|--------------------------|
| **Framework** | Next.js 13 App Router | SSR, API routes, built-in optimization | Vite + React Router (no SSR) |
| **State (Global)** | Zustand | Minimal API, small bundle, DevTools | Redux (verbose), Jotai (learning curve) |
| **State (Server)** | React Query | Cache invalidation, background refetch | SWR (simpler but less features) |
| **Forms** | React Hook Form + Zod | Performance, type safety, small bundle | Formik (large), react-final-form |
| **Styling** | Tailwind CSS | Utility-first, consistent design tokens | Styled Components (CSS-in-JS overhead) |
| **UI Components** | Headless UI + Tailwind | Accessible, unstyled, full control | Material-UI (heavy), shadcn/ui (newer) |
| **Testing** | Jest + RTL + Playwright | Comprehensive (unit/integration/E2E) | Vitest (newer), Cypress (slower) |
| **Validation** | Zod | TypeScript inference, runtime validation | Yup (larger), io-ts (complex) |
| **HTTP Client** | Axios | Interceptors, timeout, CancelToken | Fetch (no interceptors), got (Node.js) |
| **Icons** | Heroicons | 286 icons, Tailwind native | FontAwesome (large), Feather (unmaintained) |
| **Linting** | ESLint + Prettier | Standard, widely adopted, auto-format | Biome (early stage) |

---
