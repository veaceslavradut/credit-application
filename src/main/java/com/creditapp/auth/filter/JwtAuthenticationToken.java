package com.creditapp.auth.filter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class JwtAuthenticationToken implements Authentication {
    private final UUID userId;
    private final String email;
    private final Collection<GrantedAuthority> authorities;

    public JwtAuthenticationToken(UUID userId, String email, Collection<GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public Object getCredentials() { return null; }
    @Override
    public Object getDetails() { return null; }
    @Override
    public Object getPrincipal() { return email; }
    @Override
    public boolean isAuthenticated() { return true; }
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
    @Override
    public String getName() { return email; }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
}