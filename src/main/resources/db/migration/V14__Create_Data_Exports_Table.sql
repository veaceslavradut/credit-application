-- V14__Create_Data_Exports_Table.sql
-- GDPR Article 20 - Right to Data Portability: Export tracking for borrower data exports

CREATE TABLE data_exports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    borrower_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    format VARCHAR(20) NOT NULL DEFAULT 'JSON',
    file_url VARCHAR(500),
    download_token VARCHAR(50) UNIQUE,
    download_token_expires_at TIMESTAMP,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by_ip VARCHAR(45),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_data_exports_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_data_exports_borrower_status ON data_exports(borrower_id, status);
CREATE INDEX idx_data_exports_download_token ON data_exports(download_token);
