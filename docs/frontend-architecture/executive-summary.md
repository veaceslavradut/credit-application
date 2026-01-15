# Executive Summary

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
