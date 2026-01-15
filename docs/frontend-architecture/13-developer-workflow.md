# 13. DEVELOPER WORKFLOW

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
