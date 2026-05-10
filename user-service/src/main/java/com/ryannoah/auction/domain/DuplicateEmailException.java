package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;

public class DuplicateEmailException extends DomainConflictException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
