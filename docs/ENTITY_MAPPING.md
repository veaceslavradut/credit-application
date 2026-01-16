# Entity Mapping

- `Organization` → `organizations`
  - Fields: `id`, `name`, `tax_id`, `country_code`, `is_active`, `created_at`

- `User` → `users`
  - Fields: `id`, `email`, `password_hash`, `first_name`, `last_name`, `phone`, `role`, `organization_id`, `is_active`, `created_at`, `updated_at`
  - Relationships: `@ManyToOne Organization`
  - Entity listener: `AuditLogListener`

- `UserRole` → `user_roles`
  - Fields: `id`, `user_id`, `role`, `created_at`
  - Relationships: `@ManyToOne User`
  - Unique composite: `(user_id, role)`

- `Session` → `sessions`
  - Fields: `id`, `user_id`, `refresh_token`, `expires_at`, `created_at`
  - Relationships: `@ManyToOne User`

- `AuditLog` → `audit_logs` (immutable)
  - Fields: `id`, `user_id`, `action`, `resource`, `timestamp`, `ip_address`, `result`
  - Annotation: `@Immutable`
