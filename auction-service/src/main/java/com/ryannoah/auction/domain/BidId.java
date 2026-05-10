package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;

import java.util.UUID;

public record BidId(String value) {

    public BidId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("bidId must not be blank");
        }
    }

    public static BidId newId() {
        return new BidId(UUID.randomUUID().toString());
    }
}
