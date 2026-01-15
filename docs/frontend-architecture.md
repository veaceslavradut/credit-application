# Credit Aggregator MVP - Frontend Architecture

**Date:** 2026-01-14  
**Status:** Architecture Design - Ready for Implementation  
**Version:** 1.0

---

## Executive Summary

This document defines the frontend architecture for the Credit Aggregator MVP, covering two complementary React applications:

1. **Borrower Portal** - Enables borrowers to register, apply for loans, compare offers, and track application status
2. **Bank Admin Portal** - Enables bank administrators to review applications, configure rate cards, and submit offers

Both applications share a common technology stack built on **Next.js 13** with **TypeScript**, **Zustand** for state management, and **Tailwind CSS** for styling. The architecture emphasizes **developer productivity** and **AI agent implementation clarity** through explicit patterns and comprehensive examples.

**Design Principles:**
- **User-Centric** - Simple, intuitive flows matching borrower mental models
- **Mobile-First** - Responsive design optimized for mobile borrowers and tablet-using bank staff
- **Type-Safe** - Full TypeScript coverage for compiler-checked reliability
- **Testable** - Clear separation of concerns enabling comprehensive testing
- **AI-Friendly** - Explicit patterns and examples for AI agent implementation

---

## 1. TECHNOLOGY STACK

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

## 2. DIRECTORY STRUCTURE

### 2.1 Project Root Layout

```
credit-aggregator/
├── frontend/
│   ├── src/
│   │   ├── app/                    # Next.js App Router - pages and layouts
│   │   ├── components/             # Shared UI components
│   │   ├── features/               # Feature-based modules
│   │   ├── services/               # API communication layer
│   │   ├── stores/                 # Zustand stores (global state)
│   │   ├── hooks/                  # Custom React hooks
│   │   ├── types/                  # Shared TypeScript types
│   │   ├── utils/                  # Utility functions
│   │   ├── constants/              # Configuration constants
│   │   └── styles/                 # Global styles
│   ├── public/                     # Static assets (images, fonts)
│   ├── tests/                      # Test utilities and fixtures
│   ├── __tests__/                  # Test files (parallel src/ structure)
│   ├── .env.example                # Environment variables template
│   ├── .env.local                  # Local development env (git ignored)
│   ├── next.config.js              # Next.js configuration
│   ├── tsconfig.json               # TypeScript configuration
│   ├── tailwind.config.ts           # Tailwind CSS configuration
│   ├── jest.config.js              # Jest configuration
│   ├── package.json
│   └── README.md
├── backend/
├── docs/
└── docker-compose.yml
```

### 2.2 Detailed src/ Directory Structure

