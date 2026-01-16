package com.creditapp.borrower.exception;

/**
 * Exception thrown when application creation fails unexpectedly.
 * Results in HTTP 500 Internal Server Error response.
 */
public class ApplicationCreationException extends RuntimeException {

    public ApplicationCreationException(String message) {
        super(message);
    }

    public ApplicationCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
