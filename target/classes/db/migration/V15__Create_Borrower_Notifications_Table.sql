-- Create borrower_notifications table for storing notification history
CREATE TABLE borrower_notifications (
    id UUID PRIMARY KEY,
    borrower_id UUID NOT NULL,
    application_id UUID,
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    delivery_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX idx_notifications_borrower_sent ON borrower_notifications(borrower_id, sent_at DESC);
CREATE INDEX idx_notifications_borrower_read ON borrower_notifications(borrower_id, read_at);
CREATE INDEX idx_notifications_status ON borrower_notifications(delivery_status);

-- Add comments for documentation
COMMENT ON TABLE borrower_notifications IS 'Stores all notifications sent to borrowers with delivery tracking';
COMMENT ON COLUMN borrower_notifications.notification_type IS 'Type of notification: APPLICATION_SUBMITTED, APPLICATION_UNDER_REVIEW, etc.';
COMMENT ON COLUMN borrower_notifications.channel IS 'Delivery channel: EMAIL, IN_APP, SMS';
COMMENT ON COLUMN borrower_notifications.delivery_status IS 'Delivery status: PENDING, SENT, FAILED';