```
src/
├── app/                                    # Next.js App Router pages
│   ├── layout.tsx                          # Root layout (navigation, modals)
│   ├── page.tsx                            # Home page (redirects to dashboard)
│   │
│   ├── (borrower)/                         # Borrower route group
│   │   ├── layout.tsx                      # Borrower layout (navbar, sidebar)
│   │   ├── dashboard/
│   │   │   └── page.tsx                    # Borrower dashboard
│   │   ├── applications/
│   │   │   ├── page.tsx                    # List applications
│   │   │   ├── new/
│   │   │   │   └── page.tsx                # Create new application
│   │   │   └── [id]/
│   │   │       ├── page.tsx                # Application detail
│   │   │       ├── edit/
│   │   │       │   └── page.tsx            # Edit application (draft only)
│   │   │       └── submit/
│   │   │           └── page.tsx            # Submit with consent
│   │   ├── offers/
│   │   │   ├── page.tsx                    # Offer comparison page
│   │   │   └── [id]/
│   │   │       └── page.tsx                # Single offer detail
│   │   ├── calculator/
│   │   │   └── page.tsx                    # Scenario calculator
│   │   ├── settings/
│   │   │   ├── page.tsx                    # Profile settings
│   │   │   ├── password/
│   │   │   │   └── page.tsx                # Change password
│   │   │   └── consent-history/
│   │   │       └── page.tsx                # View consent audit trail
│   │   ├── help/
│   │   │   └── page.tsx                    # Help & FAQ
│   │   └── not-found.tsx                   # 404 for borrower routes
│   │
│   ├── (bank)/                             # Bank admin route group
│   │   ├── layout.tsx                      # Bank layout
│   │   ├── dashboard/
│   │   │   └── page.tsx                    # Bank dashboard (analytics)
│   │   ├── applications/
│   │   │   ├── page.tsx                    # Application queue
│   │   │   ├── [id]/
│   │   │   │   ├── page.tsx                # Review application
│   │   │   │   └── offer/
│   │   │   │       └── page.tsx            # Submit offer for application
│   │   │   └── not-found.tsx
│   │   ├── rate-cards/
│   │   │   ├── page.tsx                    # List rate cards
│   │   │   ├── new/
│   │   │   │   └── page.tsx                # Create rate card
│   │   │   └── [id]/
│   │   │       ├── page.tsx                # View rate card
│   │   │       └── edit/
│   │   │           └── page.tsx            # Edit rate card
│   │   ├── offers/
│   │   │   ├── page.tsx                    # Offers history & tracking
│   │   │   └── [id]/
│   │   │       └── page.tsx                # Offer detail
│   │   ├── settings/
│   │   │   ├── page.tsx                    # Bank settings
│   │   │   └── team/
│   │   │       └── page.tsx                # Team management (Phase 2)
│   │   ├── audit-logs/
│   │   │   └── page.tsx                    # View organization audit trail
│   │   └── not-found.tsx
│   │
│   ├── auth/
│   │   ├── login/
│   │   │   └── page.tsx                    # Login page (all users)
│   │   ├── register/
│   │   │   ├── page.tsx                    # Choose borrower or bank
│   │   │   ├── borrower/
│   │   │   │   └── page.tsx                # Borrower registration
│   │   │   └── bank/
│   │   │       └── page.tsx                # Bank registration
│   │   ├── forgot-password/
│   │   │   └── page.tsx
│   │   └── reset-password/
│   │       └── page.tsx
│   │
│   ├── api/                                # Next.js API routes
│   │   └── auth/
│   │       ├── login/route.ts
│   │       ├── logout/route.ts
│   │       └── refresh/route.ts
│   │
│   ├── error.tsx                           # Error boundary
│   ├── loading.tsx                         # Global loading UI
│   └── not-found.tsx                       # Global 404
│
├── components/                             # Shared reusable components
│   ├── ui/                                 # Base UI components (atoms)
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Card.tsx
│   │   ├── Badge.tsx
│   │   ├── Alert.tsx
│   │   ├── Modal.tsx
│   │   ├── Table.tsx
│   │   ├── Select.tsx
│   │   ├── Checkbox.tsx
│   │   ├── Textarea.tsx
│   │   ├── Label.tsx
│   │   ├── Spinner.tsx
│   │   ├── Toast.tsx
│   │   └── Skeleton.tsx
│   │
│   ├── forms/                              # Form components (molecules)
│   │   ├── ApplicationForm.tsx             # Multi-step application form
│   │   ├── OfferSubmissionForm.tsx         # Bank offer submission form
│   │   ├── RateCardForm.tsx                # Rate card configuration form
│   │   ├── LoginForm.tsx
│   │   ├── RegistrationForm.tsx
│   │   ├── ProfileForm.tsx
│   │   ├── PasswordChangeForm.tsx
│   │   └── ConsentCheckbox.tsx
│   │
│   ├── tables/                             # Table components (molecules)
│   │   ├── OfferComparisonTable.tsx        # 8-metric offer table
│   │   ├── ApplicationQueueTable.tsx       # Bank's applications list
│   │   ├── ApplicationHistoryTable.tsx     # Borrower's applications list
│   │   ├── RateCardTable.tsx
│   │   └── AuditLogTable.tsx
│   │
│   ├── layouts/                            # Layout components (organisms)
│   │   ├── BorrowerLayout.tsx
│   │   ├── BankLayout.tsx
│   │   ├── AuthLayout.tsx
│   │   ├── Navbar.tsx
│   │   ├── Sidebar.tsx
│   │   └── Footer.tsx
│   │
│   ├── navigation/                         # Navigation components
│   │   ├── BreadcrumbNav.tsx
│   │   ├── TabNav.tsx
│   │   └── PaginationNav.tsx
│   │
│   ├── dialogs/                            # Dialog/Modal components (organisms)
│   │   ├── ConfirmDialog.tsx               # Generic confirmation modal
│   │   ├── ConsentModal.tsx                # Data sharing consent modal
│   │   ├── DeleteConfirmDialog.tsx
│   │   └── ErrorDialog.tsx
│   │
│   └── common/                             # Common utility components
│       ├── ErrorBoundary.tsx
│       ├── Loading.tsx
│       ├── NotFound.tsx
│       └── AccessDenied.tsx
│
├── features/                               # Feature-based modules (domain logic)
│   ├── auth/
│   │   ├── components/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ForgotPasswordForm.tsx
│   │   ├── hooks/
│   │   │   ├── useLogin.ts
│   │   │   ├── useRegister.ts
│   │   │   ├── useLogout.ts
│   │   │   └── useAuth.ts                  # Get current user from store
│   │   ├── services/
│   │   │   ├── authService.ts              # API calls to /api/auth/*
│   │   │   └── tokenService.ts             # Token storage/retrieval
│   │   ├── stores/
│   │   │   └── authStore.ts                # Zustand user & auth state
│   │   ├── types/
│   │   │   └── auth.types.ts
│   │   └── middleware/
│   │       └── protectedRoute.ts           # Route protection logic
│   │
│   ├── applications/
│   │   ├── components/
│   │   │   ├── ApplicationWizard.tsx       # Multi-step form wrapper
│   │   │   ├── ApplicationStatusBadge.tsx
│   │   │   ├── ApplicationList.tsx
│   │   │   └── ApplicationDetail.tsx
│   │   ├── hooks/
│   │   │   ├── useCreateApplication.ts
│   │   │   ├── useUpdateApplication.ts
│   │   │   ├── useSubmitApplication.ts
│   │   │   ├── useApplications.ts          # Fetch list with React Query
│   │   │   └── useApplicationStatus.ts     # Real-time polling
│   │   ├── services/
│   │   │   └── applicationService.ts       # API calls to /api/applications/*
│   │   ├── stores/
│   │   │   └── applicationStore.ts         # Draft app state
│   │   ├── types/
│   │   │   └── application.types.ts
│   │   └── utils/
│   │       ├── statusTransitions.ts        # State machine logic
│   │       └── applicationFormatter.ts     # Format for display
│   │
│   ├── offers/
│   │   ├── components/
│   │   │   ├── OfferComparisonTable.tsx
│   │   │   ├── OfferCard.tsx
│   │   │   ├── OfferDetail.tsx
│   │   │   └── OfferSelectionModal.tsx
│   │   ├── hooks/
│   │   │   ├── useOffers.ts                # Fetch offers for app
│   │   │   ├── useSelectOffer.ts
│   │   │   └── useOfferCalculation.ts      # Trigger calculation
│   │   ├── services/
│   │   │   └── offerService.ts             # API calls to /api/offers/*
│   │   ├── stores/
│   │   │   └── offerStore.ts               # Selected offer state
│   │   ├── types/
│   │   │   └── offer.types.ts
│   │   └── utils/
│   │       ├── offerComparison.ts          # Comparison logic
│   │       └── offerCalculation.ts         # Client-side math
│   │
│   ├── calculator/
│   │   ├── components/
│   │   │   ├── ScenarioCalculator.tsx
│   │   │   └── PaymentBreakdown.tsx
│   │   ├── hooks/
│   │   │   └── useScenarioCalculation.ts
│   │   ├── services/
│   │   │   └── calculatorService.ts
│   │   ├── types/
│   │   │   └── calculator.types.ts
│   │   └── utils/
│   │       └── mortgageCalculator.ts       # PMT formula implementation
│   │
│   ├── bank/
│   │   ├── components/
│   │   │   ├── ApplicationQueueTable.tsx
│   │   │   ├── ApplicationReviewPanel.tsx
│   │   │   ├── OfferSubmissionForm.tsx
│   │   │   └── RateCardManager.tsx
│   │   ├── hooks/
│   │   │   ├── useApplicationQueue.ts
│   │   │   ├── useSubmitOffer.ts
│   │   │   ├── useRateCards.ts
│   │   │   └── useBankOrganization.ts
│   │   ├── services/
│   │   │   ├── bankService.ts
│   │   │   └── rateCardService.ts
│   │   ├── stores/
│   │   │   └── bankStore.ts                # Bank context & settings
│   │   ├── types/
│   │   │   └── bank.types.ts
│   │   └── utils/
│   │       └── offerFormatting.ts
│   │
│   └── notifications/
│       ├── components/
│       │   ├── NotificationCenter.tsx
│       │   ├── ToastStack.tsx
│       │   └── NotificationBell.tsx
│       ├── hooks/
│       │   ├── useNotifications.ts
│       │   └── useWebSocket.ts             # Real-time updates
│       ├── stores/
│       │   └── notificationStore.ts        # Toast & notification state
│       └── types/
│           └── notification.types.ts
│
├── services/                               # API communication layer
│   ├── api/
│   │   └── apiClient.ts                    # Axios instance with interceptors
│   ├── auth/
│   │   └── authService.ts
│   ├── applications/
│   │   └── applicationService.ts
│   ├── offers/
│   │   └── offerService.ts
│   ├── bank/
│   │   └── bankService.ts
│   └── utils/
│       └── errorHandler.ts                 # API error handling & mapping
│
├── stores/                                 # Zustand store definitions
│   ├── authStore.ts                        # User session, JWT
│   ├── uiStore.ts                          # UI state (notifications, modals)
│   ├── applicationStore.ts                 # Draft application
│   ├── offerStore.ts                       # Selected offer
│   ├── bankStore.ts                        # Bank context
│   └── index.ts                            # Export all stores
│
├── hooks/                                  # Custom React hooks
│   ├── useApi.ts                           # React Query wrapper
│   ├── useForm.ts                          # React Hook Form wrapper
│   ├── usePagination.ts
│   ├── useMediaQuery.ts                    # Responsive breakpoints
│   ├── useLocalStorage.ts
│   ├── useDebounce.ts
│   ├── useAsyncError.ts
│   └── useIntersectionObserver.ts
│
├── types/                                  # Shared TypeScript definitions
│   ├── index.ts
│   ├── api.types.ts                        # API request/response types
│   ├── domain.types.ts                     # Domain models
│   ├── common.types.ts                     # Common utilities
│   └── generated/                          # Auto-generated from OpenAPI
│       └── api.ts                          # (generated by openapi-codegen)
│
├── utils/                                  # Utility functions
│   ├── formatters.ts                       # Format currency, date, etc
│   ├── validators.ts                       # Custom validation functions
│   ├── logger.ts                           # Logging utility
│   ├── storage.ts                          # localStorage abstraction
│   ├── math.ts                             # Financial calculations
│   └── constants.ts                        # Constants (API paths, etc)
│
├── constants/                              # Application constants
│   ├── routes.ts                           # Route definitions
│   ├── api.ts                              # API endpoint paths
│   ├── validations.ts                      # Validation rules
│   ├── application.ts                      # Application status enums
│   ├── offer.ts                            # Offer-related constants
│   └── messages.ts                         # Error & success messages
│
├── styles/                                 # Global styles
│   ├── globals.css                         # Tailwind imports, resets
│   ├── animations.css                      # Custom animations
│   └── themes.css                          # Theme variables (if needed)
│
└── middleware.ts                           # Next.js middleware (auth checks)
```

### 2.3 Public Assets Structure

