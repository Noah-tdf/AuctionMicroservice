package com.ryannoah.auction.domain.core.auctionorchestration;

import com.ryannoah.auction.domain.shared.DomainConflictException;

public class AuctionHasBidsException extends DomainConflictException {

    public AuctionHasBidsException(String auctionId) {
        super("Auction has bids and cannot be changed: " + auctionId);
    }
}
