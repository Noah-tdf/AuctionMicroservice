package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;

public record ListingId(String value) {

    public ListingId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("listingId must not be blank");
        }
    }
}
