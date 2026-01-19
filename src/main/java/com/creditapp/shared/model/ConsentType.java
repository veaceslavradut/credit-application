package com.creditapp.shared.model;

public enum ConsentType {
    DATA_COLLECTION("Data Collection"),
    BANK_SHARING("Bank Sharing"),
    MARKETING("Marketing"),
    ESIGNATURE("E-Signature");

    private final String description;

    ConsentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}