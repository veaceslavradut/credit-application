-- V11__Create_Application_Details_Table.sql
-- Create application_details table for optional application information

CREATE TABLE IF NOT EXISTS application_details (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    annual_income DECIMAL(15, 2),
    employment_status VARCHAR(50),
    down_payment_amount DECIMAL(15, 2),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_application_details_application FOREIGN KEY (application_id)
        REFERENCES applications (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uq_application_details_application UNIQUE (application_id)
);

-- Create index for query performance
CREATE INDEX IF NOT EXISTS idx_application_details_application_id ON application_details(application_id);
