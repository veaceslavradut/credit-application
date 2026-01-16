-- Add revocation support to refresh_tokens table
ALTER TABLE refresh_tokens ADD COLUMN revoked BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMP;

-- Create index for efficient revocation queries
CREATE INDEX idx_refresh_tokens_validation ON refresh_tokens(user_id, revoked, expires_at);