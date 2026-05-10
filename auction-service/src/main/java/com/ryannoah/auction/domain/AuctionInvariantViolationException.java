package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;

public class AuctionInvariantViolationException extends DomainConflictException {

    public AuctionInvariantViolationException(String message) {
        super(message);
    }
}
