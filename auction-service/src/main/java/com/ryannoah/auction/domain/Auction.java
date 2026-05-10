package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.utilities.DomainValidationException;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;

import java.time.LocalDateTime;

public class Auction {

    private final AuctionId auctionId;
    private final ListingId listingId;
    private final UserId sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Money startingPrice;
    private Money currentPrice;
    private AuctionStatus status;

    public Auction(
            AuctionId auctionId,
            ListingId listingId,
            UserId sellerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Money startingPrice,
            Money currentPrice,
            AuctionStatus status
    ) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new DomainValidationException("endTime must be after startTime");
        }
        startingPrice.ensurePositive("startingPrice");
        if (currentPrice.amount().compareTo(startingPrice.amount()) < 0) {
            throw new DomainValidationException("currentPrice must be greater than or equal to startingPrice");
        }
        this.auctionId = auctionId;
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.status = status;
    }

    public static Auction schedule(ListingId listingId, UserId sellerId, LocalDateTime startTime, LocalDateTime endTime, Money startingPrice) {
        return new Auction(
                AuctionId.newId(),
                listingId,
                sellerId,
                startTime,
                endTime,
                startingPrice,
                startingPrice,
                AuctionStatus.SCHEDULED
        );
    }

    public void activate() {
        if (status != AuctionStatus.SCHEDULED) {
            throw new DomainConflictException("Auction can only be activated from SCHEDULED state");
        }
        status = AuctionStatus.ACTIVE;
    }

    public void acceptBid(Bid bid) {
        if (status != AuctionStatus.ACTIVE) {
            throw new DomainConflictException("A bid can only be accepted when the auction is ACTIVE");
        }
        if (!bid.getBidAmount().isGreaterThan(currentPrice)) {
            throw new DomainConflictException("Bid amount must be strictly greater than the current price");
        }
        currentPrice = bid.getBidAmount();
    }

    public void close(boolean sold) {
        if (status != AuctionStatus.ACTIVE) {
            throw new DomainConflictException("Only ACTIVE auctions can be closed");
        }
        status = sold ? AuctionStatus.SOLD : AuctionStatus.CLOSED;
    }

    public void updateSchedule(LocalDateTime startTime, LocalDateTime endTime, Money startingPrice) {
        if (status != AuctionStatus.SCHEDULED) {
            throw new DomainConflictException("Only SCHEDULED auctions can be updated");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new DomainValidationException("endTime must be after startTime");
        }
        startingPrice.ensurePositive("startingPrice");
        this.startTime = startTime;
        this.endTime = endTime;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
    }

    public void synchronizeCurrentPrice(Money newCurrentPrice) {
        if (newCurrentPrice.amount().compareTo(startingPrice.amount()) < 0) {
            throw new DomainValidationException("currentPrice must be greater than or equal to startingPrice");
        }
        this.currentPrice = newCurrentPrice;
    }

    public AuctionId getAuctionId() {
        return auctionId;
    }

    public ListingId getListingId() {
        return listingId;
    }

    public UserId getSellerId() {
        return sellerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Money getStartingPrice() {
        return startingPrice;
    }

    public Money getCurrentPrice() {
        return currentPrice;
    }

    public AuctionStatus getStatus() {
        return status;
    }
}
