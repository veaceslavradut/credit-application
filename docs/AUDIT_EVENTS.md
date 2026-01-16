# Audit Events Documentation

## Overview

This document describes all audit events tracked by the Credit Application Platform. All audit events are immutable and stored in the `audit_logs` table for compliance and regulatory purposes.

## Audit Event Categories

### Authentication Events

#### USER_REGISTERED
- **Description**: User registered as a borrower
- **Entity Type**: `User`
- **Actor**: System or admin registering the user
- **Trigger Point**: [AuthController.java](../../src/main/java/com/creditapp/auth/controller/AuthController.java#L49) - `/api/auth/register`
- **Captured Fields**: email, firstName, lastName, phone
- **Retention**: 3 years

#### USER_LOGGED_IN
- **Description**: User successfully logged in
- **Entity Type**: `User`
- **Actor**: The user logging in
- **Trigger Point**: [AuthController.java](../../src/main/java/com/creditapp/auth/controller/AuthController.java#L73) - `/api/auth/login`
- **Request Context**: IP Address, User-Agent captured
- **Retention**: 3 years

#### USER_LOGGED_OUT
- **Description**: User logged out
- **Entity Type**: `User`
- **Actor**: The user logging out
- **Trigger Point**: [AuthController.java](../../src/main/java/com/creditapp/auth/controller/AuthController.java#L143) - `/api/auth/logout`
- **Request Context**: IP Address, User-Agent captured
- **Retention**: 3 years

#### PASSWORD_CHANGED
- **Description**: User changed password
- **Entity Type**: `User`
- **Actor**: The user changing password
- **Captured Fields**: passwordHash (redacted)
- **Retention**: 3 years

### Bank Management Events

#### BANK_REGISTERED
- **Description**: Bank registered in the system
- **Entity Type**: `Organization`
- **Actor**: Bank representative or system admin
- **Trigger Point**: [AuthController.java](../../src/main/java/com/creditapp/auth/controller/AuthController.java#L59) - `/api/auth/register-bank`
- **Captured Fields**: name, registrationNumber, taxId
- **Retention**: 3 years

#### BANK_ACTIVATED
- **Description**: Bank account activated via activation token
- **Entity Type**: `Organization`
- **Actor**: System (activation token verification)
- **Trigger Point**: [AuthController.java](../../src/main/java/com/creditapp/auth/controller/AuthController.java#L104) - `/api/auth/activate`
- **Captured Fields**: status, activatedAt
- **Retention**: 3 years

### Profile & Account Events

#### PROFILE_UPDATED
- **Description**: User profile information updated
- **Entity Type**: `User`
- **Actor**: The user or admin updating profile
- **Captured Fields**: firstName, lastName, phone, status
- **Trigger Point**: JPA EntityListener on User entity update
- **Retention**: 3 years

### Application Lifecycle Events

#### APPLICATION_CREATED
- **Description**: Credit application created
- **Entity Type**: `Application`
- **Actor**: Borrower creating application
- **Trigger Point**: Application creation endpoint
- **Retention**: 3 years

#### APPLICATION_SUBMITTED
- **Description**: Credit application submitted for review
- **Entity Type**: `Application`
- **Actor**: Borrower submitting application
- **Trigger Point**: Application submission endpoint
- **Retention**: 3 years

#### APPLICATION_STATUS_CHANGED
- **Description**: Application status changed by bank/admin
- **Entity Type**: `Application`
- **Actor**: Bank officer or system
- **Captured Fields**: previousStatus, newStatus, reason
- **Retention**: 3 years

#### APPLICATION_VIEWED
- **Description**: Application details viewed
- **Entity Type**: `Application`
- **Actor**: Bank officer accessing application
- **Request Context**: IP Address, User-Agent
- **Retention**: 3 years

### Offer Management Events

#### OFFER_CREATED
- **Description**: Bank created loan offer for applicant
- **Entity Type**: `Offer`
- **Actor**: Bank officer
- **Captured Fields**: offerAmount, interestRate, terms
- **Retention**: 3 years

#### OFFER_ACCEPTED
- **Description**: Borrower accepted loan offer
- **Entity Type**: `Offer`
- **Actor**: Borrower
- **Trigger Point**: Offer acceptance endpoint
- **Retention**: 3 years

### Authorization Events

#### ROLE_ASSIGNED
- **Description**: Role assigned to user (BORROWER, BANK_OFFICER, COMPLIANCE_OFFICER, ADMIN)
- **Entity Type**: `User`
- **Actor**: System or admin assigning role
- **Captured Fields**: role, permissions
- **Retention**: 3 years

## Sensitive Field Sanitization

The following fields are automatically redacted in audit logs to protect sensitive information:

- `password`, `passwordHash`
- `refreshToken`, `accessToken`, `apiKey`
- `secret`, `token`
- `ssn` (Social Security Number)
- `creditCard` (Credit card numbers)
- `bankAccount` (Bank account numbers)

All sensitive fields appear as `[REDACTED]` in audit log entries.

## Immutability & Compliance

### Immutability Constraints
- Audit logs cannot be modified after creation
- Database trigger enforces immutability at data layer
- Attempted modifications/deletions raise exception: "Audit logs are immutable - modification or deletion is not allowed"

### Retention Policy
- Active logs: 3 years in `audit_logs` table
- Archived logs: Moved to `audit_logs_archive` table after 3 years
- Daily archival scheduled at 2:00 AM UTC
- Archival process: Transparent to audit integrity

## Database Schema

### audit_logs Table
```sql
CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  entity_type VARCHAR(255) NOT NULL,
  entity_id UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  actor_id UUID,
  actor_role VARCHAR(50),
  old_values JSONB,
  new_values JSONB,
  ip_address VARCHAR(45),
  user_agent TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Indexes
- `idx_entity_type_id`: Fast lookup by entity
- `idx_actor_id`: Fast lookup by actor/user
- `idx_created_at`: Fast lookup by date range

## REST API Access

### Compliance Officer Endpoints

#### Get Audit Logs
```
GET /api/compliance/audit-logs?entityType=User&startDate=2024-01-01&endDate=2024-12-31&page=0&size=20
Authorization: Bearer {token} (COMPLIANCE_OFFICER role required)
```

#### Get Single Audit Log
```
GET /api/compliance/audit-logs/{id}
Authorization: Bearer {token} (COMPLIANCE_OFFICER role required)
```

#### Get User Audit Logs
```
GET /api/compliance/audit-logs/user/{userId}?page=0&size=20
Authorization: Bearer {token} (COMPLIANCE_OFFICER role required)
```

## Application Configuration

### Properties
```properties
# Audit retention period (in years)
audit.retention.years=3

# Scheduled archival (cron format: sec min hour day month day-of-week)
# Default: 0 0 2 * * * (Daily at 2:00 AM)
```

## Compliance & Regulatory

- **GDPR**: Right to be forgotten exceptions for permanent audit logs
- **PCI-DSS**: Credit card data sanitization in logs
- **SOX**: Immutable audit trail for financial transactions
- **HIPAA**: Sensitive health data redaction (if applicable)

## References

- [AuditLog Entity](../../src/main/java/com/creditapp/shared/model/AuditLog.java)
- [AuditService](../../src/main/java/com/creditapp/shared/service/AuditService.java)
- [ComplianceController](../../src/main/java/com/creditapp/compliance/controller/ComplianceController.java)
- [Database Migrations](../db/migration/)