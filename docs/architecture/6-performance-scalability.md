# 6. PERFORMANCE & SCALABILITY

## 6.1 Performance Targets

| Metric | Target | Approach |
|--------|--------|----------|
| **Offer Calculation** | <500ms | Pre-loaded rate cards in Redis |
| **Page Load Time** | <3 seconds (borrower), <2s (admin) | Next.js SSR, compression, CDN |
| **Time-to-Offer** | <30 min avg | Async offer calculation, real-time notifications |
| **Uptime** | 99.5% | Multi-AZ deployment, auto-failover, monitoring |
| **API Response** | <200ms (p95) | Database indexing, caching, async processing |

## 6.2 Caching Strategy

**Cache Layers:**
1. **Browser Cache** — Static assets (JS, CSS, images) with 1-year max-age
2. **CDN** — CloudFlare for global distribution
3. **Redis Session Cache** — User sessions, refresh tokens (24-hour TTL)
4. **Redis Data Cache** — Rate cards (TTL: 1 hour), exchange rates (TTL: 1 hour)
5. **Database Query Cache** — JPA second-level cache for read-heavy queries

## 6.3 Database Performance

**Indexing Strategy:**
```sql
-- High-query paths
CREATE INDEX ix_applications_borrower ON loan_applications(borrower_id, created_at DESC);
CREATE INDEX ix_applications_status ON loan_applications(application_status, created_at DESC);
CREATE INDEX ix_offers_application ON preliminary_offers(application_id, created_at DESC);
CREATE INDEX ix_offers_org_status ON bank_offers(organization_id, offer_status, created_at DESC);
CREATE INDEX ix_audit_entity ON audit_logs(entity_type, entity_id, created_at DESC);

-- Partial index for active records
CREATE INDEX ix_active_rate_cards ON rate_cards(organization_id) WHERE is_active = true;

-- JSONB index for semi-structured data
CREATE INDEX ix_required_docs ON preliminary_offers USING gin(required_documents);
```

---
