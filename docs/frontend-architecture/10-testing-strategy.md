# 10. TESTING STRATEGY

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