```
public/
├── images/
│   ├── logo.svg
│   ├── borrower-hero.png
│   ├── bank-hero.png
│   ├── icons/
│   │   ├── applications.svg
│   │   ├── offers.svg
│   │   ├── settings.svg
│   │   └── ...
│   └── illustrations/
│       ├── empty-state.svg
│       ├── error-500.svg
│       └── ...
├── fonts/
│   ├── inter-regular.woff2
│   ├── inter-bold.woff2
│   └── ...
└── videos/
    └── (for demo/tutorial videos in Phase 2)
```

---

## 3. COMPONENT ARCHITECTURE

### 3.1 Atomic Design Principles

The component system follows **Atomic Design** with clear size/scope definitions:

**Atoms** (Base UI Components - `/components/ui/`)
- Single responsibility
- Highly reusable
- Examples: Button, Input, Card, Badge, Alert
- No business logic
- Full TypeScript props typing

**Molecules** (Combined Components - `/components/forms/`, `/components/tables/`, `/components/navigation/`)
- Combine atoms into functional units
- Limited business logic
- Examples: LoginForm, OfferComparisonTable, BreadcrumbNav
- Reusable across features

**Organisms** (Complex Sections - `/components/layouts/`, `/components/dialogs/`)
- Combine molecules + atoms
- Feature-specific logic
- Examples: BorrowerLayout, OfferSubmissionDialog
- Handles data fetching and state management

**Templates** (Page Layouts - `/components/layouts/`)
- Organism combinations
- Define page structure
- Examples: BorrowerLayout (navbar + sidebar + content)

**Pages** (Routes - `/app/`)
- Route handlers
- Page-specific composition
- Examples: `/app/(borrower)/applications/page.tsx`

### 3.2 Component Template Specifications

**Atom Component Template (Button.tsx):**
```typescript
import React from 'react';
import { VariantProps } from 'class-variance-authority';

// Define variant styles using CVA (optional but recommended)
export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  isLoading?: boolean;
  icon?: React.ReactNode;
  children: React.ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', isLoading, icon, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={`inline-flex items-center justify-center font-medium transition-colors
          ${isLoading ? 'opacity-50 cursor-not-allowed' : ''}
          ${variant === 'primary' ? 'bg-blue-800 text-white hover:bg-blue-900' : ''}
          ${size === 'md' ? 'px-4 py-2' : ''}
          ${className || ''}`}
        disabled={isLoading || props.disabled}
        {...props}
      >
        {isLoading && <Spinner size="sm" className="mr-2" />}
        {icon && <span className="mr-2">{icon}</span>}
        {props.children}
      </button>
    );
  }
);
Button.displayName = 'Button';
```

**Molecule Component Template (LoginForm.tsx):**
```typescript
'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useLogin } from '@/features/auth/hooks/useLogin';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';

// Define validation schema
const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password too short'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });
  const { mutate: login } = useLogin();

  const onSubmit = async (data: LoginFormData) => {
    login(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="Email"
        type="email"
        placeholder="you@example.com"
        {...register('email')}
        error={errors.email?.message}
        required
      />
      <Input
        label="Password"
        type="password"
        placeholder="••••••••"
        {...register('password')}
        error={errors.password?.message}
        required
      />
      <Button type="submit" isLoading={isSubmitting} className="w-full">
        Sign In
      </Button>
    </form>
  );
}
```

**Organism Component Template (ApplicationQueueTable.tsx):**
```typescript
'use client';

import { useApplicationQueue } from '@/features/bank/hooks/useApplicationQueue';
import { ApplicationQueueTable as Table } from '@/components/tables/ApplicationQueueTable';
import { useRouter } from 'next/navigation';

export function ApplicationQueueSection() {
  const { data: applications, isLoading, error } = useApplicationQueue();
  const router = useRouter();

  const handleReviewApplication = (id: string) => {
    router.push(`/bank/applications/${id}`);
  };

  if (isLoading) return <Skeleton rows={5} />;
  if (error) return <Alert type="error" message={error.message} />;

  return (
    <Table
      applications={applications || []}
      onReview={handleReviewApplication}
    />
  );
}
```

### 3.3 Prop Typing Convention

All components use strict TypeScript typing:

```typescript
// DON'T: Loose typing
function Card(props: any) { }

// DO: Explicit interface extending HTML attributes
interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  title: string;
  description?: string;
  actions?: React.ReactNode;
}

function Card({ title, description, actions, ...props }: CardProps) {
  return (
    <div {...props} className="rounded-lg border bg-white p-4">
      <h3 className="font-bold">{title}</h3>
      {description && <p className="text-gray-600">{description}</p>}
      {actions && <div className="mt-4 flex gap-2">{actions}</div>}
    </div>
  );
}

export default Card;
```

### 3.4 Component Checklist for Developers

Before marking a component as complete:

- [ ] **Props typed** - All props have explicit TypeScript interfaces
- [ ] **Accessibility** - Semantic HTML, ARIA labels where needed, keyboard navigation
- [ ] **Mobile responsive** - Works on 320px (mobile), 768px (tablet), 1024px (desktop)
- [ ] **Error states** - Handles loading, error, and empty states
- [ ] **Unit tested** - Component behavior tested with React Testing Library
- [ ] **Storybook entry** - (Optional Phase 2) Component story documented
- [ ] **Documentation** - JSDoc comments for complex props/behavior
- [ ] **Performance** - Memoized if receives expensive props
- [ ] **Dark mode** - (Optional) Supports dark theme if applicable

---

## 4. STATE MANAGEMENT STRATEGY

### 4.1 Three-Layer State Architecture

**Layer 1: Component State (React.useState)**
- UI-only state: form inputs, collapse toggles, modal visibility
- Scope: Single component
- Example: `const [isExpanded, setIsExpanded] = useState(false)`

**Layer 2: Feature State (Zustand stores + React Hook Form)**
- User session, authentication, draft applications, notifications
- Scope: Feature or cross-feature (global)
- Examples: `authStore`, `applicationStore`, `uiStore`

**Layer 3: Server State (React Query)**
- Data from backend: applications list, offers, user profile
- Scope: Async data with caching, mutations, background refetching
- Examples: `useQuery(['applications'])`, `useMutation(submitApplication)`

**Decision Tree:**
```
State Type?
├─ Only used in this component + no async data
│  └─> useState
├─ Used by multiple components + no async data
│  └─> Zustand store
├─ Async data from backend + caching needed
│  └─> React Query (useQuery)
├─ Mutating async data
│  └─> React Query (useMutation)
└─ Form state
   └─> React Hook Form + Zod validation
```

### 4.2 Zustand Store Examples

**authStore.ts** - User session & authentication:
```typescript
import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

export interface User {
  id: string;
  email: string;
  role: 'BORROWER' | 'BANK_ADMIN';
  organizationId?: string;
  organizationName?: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;

  // Actions
  setUser: (user: User) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      (set) => ({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,

        setUser: (user) => set({ user, isAuthenticated: true }),
        setTokens: (token, refreshToken) => set({ token, refreshToken }),
        logout: () => set((state) => ({ ...state, isAuthenticated: false })),
        clearAuth: () => set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
        }),
      }),
      { name: 'auth-store' } // Persisted to localStorage
    )
  )
);
```

**uiStore.ts** - Global UI state (modals, toasts, sidebar):
```typescript
import { create } from 'zustand';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  duration?: number;
}

interface UIState {
  // Notifications
  toasts: Toast[];
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;

  // Modals
  openModals: Set<string>;
  openModal: (name: string) => void;
  closeModal: (name: string) => void;
  isModalOpen: (name: string) => boolean;

  // Sidebar
  isSidebarOpen: boolean;
  toggleSidebar: () => void;
}

export const useUIStore = create<UIState>((set, get) => ({
  toasts: [],
  addToast: (toast) => set((state) => ({
    toasts: [...state.toasts, { ...toast, id: Date.now().toString() }],
  })),
  removeToast: (id) => set((state) => ({
    toasts: state.toasts.filter((t) => t.id !== id),
  })),

  openModals: new Set(),
  openModal: (name) => set((state) => {
    const modals = new Set(state.openModals);
    modals.add(name);
    return { openModals: modals };
  }),
  closeModal: (name) => set((state) => {
    const modals = new Set(state.openModals);
    modals.delete(name);
    return { openModals: modals };
  }),
  isModalOpen: (name) => get().openModals.has(name),

  isSidebarOpen: true,
  toggleSidebar: () => set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),
}));
```

