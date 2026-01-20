package com.creditapp.shared.model;

public enum ComplianceStatus {
    RED("Not Compliant", "not_done"),
    YELLOW("In Progress", "in_progress"),
    GREEN("Compliant", "done");

    private final String displayName;
    private final String code;

    ComplianceStatus(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }
}