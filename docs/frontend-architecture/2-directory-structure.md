# 2. DIRECTORY STRUCTURE

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
