package com.creditapp.borrower.exception;

public class InvalidOfferException extends RuntimeException {

    public InvalidOfferException(String message) {
        super(message);
    }
}