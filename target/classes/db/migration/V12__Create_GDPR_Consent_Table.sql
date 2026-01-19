CREATE TABLE IF NOT EXISTS gdpr_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrower_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    consented_at TIMESTAMP NOT NULL,
    withdrawn_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (borrower_id, consent_type, version)
);

CREATE INDEX idx_gdpr_consents_borrower_type ON gdpr_consents(borrower_id, consent_type);