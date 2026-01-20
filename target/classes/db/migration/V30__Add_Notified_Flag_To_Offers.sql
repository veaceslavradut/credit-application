-- Migration: Add notified flag to offers table for Story 4.6
-- Purpose: Track whether expiration notification has been sent to prevent duplicates

ALTER TABLE offers ADD COLUMN notified BOOLEAN DEFAULT false NOT NULL;

-- Add index for batch job query (expires_at between now and now+24h, notified=false)
CREATE INDEX idx_offers_expiring_soon ON offers(expires_at, notified) WHERE notified = false;

-- Update existing offers to notified=false (already defaulted, but explicit for clarity)
UPDATE offers SET notified = false WHERE notified IS NULL;
