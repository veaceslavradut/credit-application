-- V1__Initial_Schema.sql
-- Foundational schema for Credit Application Platform

CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    tax_id VARCHAR(64) NOT NULL UNIQUE,
    country_code VARCHAR(3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    organization_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role)
);

CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    action VARCHAR(32) NOT NULL,
    resource VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(64),
    result VARCHAR(32) NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- Immutability: prevent UPDATE/DELETE on audit_logs
CREATE OR REPLACE FUNCTION prevent_audit_log_updates()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs are immutable';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS audit_logs_prevent_update ON audit_logs;
CREATE TRIGGER audit_logs_prevent_update
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_updates();
