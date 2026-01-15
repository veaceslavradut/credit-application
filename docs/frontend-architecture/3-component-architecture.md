# 3. COMPONENT ARCHITECTURE

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
