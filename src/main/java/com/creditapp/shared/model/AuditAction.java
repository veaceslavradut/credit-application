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
    APPLICATION_SUBMITTED("Application submitted"),
    APPLICATION_STATUS_CHANGED("Application status changed"),
    OFFER_CREATED("Offer created"),
    OFFER_ACCEPTED("Offer accepted"),
    APPLICATION_VIEWED("Application viewed"),
    ROLE_ASSIGNED("Role assigned");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}