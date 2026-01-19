-- V12__Create_Application_History_Table.sql
-- Create application_history table to track status transitions

CREATE TABLE IF NOT EXISTS application_history (
    id BIGSERIAL PRIMARY KEY,
    application_id UUID NOT NULL,
    old_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    changed_by_user_id UUID,
    change_reason VARCHAR(500),
    CONSTRAINT fk_application_history_application FOREIGN KEY (application_id)
        REFERENCES applications (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_application_history_user FOREIGN KEY (changed_by_user_id)
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- Create indexes for query performance
CREATE INDEX IF NOT EXISTS idx_application_history_application_id ON application_history(application_id);
CREATE INDEX IF NOT EXISTS idx_application_history_application_changed ON application_history(application_id, changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_application_history_changed_at ON application_history(changed_at DESC);
