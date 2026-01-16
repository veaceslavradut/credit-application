package com.creditapp.borrower.exception;

import java.util.List;

public class SubmissionValidationException extends RuntimeException {
    private final List<String> missingFields;

    public SubmissionValidationException(String message, List<String> missingFields) {
        super(message);
        this.missingFields = missingFields;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}