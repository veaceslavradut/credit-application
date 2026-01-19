-- Story 5.6: E-Signature Integration Readiness
-- Create signature_logs table for tracking document signing events

CREATE TABLE IF NOT EXISTS signature_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL,
    signer_id UUID NOT NULL,
    signed_at TIMESTAMP,
    ip_address VARCHAR(45),
    signature_certificate TEXT,
    signature_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    signature_id_external VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_signature_logs_document FOREIGN KEY (document_id) 
        REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_signature_logs_signer FOREIGN KEY (signer_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT check_signature_status CHECK (signature_status IN ('PENDING', 'SIGNED', 'REJECTED', 'EXPIRED'))
);

-- Indexes for efficient queries
CREATE INDEX idx_signature_logs_document_id ON signature_logs(document_id);
CREATE INDEX idx_signature_logs_status ON signature_logs(document_id, signature_status);
CREATE INDEX idx_signature_logs_created_at ON signature_logs(created_at);