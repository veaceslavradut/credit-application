-- Story 5.6: E-Signature Integration Readiness
-- Create documents table for e-signature infrastructure (Phase 1 - stubbed)

CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500),
    document_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by UUID,
    created_by_ip VARCHAR(45),
    
    CONSTRAINT fk_documents_application FOREIGN KEY (application_id) 
        REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_documents_uploaded_by FOREIGN KEY (uploaded_by) 
        REFERENCES users(id) ON DELETE SET NULL
);

-- Index for efficient queries
CREATE INDEX idx_documents_application_id_type ON documents(application_id, document_type);
CREATE INDEX idx_documents_created_at ON documents(created_at);