# Data Encryption Strategy

## Overview

This document describes the comprehensive encryption strategy implemented for the Credit Application system to protect sensitive data at rest and in transit, ensuring GDPR compliance and meeting Moldovan NBM encryption requirements.

## Encryption Architecture

### Three-Layer Defense Strategy

#### Layer 1: Transport Layer Security (TLS 1.3)
- **Purpose**: Encrypt all data in transit between clients and servers
- **Implementation**: Spring Boot with TLS 1.3 enforcement
- **Configuration**: `application.yml` server.ssl settings
- **Cipher Suites**:
  - TLS_AES_256_GCM_SHA384
  - TLS_CHACHA20_POLY1305_SHA256
- **HSTS Headers**: Strict-Transport-Security with 1-year max-age, includeSubDomains, preload
- **Certificate**: LetsEncrypt (development/staging), Commercial CA (production)

#### Layer 2: Application Layer Encryption (AES-256-GCM)
- **Purpose**: Encrypt PII fields in database at column level
- **Algorithm**: AES-256-GCM (Advanced Encryption Standard with Galois/Counter Mode)
- **Key Size**: 256 bits
- **Authentication**: GCM provides built-in authentication (detects tampering)
- **Implementation**: Transparent via JPA AttributeConverter
- **Encrypted Fields**:
  - User.email
  - User.firstName
  - User.lastName
  - User.phone
  - User.phoneNumber

#### Layer 3: Password Storage (BCrypt)
- **Purpose**: Irreversible password hashing
- **Algorithm**: BCrypt with adaptive work factor
- **Salt Factor**: 12 (4096 iterations)
- **Salt**: Unique per password (automatic)
- **Performance**: ~250ms per hash (intentionally slow to prevent brute force)

## Key Management

### Phase 1: Local Encryption Keys
- **Provider**: Local key generation (EncryptionService)
- **Storage**: Base64-encoded key in environment variable: `app.encryption.local-key`
- **Key Rotation**: Manual (requires re-encryption of existing data)
- **Use Case**: Development and staging environments

### Phase 2: AWS KMS (Planned)
- **Provider**: AWS Key Management Service
- **Master Key**: Created in AWS KMS (never leaves hardware)
- **Data Encryption Keys (DEKs)**: Generated from master key per encryption operation
- **Key Rotation**: Automatic annual rotation (AWS managed)
- **Access Control**: IAM policies control service access
- **Audit**: All key usage logged to CloudTrail
- **Configuration**:
  ```yaml
  app:
    encryption:
      provider: aws-kms
      kms:
        region: eu-west-1
        key-id: arn:aws:kms:eu-west-1:123456789012:key/...
  ```

### Alternative: HashiCorp Vault (Phase 2)
- **Provider**: Vault Transit Secrets Engine
- **Use Case**: On-premise or multi-cloud deployments
- **Features**: Policy-based access control, key versioning, encryption-as-a-service
- **Configuration**:
  ```yaml
  app:
    encryption:
      provider: vault
      vault:
        url: https://vault.example.com
        token: ${VAULT_TOKEN}
        mount: transit
  ```

## Encryption Implementation

### JPA AttributeConverter Pattern

**How It Works:**
1. Application code uses plaintext values (String)
2. JPA converter automatically encrypts before INSERT/UPDATE
3. JPA converter automatically decrypts after SELECT
4. Transparent to business logic

**Example:**
```java
@Entity
public class User {
    @Column(name = "email")
    @Convert(converter = EncryptedAttributeConverter.class)
    private String email;  // Plaintext in memory, encrypted in DB
}
```

**Encryption Format:**
```
[Base64 Encoded: IV (12 bytes) + Ciphertext + Auth Tag (16 bytes)]
```

### S3 File Encryption (SSE-KMS)

**Purpose**: Encrypt uploaded documents (loan agreements, offers, consents)

**Implementation**: Server-Side Encryption with KMS
- Each file encrypted with unique data encryption key
- Data keys encrypted with KMS master key
- Files never stored unencrypted

**Bucket Policy**: Denies uploads without encryption
```json
{
  "Effect": "Deny",
  "Action": "s3:PutObject",
  "Condition": {
    "StringNotEquals": {
      "s3:x-amz-server-side-encryption": "aws:kms"
    }
  }
}
```