**applicationStore.ts** - Draft application state:
```typescript
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

export interface DraftApplication {
  loanType: string;
  loanAmount: number;
  loanCurrency: string;
  loanTermMonths: number;
  rateType: 'fixed' | 'variable';
  annualIncome?: number;
  employmentStatus?: string;
}

interface ApplicationState {
  draft: Partial<DraftApplication>;
  currentStep: number;

  // Actions
  setDraft: (app: Partial<DraftApplication>) => void;
  updateField: <K extends keyof DraftApplication>(key: K, value: DraftApplication[K]) => void;
  setCurrentStep: (step: number) => void;
  clearDraft: () => void;
}

export const useApplicationStore = create<ApplicationState>()(
  devtools((set) => ({
    draft: {},
    currentStep: 0,

    setDraft: (app) => set({ draft: app }),
    updateField: (key, value) => set((state) => ({
      draft: { ...state.draft, [key]: value },
    })),
    setCurrentStep: (step) => set({ currentStep: step }),
    clearDraft: () => set({ draft: {}, currentStep: 0 }),
  }))
);
```

### 4.3 React Query (TanStack Query) Setup

**API query hook example** (`useApplications.ts`):
```typescript
import { useQuery } from '@tanstack/react-query';
import { applicationService } from '@/services/applications/applicationService';

export function useApplications() {
  return useQuery({
    queryKey: ['applications'],
    queryFn: async () => {
      const response = await applicationService.list();
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 30 * 60 * 1000,   // Garbage collect after 30 min (renamed from cacheTime)
  });
}
```

**Mutation hook example** (`useCreateApplication.ts`):
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { applicationService } from '@/services/applications/applicationService';

export function useCreateApplication() {
  const queryClient = useQueryClient();
  const { mutate, ...rest } = useMutation({
    mutationFn: async (data: CreateApplicationDTO) => {
      return applicationService.create(data);
    },
    onSuccess: (data) => {
      // Invalidate list to refetch
      queryClient.invalidateQueries({ queryKey: ['applications'] });
      // Optionally set individual application in cache
      queryClient.setQueryData(['application', data.id], data);
    },
    onError: (error: ApiError) => {
      useUIStore.getState().addToast({
        type: 'error',
        message: error.message || 'Failed to create application',
      });
    },
  });

  return { mutate, ...rest };
}
```

### 4.4 React Hook Form + Zod Integration

**Form hook with validation:**
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const applicationSchema = z.object({
  loanType: z.enum(['personal', 'mortgage', 'auto']),
  loanAmount: z.number().min(500).max(100000),
  loanTermMonths: z.number().min(6).max(240),
  annualIncome: z.number().optional(),
});

type ApplicationFormData = z.infer<typeof applicationSchema>;

export function useApplicationForm(initialData?: Partial<ApplicationFormData>) {
  const { register, handleSubmit, formState: { errors }, control } = useForm<ApplicationFormData>({
    resolver: zodResolver(applicationSchema),
    defaultValues: initialData,
    mode: 'onBlur', // Validate on blur, not onChange
  });

  return { register, handleSubmit, errors, control };
}
```

---

## 5. API INTEGRATION LAYER

### 5.1 Axios Setup with Interceptors

**apiClient.ts** - Centralized HTTP client:
```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';

// Create Axios instance with base config
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token to all requests
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response interceptor: Handle errors & refresh tokens
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Handle 401 Unauthorized - try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const { refreshToken } = useAuthStore.getState();
        const response = await axios.post(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
          { refreshToken }
        );

        const { access_token, refresh_token } = response.data;
        useAuthStore.getState().setTokens(access_token, refresh_token);

        // Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed - logout user
        useAuthStore.getState().clearAuth();
        window.location.href = '/auth/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle 403 Forbidden - access denied
    if (error.response?.status === 403) {
      useUIStore.getState().addToast({
        type: 'error',
        message: 'Access denied. You do not have permission to perform this action.',
      });
    }

    // Handle 5xx server errors
    if (error.response?.status && error.response.status >= 500) {
      useUIStore.getState().addToast({
        type: 'error',
        message: 'Server error. Please try again later.',
      });
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

### 5.2 API Service Pattern

**applicationService.ts** - Encapsulate API calls by domain:
```typescript
import apiClient from './apiClient';
import { CreateApplicationDTO, UpdateApplicationDTO, Application } from '@/types/api.types';

export const applicationService = {
  // Create new application
  async create(data: CreateApplicationDTO): Promise<{ data: Application }> {
    const response = await apiClient.post<Application>('/applications', data);
    return { data: response.data };
  },

  // Fetch single application
  async get(id: string): Promise<{ data: Application }> {
    const response = await apiClient.get<Application>(`/applications/${id}`);
    return { data: response.data };
  },

  // List applications for borrower
  async list(params?: { status?: string; page?: number; limit?: number }) {
    const response = await apiClient.get<{ data: Application[]; total: number }>('/applications', {
      params,
    });
    return response.data;
  },

  // Update application (draft only)
  async update(id: string, data: UpdateApplicationDTO): Promise<{ data: Application }> {
    const response = await apiClient.put<Application>(`/applications/${id}`, data);
    return { data: response.data };
  },

  // Submit application with consent
  async submit(id: string, consentGiven: boolean): Promise<{ data: Application }> {
    const response = await apiClient.post<Application>(`/applications/${id}/submit`, {
      consentGiven,
    });
    return { data: response.data };
  },

  // Get application status
  async getStatus(id: string): Promise<{ data: { status: string; updatedAt: string } }> {
    const response = await apiClient.get(`/applications/${id}/status`);
    return { data: response.data };
  },

  // Get status history
  async getHistory(id: string): Promise<{ data: { status: string; timestamp: string }[] }> {
    const response = await apiClient.get(`/applications/${id}/history`);
    return { data: response.data };
  },
};
```

### 5.3 Error Handling Pattern

**errorHandler.ts** - Centralized error management:
```typescript
import { AxiosError } from 'axios';

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: Record<string, string[]>;
  timestamp: string;
}

