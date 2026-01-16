-- V3__Add_Bank_Status_and_Activation.sql
-- Add bank activation workflow support to organizations table

ALTER TABLE organizations ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING_ACTIVATION';
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS registration_number VARCHAR(50);
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS activation_token VARCHAR(255);
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS activation_token_expires_at TIMESTAMP;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add unique constraints
ALTER TABLE organizations ADD CONSTRAINT uq_registration_number UNIQUE (registration_number);
ALTER TABLE organizations ADD CONSTRAINT uq_activation_token UNIQUE (activation_token);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_organizations_registration_number ON organizations(registration_number);
CREATE INDEX IF NOT EXISTS idx_organizations_status ON organizations(status);
CREATE INDEX IF NOT EXISTS idx_organizations_activation_token ON organizations(activation_token);
