package com.creditapp.shared.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private Expiration expiration;
    private RefreshExpiration refreshExpiration;

    public static class Expiration {
        private Integer minutes;
        public Integer getMinutes() { return minutes; }
        public void setMinutes(Integer minutes) { this.minutes = minutes; }
    }

    public static class RefreshExpiration {
        private Integer days;
        public Integer getDays() { return days; }
        public void setDays(Integer days) { this.days = days; }
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public Expiration getExpiration() { return expiration; }
    public void setExpiration(Expiration expiration) { this.expiration = expiration; }
    public RefreshExpiration getRefreshExpiration() { return refreshExpiration; }
    public void setRefreshExpiration(RefreshExpiration refreshExpiration) { this.refreshExpiration = refreshExpiration; }

    @Bean
    public SecretKey jwtSigningKey() {
        if (secret == null || secret.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secret);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Bean
    public JwtProperties jwtProperties() {
        if (secret == null || secret.isEmpty() || expiration == null || refreshExpiration == null) {
            return null;
        }
        SecretKey signingKey = jwtSigningKey();
        if (signingKey == null) {
            return null;
        }
        return new JwtProperties(secret, expiration.getMinutes(), refreshExpiration.getDays(), signingKey);
    }

    public static class JwtProperties {
        private final String secret;
        private final Integer expirationMinutes;
        private final Integer refreshExpirationDays;
        private final SecretKey signingKey;

        public JwtProperties(String secret, Integer expirationMinutes, Integer refreshExpirationDays, SecretKey signingKey) {
            this.secret = secret;
            this.expirationMinutes = expirationMinutes;
            this.refreshExpirationDays = refreshExpirationDays;
            this.signingKey = signingKey;
        }

        public String getSecret() { return secret; }
        public Integer getExpirationMinutes() { return expirationMinutes; }
        public Integer getRefreshExpirationDays() { return refreshExpirationDays; }
        public SecretKey getSigningKey() { return signingKey; }
    }
}