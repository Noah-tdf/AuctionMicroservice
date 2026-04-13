package com.ryannoah.auction.domain.supporting.listingmanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;

import java.util.UUID;

public record ListingId(String value) {

    public ListingId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("listingId must not be blank");
        }
    }

    public static ListingId newId() {
        return new ListingId(UUID.randomUUID().toString());
    }
}
