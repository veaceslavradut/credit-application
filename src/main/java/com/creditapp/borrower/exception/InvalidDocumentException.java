package com.creditapp.borrower.exception;

public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String message) {
        super(message);
    }
}
