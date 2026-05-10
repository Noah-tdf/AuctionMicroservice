package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCompareMoneyInSameCurrency() {
        Money lower = new Money(new BigDecimal("10.00"), "CAD");
        Money higher = new Money(new BigDecimal("20.00"), "CAD");

        assertThat(higher.isGreaterThan(lower)).isTrue();
    }

    @Test
    void shouldRejectMismatchedCurrency() {
        Money cad = new Money(new BigDecimal("10.00"), "CAD");
        Money usd = new Money(new BigDecimal("20.00"), "USD");

        assertThatThrownBy(() -> cad.isGreaterThan(usd))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("currency mismatch");
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> new Money(BigDecimal.ZERO, "CAD").ensurePositive("amount"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("amount must be greater than zero");
    }

    @Test
    void shouldGenerateAuctionId() {
        assertThat(AuctionId.newId().value()).isNotBlank();
    }
}
