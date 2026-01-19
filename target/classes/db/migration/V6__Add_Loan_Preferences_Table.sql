-- Create loan_preferences table for borrower loan preferences
CREATE TABLE loan_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    preferred_amount DECIMAL(15, 2),
    min_term INTEGER,
    max_term INTEGER,
    purpose_category VARCHAR(50),
    priority INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX idx_loan_preferences_user_id ON loan_preferences(user_id, created_at);
CREATE INDEX idx_loan_preferences_purpose ON loan_preferences(user_id, purpose_category);