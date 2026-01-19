-- V24__Create_Deletion_Requests_Table.sql
-- GDPR Article 17 - Right to Erasure: Track user deletion requests

CREATE TABLE deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrower_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    confirmation_token VARCHAR(255) UNIQUE,
    confirmation_token_expires_at TIMESTAMP,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deletion_requests_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_deletion_requests_borrower_status ON deletion_requests(borrower_id, status);
CREATE INDEX idx_deletion_requests_token ON deletion_requests(confirmation_token);
CREATE INDEX idx_deletion_requests_status ON deletion_requests(status);