export class ApiError extends Error {
  constructor(
    public statusCode: number,
    public code: string,
    message: string,
    public details?: Record<string, string[]>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function handleApiError(error: unknown): ApiError {
  // Handle Axios errors
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorResponse;
    return new ApiError(
      error.response?.status || 500,
      data?.code || 'UNKNOWN_ERROR',
      data?.message || error.message,
      data?.details
    );
  }

  // Handle other errors
  if (error instanceof Error) {
    return new ApiError(500, 'UNKNOWN_ERROR', error.message);
  }

  return new ApiError(500, 'UNKNOWN_ERROR', 'An unexpected error occurred');
}

export function getUserFriendlyMessage(error: ApiError): string {
  const messages: Record<string, string> = {
    VALIDATION_ERROR: 'Please check your input and try again',
    UNAUTHORIZED: 'You are not authorized to perform this action',
    NOT_FOUND: 'The requested resource was not found',
    CONFLICT: 'This action conflicts with existing data',
    RATE_LIMITED: 'Too many requests. Please wait and try again',
  };

  return messages[error.code] || error.message || 'An error occurred. Please try again.';
}
```

---

## 6. ROUTING & NAVIGATION STRATEGY

### 6.1 Next.js App Router Structure

**Key Principles:**
- **Route Groups** - Use `(borrower)` and `(bank)` to organize routes without URL segments
- **Dynamic Routes** - Use `[id]` for dynamic segments like `[applicationId]`
- **Protected Routes** - Middleware validates authentication & role-based access
- **Layouts** - Each route group has its own layout (navbar, sidebar)

**Route Hierarchy:**
```
/ (root)
├─ auth/
│  ├─ login
│  ├─ register/[type]
│  └─ forgot-password
│
├─ (borrower)/ [Protected: BORROWER role]
│  ├─ dashboard
│  ├─ applications/
│  │  ├─ [id]
│  │  └─ new
│  ├─ offers
│  ├─ calculator
│  └─ settings
│
└─ (bank)/ [Protected: BANK_ADMIN role]
   ├─ dashboard
   ├─ applications/[id]
   ├─ rate-cards/
   ├─ offers
   └─ settings
```

### 6.2 Protected Route Middleware

**middleware.ts** - Enforce authentication & role-based access:
```typescript
import { NextRequest, NextResponse } from 'next/server';

export function middleware(request: NextRequest) {
  const token = request.cookies.get('accessToken')?.value;
  const userRole = request.cookies.get('userRole')?.value;

  const path = request.nextUrl.pathname;

  // Redirect unauthenticated users to login
  if (!token && !path.startsWith('/auth')) {
    return NextResponse.redirect(new URL('/auth/login', request.url));
  }

  // Redirect authenticated users away from auth pages
  if (token && path.startsWith('/auth')) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  // Enforce role-based access
  if (path.startsWith('/borrower') && userRole !== 'BORROWER') {
    return NextResponse.redirect(new URL('/access-denied', request.url));
  }

  if (path.startsWith('/bank') && userRole !== 'BANK_ADMIN') {
    return NextResponse.redirect(new URL('/access-denied', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next|public).*)'],
};
```

### 6.3 Navigation Patterns

**useNavigation hook** - Safe navigation with state preservation:
```typescript
import { useRouter } from 'next/navigation';
import { useApplicationStore } from '@/stores/applicationStore';

export function useNavigation() {
  const router = useRouter();

  return {
    // Navigate to borrower dashboard
    toBorrowerDashboard: () => router.push('/borrower/dashboard'),

    // Navigate to create application (clears draft)
    toNewApplication: () => {
      useApplicationStore.getState().clearDraft();
      router.push('/borrower/applications/new');
    },

    // Navigate to application detail (preserves draft)
    toApplication: (id: string) => {
      router.push(`/borrower/applications/${id}`);
    },

    // Navigate to offers for application
    toOffers: (applicationId: string) => {
      router.push(`/borrower/applications/${applicationId}/offers`);
    },

    // Navigate to bank application queue
    toBankQueue: () => router.push('/bank/applications'),

    // Navigate with query params for filtering
    toBankQueueFiltered: (status: string) => {
      router.push(`/bank/applications?status=${status}`);
    },

    // Go back (with fallback)
    goBack: () => {
      if (typeof window !== 'undefined' && window.history.length > 1) {
        router.back();
      } else {
        router.push('/dashboard');
      }
    },
  };
}
```

**Link component wrapper** - Type-safe navigation:
```typescript
import Link from 'next/link';

interface NavLinkProps {
  href: string;
  active?: boolean;
  children: React.ReactNode;
  className?: string;
}

export function NavLink({ href, active, children, className }: NavLinkProps) {
  return (
    <Link
      href={href}
      className={`px-3 py-2 text-sm font-medium rounded-md transition-colors
        ${active 
          ? 'bg-blue-100 text-blue-900' 
          : 'text-gray-700 hover:bg-gray-100'}
        ${className || ''}`}
    >
      {children}
    </Link>
  );
}
```

---

## 7. FORM HANDLING & VALIDATION

### 7.1 Multi-Step Form Pattern

**ApplicationWizard.tsx** - Progressive disclosure form:
```typescript
'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useApplicationStore } from '@/stores/applicationStore';

// Step-specific schemas
const step1Schema = z.object({
  loanType: z.enum(['personal', 'mortgage', 'auto']),
});

const step2Schema = z.object({
  loanAmount: z.number().min(500).max(100000),
  loanCurrency: z.enum(['MDL', 'EUR', 'USD']),
});

const step3Schema = z.object({
  loanTermMonths: z.number().min(6).max(240),
  rateType: z.enum(['fixed', 'variable']),
});

const step4Schema = z.object({
  annualIncome: z.number().optional(),
  employmentStatus: z.string().optional(),
});

type Step1Data = z.infer<typeof step1Schema>;
type Step2Data = z.infer<typeof step2Schema>;
type Step3Data = z.infer<typeof step3Schema>;
type Step4Data = z.infer<typeof step4Schema>;

export function ApplicationWizard() {
  const [currentStep, setCurrentStep] = useState(1);
  const { draft, updateField } = useApplicationStore();

  // Form hook with step-specific schema
  const { register, handleSubmit, formState: { errors } } = useForm<Step1Data & Step2Data & Step3Data & Step4Data>({
    resolver: zodResolver(
      currentStep === 1 ? step1Schema :
      currentStep === 2 ? step2Schema :
      currentStep === 3 ? step3Schema :
      step4Schema
    ),
    defaultValues: draft,
  });

  const onNext = async (data: any) => {
    // Save current step data
    Object.entries(data).forEach(([key, value]) => {
      updateField(key as any, value);
    });
    setCurrentStep(prev => prev + 1);
  };

  const onSubmit = (data: any) => {
    // Final submission
    console.log('Submit', { ...draft, ...data });
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      {/* Progress indicator */}
      <div className="mb-8">
        <div className="flex justify-between">
          {[1, 2, 3, 4].map((step) => (
            <div
              key={step}
              className={`flex items-center justify-center w-10 h-10 rounded-full
                ${step <= currentStep
                  ? 'bg-blue-800 text-white'
                  : 'bg-gray-200 text-gray-600'}`}
            >
              {step}
            </div>
          ))}
        </div>
      </div>

      {/* Step 1: Loan Type */}
      {currentStep === 1 && (
        <form onSubmit={handleSubmit(onNext)}>
          <h2 className="text-2xl font-bold mb-6">What type of loan?</h2>
          <div className="space-y-4">
            {['personal', 'mortgage', 'auto'].map((type) => (
              <label key={type} className="flex items-center">
                <input
                  type="radio"
                  value={type}
                  {...register('loanType')}
                  className="mr-3"
                />
                <span className="capitalize">{type}</span>
              </label>
            ))}
          </div>
          {errors.loanType && <p className="text-red-600 mt-2">{errors.loanType.message}</p>}
          <button type="submit" className="mt-6 btn btn-primary">Next</button>
        </form>
      )}

      {/* Step 2: Amount */}
      {currentStep === 2 && (
        <form onSubmit={handleSubmit(onNext)}>
          <h2 className="text-2xl font-bold mb-6">How much do you need?</h2>
          <input
            type="number"
            placeholder="Loan amount"
            {...register('loanAmount', { valueAsNumber: true })}
          />
          {errors.loanAmount && <p className="text-red-600">{errors.loanAmount.message}</p>}
          <div className="flex gap-4 mt-6">
            <button type="button" onClick={() => setCurrentStep(1)} className="btn btn-secondary">Back</button>
            <button type="submit" className="btn btn-primary">Next</button>
          </div>
        </form>
      )}

      {/* Continue for steps 3, 4... */}
    </div>
  );
}
```

### 7.2 Field Validation Strategy

**validators.ts** - Reusable validation functions:
```typescript
import { z } from 'zod';

// Reusable field validators
export const validators = {
  email: z.string().email('Invalid email format'),
  
  password: z
    .string()
    .min(12, 'Password must be at least 12 characters')
    .regex(/[A-Z]/, 'Must contain uppercase letter')
    .regex(/[a-z]/, 'Must contain lowercase letter')
    .regex(/[0-9]/, 'Must contain number')
    .regex(/[!@#$%^&*]/, 'Must contain special character'),
  
  phoneNumber: z
    .string()
    .regex(/^\+?[1-9]\d{1,14}$/, 'Invalid phone number format'),
  
  loanAmount: z
    .number()
    .min(500, 'Minimum loan amount is 500')
    .max(100000, 'Maximum loan amount is 100,000'),
  
  loanTerm: z
    .number()
    .min(6, 'Minimum term is 6 months')
    .max(240, 'Maximum term is 240 months'),
  
  apr: z
    .number()
    .min(0.01, 'APR must be positive')
    .max(100, 'APR cannot exceed 100%'),
};

// Composite validators
export const applicationValidator = z.object({
  loanType: z.enum(['personal', 'mortgage', 'auto']),
  loanAmount: validators.loanAmount,
  loanTermMonths: validators.loanTerm,
  rateType: z.enum(['fixed', 'variable']),
  annualIncome: z.number().optional(),
  consentGiven: z.boolean().refine(v => v === true, {
    message: 'You must consent to data sharing',
  }),
});
```

---

## 8. ERROR HANDLING & RESILIENCE

### 8.1 Error Boundary Component

**ErrorBoundary.tsx** - Catch React component errors:
```typescript
'use client';

import React from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface Props {
  children: React.ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log to error reporting service (Sentry, etc.)
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-4">
          <div className="max-w-md text-center">
            <AlertTriangle className="w-16 h-16 text-red-600 mx-auto mb-4" />
            <h1 className="text-2xl font-bold mb-2">Something went wrong</h1>
            <p className="text-gray-600 mb-6">{this.state.error?.message}</p>
            <Button
              onClick={() => {
                this.setState({ hasError: false, error: null });
                window.location.href = '/';
              }}
            >
              Go Home
            </Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
```

### 8.2 API Error Recovery Pattern

**useAsyncError hook** - Handle async errors in functional components:
```typescript
import { useCallback } from 'react';
import { useUIStore } from '@/stores/uiStore';

export function useAsyncError() {
  const addToast = useUIStore((state) => state.addToast);

  const handleError = useCallback((error: any, fallbackMessage = 'An error occurred') => {
    if (error instanceof Error) {
      addToast({
        type: 'error',
        message: error.message || fallbackMessage,
        duration: 5000,
      });
    } else {
      addToast({
        type: 'error',
        message: fallbackMessage,
        duration: 5000,
      });
    }
  }, [addToast]);

  return { handleError };
}
```

### 8.3 Loading & Empty States

**Skeleton component** - Loading placeholder:
```typescript
export function Skeleton({ lines = 3, className = '' }: { lines?: number; className?: string }) {
  return (
    <div className={`space-y-4 ${className}`}>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="h-4 bg-gray-200 rounded animate-pulse"
          style={{
            width: `${Math.random() * 40 + 60}%`,
          }}
        />
      ))}
    </div>
  );
}
```

**Empty state component:**
```typescript
interface EmptyStateProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="text-center py-12">
      <div className="flex justify-center mb-4">{icon}</div>
      <h3 className="text-lg font-semibold mb-2">{title}</h3>
      <p className="text-gray-600 mb-6">{description}</p>
      {action && (
        <Button onClick={action.onClick}>{action.label}</Button>
      )}
    </div>
  );
}
```

---

## 9. PERFORMANCE OPTIMIZATION

### 9.1 Image Optimization

**NextImage wrapper:**
```typescript
import Image from 'next/image';

