-- Story 5.5: Audit Trail Immutability & add result column
-- Add result column to audit_logs with default 'SUCCESS'
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS';

-- Create guard function to prevent UPDATE/DELETE on audit_logs
CREATE OR REPLACE FUNCTION audit_logs_guard()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs are immutable; % operations are not allowed', TG_OP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Drop existing update/delete triggers if any
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'audit_logs_no_update'
    ) THEN
        DROP TRIGGER audit_logs_no_update ON audit_logs;
    END IF;
    IF EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'audit_logs_no_delete'
    ) THEN
        DROP TRIGGER audit_logs_no_delete ON audit_logs;
    END IF;
END;
$$;

-- Add immutability triggers
CREATE TRIGGER audit_logs_no_update
BEFORE UPDATE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION audit_logs_guard();

CREATE TRIGGER audit_logs_no_delete
BEFORE DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION audit_logs_guard();
