package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("userId must not be blank");
        }
    }
}
