-- V13: Create application_documents table for document upload management
CREATE TABLE application_documents (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    uploaded_by_user_id UUID,
    CONSTRAINT fk_document_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_uploader FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_file_size_positive CHECK (file_size > 0),
    CONSTRAINT chk_document_type CHECK (document_type IN ('INCOME_STATEMENT', 'EMPLOYMENT_VERIFICATION', 'IDENTIFICATION', 'BANK_STATEMENT', 'OTHER'))
);

-- Create indexes for performance
CREATE INDEX idx_documents_application_id ON application_documents(application_id);
CREATE INDEX idx_documents_upload_date ON application_documents(upload_date);
CREATE INDEX idx_documents_application_deleted ON application_documents(application_id, deleted_at);

-- Add comment for documentation
COMMENT ON TABLE application_documents IS 'Stores metadata for documents uploaded by borrowers to support loan applications';
COMMENT ON COLUMN application_documents.stored_filename IS 'UUID-based filename stored on disk or S3 to avoid conflicts';
COMMENT ON COLUMN application_documents.deleted_at IS 'Soft delete timestamp - NULL means document is active';
