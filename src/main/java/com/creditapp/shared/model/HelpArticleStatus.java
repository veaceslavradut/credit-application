package com.creditapp.shared.model;

public enum HelpArticleStatus {
    PUBLISHED("Published"),
    DRAFT("Draft"),
    ARCHIVED("Archived");

    private final String displayName;

    HelpArticleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
