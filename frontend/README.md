# Credit Application Platform - Frontend

Modern React-based frontend for the Credit Aggregator MVP platform.

## Tech Stack

- **Framework**: Next.js 14 with App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: Zustand (global) + React Query (server state)
- **Forms**: React Hook Form + Zod validation
- **HTTP Client**: Axios with interceptors
- **Testing**: Jest + React Testing Library
- **Build**: Next.js with SWC minifier

## Project Structure

```
frontend/
 src/
    pages/               # Next.js pages
       auth/           # Login, register, password reset
       borrower/       # Borrower portal
       bank/           # Bank admin portal
       compliance/     # Compliance officer portal
    components/         # Reusable UI components
    features/           # Feature-based modules
    services/           # API communication
    stores/             # Zustand stores (global state)
    hooks/              # Custom React hooks
    types/              # TypeScript types
    utils/              # Utility functions
    constants/          # Application constants
    styles/             # Global styles
 public/                 # Static assets
 Dockerfile              # Multi-stage build
 next.config.js          # Next.js config
 tsconfig.json           # TypeScript config
 tailwind.config.js      # Tailwind config
 package.json            # Dependencies
```

## Getting Started

### Prerequisites
- Node.js 20+
- npm 10+

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm start
```

The frontend will be available at `http://localhost:3000`

## Development

### Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

### Available Scripts

```bash
# Development
npm run dev              # Start dev server with hot reload
npm run build            # Build for production
npm start                # Run production build

# Testing & Quality
npm test                 # Run tests
npm test:watch          # Run tests in watch mode
npm test:coverage       # Generate coverage report
npm run lint             # Run ESLint
npm run type-check      # TypeScript type checking
npm run format           # Format code with Prettier
npm run format:check    # Check if code is formatted
```

## API Integration

The frontend connects to the backend API at `http://localhost:8080/api`.

### Service Layer

API calls are organized in `/src/services`:

- `authService.ts` - Authentication endpoints
- `applicationService.ts` - Application CRUD operations
- `offerService.ts` - Offer management

### Example: Creating an Application

```typescript
import { applicationService } from '@/services/applicationService';

const application = await applicationService.create({
  loanType: 'personal',
  loanAmount: 50000,
  loanTermMonths: 60,
  currency: 'MDL',
  ratePreference: 'fixed',
});
```

## State Management

### Zustand Stores

Global state is managed with Zustand:

- `authStore.ts` - User authentication state
- `uiStore.ts` - UI notifications and modals
- `applicationStore.ts` - Draft applications
- `offerStore.ts` - Selected offers

### Example: Using Auth Store

```typescript
import { useAuthStore } from '@/stores/authStore';

export function Profile() {
  const { user, logout } = useAuthStore();

  return (
    <div>
      <p>{user?.email}</p>
      <button onClick={logout}>Logout</button>
    </div>
  );
}
```

## Portal Features

### Borrower Portal

-  Dashboard with application overview
-  Create new loan application
-  View submitted applications
-  Track application status
-  Compare offers
-  Accept/reject offers
-  Profile management

### Bank Admin Portal

-  Application queue
-  Application details view
-  Submit offers
-  Rate card management
-  Dashboard metrics

### Compliance Officer Portal

-  Audit logs
-  Compliance reports
-  Data export

## Docker Deployment

### Build Docker Image

```bash
docker build -f Dockerfile -t credit-app-frontend:latest .
```

### Run with Docker Compose

```bash
# From project root
docker compose up -d frontend
```

The frontend will be available at `http://localhost:3000`

## Production Build

The production build uses multi-stage Docker build:

1. **Dependencies** - Install production dependencies
2. **Builder** - Build Next.js app with optimizations
3. **Runtime** - Minimal image with only needed files

Environment variables for production:

```env
NEXT_PUBLIC_API_URL=https://api.example.com
NEXT_PUBLIC_WS_URL=wss://api.example.com/ws
NODE_ENV=production
```

## Testing

### Unit & Integration Tests

```bash
npm test

# Watch mode
npm test:watch

# Coverage report
npm test:coverage
```

### E2E Testing (Playwright - coming soon)

```bash
npx playwright test
```

## Performance

- **Code Splitting**: Automatic with Next.js
- **Image Optimization**: Next.js Image component
- **CSS Optimization**: Tailwind CSS purging
- **Font Optimization**: Built-in with Next.js

## Security

-  TypeScript for type safety
-  Content Security Policy headers
-  CORS protection
-  Input validation with Zod
-  Secure token storage in localStorage
-  HTTPS ready (with proper certificates in production)

## Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Troubleshooting

### Port 3000 in use

```bash
# Find process using port 3000
lsof -i :3000

# Kill process
kill -9 <PID>

# Or use different port
PORT=3001 npm run dev
```

### API Connection Issues

1. Verify backend is running: `http://localhost:8080/actuator/health`
2. Check `NEXT_PUBLIC_API_URL` environment variable
3. Check CORS configuration on backend
4. View network requests in browser DevTools

### Build Failures

```bash
# Clear build cache
rm -rf .next node_modules

# Reinstall and rebuild
npm install
npm run build
```

## Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and test
3. Commit: `git commit -am 'Add feature'`
4. Push: `git push origin feature/your-feature`
5. Create Pull Request

## License

Internal project. Do not distribute.