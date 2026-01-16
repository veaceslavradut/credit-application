package com.creditapp.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public PasswordHasher() {
        this.encoder = new BCryptPasswordEncoder(12);
    }

    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public boolean matches(String plainPassword, String hash) {
        return encoder.matches(plainPassword, hash);
    }
}