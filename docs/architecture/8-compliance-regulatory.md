# 8. COMPLIANCE & REGULATORY

## 8.1 Data Residency

**Requirement:** All personal data stored in Moldova or EU (NFR5)

**Implementation:**
- PostgreSQL primary DB deployed in Moldovan data center or EU-compliant cloud region
- S3 backup bucket in same region
- No cross-border data transfers without explicit consent

## 8.2 Audit & Compliance

**Key Features:**
- **Audit Trail:** Every transaction logged with timestamp, actor, action, before/after state
- **Immutability:** Database constraints prevent deletion; append-only design
- **Retention:** 7-year retention for loan documents; 3-year for transactional logs
- **Encryption:** Sensitive fields encrypted with AES-256

**Schema:**
```sql
CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  entity_type VARCHAR(100) NOT NULL,
  entity_id UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  actor_id UUID,
  actor_role VARCHAR(50),
  old_values JSONB,
  new_values JSONB,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX (entity_type, entity_id, created_at)
);
```

## 8.3 Consent Management

**Consent Types:**
1. **Data Collection Consent** — Personal data collection
2. **Bank Sharing Consent** — Share application with banks
3. **E-Signature Consent** — Electronic document signing (Phase 2)

**Implementation:**
- Pop-up modal at application submission with checkboxes
- Consent recorded with timestamp, IP, user agent
- Immutable audit log
- Verification: Borrower can request proof anytime

---