**Configuration**:
```yaml
app:
  s3:
    documents-bucket: credit-app-documents
    region: eu-west-1
```

## PII Redaction in Audit Logs

### Redaction Rules

| Field | Redaction Strategy | Example |
|-------|-------------------|---------|
| Email | Keep domain only | `user@example.com`  `***@example.com` |
| Phone | Complete removal | `+373 69 123456`  `[REDACTED]` |
| Name | First letter only | `John Doe`  `J*** D***` |
| Address | Complete removal | `123 Main St`  `[REDACTED]` |
| Password | Never logged | N/A |
| SSN/National ID | Never logged | N/A |

### Implementation

**Service**: `DataRedactionService`
- Called by `AuditService.sanitizeValues()` before persisting audit logs
- Removes PII fields: name, address, phone, password, ssn
- Redacts email (keeps domain for debugging)

**Example**:
```java
Map<String, Object> details = Map.of(
    "email", "john.doe@example.com",
    "phone", "+373 69 123456",
    "action", "LOGIN"
);

Map<String, Object> redacted = dataRedactionService.redactAuditDetails(details);
// Result: { "email": "***@example.com", "action": "LOGIN" }
// phone removed completely
```

## Secrets Management

### Environment Variables (Current Implementation)

**Sensitive Configuration**:
- `DATABASE_PASSWORD`: PostgreSQL password
- `REDIS_PASSWORD`: Redis password
- `JWT_SECRET`: JWT signing key
- `SENDGRID_API_KEY`: Email service API key
- `SSL_KEYSTORE_PASSWORD`: SSL certificate keystore password
- `app.encryption.local-key`: Encryption key (Base64)

**Storage**:
- Development: `.env` file (gitignored)
- Production: Environment variables set by deployment system (Docker secrets, Kubernetes secrets, AWS Parameter Store)

### Phase 2: Secrets Manager Integration

**AWS Secrets Manager**:
- Automatic rotation for database credentials
- Versioning and rollback support
- Access logged to CloudTrail
- Integration with Spring Cloud AWS

**Kubernetes Secrets**:
- Encrypted at rest in etcd
- Mounted as files or environment variables
- RBAC controls access

## Compliance

### GDPR Requirements Met

| Article | Requirement | Implementation |
|---------|-------------|----------------|
| Article 32 | Encryption of personal data | AES-256-GCM for PII fields, TLS 1.3 for transit |
| Article 32 | Pseudonymisation | Email redaction in logs, encrypted storage |
| Recital 83 | State-of-the-art encryption | TLS 1.3, AES-256-GCM, BCrypt |
| Article 5(1)(f) | Integrity and confidentiality | GCM authentication, HSTS, secure key management |

### Moldovan NBM Requirements

**NBM Regulation 200/2018**: Financial institutions must encrypt customer data
-  PII fields encrypted at rest
-  Encryption keys stored separately (KMS)
-  Audit trail of encryption operations

**Data Protection Law 133/2011**: Aligns with GDPR
-  Encryption as technical measure
-  PII redaction in logs
-  Secure key management

## Key Rotation Procedures

### Manual Rotation (Phase 1 - Local Keys)

1. Generate new encryption key:
   ```bash
   openssl rand -base64 32
   ```

2. Store new key in environment: `app.encryption.local-key-v2`

