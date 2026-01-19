package com.creditapp.shared.model;

import lombok.Getter;

@Getter
public enum SignatureStatus {
    PENDING("Awaiting signature"),
    SIGNED("Successfully signed"),
    REJECTED("Signature rejected"),
    EXPIRED("Signature expired");

    private final String description;

    SignatureStatus(String description) {
        this.description = description;
    }
}