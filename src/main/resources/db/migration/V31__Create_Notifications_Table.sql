-- Story 4.6: Offer Expiration Notification - Task 4
-- Create notifications table for in-portal notifications

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    bank_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    link VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_notifications_bank FOREIGN KEY (bank_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_notifications_bank_created ON notifications(bank_id, created_at DESC);
CREATE INDEX idx_notifications_bank_read ON notifications(bank_id, read_at);

-- Comment
COMMENT ON TABLE notifications IS 'In-portal notifications for banks';
COMMENT ON COLUMN notifications.type IS 'Notification type (e.g., OFFER_EXPIRING, OFFER_EXPIRED)';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when notification was read (NULL = unread)';