interface OptimizedImageProps {
  src: string;
  alt: string;
  width?: number;
  height?: number;
  priority?: boolean;
  className?: string;
}

export function OptimizedImage({
  src,
  alt,
  width = 400,
  height = 300,
  priority = false,
  className,
}: OptimizedImageProps) {
  return (
    <Image
      src={src}
      alt={alt}
      width={width}
      height={height}
      priority={priority}
      quality={75} // 75% quality balances size vs visual quality
      placeholder="blur"
      blurDataURL="data:image/svg+xml;base64,..." // Placeholder while loading
      className={className}
    />
  );
}
```

### 9.2 Code Splitting Strategy

**Dynamic imports for heavy features:**
```typescript
import dynamic from 'next/dynamic';

// Lazy load calculator (only needed on that page)
const ScenarioCalculator = dynamic(
  () => import('@/features/calculator/components/ScenarioCalculator'),
  {
    loading: () => <Skeleton lines={5} />,
    ssr: false, // Don't server-render heavy components
  }
);

export function CalculatorPage() {
  return <ScenarioCalculator />;
}
```

### 9.3 Data Fetching Optimization

**React Query cache strategies:**
```typescript
// Keep offer comparison data fresh (5 min) but background refetch
export function useOffers(applicationId: string) {
  return useQuery({
    queryKey: ['offers', applicationId],
    queryFn: () => offerService.list(applicationId),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 30 * 60 * 1000,   // Keep in cache 30 min
    refetchOnWindowFocus: false, // Don't refetch on window focus
    refetchOnReconnect: true,    // But do refetch when reconnecting
  });
}

// Keep user session fresh (background)
export function useUser() {
  return useQuery({
    queryKey: ['user'],
    queryFn: () => authService.getProfile(),
    staleTime: 15 * 60 * 1000, // 15 minutes
    refetchInterval: 10 * 60 * 1000, // Refetch every 10 min in background
  });
}
```

### 9.4 Memoization Strategy

**Use React.memo for expensive components:**
```typescript
interface OfferCardProps {
  offer: Offer;
  onSelect: (offerId: string) => void;
  isSelected: boolean;
}

