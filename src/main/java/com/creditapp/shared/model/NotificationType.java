package com.creditapp.shared.model;

public enum NotificationType {
    APPLICATION_SUBMITTED("Application Submitted"),
    APPLICATION_UNDER_REVIEW("Application Under Review"),
    OFFERS_AVAILABLE("Offers Available"),
    OFFER_ACCEPTED("Offer Accepted"),
    APPLICATION_REJECTED("Application Rejected"),
    APPLICATION_EXPIRED("Application Expired"),
    APPLICATION_WITHDRAWN("Application Withdrawn");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
