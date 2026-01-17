package com.creditapp.shared.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankStatus status = BankStatus.PENDING_ACTIVATION;

    @Column(name = "activation_token", unique = true)
    private String activationToken;

    @Column(name = "activation_token_expires_at")
    private LocalDateTime activationTokenExpiresAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "logo_url")
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public BankStatus getStatus() { return status; }
    public void setStatus(BankStatus status) { this.status = status; }
    public String getActivationToken() { return activationToken; }
    public void setActivationToken(String activationToken) { this.activationToken = activationToken; }
    public LocalDateTime getActivationTokenExpiresAt() { return activationTokenExpiresAt; }
    public void setActivationTokenExpiresAt(LocalDateTime activationTokenExpiresAt) { this.activationTokenExpiresAt = activationTokenExpiresAt; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization org = (Organization) o;
        return id != null && id.equals(org.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
