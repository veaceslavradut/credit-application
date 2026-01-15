# Executive Summary

This document defines a pragmatic, user-centric full-stack architecture for the Credit Aggregator MVP. The system is designed as a two-sided marketplace connecting borrowers with Moldovan banks through a modern, compliant platform optimized for speed, security, and regulatory clarity.

**Core Design Principles:**
1. **User-Centric**: Borrower simplicity (one app) + Bank efficiency (minimal manual work)
2. **Real-Time Calculations**: Preliminary offers in <500ms using configurable rate cards
3. **Regulatory-First**: Data protection, audit trails, and consent management built-in
4. **Scalable-by-Design**: Multi-tenant, microservices-ready backend with Spring Boot
5. **Operational Simplicity**: Monolith-first approach; services split as volume justifies

---
