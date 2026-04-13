package com.ryannoah.auction.domain.supporting.usermanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;

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