const OfferCard = React.memo(function OfferCard({
  offer,
  onSelect,
  isSelected,
}: OfferCardProps) {
  return (
    <div className={`border-2 rounded-lg p-6 cursor-pointer transition-colors
      ${isSelected ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}
      onClick={() => onSelect(offer.id)}
    >
      {/* Offer details */}
    </div>
  );
}, (prevProps, nextProps) => {
  // Custom comparison: only rerender if offer or selection changed
  return prevProps.offer.id === nextProps.offer.id &&
         prevProps.isSelected === nextProps.isSelected;
});
```

### 9.5 Performance Metrics

**Web Vitals tracking:**
```typescript
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

function sendMetric(metric: any) {
  // Send to analytics service (Vercel Analytics, Datadog, etc.)
  console.log(metric);
}

getCLS(sendMetric);
getFID(sendMetric);
getFCP(sendMetric);
getLCP(sendMetric);
getTTFB(sendMetric);
```

---

## 10. TESTING STRATEGY

### 10.1 Unit Testing Components

**Button.test.tsx:**
```typescript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Button } from '@/components/ui/Button';

describe('Button Component', () => {
  it('renders button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
  });

  it('calls onClick handler when clicked', async () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    await userEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledOnce();
  });

  it('disables button when isLoading is true', () => {
    render(<Button isLoading>Loading</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('shows spinner when loading', () => {
    render(<Button isLoading>Loading</Button>);
    expect(screen.getByRole('img', { hidden: true })).toHaveClass('animate-spin');
  });

  it('supports variant styles', () => {
    render(<Button variant="secondary">Secondary</Button>);
    expect(screen.getByRole('button')).toHaveClass('bg-gray-200');
  });
});
```

### 10.2 Integration Testing Forms

**ApplicationForm.test.tsx:**
```typescript
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { ApplicationForm } from '@/features/applications/components/ApplicationForm';

// Mock API server
const server = setupServer(
  rest.post('/api/applications', (req, res, ctx) => {
    return res(ctx.json({ id: '123', status: 'DRAFT' }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('ApplicationForm Integration', () => {
  it('submits form with valid data', async () => {
    render(<ApplicationForm />);
    const user = userEvent.setup();

    // Fill form
    await user.selectOptions(
      screen.getByRole('combobox', { name: /loan type/i }),
      'personal'
    );
    await user.type(
      screen.getByRole('spinbutton', { name: /amount/i }),
      '50000'
    );
    await user.selectOptions(
      screen.getByRole('combobox', { name: /term/i }),
      '60'
    );

    // Submit
    await user.click(screen.getByRole('button', { name: /submit/i }));

    // Verify success
    await waitFor(() => {
      expect(screen.getByText(/application submitted/i)).toBeInTheDocument();
    });
  });

  it('shows validation errors on submit', async () => {
    render(<ApplicationForm />);
    const user = userEvent.setup();

    // Try to submit empty form
    await user.click(screen.getByRole('button', { name: /submit/i }));

    // Verify errors
    await waitFor(() => {
      expect(screen.getByText(/loan type is required/i)).toBeInTheDocument();
      expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
    });
  });
});
```

### 10.3 E2E Testing (Playwright)

**borrower.e2e.spec.ts:**
```typescript
import { test, expect } from '@playwright/test';

test.describe('Borrower Application Flow', () => {
  test('complete loan application journey', async ({ page }) => {
    // 1. Login
    await page.goto('/auth/login');
    await page.fill('input[name="email"]', 'borrower@example.com');
    await page.fill('input[name="password"]', 'TestPassword123!');
    await page.click('button:has-text("Sign In")');
    
    // Wait for redirect to dashboard
    await expect(page).toHaveURL('/borrower/dashboard');
    
    // 2. Start new application
    await page.click('a:has-text("New Application")');
    await expect(page).toHaveURL('/borrower/applications/new');
    
    // 3. Fill loan details
    await page.selectOption('select[name="loanType"]', 'personal');
    await page.fill('input[name="loanAmount"]', '50000');
    await page.selectOption('select[name="loanTerm"]', '60');
    await page.selectOption('select[name="rateType"]', 'fixed');
    
    // 4. Submit application
    await page.click('button:has-text("Submit Application")');
    
    // 5. Accept consent
    await page.check('input[name="consentGiven"]');
    await page.click('button:has-text("Confirm Consent")');
    
    // 6. Verify application submitted
    await expect(page).toHaveURL(/\/borrower\/applications\/\d+/);
    await expect(page.locator('text=Application Submitted')).toBeVisible();
    
    // 7. View offers (when ready)
    await page.waitForTimeout(2000);
    await page.reload();
    await expect(page.locator('text=Offers Received')).toBeVisible();
  });
});
```

### 10.4 Accessibility Testing

**accessibility.test.tsx:**
```typescript
import { render } from '@testing-library/react';
import { axe, toHaveNoViolations } from 'jest-axe';
import { ApplicationForm } from '@/features/applications/components/ApplicationForm';

expect.extend(toHaveNoViolations);

describe('ApplicationForm Accessibility', () => {
  it('has no accessibility violations', async () => {
    const { container } = render(<ApplicationForm />);
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });

  it('form is keyboard navigable', async () => {
    const { container } = render(<ApplicationForm />);
    
    // Simulate Tab key presses
    const inputs = container.querySelectorAll('input, select, button');
    expect(inputs.length).toBeGreaterThan(0);
    
    // Verify focus order
    inputs.forEach((input, idx) => {
      expect(input).toHaveAttribute('tabindex', idx === 0 ? '0' : '-1');
    });
  });

  it('form labels are associated with inputs', () => {
    const { container } = render(<ApplicationForm />);
    
    const labels = container.querySelectorAll('label');
    labels.forEach((label) => {
      const htmlFor = label.getAttribute('for');
      const input = container.querySelector(`input#${htmlFor}`);
      expect(input).toBeInTheDocument();
    });
  });
});
```

### 10.5 Test Coverage Goals

**Target: 80%+ code coverage**

```json
{
  "coverageThreshold": {
    "global": {
      "branches": 75,
      "functions": 80,
      "lines": 80,
      "statements": 80
    }
  },
  "collectCoverageFrom": [
    "src/**/*.{ts,tsx}",
    "!src/**/*.d.ts",
    "!src/types/**",
    "!src/constants/**"
  ]
}
```

---

## 11. BUILD CONFIGURATION & DEPLOYMENT

### 11.1 Next.js Configuration

**next.config.js:**
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  // Strict mode for development
  reactStrictMode: true,

  // Image optimization
  images: {
    formats: ['image/avif', 'image/webp'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    domains: ['localhost', 'cdn.example.com'],
    unoptimized: process.env.NODE_ENV === 'development', // Faster builds in dev
  },

  // Internationalization (Phase 2)
  i18n: {
    locales: ['en', 'ro'],
    defaultLocale: 'en',
  },

  // Environment variables
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
    NEXT_PUBLIC_APP_VERSION: process.env.npm_package_version,
  },

  // Headers for security
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'SAMEORIGIN',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
        ],
      },
    ];
  },

  // Webpack optimization
  webpack: (config, { isServer }) => {
    if (!isServer) {
      config.optimization.splitChunks.cacheGroups = {
        ...config.optimization.splitChunks.cacheGroups,
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          priority: 10,
        },
        react: {
          test: /[\\/]node_modules[\\/](react|react-dom)[\\/]/,
          name: 'react-vendors',
          priority: 20,
        },
      };
    }
    return config;
  },

  // Redirect www to non-www
  async redirects() {
    return [
      {
        source: '/:path*',
        destination: 'https://credit-aggregator.md/:path*',
        basePath: false,
        permanent: true,
        has: [{ type: 'host', value: 'www.credit-aggregator.md' }],
      },
    ];
  },

  // Compression and optimization
  compress: true,
  swcMinify: true, // Use SWC for faster builds
  productionBrowserSourceMaps: false, // Don't expose source maps in production
  onDemandEntries: {
    maxInactiveAge: 1000 * 60 * 60, // 1 hour
    pagesBufferLength: 5,
  },
};

module.exports = nextConfig;
```

### 11.2 TypeScript Configuration

