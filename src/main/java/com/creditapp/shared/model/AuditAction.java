package com.creditapp.shared.model;

public enum AuditAction {
    USER_REGISTERED("User registered"),
    USER_LOGGED_IN("User logged in"),
    USER_LOGGED_OUT("User logged out"),
    PASSWORD_CHANGED("Password changed"),
    PROFILE_UPDATED("Profile updated"),
    BANK_REGISTERED("Bank registered"),
    BANK_ACTIVATED("Bank activated"),
    APPLICATION_CREATED("Application created"),
    APPLICATION_UPDATED("Application updated"),
    APPLICATION_SUBMITTED("Application submitted"),
    APPLICATION_STATUS_CHANGED("Application status changed"),
    APPLICATION_WITHDRAWN("Application withdrawn"),
    APPLICATION_DECLINED("Application declined"),
    DOCUMENT_UPLOADED("Document uploaded"),
    DOCUMENT_DOWNLOADED("Document downloaded"),
    DOCUMENT_DELETED("Document deleted"),
    OFFER_CREATED("Offer created"),
    OFFER_SUBMITTED("Offer submitted by bank officer"),
    OFFER_ACCEPTED("Offer accepted"),
    OFFER_SELECTED("Offer selected by borrower"),
    OFFER_DESELECTED("Offer deselected by borrower"),
    OFFER_SELECTION_FAILED("Offer selection failed"),
    OFFER_WITHDRAWN("Offer withdrawn by bank"),
    APPLICATION_VIEWED("Application viewed"),
    ROLE_ASSIGNED("Role assigned"),
    NOTIFICATION_SENT("Notification sent"),
    RATE_CARD_CREATED("Rate card created"),
    RATE_CARD_UPDATED("Rate card updated"),
    CONSENT_GRANTED("Consent granted"),
    CONSENT_WITHDRAWN("Consent withdrawn"),
    LEGAL_DOCUMENT_UPDATED("Legal document updated"),
    DATA_EXPORT_REQUESTED("Data export requested"),
    DATA_EXPORT_COMPLETED("Data export completed"),
    DATA_EXPORT_DOWNLOADED("Data export downloaded");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}