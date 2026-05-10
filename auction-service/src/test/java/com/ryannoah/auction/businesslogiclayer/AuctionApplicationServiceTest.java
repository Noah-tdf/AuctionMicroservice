package com.ryannoah.auction.businesslogiclayer;

import com.ryannoah.auction.domain.Auction;
import com.ryannoah.auction.domain.AuctionHasBidsException;
import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.AuctionInvariantViolationException;
import com.ryannoah.auction.domain.AuctionRepository;
import com.ryannoah.auction.domain.BidRepository;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;
import com.ryannoah.auction.domainclientlayer.InvoiceDomainClient;
import com.ryannoah.auction.domainclientlayer.ListingDomainClient;
import com.ryannoah.auction.domainclientlayer.UserDomainClient;
import com.ryannoah.auction.domainclientlayer.dto.ListingClientResponseDTO;
import com.ryannoah.auction.domainclientlayer.dto.UserClientResponseDTO;
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
class AuctionApplicationServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private ListingDomainClient listingDomainClient;
    @Mock
    private UserDomainClient userDomainClient;
    @Mock
    private InvoiceDomainClient invoiceDomainClient;

    private AuctionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AuctionApplicationService(auctionRepository, bidRepository, listingDomainClient, userDomainClient, invoiceDomainClient);
    }

    @Test
    void shouldCreateAuctionWhenListingIsPublishedAndSellerVerified() {
        when(listingDomainClient.getListing("listing-1")).thenReturn(publishedListing("listing-1", "seller-1"));
        when(userDomainClient.getUser("seller-1")).thenReturn(verifiedUser("seller-1"));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction auction = service.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                "listing-1",
                "seller-1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("25.00"),
                "CAD"
        ));

        assertThat(auction.getListingId().value()).isEqualTo("listing-1");
    }

    @Test
    void shouldRejectAuctionForUnpublishedListing() {
        when(listingDomainClient.getListing("listing-1")).thenReturn(new ListingClientResponseDTO(
                "listing-1", "seller-1", "Camera", "Digital", "Electronics", "GOOD", false
        ));
        when(userDomainClient.getUser("seller-1")).thenReturn(verifiedUser("seller-1"));

        assertThatThrownBy(() -> service.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                "listing-1",
                "seller-1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("25.00"),
                "CAD"
        ))).isInstanceOf(AuctionInvariantViolationException.class)
                .hasMessage("Auction listing must be published before scheduling: listing-1");
    }

    @Test
    void shouldRejectDeletingAuctionWithBids() {
        Auction auction = Auction.schedule(
                new ListingId("listing-2"),
                new UserId("seller-2"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("25.00"), "CAD")
        );
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of(
                com.ryannoah.auction.domain.Bid.create(
                        auction.getAuctionId(),
                        new UserId("bidder-1"),
                        new Money(new BigDecimal("30.00"), "CAD")
                )
        ));

        assertThatThrownBy(() -> service.deleteAuction(auction.getAuctionId().value()))
                .isInstanceOf(AuctionHasBidsException.class);
    }

    @Test
    void shouldUpdateScheduledAuctionWithoutBids() {
        Auction auction = scheduledAuction("listing-update", "seller-update");
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of());
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Auction updated = service.updateAuction(auction.getAuctionId().value(), new AuctionApplicationService.UpdateAuctionCommand(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                new BigDecimal("50.00"),
                "CAD"
        ));

        assertThat(updated.getStartingPrice().amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldDeleteScheduledAuctionWithoutBids() {
        Auction auction = scheduledAuction("listing-delete", "seller-delete");
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of());

        service.deleteAuction(auction.getAuctionId().value());

        verify(auctionRepository).deleteById(auction.getAuctionId());
    }

    @Test
    void shouldActivateAndCloseAuctionWithoutInvoiceWhenNoBids() {
        Auction auction = scheduledAuction("listing-close", "seller-close");
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of());

        service.activateAuction(auction.getAuctionId().value());
        Auction closed = service.closeAuction(auction.getAuctionId().value());

        assertThat(closed.getStatus().name()).isEqualTo("CLOSED");
    }

    @Test
    void shouldCreateInvoiceWhenClosingSoldAuction() {
        Auction auction = scheduledAuction("listing-sold", "seller-sold");
        auction.activate();
        com.ryannoah.auction.domain.Bid bid =
                com.ryannoah.auction.domain.Bid.create(
                        auction.getAuctionId(),
                        new UserId("buyer-sold"),
                        new Money(new BigDecimal("80.00"), "CAD")
                );
        when(auctionRepository.findById(any(AuctionId.class))).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidRepository.findByAuctionId(auction.getAuctionId())).thenReturn(List.of(bid));
        when(invoiceDomainClient.createInvoice(any())).thenReturn(null);

        Auction closed = service.closeAuction(auction.getAuctionId().value());

        assertThat(closed.getStatus().name()).isEqualTo("SOLD");
        verify(invoiceDomainClient).createInvoice(any());
    }

    @Test
    void shouldRejectAuctionWhenSellerDoesNotOwnListing() {
        when(listingDomainClient.getListing("listing-foreign")).thenReturn(publishedListing("listing-foreign", "other-seller"));
        when(userDomainClient.getUser("seller-1")).thenReturn(verifiedUser("seller-1"));

        assertThatThrownBy(() -> service.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                "listing-foreign",
                "seller-1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("25.00"),
                "CAD"
        ))).isInstanceOf(AuctionInvariantViolationException.class)
                .hasMessage("Auction seller must own listing: listing-foreign");
    }

    @Test
    void shouldRejectAuctionWhenSellerIsNotVerified() {
        when(listingDomainClient.getListing("listing-1")).thenReturn(publishedListing("listing-1", "seller-1"));
        when(userDomainClient.getUser("seller-1")).thenReturn(new UserClientResponseDTO(
                "seller-1", "seller", "seller@example.com", LocalDateTime.now(), false, BigDecimal.ZERO, 0, null
        ));

        assertThatThrownBy(() -> service.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                "listing-1",
                "seller-1",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("25.00"),
                "CAD"
        ))).isInstanceOf(AuctionInvariantViolationException.class)
                .hasMessage("Auction seller must be verified: seller-1");
    }

    private ListingClientResponseDTO publishedListing(String listingId, String sellerId) {
        return new ListingClientResponseDTO(listingId, sellerId, "Camera", "Digital", "Electronics", "GOOD", true);
    }

    private Auction scheduledAuction(String listingId, String sellerId) {
        return Auction.schedule(
                new ListingId(listingId),
                new UserId(sellerId),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("25.00"), "CAD")
        );
    }

    private UserClientResponseDTO verifiedUser(String userId) {
        return new UserClientResponseDTO(userId, "seller", "seller@example.com", LocalDateTime.now(), true, BigDecimal.ZERO, 0, null);
    }
}
