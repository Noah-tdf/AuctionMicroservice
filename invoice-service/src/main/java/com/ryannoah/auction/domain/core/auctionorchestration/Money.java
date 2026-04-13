package com.ryannoah.auction.domain.core.auctionorchestration;

import com.ryannoah.auction.domain.shared.DomainValidationException;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("amount must be greater than or equal to zero");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainValidationException("currency must not be blank");
        }
    }

    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public void ensurePositive(String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException(fieldName + " must be greater than zero");
        }
    }

    private void ensureSameCurrency(Money other) {
        if (!currency.equalsIgnoreCase(other.currency)) {
            throw new DomainValidationException("currency mismatch");
        }
    }
}
