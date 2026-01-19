-- Add withdrawal fields to applications table for Story 2.8
ALTER TABLE applications ADD COLUMN withdrawn_at TIMESTAMP;
ALTER TABLE applications ADD COLUMN withdrawal_reason VARCHAR(500);

-- Add index for withdrawn applications
CREATE INDEX idx_applications_withdrawn_at ON applications(withdrawn_at);
