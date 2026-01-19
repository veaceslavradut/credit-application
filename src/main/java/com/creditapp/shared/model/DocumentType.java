package com.creditapp.shared.model;

import lombok.Getter;

@Getter
public enum DocumentType {
    LOAN_AGREEMENT("Loan Agreement"),
    OFFER_LETTER("Offer Letter"),
    TERMS_CONDITIONS("Terms and Conditions"),
    TERMS_OF_SERVICE("Terms of Service"),
    PRIVACY_POLICY("Privacy Policy"),
    CONSENT_FORM("Consent Form"),
    OTHER("Other");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }
}