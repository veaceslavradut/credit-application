-- V13: Create Legal Documents Table for Privacy Policy & Terms of Service
-- Story 5.2: Privacy Policy & Terms of Service

CREATE TABLE IF NOT EXISTS legal_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    content TEXT NOT NULL,
    content_hash VARCHAR(255),
    language VARCHAR(10) DEFAULT 'en',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT fk_updated_by_user FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create index for efficient lookups
CREATE INDEX idx_legal_documents_type_status_language 
    ON legal_documents(document_type, status, language);

CREATE INDEX idx_legal_documents_version 
    ON legal_documents(document_type, version DESC);

-- Add audit logging
CREATE TABLE IF NOT EXISTS legal_document_audit (
    id BIGSERIAL PRIMARY KEY,
    legal_document_id UUID NOT NULL,
    document_type VARCHAR(50),
    version INTEGER,
    action VARCHAR(20),
    changed_by UUID,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_legal_doc_audit FOREIGN KEY (legal_document_id) REFERENCES legal_documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_legal_document_audit_doc_id 
    ON legal_document_audit(legal_document_id);

CREATE INDEX idx_legal_document_audit_timestamp 
    ON legal_document_audit(changed_at);