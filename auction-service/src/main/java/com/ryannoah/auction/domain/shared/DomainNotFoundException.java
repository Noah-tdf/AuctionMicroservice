package com.ryannoah.auction.domain.shared;

public class DomainNotFoundException extends RuntimeException {

    public DomainNotFoundException(String message) {
        super(message);
    }
}