**tsconfig.json:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "jsx": "preserve",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "incremental": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@/components/*": ["./src/components/*"],
      "@/features/*": ["./src/features/*"],
      "@/services/*": ["./src/services/*"],
      "@/stores/*": ["./src/stores/*"],
      "@/hooks/*": ["./src/hooks/*"],
      "@/types/*": ["./src/types/*"],
      "@/utils/*": ["./src/utils/*"],
      "@/constants/*": ["./src/constants/*"]
    },
    "types": ["jest", "@testing-library/jest-dom"],
    "allowJs": false,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx"],
  "exclude": ["node_modules", "dist", ".next"]
}
```

### 11.3 Environment Variables

**.env.example:**
```bash
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_API_TIMEOUT=30000

# Authentication
NEXT_PUBLIC_JWT_REFRESH_INTERVAL=5m

# Feature Flags
NEXT_PUBLIC_ENABLE_CALCULATOR=true
NEXT_PUBLIC_ENABLE_BANK_PORTAL=true
NEXT_PUBLIC_ENABLE_E_SIGNATURE=false

# Analytics (Phase 2)
NEXT_PUBLIC_ANALYTICS_ID=

# Monitoring
NEXT_PUBLIC_SENTRY_DSN=
NEXT_PUBLIC_SENTRY_ENVIRONMENT=development

# Build Info
NEXT_PUBLIC_BUILD_TIME=
NEXT_PUBLIC_GIT_SHA=
```

### 11.4 Docker Configuration

**Dockerfile:**
```dockerfile
# Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# Copy manifests
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci

# Copy source
COPY . .

# Build application
RUN npm run build

# Runtime stage
FROM node:20-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache dumb-init

# Copy from builder
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json

# Install only production dependencies
RUN npm ci --only=production

# Create non-root user
RUN addgroup -g 1001 -S nodejs && adduser -S nextjs -u 1001
USER nextjs

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD node -e "fetch('http://localhost:3000/api/health').then(r => r.status === 200 ? process.exit(0) : process.exit(1))"

# Start application
ENTRYPOINT ["/sbin/dumb-init", "--"]
CMD ["node_modules/.bin/next", "start"]

EXPOSE 3000
```

**.dockerignore:**
```
node_modules
npm-debug.log
.git
.gitignore
README.md
.env
.env.local
.next
coverage
```

### 11.5 CI/CD Pipeline

**.github/workflows/frontend.yml:**
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  lint-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      
      - name: Lint
        run: npm run lint
      
      - name: Type check
        run: npm run type-check
      
      - name: Unit tests
        run: npm run test:unit
      
      - name: Coverage
        run: npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/lcov.info

  build:
    runs-on: ubuntu-latest
    needs: lint-test
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      
      - name: Build
        run: npm run build
      
      - name: Export
        run: npm run export
      
      - uses: actions/upload-artifact@v3
        with:
          name: build
          path: .next/

  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      
      - name: OWASP dependency check
        run: npm audit --audit-level=moderate

  e2e:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      - run: npm run build
      
      - name: E2E tests
        run: npm run test:e2e
      
      - uses: actions/upload-artifact@v3
        if: always()
        with:
          name: playwright-report
          path: playwright-report/

  deploy-staging:
    runs-on: ubuntu-latest
    needs: [lint-test, build, security, e2e]
    if: github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v4
      
      - name: Deploy to staging
        run: |
          echo "Deploying to staging..."
          # Add your staging deployment command here

  deploy-prod:
    runs-on: ubuntu-latest
    needs: [lint-test, build, security, e2e]
    if: github.ref == 'refs/heads/main'
    environment: production
    steps:
      - uses: actions/checkout@v4
      
      - name: Deploy to production
        run: |
          echo "Deploying to production..."
          # Add your production deployment command here
```

---

## 12. ACCESSIBILITY & MOBILE RESPONSIVENESS

### 12.1 Responsive Design Breakpoints

**Tailwind breakpoints (mobile-first):**
```css
/* Tailwind default breakpoints */
sm:  640px   /* Small devices - tablets */
md:  768px   /* Medium devices - tablets landscape */
lg:  1024px  /* Large devices - desktops */
xl:  1280px  /* Extra large - large desktops */
2xl: 1536px  /* 4K screens */
```

**Usage example:**
```typescript
function ApplicationList() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {applications.map((app) => (
        <ApplicationCard key={app.id} application={app} />
      ))}
    </div>
  );
}
```

### 12.2 WCAG AA Compliance Checklist

**Level AA Accessibility Standards:**

- [ ] **Semantic HTML** - Use `<button>`, `<nav>`, `<main>`, `<section>` for structure
- [ ] **Form Labels** - Every input has associated label with `<label htmlFor="id">`
- [ ] **ARIA Labels** - Custom components have `aria-label` or `aria-labelledby`
- [ ] **Color Contrast** - Text contrast minimum 4.5:1 (normal text)
- [ ] **Focus Indicators** - Visible focus outline on all interactive elements
- [ ] **Keyboard Navigation** - All functionality accessible via keyboard (Tab, Enter, Escape)
- [ ] **Focus Management** - Proper tab order; focus moved to modals/new content
- [ ] **Screen Readers** - Content structure readable by screen readers
- [ ] **Icons** - Icons with text labels or `aria-label`
- [ ] **Links** - Links have descriptive text (avoid "click here")
- [ ] **Images** - Images have meaningful `alt` text
- [ ] **Video/Audio** - Captions provided for video (Phase 2)

### 12.3 Accessibility Testing

**jest-axe integration:**
```typescript
import { render } from '@testing-library/react';
import { axe, toHaveNoViolations } from 'jest-axe';

expect.extend(toHaveNoViolations);

test('ApplicationForm meets WCAG AA', async () => {
  const { container } = render(<ApplicationForm />);
  const results = await axe(container);
  expect(results).toHaveNoViolations();
});
```

---

## 13. DEVELOPER WORKFLOW

### 13.1 Local Development Setup

**Quick start:**
```bash
# Clone repo
git clone https://github.com/your-org/credit-aggregator.git
cd credit-aggregator/frontend

# Install dependencies
npm install

# Copy environment file
cp .env.example .env.local

# Edit .env.local with local API URL
# NEXT_PUBLIC_API_URL=http://localhost:8080/api

# Start dev server
npm run dev

# Visit http://localhost:3000
```

### 13.2 Development Scripts

**package.json scripts:**
```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint --fix",
    "type-check": "tsc --noEmit",
    "format": "prettier --write \"src/**/*.{ts,tsx}\"",
    "test": "jest",
    "test:unit": "jest --testPathPattern=__tests__",
    "test:coverage": "jest --coverage",
    "test:watch": "jest --watch",
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui",
    "test:a11y": "jest --testPathPattern=accessibility",
    "prepare": "husky install",
    "precommit": "lint-staged",
    "analyze": "ANALYZE=true npm run build"
  }
}
```

### 13.3 Git Workflow

**Branch naming:**
```
feature/borrower-dashboard     # New feature
bugfix/form-validation          # Bug fix
refactor/component-extraction   # Refactoring
docs/accessibility-guidelines   # Documentation
chore/upgrade-dependencies      # Maintenance
```

**Commit messages (Conventional Commits):**
```
feat(auth): add password reset functionality
fix(offers): correct APR calculation formula
docs(README): update setup instructions
refactor(components): extract Button variants
test(forms): add validation tests
chore(deps): upgrade React to 18.3.0
```

### 13.4 Code Review Checklist

Before submitting PR, verify:

- [ ] Code follows project style guide (ESLint, Prettier pass)
- [ ] All new components have TypeScript types
- [ ] Unit tests added/updated with >80% coverage
- [ ] No console.log() or debugger statements
- [ ] Accessibility requirements met (WCAG AA)
- [ ] Mobile responsive on 320px, 768px, 1024px
- [ ] Performance-conscious (memoization, lazy loading)
- [ ] Error handling for API calls
- [ ] Documentation added for complex logic
- [ ] No breaking changes to public APIs
- [ ] Dependencies security scan passed

---

## 14. TECHNOLOGY DECISION MATRIX

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

## 15. FUTURE ROADMAP

### Phase 1 (Current - MVP)
- ✅ Borrower portal (registration, application, offer comparison)
- ✅ Bank admin portal (application queue, offer submission)
- ✅ Responsive design (mobile/tablet/desktop)
- ✅ Authentication & authorization
- ✅ Error handling & loading states

### Phase 2 (Scaling)
- [ ] Real-time WebSocket notifications
- [ ] E-signature integration (DocuSign API)
- [ ] Advanced analytics dashboard (bank admin)
- [ ] Internationalization (Romanian language)
- [ ] Dark mode support
- [ ] Mobile native apps (React Native)
- [ ] Performance monitoring (Sentry, DataDog)
- [ ] A/B testing framework

### Phase 3 (Growth)
- [ ] Machine learning predictions (offer recommendations)
- [ ] Social sharing / referral program UI
- [ ] Advanced filtering & search
- [ ] Document management system
- [ ] Compliance reporting dashboard
- [ ] Multi-language support (3+ languages)
- [ ] Progressive Web App (PWA) capabilities

---

## 16. TROUBLESHOOTING GUIDE

### Common Issues

**Issue: Build fails with "Module not found"**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json .next
npm install
npm run build
```

**Issue: Port 3000 already in use**
```bash
# Use different port
PORT=3001 npm run dev

# Or kill existing process
lsof -i :3000
kill -9 <PID>
```

**Issue: API calls return 401 Unauthorized**
```typescript
// Check token is being sent
// 1. Verify token in authStore
console.log(useAuthStore.getState().token);

// 2. Check Authorization header in Network tab
// 3. Verify token hasn't expired
// 4. Check API CORS configuration
```

**Issue: Hydration mismatch error**
```typescript
// Use dynamic imports for client-only components
const Component = dynamic(() => import('@/components/Component'), {
  ssr: false,
  loading: () => <Skeleton />,
});
```

---

## Document Version

**Version:** 1.0  
**Created:** 2026-01-14  
**Last Updated:** 2026-01-14  
**Status:** Ready for Implementation

---

## Next Steps

1. ✅ Share frontend architecture with team for review
2. Review & finalize technology stack decisions
3. Set up development environments locally (Docker)
4. Create component library in Storybook (Phase 2)
5. Begin feature implementation following story order
6. Establish code review process
7. Set up monitoring & error tracking (Sentry)
8. Launch QA testing

---

**End of Frontend Architecture Document** 🎉
