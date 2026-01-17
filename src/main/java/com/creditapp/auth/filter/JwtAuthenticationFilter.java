package com.creditapp.auth.filter;

import com.creditapp.shared.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null && jwtTokenService.validateToken(token)) {
                UUID userId = jwtTokenService.extractUserId(token);
                String email = jwtTokenService.extractEmail(token);
                String role = jwtTokenService.extractRole(token);
                
                // Extract organizationId for bank admins
                UUID organizationId = null;
                String orgIdStr = jwtTokenService.extractOrgId(token);
                if (orgIdStr != null && !orgIdStr.isEmpty()) {
                    try {
                        organizationId = UUID.fromString(orgIdStr);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Invalid organizationId in JWT: {}", orgIdStr);
                    }
                }

                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(role));

                JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, email, authorities, organizationId);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("JWT token validated for user: {} ({})", userId, email);
            }
        } catch (Exception e) {
            logger.debug("JWT token validation failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}