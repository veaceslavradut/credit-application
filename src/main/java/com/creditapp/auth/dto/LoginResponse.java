package com.creditapp.auth.dto;

import com.creditapp.shared.model.UserRole;
import java.util.UUID;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;

    public LoginResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn, UserDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public UserDTO getUser() { return user; }

    public static class UserDTO {
        private UUID id;
        private String email;
        private UserRole role;
        private String firstName;
        private String lastName;
        private UUID organizationId;

        public UserDTO(UUID id, String email, UserRole role, String firstName, String lastName, UUID organizationId) {
            this.id = id;
            this.email = email;
            this.role = role;
            this.firstName = firstName;
            this.lastName = lastName;
            this.organizationId = organizationId;
        }

        public UUID getId() { return id; }
        public String getEmail() { return email; }
        public UserRole getRole() { return role; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public UUID getOrganizationId() { return organizationId; }
    }
}