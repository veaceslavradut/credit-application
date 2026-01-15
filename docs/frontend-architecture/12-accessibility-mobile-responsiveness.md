# 12. ACCESSIBILITY & MOBILE RESPONSIVENESS

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
