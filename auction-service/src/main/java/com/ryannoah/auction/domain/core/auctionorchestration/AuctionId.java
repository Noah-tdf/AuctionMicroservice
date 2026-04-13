package com.ryannoah.auction.domain.core.auctionorchestration;

import com.ryannoah.auction.domain.shared.DomainValidationException;

import java.util.UUID;

public record AuctionId(String value) {

    public AuctionId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("auctionId must not be blank");
        }
    }

    public static AuctionId newId() {
        return new AuctionId(UUID.randomUUID().toString());
    }
}
