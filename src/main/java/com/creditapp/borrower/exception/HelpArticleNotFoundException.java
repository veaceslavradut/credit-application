package com.creditapp.borrower.exception;

public class HelpArticleNotFoundException extends RuntimeException {
    public HelpArticleNotFoundException(String topic, String language) {
        super("Help article not found for topic: " + topic + ", language: " + language);
    }
    public HelpArticleNotFoundException(String message) {
        super(message);
    }
}