3. Run migration script to re-encrypt data:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments=--migrate-encryption-keys
   ```

4. Update environment variable to use new key

5. Remove old key after verification

### Automatic Rotation (Phase 2 - AWS KMS)

1. AWS KMS automatically rotates master key annually
2. Old key versions retained for decryption of existing data
3. New encryptions use new key version
4. No application changes required
5. CloudTrail logs all key usage

## Performance Impact

### Encryption Overhead

| Operation | Overhead | Notes |
|-----------|----------|-------|
| AES-256-GCM Encrypt | 5-10ms | Per field |
| AES-256-GCM Decrypt | 5-10ms | Per field |
| BCrypt Hash | ~250ms | Per password (intentional) |
| TLS Handshake | 1-RTT | TLS 1.3 (faster than 1.2) |

### Optimization Strategies

1. **Caching**: Cache decrypted data in Redis for read-heavy workloads
2. **Batch Operations**: Encrypt multiple fields in single transaction
3. **Lazy Loading**: Only decrypt fields when accessed
4. **Connection Pooling**: Reuse TLS connections (HTTP/2)

## Testing

### Unit Tests
- **EncryptionServiceUnitTest**: Test AES-256-GCM encryption/decryption
- **DataRedactionServiceUnitTest**: Test PII redaction logic
- **EncryptedAttributeConverterUnitTest**: Test JPA converter

### Integration Tests
- **EncryptionIntegrationTest**: Test database encryption end-to-end
- **TLSConfigurationTest**: Verify TLS 1.3 enforcement
- **AuditRedactionIntegrationTest**: Verify PII not in audit logs

### Security Scans
- **OWASP Dependency Check**: Detect vulnerable dependencies
- **SonarQube**: Detect hardcoded secrets
- **TLS Scanner**: Verify cipher suite configuration

## Disaster Recovery

### Key Backup

**AWS KMS**:
- Master key backed up automatically by AWS
- Cannot export master key (security feature)
- Use CloudFormation to recreate key with same policy

**Local Keys**:
- Export key to secure offline storage (encrypted USB drive)
- Store in password manager (LastPass, 1Password)
- Encrypt backup with GPG:
  ```bash
  echo $ENCRYPTION_KEY | gpg --encrypt --armor > encryption-key.gpg
  ```

### Data Recovery

**If encryption key lost**:
1. Encrypted data is irrecoverable (by design)
2. Restore from backup if available
3. If no backup, data must be re-collected (GDPR right to erasure may apply)

**If database compromised**:
1. Encrypted data remains protected
2. Rotate encryption keys immediately
3. Audit key access logs
4. Notify affected users per GDPR breach notification

## Monitoring

### Key Metrics

- **Encryption Operation Success Rate**: Should be >99.9%
- **Decryption Errors**: Indicates corrupted data or key mismatch
- **TLS Handshake Failures**: Indicates certificate or cipher issues
- **KMS API Latency**: Should be <50ms

### Alerts

- **Encryption Failure**: Immediate alert to on-call engineer
- **Certificate Expiration**: Alert 30 days before expiry
- **KMS Key Deletion**: Critical alert (prevent accidental deletion)
- **Unauthorized Key Access**: Security team notification

## Migration Strategy

### Migrating Existing Unencrypted Data

**Pre-Migration**:
1. Backup database
2. Verify encryption key configured
3. Test migration on staging environment

**Migration Script**:
```sql
-- Encrypt existing user data
UPDATE users SET 
  email = encrypt_aes_256_gcm(email, :encryption_key),
  first_name = encrypt_aes_256_gcm(first_name, :encryption_key),
  last_name = encrypt_aes_256_gcm(last_name, :encryption_key),
  phone = encrypt_aes_256_gcm(phone, :encryption_key)
WHERE email NOT LIKE 'base64:%';  -- Skip already encrypted
```

**Post-Migration**:
1. Verify all PII fields encrypted
2. Test application functionality
3. Monitor for decryption errors

## Certificate Management

### LetsEncrypt (Development/Staging)

**Installation**:
```bash
certbot certonly --standalone -d app.example.com
```

**Auto-Renewal**:
```bash
# Cron job (runs daily)
0 0 * * * certbot renew --quiet --post-hook "systemctl restart credit-app"
```

**Export to PKCS12**:
```bash
openssl pkcs12 -export \
  -in /etc/letsencrypt/live/app.example.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/app.example.com/privkey.pem \
  -out keystore.p12 \
  -name tomcat \
  -password pass:$KEYSTORE_PASSWORD
```

### Commercial CA (Production)

**Providers**: DigiCert, Sectigo, GlobalSign
**Validity**: 1 year (industry standard)
**Renewal**: 30 days before expiration
**Wildcard**: *.creditapp.com for subdomains

## References

- **GDPR**: https://gdpr-info.eu/
- **Moldovan Data Protection Law**: http://lex.justice.md/
- **NIST Encryption Guidelines**: https://csrc.nist.gov/publications/detail/sp/800-175b/rev-1/final
- **OWASP Cryptographic Storage**: https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html
- **AWS KMS Best Practices**: https://docs.aws.amazon.com/kms/latest/developerguide/best-practices.html

## Contact

For questions about encryption strategy:
- **Security Team**: security@creditapp.com
- **DevOps**: devops@creditapp.com
- **DPO (Data Protection Officer)**: dpo@creditapp.com