package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainValidationException;

import java.util.regex.Pattern;

public record Email(String address) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (address == null || !EMAIL_PATTERN.matcher(address).matches()) {
            throw new DomainValidationException("A valid email address is required");
        }
    }
}
