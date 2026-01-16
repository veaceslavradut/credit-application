package com.creditapp.borrower.exception;

/**
 * Exception thrown when an application is not found.
 */
public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }

    public ApplicationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
