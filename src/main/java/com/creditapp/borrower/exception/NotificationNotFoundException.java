package com.creditapp.borrower.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID notificationId) {
        super("Notification not found with id: " + notificationId);
    }
    
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
