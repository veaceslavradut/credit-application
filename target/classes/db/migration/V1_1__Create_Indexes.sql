-- V1_1__Create_Indexes.sql

-- Users performance indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_organization_id ON users (organization_id);

-- Audit logs index for compliance queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs (timestamp);

-- Sessions cleanup support
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions (expires_at);
