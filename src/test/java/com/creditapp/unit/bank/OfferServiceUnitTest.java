package com.creditapp.unit.bank;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import com.creditapp.bank.repository.OfferRepository;
import com.creditapp.bank.service.OfferService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OfferServiceUnitTest {

    @Test
    void getOffersByApplication_sortsByAprAscending() {
        UUID appId = UUID.randomUUID();

        Offer o1 = new Offer(UUID.randomUUID(), appId, UUID.randomUUID(), OfferStatus.CALCULATED,
                new BigDecimal("9.50"), new BigDecimal("250.00"), new BigDecimal("9000.00"),
                new BigDecimal("100.00"), null, 7, 30, null, LocalDateTime.now().plusDays(30));
        Offer o2 = new Offer(UUID.randomUUID(), appId, UUID.randomUUID(), OfferStatus.CALCULATED,
                new BigDecimal("7.25"), new BigDecimal("220.00"), new BigDecimal("8000.00"),
                new BigDecimal("80.00"), null, 5, 30, null, LocalDateTime.now().plusDays(30));
        Offer o3 = new Offer(UUID.randomUUID(), appId, UUID.randomUUID(), OfferStatus.CALCULATED,
                new BigDecimal("8.10"), new BigDecimal("230.00"), new BigDecimal("8500.00"),
                new BigDecimal("90.00"), null, 6, 30, null, LocalDateTime.now().plusDays(30));

        OfferRepository repo = Mockito.mock(OfferRepository.class);
        when(repo.findByApplicationId(appId)).thenReturn(Arrays.asList(o1, o2, o3));

        OfferService service = new OfferService(repo);
        List<com.creditapp.bank.dto.OfferDTO> results = service.getOffersByApplication(appId);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).apr).isEqualByComparingTo("7.25");
        assertThat(results.get(1).apr).isEqualByComparingTo("8.10");
        assertThat(results.get(2).apr).isEqualByComparingTo("9.50");
    }
}