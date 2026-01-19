-- V10__Create_Application_Table.sql
-- Create applications table for borrower loan applications

CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY,
    borrower_id UUID NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_amount DECIMAL(15, 2) NOT NULL,
    loan_term_months INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL,
    rate_preference VARCHAR(20) DEFAULT 'VARIABLE',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    submitted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_applications_borrower FOREIGN KEY (borrower_id)
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_loan_amount CHECK (loan_amount >= 100),
    CONSTRAINT chk_loan_term CHECK (loan_term_months >= 6)
);

-- Create indexes for query performance
CREATE INDEX IF NOT EXISTS idx_applications_borrower_id ON applications(borrower_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_created_at ON applications(created_at);
CREATE INDEX IF NOT EXISTS idx_applications_borrower_created ON applications(borrower_id, created_at);
