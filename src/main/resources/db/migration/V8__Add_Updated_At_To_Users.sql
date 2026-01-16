-- Add updated_at column to users table for tracking profile updates
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create index on updated_at for efficient querying
CREATE INDEX IF NOT EXISTS idx_users_updated_at ON users(updated_at);