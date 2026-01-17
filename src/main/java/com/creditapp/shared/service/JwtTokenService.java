package com.creditapp.shared.service;

import com.creditapp.shared.config.JwtConfig;
import com.creditapp.shared.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtConfig.JwtProperties jwtProperties;

    public JwtTokenService(@Autowired(required = false) JwtConfig.JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public boolean isConfigured() {
        return jwtProperties != null;
    }

    public String generateToken(User user) {
        if (!isConfigured()) return null;
        Date now = new Date();
        long expirationMillis = jwtProperties.getExpirationMinutes() * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("orgId", user.getOrganizationId() != null ? user.getOrganizationId().toString() : null)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtProperties.getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        if (!isConfigured()) return null;
        Date now = new Date();
        long expirationMillis = jwtProperties.getRefreshExpirationDays() * 24 * 60 * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtProperties.getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtProperties.getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("email");
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    public String extractOrgId(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("orgId");
    }

    public Long extractExpiration(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().getTime();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(jwtProperties.getSigningKey()).build().parseSignedClaims(token).getPayload();
    }
}