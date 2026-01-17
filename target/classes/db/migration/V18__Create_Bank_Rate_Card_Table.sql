-- Create bank_rate_cards table for storing bank-specific loan rate configurations
CREATE TABLE bank_rate_cards (
    id UUID PRIMARY KEY,
    bank_id UUID NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    min_loan_amount DECIMAL(15, 2) NOT NULL,
    max_loan_amount DECIMAL(15, 2) NOT NULL,
    base_apr DECIMAL(5, 2) NOT NULL,
    apr_adjustment_range DECIMAL(5, 2) NOT NULL,
    origination_fee_percent DECIMAL(5, 2) NOT NULL,
    insurance_percent DECIMAL(5, 2),
    processing_time_days INTEGER NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_bank_rate_cards_bank_id 
        FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE RESTRICT,
    
    -- Validation constraints
    CONSTRAINT chk_base_apr_range 
        CHECK (base_apr >= 0.5 AND base_apr <= 50.0),
    CONSTRAINT chk_origination_fee_range 
        CHECK (origination_fee_percent >= 0 AND origination_fee_percent <= 10.0),
    CONSTRAINT chk_insurance_percent_range 
        CHECK (insurance_percent IS NULL OR (insurance_percent >= 0 AND insurance_percent <= 5.0)),
    CONSTRAINT chk_loan_amounts 
        CHECK (min_loan_amount < max_loan_amount)
);

-- Create indexes for query performance
CREATE INDEX idx_bank_rate_cards_bank_id ON bank_rate_cards(bank_id);
CREATE INDEX idx_bank_rate_cards_loan_type_currency ON bank_rate_cards(loan_type, currency);
CREATE INDEX idx_bank_rate_cards_valid_to ON bank_rate_cards(valid_to);
CREATE INDEX idx_bank_rate_cards_active ON bank_rate_cards(bank_id, valid_to) WHERE valid_to IS NULL;