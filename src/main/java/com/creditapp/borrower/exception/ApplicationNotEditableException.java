package com.creditapp.borrower.exception;

public class ApplicationNotEditableException extends RuntimeException {
    public ApplicationNotEditableException(String message) {
        super(message);
    }

    public ApplicationNotEditableException(String message, Throwable cause) {
        super(message, cause);
    }
}