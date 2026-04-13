package com.ryannoah.auction.domain.supporting.listingmanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;

public record ListingId(String value) {

    public ListingId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("listingId must not be blank");
        }
    }
}
