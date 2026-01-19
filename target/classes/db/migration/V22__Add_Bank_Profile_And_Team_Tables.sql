-- Add bank profile fields to organizations table
ALTER TABLE organizations ADD COLUMN contact_email VARCHAR(255);
ALTER TABLE organizations ADD COLUMN phone VARCHAR(20);
ALTER TABLE organizations ADD COLUMN address_street VARCHAR(255);
ALTER TABLE organizations ADD COLUMN address_city VARCHAR(100);
ALTER TABLE organizations ADD COLUMN address_state VARCHAR(50);
ALTER TABLE organizations ADD COLUMN address_zip VARCHAR(20);
ALTER TABLE organizations ADD COLUMN website VARCHAR(255);

-- Create bank_team_members table
CREATE TABLE bank_team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_bank FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create indexes for bank_team_members
CREATE INDEX idx_bank_team_members_bank_id ON bank_team_members(bank_id);
CREATE INDEX idx_bank_team_members_email ON bank_team_members(email);
CREATE INDEX idx_bank_team_members_status ON bank_team_members(status);

-- Create team_member_invites table
CREATE TABLE team_member_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    invite_token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_team_invite_bank FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Create indexes for team_member_invites
CREATE INDEX idx_team_invites_bank_id ON team_member_invites(bank_id);
CREATE INDEX idx_team_invites_email ON team_member_invites(email);
CREATE INDEX idx_team_invites_token ON team_member_invites(invite_token);
CREATE INDEX idx_team_invites_status ON team_member_invites(status);
