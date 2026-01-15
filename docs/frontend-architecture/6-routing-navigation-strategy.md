# 6. ROUTING & NAVIGATION STRATEGY

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
