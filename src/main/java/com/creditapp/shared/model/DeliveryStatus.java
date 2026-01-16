package com.creditapp.shared.model;

/**
 * Email delivery status enumeration
 * Tracks the lifecycle of email delivery
 */
public enum DeliveryStatus {
    /** Email sent to SendGrid API successfully */
    SENT,
    
    /** Email delivered to recipient's inbox */
    DELIVERED,
    
    /** Email bounced (invalid address or inbox full) */
    BOUNCED,
    
    /** Email delivery failed (SendGrid error or network issue) */
    FAILED
}