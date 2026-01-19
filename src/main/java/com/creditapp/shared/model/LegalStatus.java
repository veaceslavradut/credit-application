package com.creditapp.shared.model;

/**
 * Enum for legal document status (PUBLISHED or DRAFT)
 */
public enum LegalStatus {
    DRAFT("Document is in draft state, not yet published"),
    PUBLISHED("Document is published and active");

    private final String description;

    LegalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}