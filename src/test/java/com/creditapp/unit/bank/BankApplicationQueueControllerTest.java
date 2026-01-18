package com.creditapp.unit.bank;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.creditapp.bank.dto.ApplicationQueueItem;
import com.creditapp.bank.dto.ApplicationQueueRequest;
import com.creditapp.bank.dto.ApplicationQueueResponse;
import com.creditapp.bank.dto.ApplicationStatusUpdateRequest;
import com.creditapp.bank.dto.ApplicationStatusUpdateResponse;
import com.creditapp.bank.service.ApplicationQueueService;
import com.creditapp.bank.service.ApplicationStatusUpdateService;
import com.creditapp.shared.model.ApplicationQueueMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(authorities = "BANK_OFFICER")
class BankApplicationQueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationQueueService applicationQueueService;

    @MockBean
    private ApplicationStatusUpdateService applicationStatusUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID bankId;
    private UUID applicationId;

    @BeforeEach
    void setUp() {
        bankId = UUID.randomUUID();
        applicationId = UUID.randomUUID();
    }

    @Test
    void testGetApplicationQueueReturnsQueue() throws Exception {
        ApplicationQueueItem item1 = ApplicationQueueItem.builder()
            .applicationId(applicationId)
            .referenceNumber("APP001")
            .borrowerName("John Doe")
            .borrowerEmail("john@example.com")
            .borrowerPhone("+1234567890")
            .loanAmount(BigDecimal.valueOf(100000))
            .termMonths(36)
            .approvalStatus("PENDING")
            .selectedOfferAPR(BigDecimal.valueOf(5.5))
            .selectedOfferMonthlyPayment(BigDecimal.valueOf(2500))
            .lastUpdatedAt(LocalDateTime.now())
            .documentsStatus("SUBMITTED")
            .build();

        List<ApplicationQueueItem> items = new ArrayList<>();
        items.add(item1);

        ApplicationQueueMetrics metrics = ApplicationQueueMetrics.builder()
            .totalApplications(1)
            .documentsAwaitingReview(0)
            .approvedCount(0)
            .rejectedCount(0)
            .build();

        ApplicationQueueResponse response = ApplicationQueueResponse.builder()
            .applications(items)
            .totalCount((int)1)
            .limit(20)
            .offset(0)
            .hasMore(false)
            .queueMetrics(metrics)
            .retrievedAt(LocalDateTime.now())
            .build();

        when(applicationQueueService.getApplicationQueue(eq(bankId), any(ApplicationQueueRequest.class)))
            .thenReturn(response);

        mockMvc.perform(get("/api/bank/dashboard/application-queue")
            .param("bankId", bankId.toString())
            .param("limit", "20")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("totalCount").value(1))
            .andExpect(jsonPath("applications[0].borrowerName").value("John Doe"))
            .andExpect(jsonPath("queueMetrics.totalApplications").value(1));
    }

    @Test
    void testGetApplicationQueueWithFilters() throws Exception {
        ApplicationQueueResponse response = ApplicationQueueResponse.builder()
            .applications(new ArrayList<>())
            .totalCount((int)0)
            .limit(20)
            .offset(0)
            .hasMore(false)
            .queueMetrics(ApplicationQueueMetrics.builder()
                .totalApplications(0)
                .documentsAwaitingReview(0)
                .approvedCount(0)
                .rejectedCount(0)
                .build())
            .retrievedAt(LocalDateTime.now())
            .build();

        when(applicationQueueService.getApplicationQueue(eq(bankId), any(ApplicationQueueRequest.class)))
            .thenReturn(response);

        mockMvc.perform(get("/api/bank/dashboard/application-queue")
            .param("bankId", bankId.toString())
            .param("limit", "20")
            .param("offset", "0")
            .param("status", "PENDING")
            .param("aprMin", "3.0")
            .param("aprMax", "7.0")
            .param("loanAmountMin", "50000")
            .param("loanAmountMax", "200000")
            .param("sortBy", "lastUpdatedAt")
            .param("sortOrder", "DESC")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("totalCount").value(0));
    }

    @Test
    void testUpdateApplicationStatusReturnsUpdated() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setReason("Approved by bank officer");
        request.setComments("Good credit score");

        ApplicationStatusUpdateResponse response = ApplicationStatusUpdateResponse.builder()
            .applicationId(applicationId)
            .previousStatus("OFFERS_AVAILABLE")
            .newStatus("ACCEPTED")
            .changedAt(LocalDateTime.now())
            .updatedApplication(null)
            .build();

        when(applicationStatusUpdateService.updateApplicationStatus(
            eq(applicationId),
            eq(bankId),
            any(ApplicationStatusUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put("/api/bank/dashboard/applications/{applicationId}/status", applicationId)
            .param("bankId", bankId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicationId").value(applicationId.toString()))
            .andExpect(jsonPath("previousStatus").value("OFFERS_AVAILABLE"))
            .andExpect(jsonPath("newStatus").value("ACCEPTED"));
    }

    @Test
    void testUpdateApplicationStatusWithMissingStatus() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setReason("Approved by bank officer");
        request.setComments("Good credit score");

        mockMvc.perform(put("/api/bank/dashboard/applications/{applicationId}/status", applicationId)
            .param("bankId", bankId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateApplicationStatusWithMissingReason() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus("ACCEPTED");
        request.setComments("Good credit score");

        mockMvc.perform(put("/api/bank/dashboard/applications/{applicationId}/status", applicationId)
            .param("bankId", bankId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}