package com.ryannoah.auction.domain.shared;

public class DomainConflictException extends RuntimeException {

    public DomainConflictException(String message) {
        super(message);
    }
}
