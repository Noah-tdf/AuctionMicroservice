package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.utilities.DomainValidationException;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionTest {

    @Test
    void shouldScheduleAuctionWithStartingPriceAsCurrentPrice() {
        Auction auction = Auction.schedule(
                new ListingId("listing-1"),
                new UserId("seller-1"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("10.00"), "CAD")
        );

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
        assertThat(auction.getCurrentPrice()).isEqualTo(auction.getStartingPrice());
    }

    @Test
    void shouldAcceptHigherBidWhenAuctionIsActive() {
        Auction auction = scheduledAuction();
        auction.activate();

        Bid higherBid = Bid.create(
                auction.getAuctionId(),
                new UserId("bidder-1"),
                new Money(new BigDecimal("15.00"), "CAD")
        );

        auction.acceptBid(higherBid);

        assertThat(auction.getCurrentPrice().amount()).isEqualByComparingTo("15.00");
    }

    @Test
    void shouldRejectBidThatDoesNotBeatCurrentPrice() {
        Auction auction = scheduledAuction();
        auction.activate();

        Bid lowBid = Bid.create(
                auction.getAuctionId(),
                new UserId("bidder-1"),
                new Money(new BigDecimal("10.00"), "CAD")
        );

        assertThatThrownBy(() -> auction.acceptBid(lowBid))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("Bid amount must be strictly greater than the current price");
    }

    @Test
    void shouldRejectActivationWhenAuctionIsNotScheduled() {
        Auction auction = scheduledAuction();
        auction.activate();

        assertThatThrownBy(auction::activate)
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("Auction can only be activated from SCHEDULED state");
    }

    @Test
    void shouldCloseAuctionAsSold() {
        Auction auction = scheduledAuction();
        auction.activate();

        auction.close(true);

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SOLD);
    }

    @Test
    void shouldRejectScheduleWithInvalidTimes() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> Auction.schedule(
                new ListingId("listing-1"),
                new UserId("seller-1"),
                start,
                start.minusHours(1),
                new Money(new BigDecimal("10.00"), "CAD")
        ))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("endTime must be after startTime");
    }

    @Test
    void shouldRejectBlankAuctionAndBidIdentifiers() {
        assertThatThrownBy(() -> new AuctionId(""))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("auctionId must not be blank");
        assertThatThrownBy(() -> new BidId(" "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("bidId must not be blank");
    }

    @Test
    void shouldRejectBlankUserAndListingIdentifiers() {
        assertThatThrownBy(() -> new UserId(""))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("userId must not be blank");
        assertThatThrownBy(() -> new ListingId(" "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("listingId must not be blank");
    }

    @Test
    void shouldRejectInvalidMoneyValues() {
        assertThatThrownBy(() -> new Money(BigDecimal.ZERO, "CAD").ensurePositive("price"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("price must be greater than zero");
        assertThatThrownBy(() -> new Money(new BigDecimal("10.00"), "").ensurePositive("price"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("currency must not be blank");
        assertThat(new Money(new BigDecimal("11.00"), "CAD").isGreaterThan(new Money(new BigDecimal("10.00"), "CAD"))).isTrue();
    }

    private Auction scheduledAuction() {
        return Auction.schedule(
                new ListingId("listing-1"),
                new UserId("seller-1"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("10.00"), "CAD")
        );
    }
}
