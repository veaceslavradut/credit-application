package com.creditapp.borrower.exception;

public class ApplicationNotDraftException extends RuntimeException {
    public ApplicationNotDraftException(String message) {
        super(message);
    }

    public ApplicationNotDraftException(String message, Throwable cause) {
        super(message, cause);
    }
}