package com.ryannoah.auction.domain.supporting.paymentprocessing;

import com.ryannoah.auction.domain.shared.DomainValidationException;

import java.util.UUID;

public record InvoiceId(String value) {

    public InvoiceId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("invoiceId must not be blank");
        }
    }

    public static InvoiceId newId() {
        return new InvoiceId(UUID.randomUUID().toString());
    }
}
