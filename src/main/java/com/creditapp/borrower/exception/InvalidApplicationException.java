package com.creditapp.borrower.exception;

/**
 * Exception thrown when application validation fails.
 * Results in HTTP 400 Bad Request response.
 */
public class InvalidApplicationException extends RuntimeException {

    public InvalidApplicationException(String message) {
        super(message);
    }

    public InvalidApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
