package com.creditapp.borrower.exception;

public class ApplicationStaleException extends RuntimeException {
    private final Long currentVersion;

    public ApplicationStaleException(String message, Long currentVersion) {
        super(message);
        this.currentVersion = currentVersion;
    }

    public Long getCurrentVersion() {
        return currentVersion;
    }
}