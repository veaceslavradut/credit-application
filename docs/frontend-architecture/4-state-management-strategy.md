# 4. STATE MANAGEMENT STRATEGY

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
