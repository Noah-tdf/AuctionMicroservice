package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;

import java.util.UUID;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("userId must not be blank");
        }
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }
}
