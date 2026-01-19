-- V14: Create Data Exports Table for GDPR Data Portability (Story 5.3)

CREATE TABLE data_exports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrower_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    format VARCHAR(10) NOT NULL CHECK (format IN ('JSON', 'PDF')),
    file_url VARCHAR(1024),
    download_token VARCHAR(128) UNIQUE,
    download_token_expires_at TIMESTAMP,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by_ip VARCHAR(64),
    expires_at TIMESTAMP,
    CONSTRAINT fk_data_exports_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for querying exports by borrower and status
CREATE INDEX idx_data_exports_borrower_status ON data_exports(borrower_id, status);

-- Index for token-based downloads
CREATE INDEX idx_data_exports_download_token ON data_exports(download_token) WHERE download_token IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE data_exports IS 'Tracks data export requests for GDPR Article 20 (Right to Portability)';
COMMENT ON COLUMN data_exports.download_token IS 'One-time use token for secure downloads, valid for 24 hours';
COMMENT ON COLUMN data_exports.status IS 'PENDING: export queued, COMPLETED: ready for download, FAILED: generation error';