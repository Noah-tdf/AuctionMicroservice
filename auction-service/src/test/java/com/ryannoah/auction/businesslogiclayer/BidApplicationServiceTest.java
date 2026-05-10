package com.ryannoah.auction.businesslogiclayer;

import com.ryannoah.auction.domain.Auction;
import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.AuctionInvariantViolationException;
import com.ryannoah.auction.domain.AuctionRepository;
import com.ryannoah.auction.domain.Bid;
import com.ryannoah.auction.domain.BidId;
import com.ryannoah.auction.domain.BidRepository;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.utilities.DomainNotFoundException;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;
import com.ryannoah.auction.domainclientlayer.UserDomainClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidApplicationServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private UserDomainClient userDomainClient;

    private BidApplicationService service;
    private Auction auction;

    @BeforeEach
    void setUp() {
        service = new BidApplicationService(auctionRepository, bidRepository, userDomainClient);
        auction = Auction.schedule(
                new ListingId("listing-bid"),
                new UserId("seller-bid"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("100.00"), "CAD")
        );
        auction.activate();
    }

    @Test
    void shouldPlaceBidForVerifiedBidder() {
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(userDomainClient.getUser("bidder-1")).thenReturn(verifiedUser("bidder-1", true));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bid bid = service.placeBid(new BidApplicationService.PlaceBidCommand("auction-id", "bidder-1", new BigDecimal("125.00"), "CAD"));

        assertThat(bid.getBidderId().value()).isEqualTo("bidder-1");
        assertThat(auction.getCurrentPrice().amount()).isEqualByComparingTo("125.00");
    }

    @Test
    void shouldRejectUnverifiedBidder() {
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(userDomainClient.getUser("bidder-2")).thenReturn(verifiedUser("bidder-2", false));

        assertThatThrownBy(() -> service.placeBid(new BidApplicationService.PlaceBidCommand("auction-id", "bidder-2", new BigDecimal("125.00"), "CAD")))
                .isInstanceOf(AuctionInvariantViolationException.class)
                .hasMessage("Auction bidder must be verified: bidder-2");
    }

    @Test
    void shouldUpdateBidWhenAmountBeatsRemainingBaseline() {
        Bid existing = Bid.create(auction.getAuctionId(), new UserId("bidder-1"), new Money(new BigDecimal("125.00"), "CAD"));
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findById(new BidId(existing.getBidId().value()))).thenReturn(Optional.of(existing));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of(existing));
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bid updated = service.updateBid(auction.getAuctionId().value(), existing.getBidId().value(), new BidApplicationService.UpdateBidCommand(new BigDecimal("150.00"), "CAD"));

        assertThat(updated.getBidAmount().amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldRejectUpdateWhenBidBelongsToDifferentAuction() {
        Bid existing = Bid.create(new AuctionId("other-auction"), new UserId("bidder-1"), new Money(new BigDecimal("125.00"), "CAD"));
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findById(new BidId(existing.getBidId().value()))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateBid(auction.getAuctionId().value(), existing.getBidId().value(), new BidApplicationService.UpdateBidCommand(new BigDecimal("150.00"), "CAD")))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("Bid does not belong to the specified auction");
    }

    @Test
    void shouldDeleteBidAndRecalculateCurrentPrice() {
        Bid existing = Bid.create(auction.getAuctionId(), new UserId("bidder-1"), new Money(new BigDecimal("125.00"), "CAD"));
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findById(new BidId(existing.getBidId().value()))).thenReturn(Optional.of(existing));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of());

        service.deleteBid(auction.getAuctionId().value(), existing.getBidId().value());

        verify(bidRepository).deleteById(existing.getBidId());
        assertThat(auction.getCurrentPrice().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldRejectListingBidsForMissingAuction() {
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listBids("missing-auction"))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage("Auction not found: missing-auction");
    }

    private UserDomainClient.UserResponse verifiedUser(String userId, boolean verified) {
        return new UserDomainClient.UserResponse(userId, "bidder", "bidder@example.com", LocalDateTime.now(), verified, BigDecimal.ZERO, 0, null);
    }
}
