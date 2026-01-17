-- Create offer_calculation_log table for audit trail of all calculations
CREATE TABLE offer_calculation_log (
    id BIGSERIAL PRIMARY KEY,
    application_id UUID NOT NULL,
    bank_id UUID,
    calculation_method VARCHAR(255),
    input_parameters JSONB,
    calculated_values JSONB,
    calculation_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_offer_calc_log_application_id 
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_calc_log_bank_id 
        FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE SET NULL
);

-- Create indexes for query performance
CREATE INDEX idx_offer_calc_log_application_id ON offer_calculation_log(application_id);
CREATE INDEX idx_offer_calc_log_bank_id ON offer_calculation_log(bank_id);
CREATE INDEX idx_offer_calc_log_application_timestamp ON offer_calculation_log(application_id, timestamp DESC);