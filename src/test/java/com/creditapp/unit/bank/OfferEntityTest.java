package com.creditapp.unit.bank;

import com.creditapp.bank.model.Offer;
import com.creditapp.bank.model.OfferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OfferEntityTest {

    @Test
    void isExpired_returnsTrue_whenExpiresAtInPast() {
        Offer offer = new Offer(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OfferStatus.CALCULATED,
                new BigDecimal("8.00"), new BigDecimal("200.00"), new BigDecimal("7000.00"),
                new BigDecimal("50.00"), null, 5, 30, null, LocalDateTime.now().minusDays(1));
        assertThat(offer.isExpired()).isTrue();
    }

    @Test
    void isExpired_returnsFalse_whenExpiresAtInFuture() {
        Offer offer = new Offer(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OfferStatus.CALCULATED,
                new BigDecimal("8.00"), new BigDecimal("200.00"), new BigDecimal("7000.00"),
                new BigDecimal("50.00"), null, 5, 30, null, LocalDateTime.now().plusDays(1));
        assertThat(offer.isExpired()).isFalse();
    }
}