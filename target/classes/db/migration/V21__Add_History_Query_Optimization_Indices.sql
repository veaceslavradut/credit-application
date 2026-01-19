-- V21__Add_History_Query_Optimization_Indices.sql
-- Task 6: Query Optimization for History Retrieval
-- Adds database indices to optimize offer and application history queries

-- Index for offer history queries by borrower with sorting
CREATE INDEX idx_offer_borrower_created ON offers(
    (SELECT a.borrower_id FROM applications a WHERE a.id = applications.application_id),
    created_at DESC
) WHERE offer_status != 'EXPIRED';

-- Index for application history queries by borrower with sorting  
CREATE INDEX idx_application_borrower_submitted ON applications(borrower_id, submitted_at DESC);

-- Index for offer count aggregation per application
CREATE INDEX idx_offer_application_count ON offers(application_id, id);

-- Index for best APR calculation per application
CREATE INDEX idx_offer_application_apr ON offers(application_id, apr);

-- Index for application status filtering in history
CREATE INDEX idx_application_borrower_status ON applications(borrower_id, status);

-- Index for date range filtering
CREATE INDEX idx_application_borrower_dates ON applications(
    borrower_id, 
    created_at DESC, 
    submitted_at DESC
);
