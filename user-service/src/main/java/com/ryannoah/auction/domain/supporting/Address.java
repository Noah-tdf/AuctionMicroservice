package com.ryannoah.auction.domain.supporting.usermanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;

public record Address(String street, String city, String zipCode, String country) {

    public Address {
        if (street == null || street.isBlank()
                || city == null || city.isBlank()
                || zipCode == null || zipCode.isBlank()
                || country == null || country.isBlank()) {
            throw new DomainValidationException("Address fields must not be blank");
        }
    }
}
