# Frontend Development Guide

## Quick Start

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Visit http://localhost:3000
```

## Project Layout

### `/src/pages` - Next.js Pages
- Auth pages (login, register, password reset)
- Borrower portal pages
- Bank admin portal pages
- Compliance officer pages

### `/src/components` - Reusable Components
- Buttons, inputs, forms
- Modals, drawers
- Navigation components
- Layout wrappers

### `/src/features` - Feature Modules
Each feature has its own folder with:
- Components
- Hooks
- Services
- Types
- Stores

Example:
```
features/
 applications/
     components/
     hooks/
     services/
     types/
     store.ts
```

### `/src/services` - API Layer
Encapsulates all backend API calls:
- `authService` - Authentication
- `applicationService` - Applications
- `offerService` - Offers
- `apiClient` - Axios instance

### `/src/stores` - Global State
Zustand stores for global state:
- `authStore` - Current user and auth state
- `uiStore` - Notifications, modals
- `applicationStore` - Draft applications
- `offerStore` - Selected offers

### `/src/hooks` - Custom Hooks
- `useAuth` - Authentication helpers
- `useApi` - React Query wrapper
- `usePagination` - Pagination logic
- `useMediaQuery` - Responsive breakpoints

### `/src/types` - TypeScript Types
- `api.ts` - Backend API response types
- Feature-specific types

### `/src/utils` - Utilities
- `formatters.ts` - Date, currency, number formatting
- `validators.ts` - Form validation
- `errorHandler.ts` - API error handling

### `/src/constants` - Constants
- Route paths
- API endpoints
- Validation rules
- Application enums

## Common Tasks

### Add a New Page

1. Create file in `/src/pages/feature/page-name.tsx`
2. Export default component
3. Add route to constants (optional)

```typescript
// src/pages/borrower/applications/new.tsx
export default function NewApplication() {
  return <div>Create New Application</div>;
}
```

Access at: `http://localhost:3000/borrower/applications/new`

### Add a New API Service

1. Create file in `/src/services/featureService.ts`
2. Use `apiClient` for HTTP calls
3. Import and use in components

```typescript
// src/services/featureService.ts
import apiClient from './apiClient';

export const featureService = {
  async getAll() {
    const { data } = await apiClient.get('/feature');
    return data;
  },
};
```

### Add Global State

1. Create store in `/src/stores/featureStore.ts`
2. Use `create` from Zustand
3. Use `useFeatureStore()` hook in components

```typescript
// src/stores/featureStore.ts
import { create } from 'zustand';

export const useFeatureStore = create((set) => ({
  items: [],
  setItems: (items) => set({ items }),
}));

// In component:
const { items } = useFeatureStore();
```

### Add Form with Validation

```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
});

export default function LoginForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = (data) => {
    console.log(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('email')} />
      {errors.email && <span>{errors.email.message}</span>}
      
      <input {...register('password')} type="password" />
      {errors.password && <span>{errors.password.message}</span>}
      
      <button type="submit">Submit</button>
    </form>
  );
}
```

### Use React Query for Data Fetching

```typescript
import { useQuery } from 'react-query';
import { applicationService } from '@/services/applicationService';

export function ApplicationList() {
  const { data, isLoading, error } = useQuery(
    ['applications'],
    () => applicationService.list()
  );

  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading applications</div>;

  return (
    <ul>
      {data?.content?.map((app) => (
        <li key={app.id}>{app.loanType}</li>
      ))}
    </ul>
  );
}
```

### Add Tailwind Styles

```typescript
export default function Button() {
  return (
    <button className="
      px-4 py-2
      bg-blue-600 hover:bg-blue-700
      text-white font-semibold
      rounded-lg
      transition-colors
    ">
      Click me
    </button>
  );
}
```

## Authentication Flow

1. User logs in at `/auth/login`
2. Credentials sent to backend
3. Token stored in localStorage
4. Token added to all subsequent requests (interceptor)
5. Token added to Authorization header: `Bearer <token>`

Protected routes check `useAuthStore().isAuthenticated`

## Error Handling

API Client interceptor handles:
- 401 (Unauthorized) - Redirect to login
- 4xx (Client errors) - Display error message
- 5xx (Server errors) - Display generic error

UI Store displays notifications:

```typescript
import { useUIStore } from '@/stores/uiStore';

const { addNotification } = useUIStore();

addNotification({
  type: 'success',
  message: 'Application submitted!',
  duration: 3000,
});
```

## Performance Tips

1. **Code Splitting**: Next.js does automatic route-based splitting
2. **Image Optimization**: Use `next/image` component
3. **Bundle Analysis**: `npm run build` shows bundle stats
4. **Lazy Loading**: Use `dynamic()` for heavy components

```typescript
import dynamic from 'next/dynamic';

const HeavyComponent = dynamic(
  () => import('@/components/Heavy'),
  { loading: () => <div>Loading...</div> }
);
```

## Testing

### Write Unit Tests

```typescript
// components/Button.test.tsx
import { render, screen } from '@testing-library/react';
import { Button } from '@/components/Button';

describe('Button', () => {
  it('renders button text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });
});
```

Run tests:
```bash
npm test                    # Run once
npm test:watch             # Watch mode
npm test:coverage          # Coverage report
```

## Debugging

### Browser DevTools

- Network tab: Monitor API calls
- Application tab: View localStorage and cookies
- Console: Check for errors

### VS Code Debugging

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Next.js Client",
      "type": "chrome",
      "request": "launch",
      "url": "http://localhost:3000",
      "webRoot": "${workspaceFolder}",
      "sourceMap": true
    }
  ]
}
```

### React DevTools Browser Extension

Install "React Developer Tools" extension to:
- Inspect component hierarchy
- View and edit component state
- Track re-renders
- Profile performance

## Production Build

```bash
# Build for production
npm run build

# Start production server
npm start

# Or use Docker
docker compose up -d frontend
```

Production optimizations:
- Minified code
- Tree shaking
- Image optimization
- Static export (when applicable)

## Deployment

### To Docker Container

Docker image is built automatically via docker-compose.yml

```bash
docker compose up -d frontend
```

### To Vercel (Next.js native)

```bash
npm install -g vercel
vercel
```

### To Traditional Server

```bash
npm run build
npm start
# Or use PM2:
pm2 start npm --name "frontend" -- start
```

## Troubleshooting

### Changes not reflecting

- Clear browser cache (Ctrl+Shift+Del)
- Restart dev server (Ctrl+C, then `npm run dev`)
- Check for TypeScript errors: `npm run type-check`

### API not responding

- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check CORS headers in browser Network tab
- Verify API URL in `.env.local`

### Build fails

```bash
# Clean dependencies and cache
rm -rf node_modules .next
npm install
npm run build
```

### Port already in use

```bash
# Use different port
PORT=3001 npm run dev
```

## Resources

- [Next.js Docs](https://nextjs.org/docs)
- [React Docs](https://react.dev)
- [TypeScript Docs](https://www.typescriptlang.org/docs)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [Zustand Docs](https://github.com/pmndrs/zustand)
- [React Hook Form Docs](https://react-hook-form.com)
- [Zod Validation](https://zod.dev)

## Questions?

Check the comments in code files or refer to the documented architecture in docs/frontend-architecture.md