package com.creditapp.unit.security;

import com.creditapp.auth.filter.JwtAuthenticationToken;
import com.creditapp.shared.model.UserRole;
import com.creditapp.shared.security.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationUnitTest {

    private AuthorizationService authorizationService;

    @BeforeEach
    public void setUp() {
        authorizationService = new AuthorizationService();
    }

    @Test
    public void testBorrowerRoleHasAuthority() {
        UUID userId = UUID.randomUUID();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BORROWER"));
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, "borrower@example.com", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertTrue(authorizationService.isBorrower());
        assertFalse(authorizationService.isBankAdmin());
        assertFalse(authorizationService.isComplianceOfficer());
    }

    @Test
    public void testBankAdminRoleHasAuthority() {
        UUID userId = UUID.randomUUID();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BANK_ADMIN"));
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, "admin@bank.com", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertFalse(authorizationService.isBorrower());
        assertTrue(authorizationService.isBankAdmin());
        assertFalse(authorizationService.isComplianceOfficer());
    }

    @Test
    public void testComplianceOfficerRoleHasAuthority() {
        UUID userId = UUID.randomUUID();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("COMPLIANCE_OFFICER"));
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, "compliance@example.com", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertFalse(authorizationService.isBorrower());
        assertFalse(authorizationService.isBankAdmin());
        assertTrue(authorizationService.isComplianceOfficer());
    }

    @Test
    public void testGetCurrentUserId() {
        UUID userId = UUID.randomUUID();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BORROWER"));
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, "borrower@example.com", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertEquals(userId, authorizationService.getCurrentUserId());
    }

    @Test
    public void testGetCurrentUserEmail() {
        UUID userId = UUID.randomUUID();
        String email = "borrower@example.com";
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BORROWER"));
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, email, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        assertEquals(email, authorizationService.getCurrentUserEmail());
    }

    @Test
    public void testNoAuthenticationReturnsNull() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(context);

        assertNull(authorizationService.getCurrentUserId());
        assertNull(authorizationService.getCurrentUserEmail());
        assertNull(authorizationService.getCurrentUserRole());
    }
}