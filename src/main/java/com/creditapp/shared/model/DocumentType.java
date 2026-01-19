package com.creditapp.shared.model;

/**
 * Enum for types of legal documents that need versioning and tracking
 */
public enum DocumentType {
    PRIVACY_POLICY("Privacy Policy"),
    TERMS_OF_SERVICE("Terms of Service");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}