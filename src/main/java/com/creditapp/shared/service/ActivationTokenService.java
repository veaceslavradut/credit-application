package com.creditapp.shared.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Service
public class ActivationTokenService {
    private static final int TOKEN_LENGTH = 32;
    private static final String TOKEN_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32}$");
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(TOKEN_CHARSET.length());
            token.append(TOKEN_CHARSET.charAt(randomIndex));
        }
        return token.toString();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return TOKEN_PATTERN.matcher(token).matches();
    }
}