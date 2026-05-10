package com.ryannoah.auction.domain;

import com.ryannoah.auction.domain.UserId;

import java.time.LocalDateTime;

public class Bid {

    private final BidId bidId;
    private final AuctionId auctionId;
    private final UserId bidderId;
    private final Money bidAmount;
    private final LocalDateTime bidTime;

    public Bid(BidId bidId, AuctionId auctionId, UserId bidderId, Money bidAmount, LocalDateTime bidTime) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    public static Bid create(AuctionId auctionId, UserId bidderId, Money bidAmount) {
        return new Bid(BidId.newId(), auctionId, bidderId, bidAmount, LocalDateTime.now());
    }

    public Bid withAmount(Money updatedAmount) {
        return new Bid(bidId, auctionId, bidderId, updatedAmount, LocalDateTime.now());
    }

    public BidId getBidId() {
        return bidId;
    }

    public AuctionId getAuctionId() {
        return auctionId;
    }

    public UserId getBidderId() {
        return bidderId;
    }

    public Money getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }
}
