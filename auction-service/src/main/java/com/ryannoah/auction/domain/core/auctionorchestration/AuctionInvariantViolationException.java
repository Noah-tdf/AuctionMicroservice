package com.ryannoah.auction.domain.core.auctionorchestration;

import com.ryannoah.auction.domain.shared.DomainConflictException;

public class AuctionInvariantViolationException extends DomainConflictException {

    public AuctionInvariantViolationException(String message) {
        super(message);
    }
}
