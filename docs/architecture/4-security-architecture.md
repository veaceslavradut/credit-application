# 4. SECURITY ARCHITECTURE

## 4.1 Authentication & Authorization

**Authentication Flow:**
```
1. User submits email + password
2. Backend validates (bcrypt verify)
3. Generate JWT with claims: sub, role, org_id, exp (15 min)
4. Return access_token + refresh_token (7 days, stored in Redis)
5. Client includes access_token in Authorization header
6. API Gateway validates JWT signature (RS256) + expiration
```

**Authorization (RBAC):**
- Borrowers: View own applications & offers only
- Bank Admins: View organization's applications & offers
- Compliance Officers: View audit logs (read-only)

## 4.2 Data Protection

**Encryption Strategy:**

| Data | Method | Key Management |
|------|--------|-----------------|
| **In Transit** | HTTPS (TLS 1.3) | Let's Encrypt certs |
| **At Rest - Sensitive Fields** | AES-256 (column-level) | AWS KMS or Vault |
| **Passwords** | bcrypt (factor 12) | N/A (one-way) |
| **Sessions** | JWT (RS256) | Rotating key pairs |
| **Audit Logs** | Append-only + AES-256 | Same as above |
| **File Storage** | Server-side encryption (S3) | KMS managed |

## 4.3 API Security

**Rate Limiting:**
- Per-borrower: 10 requests/second
- Per-bank: 100 requests/second
- Burst limit: 50 req/sec for 5 seconds

**Input Validation:**
- JSON Schema validation for all POST/PUT payloads
- SQL injection prevention via parameterized queries (JPA)
- XSS prevention: HTML escaping in responses

---
