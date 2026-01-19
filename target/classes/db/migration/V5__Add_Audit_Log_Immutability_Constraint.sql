-- V5__Add_Audit_Log_Immutability_Constraint.sql
-- Prevent any modifications or deletions to audit logs (immutability constraint)

-- Drop existing trigger if it exists
DROP TRIGGER IF EXISTS audit_log_immutability_check ON audit_logs;
DROP FUNCTION IF EXISTS check_audit_log_immutability();

-- Create function to prevent updates and deletes on audit logs
CREATE FUNCTION check_audit_log_immutability()
RETURNS TRIGGER AS ''$$
BEGIN
    IF (TG_OP = ''DELETE'' OR TG_OP = ''UPDATE'') THEN
        RAISE EXCEPTION ''Audit logs are immutable - modification or deletion is not allowed'';
    END IF;
    RETURN NEW;
END;
$$'' LANGUAGE plpgsql;

-- Create trigger for UPDATE and DELETE operations
CREATE TRIGGER audit_log_immutability_check
BEFORE UPDATE OR DELETE ON audit_logs
FOR EACH ROW
EXECUTE FUNCTION check_audit_log_immutability();

-- Add comment to document the constraint
COMMENT ON TRIGGER audit_log_immutability_check ON audit_logs IS 
''Enforces immutability of audit logs - prevents any modifications or deletions. All audit records are permanent and cannot be altered after creation.'';