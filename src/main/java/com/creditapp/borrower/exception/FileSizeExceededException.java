package com.creditapp.borrower.exception;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(String message) {
        super(message);
    }
}
