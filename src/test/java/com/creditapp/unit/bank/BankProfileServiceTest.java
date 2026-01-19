package com.creditapp.unit.bank;

import com.creditapp.bank.dto.BankAddressDTO;
import com.creditapp.bank.dto.BankProfileDTO;
import com.creditapp.bank.dto.UpdateBankProfileRequest;
import com.creditapp.bank.service.BankProfileService;
import com.creditapp.shared.model.Organization;
import com.creditapp.shared.repository.OrganizationRepository;
import com.creditapp.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankProfileServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AuditService auditService;

    private BankProfileService service;
    private UUID bankId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        service = new BankProfileService(organizationRepository, auditService);
        bankId = UUID.randomUUID();

        organization = Organization.builder()
            .id(bankId)
            .name("Test Bank")
            .registrationNumber("REG123")
            .contactEmail("contact@testbank.com")
            .phone("+1234567890")
            .addressStreet("123 Main St")
            .addressCity("Springfield")
            .addressState("IL")
            .addressZip("62701")
            .website("https://testbank.com")
            .logoUrl("https://testbank.com/logo.png")
            .build();
    }

    @Test
    void testGetBankProfile_Success() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(organization));

        BankProfileDTO profile = service.getBankProfile(bankId);

        assertNotNull(profile);
        assertEquals(bankId, profile.getBankId());
        assertEquals("Test Bank", profile.getName());
        assertEquals("REG123", profile.getRegistrationNumber());
        assertEquals("contact@testbank.com", profile.getContactEmail());
        assertEquals("+1234567890", profile.getPhone());
        assertEquals("https://testbank.com", profile.getWebsite());
        assertNotNull(profile.getAddress());
        assertEquals("123 Main St", profile.getAddress().getStreet());
        assertEquals("Springfield", profile.getAddress().getCity());
        assertEquals("IL", profile.getAddress().getState());
        assertEquals("62701", profile.getAddress().getZip());
    }

    @Test
    void testGetBankProfile_NotFound() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getBankProfile(bankId));
    }

    @Test
    void testUpdateBankProfile_SingleField() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

        UpdateBankProfileRequest request = UpdateBankProfileRequest.builder()
            .contactEmail("newemail@testbank.com")
            .build();

        BankProfileDTO updated = service.updateBankProfile(bankId, request);

        assertNotNull(updated);
        assertEquals("newemail@testbank.com", updated.getContactEmail());
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testUpdateBankProfile_MultipleFields() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

        UpdateBankProfileRequest request = UpdateBankProfileRequest.builder()
            .name("Updated Bank Name")
            .phone("+9876543210")
            .website("https://newurl.com")
            .build();

        BankProfileDTO updated = service.updateBankProfile(bankId, request);

        assertNotNull(updated);
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }

    @Test
    void testUpdateBankProfile_NoChanges() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(organization));

        UpdateBankProfileRequest request = UpdateBankProfileRequest.builder().build();

        BankProfileDTO updated = service.updateBankProfile(bankId, request);

        assertNotNull(updated);
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void testUpdateBankProfile_AddressUpdate() {
        when(organizationRepository.findById(bankId)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

        BankAddressDTO newAddress = BankAddressDTO.builder()
            .street("456 Oak Ave")
            .city("Chicago")
            .state("IL")
            .zip("60601")
            .build();

        UpdateBankProfileRequest request = UpdateBankProfileRequest.builder()
            .address(newAddress)
            .build();

        BankProfileDTO updated = service.updateBankProfile(bankId, request);

        assertNotNull(updated);
        verify(organizationRepository, times(1)).save(any(Organization.class));
    }
}
