package com.ryannoah.auction.domain.core.auctionorchestration;

import com.ryannoah.auction.domain.shared.DomainValidationException;

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
