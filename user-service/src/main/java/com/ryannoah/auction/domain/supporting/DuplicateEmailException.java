package com.ryannoah.auction.domain.supporting.usermanagement;

import com.ryannoah.auction.domain.shared.DomainConflictException;

public class DuplicateEmailException extends DomainConflictException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
