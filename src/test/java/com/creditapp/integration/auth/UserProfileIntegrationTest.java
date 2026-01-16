package com.creditapp.integration.auth;

import com.creditapp.auth.dto.ChangePasswordRequest;
import com.creditapp.auth.dto.UpdateProfileRequest;
import com.creditapp.auth.dto.UserProfileResponse;
import com.creditapp.auth.repository.UserRepository;
import com.creditapp.shared.model.User;
import com.creditapp.shared.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String testPassword = "TestPassword123!";
    private String testJwtToken;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test.user@example.com");
        testUser.setPasswordHash(passwordEncoder.encode(testPassword));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("+373-12-345-67");
        testUser.setRole(UserRole.BORROWER);
        testUser.setIsActive(true);
        
        userRepository.save(testUser);
    }

    @Test
    public void testGetProfileSuccessfully() throws Exception {
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateProfile() throws Exception {
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhoneNumber("+373-12-345-68");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    public void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword(testPassword);
        changeRequest.setNewPassword("NewPassword456!");
        changeRequest.setNewPasswordConfirm("NewPassword456!");

        String requestBody = objectMapper.writeValueAsString(changeRequest);

        mockMvc.perform(put("/api/profile/change-password")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    public void testChangePasswordWithInvalidCurrentPassword() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword("WrongPassword123!");
        changeRequest.setNewPassword("NewPassword456!");
        changeRequest.setNewPasswordConfirm("NewPassword456!");

        String requestBody = objectMapper.writeValueAsString(changeRequest);

        mockMvc.perform(put("/api/profile/change-password")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testChangePasswordWithWeakPassword() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword(testPassword);
        changeRequest.setNewPassword("weak");
        changeRequest.setNewPasswordConfirm("weak");

        String requestBody = objectMapper.writeValueAsString(changeRequest);

        mockMvc.perform(put("/api/profile/change-password")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testChangePasswordWithMismatchedConfirmation() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setCurrentPassword(testPassword);
        changeRequest.setNewPassword("NewPassword456!");
        changeRequest.setNewPasswordConfirm("DifferentPassword789!");

        String requestBody = objectMapper.writeValueAsString(changeRequest);

        mockMvc.perform(put("/api/profile/change-password")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProfileWithInvalidPhoneFormat() throws Exception {
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhoneNumber("invalid-phone-format");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}