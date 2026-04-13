package com.ryannoah.auction.domain.supporting.usermanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("userId must not be blank");
        }
    }
}
