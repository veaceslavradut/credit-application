# Database Schema

Mermaid ER diagram of foundational tables:

```mermaid
erDiagram
    ORGANIZATIONS ||--o{ USERS : has
    USERS ||--o{ USER_ROLES : has
    USERS ||--o{ SESSIONS : has
    USERS ||--o{ AUDIT_LOGS : has

    ORGANIZATIONS {
        UUID id PK
        string name UK
        string tax_id UK
        string country_code
        boolean is_active
        timestamp created_at
    }
    USERS {
        UUID id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        string phone
        string role
        UUID organization_id FK
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    USER_ROLES {
        UUID id PK
        UUID user_id FK
        string role
        timestamp created_at
    }
    SESSIONS {
        UUID id PK
        UUID user_id FK
        string refresh_token UK
        timestamp expires_at
        timestamp created_at
    }
    AUDIT_LOGS {
        UUID id PK
        UUID user_id FK
        string action
        string resource
        timestamp timestamp
        string ip_address
        string result
    }
```

Indexes:
- `users(email)`, `users(organization_id)`
- `audit_logs(timestamp)`
- `sessions(expires_at)`

Constraints:
- Unique: `organizations.name`, `organizations.tax_id`, `users.email`, `sessions.refresh_token`
- Composite Unique: `(user_roles.user_id, user_roles.role)`
- FKs with cascade rules per V1 migration
- Audit logs immutable (DB trigger prevents updates/deletes)
