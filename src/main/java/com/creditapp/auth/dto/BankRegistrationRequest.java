package com.creditapp.auth.dto;

import jakarta.validation.constraints.*;

public class BankRegistrationRequest {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "Registration number must be 5-20 alphanumeric characters")
    private String registrationNumber;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;

    @NotBlank(message = "Admin password is required")
    @Size(min = 12, message = "Password must be at least 12 characters")
    private String adminPassword;

    @NotBlank(message = "Password confirmation is required")
    private String adminPasswordConfirm;

    @NotBlank(message = "Admin phone is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Phone number format is invalid")
    private String adminPhone;

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getAdminFirstName() { return adminFirstName; }
    public void setAdminFirstName(String adminFirstName) { this.adminFirstName = adminFirstName; }
    public String getAdminLastName() { return adminLastName; }
    public void setAdminLastName(String adminLastName) { this.adminLastName = adminLastName; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getAdminPasswordConfirm() { return adminPasswordConfirm; }
    public void setAdminPasswordConfirm(String adminPasswordConfirm) { this.adminPasswordConfirm = adminPasswordConfirm; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
}