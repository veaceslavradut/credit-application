package com.creditapp.auth.exception;

public class DuplicateBankRegistrationException extends RuntimeException {
    public DuplicateBankRegistrationException(String message) {
        super(message);
    }

    public DuplicateBankRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}