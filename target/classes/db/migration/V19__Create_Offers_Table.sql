-- Create offers table for storing loan offers made by banks
CREATE TABLE offers (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    bank_id UUID NOT NULL,
    offer_status VARCHAR(50) NOT NULL,
    apr DECIMAL(5, 2) NOT NULL,
    monthly_payment DECIMAL(15, 2) NOT NULL,
    total_cost DECIMAL(15, 2) NOT NULL,
    origination_fee DECIMAL(15, 2) NOT NULL,
    insurance_cost DECIMAL(15, 2),
    processing_time_days INTEGER NOT NULL,
    validity_period_days INTEGER NOT NULL,
    required_documents TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    offer_submitted_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_offers_application_id 
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_offers_bank_id 
        FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE RESTRICT
);

-- Create indexes for query performance
CREATE INDEX idx_offers_application_id ON offers(application_id);
CREATE INDEX idx_offers_bank_id ON offers(bank_id);
CREATE INDEX idx_offers_expires_at ON offers(expires_at);
CREATE INDEX idx_offers_application_bank ON offers(application_id, bank_id);
CREATE INDEX idx_offers_status ON offers(offer_status);