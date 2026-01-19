package com.creditapp.bank.service;

import com.creditapp.shared.service.AuditService;
import com.creditapp.bank.dto.BankAddressDTO;
import com.creditapp.bank.dto.BankProfileDTO;
import com.creditapp.bank.dto.UpdateBankProfileRequest;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing bank profile information.
 */
@Service
@Transactional
@Slf4j
public class BankProfileService {
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    public BankProfileService(OrganizationRepository organizationRepository, AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
    }

    /**
     * Retrieve bank profile by bank ID.
     */
    @Transactional(readOnly = true)
    public BankProfileDTO getBankProfile(UUID bankId) {
        log.debug("Retrieving bank profile for bankId: {}", bankId);
        Organization organization = organizationRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank profile not found: " + bankId));
        return mapToDTO(organization);
    }

    /**
     * Update bank profile information.
     */
    public BankProfileDTO updateBankProfile(UUID bankId, UpdateBankProfileRequest request) {
        log.debug("Updating bank profile for bankId: {}", bankId);
        Organization organization = organizationRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank profile not found: " + bankId));

        // Track changes for audit
        StringBuilder changes = new StringBuilder();

        if (request.getName() != null && !request.getName().equals(organization.getName())) {
            changes.append("name: ").append(organization.getName()).append(" -> ").append(request.getName()).append("; ");
            organization.setName(request.getName());
        }

        if (request.getContactEmail() != null && !request.getContactEmail().equals(organization.getContactEmail())) {
            changes.append("contactEmail: ").append(organization.getContactEmail()).append(" -> ").append(request.getContactEmail()).append("; ");
            organization.setContactEmail(request.getContactEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(organization.getPhone())) {
            changes.append("phone: ").append(organization.getPhone()).append(" -> ").append(request.getPhone()).append("; ");
            organization.setPhone(request.getPhone());
        }

        if (request.getWebsite() != null && !request.getWebsite().equals(organization.getWebsite())) {
            changes.append("website: ").append(organization.getWebsite()).append(" -> ").append(request.getWebsite()).append("; ");
            organization.setWebsite(request.getWebsite());
        }

        if (request.getLogoUrl() != null && !request.getLogoUrl().equals(organization.getLogoUrl())) {
            changes.append("logoUrl updated; ");
            organization.setLogoUrl(request.getLogoUrl());
        }

        if (request.getAddress() != null) {
            if (request.getAddress().getStreet() != null && !request.getAddress().getStreet().equals(organization.getAddressStreet())) {
                changes.append("addressStreet updated; ");
                organization.setAddressStreet(request.getAddress().getStreet());
            }
            if (request.getAddress().getCity() != null && !request.getAddress().getCity().equals(organization.getAddressCity())) {
                changes.append("addressCity updated; ");
                organization.setAddressCity(request.getAddress().getCity());
            }
            if (request.getAddress().getState() != null && !request.getAddress().getState().equals(organization.getAddressState())) {
                changes.append("addressState updated; ");
                organization.setAddressState(request.getAddress().getState());
            }
            if (request.getAddress().getZip() != null && !request.getAddress().getZip().equals(organization.getAddressZip())) {
                changes.append("addressZip updated; ");
                organization.setAddressZip(request.getAddress().getZip());
            }
        }

        if (changes.length() > 0) {
            Organization saved = organizationRepository.save(organization);
            // TODO: Use proper AuditAction enum once defined for bank profile updates
            // auditService.logAction("ORGANIZATION", bankId, AuditAction.BANK_PROFILE_UPDATED);
            log.info("Bank profile updated for bankId: {} with changes: {}", bankId, changes);
            return mapToDTO(saved);
        }

        return mapToDTO(organization);
    }

    /**
     * Map Organization entity to BankProfileDTO.
     */
    private BankProfileDTO mapToDTO(Organization organization) {
        BankAddressDTO address = BankAddressDTO.builder()
            .street(organization.getAddressStreet())
            .city(organization.getAddressCity())
            .state(organization.getAddressState())
            .zip(organization.getAddressZip())
            .build();

        return BankProfileDTO.builder()
            .bankId(organization.getId())
            .name(organization.getName())
            .registrationNumber(organization.getRegistrationNumber())
            .contactEmail(organization.getContactEmail())
            .phone(organization.getPhone())
            .address(address)
            .website(organization.getWebsite())
            .logoUrl(organization.getLogoUrl())
            .rateCardsUrl("/api/bank/rate-cards")
            .build();
    }
}
