package com.creditapp.shared.model;

public enum NotificationChannel {
    EMAIL("Email"),
    IN_APP("In-App"),
    SMS("SMS");

    private final String displayName;

    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
