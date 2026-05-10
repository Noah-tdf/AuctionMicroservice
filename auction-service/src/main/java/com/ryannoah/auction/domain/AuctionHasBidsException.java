package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;

public class AuctionHasBidsException extends DomainConflictException {

    public AuctionHasBidsException(String auctionId) {
        super("Auction has bids and cannot be changed: " + auctionId);
    }
}
