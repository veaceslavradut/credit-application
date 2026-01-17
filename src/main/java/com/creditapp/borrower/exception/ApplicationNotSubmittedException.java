package com.creditapp.borrower.exception;

public class ApplicationNotSubmittedException extends RuntimeException {
    public ApplicationNotSubmittedException(String message) {
        super(message);
    }

    public ApplicationNotSubmittedException(String message, Throwable cause) {
        super(message, cause);
    }
}