-- V25__Add_Deletion_Support_To_Users_Table.sql
-- Add deleted_at and enabled columns to support soft deletion and user disable functionality

ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE;

CREATE INDEX idx_users_deleted_at ON users(deleted_at);
