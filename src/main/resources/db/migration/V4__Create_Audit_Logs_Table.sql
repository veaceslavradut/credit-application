-- Audit Logs Table for Compliance and Audit Trail
-- Immutable: no delete or update operations allowed
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor_id UUID,
    actor_role VARCHAR(50),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create index for efficient queries
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id, created_at DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id, created_at DESC);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

-- Archive table for logs older than 3 years (same schema)
CREATE TABLE audit_logs_archive (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor_id UUID,
    actor_role VARCHAR(50),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create index on archive table
CREATE INDEX idx_audit_logs_archive_entity ON audit_logs_archive(entity_type, entity_id, created_at DESC);

-- Comment for documentation
COMMENT ON TABLE audit_logs IS 'Immutable audit trail for compliance (3-year minimum retention). No delete/update operations allowed.';
COMMENT ON TABLE audit_logs_archive IS 'Archive for audit logs older than 3 years. Immutable.';
