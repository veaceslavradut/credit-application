package com.creditapp.borrower.exception;

public class ApplicationAlreadySubmittedException extends RuntimeException {
    public ApplicationAlreadySubmittedException(String message) {
        super(message);
    }
}