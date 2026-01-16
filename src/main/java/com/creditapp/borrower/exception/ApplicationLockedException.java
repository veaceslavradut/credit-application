package com.creditapp.borrower.exception;

public class ApplicationLockedException extends RuntimeException {
    public ApplicationLockedException(String message) {
        super(message);
    }
}
