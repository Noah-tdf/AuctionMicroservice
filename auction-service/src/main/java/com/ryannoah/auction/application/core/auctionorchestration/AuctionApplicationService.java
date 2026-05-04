package com.ryannoah.auction.application.core.auctionorchestration;

import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionHasBidsException;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionInvariantViolationException;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.BidRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.DownstreamServiceException;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.InvoiceDomainClient;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.ListingDomainClient;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.UserDomainClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuctionApplicationService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingDomainClient listingDomainClient;
    private final UserDomainClient userDomainClient;
    private final InvoiceDomainClient invoiceDomainClient;

    public AuctionApplicationService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            ListingDomainClient listingDomainClient,
            UserDomainClient userDomainClient,
            InvoiceDomainClient invoiceDomainClient
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.listingDomainClient = listingDomainClient;
        this.userDomainClient = userDomainClient;
        this.invoiceDomainClient = invoiceDomainClient;
    }

    public Auction createAuction(CreateAuctionCommand command) {
        validateAuctionInvariant(command.listingId(), command.sellerId());
        Auction auction = Auction.schedule(
                new ListingId(command.listingId()),
                new UserId(command.sellerId()),
                command.startTime(),
                command.endTime(),
                new Money(command.startingPrice(), command.currency())
        );
        return auctionRepository.save(auction);
    }

    public Auction updateAuction(String auctionId, UpdateAuctionCommand command) {
        Auction auction = getAuction(auctionId);
        if (!bidRepository.findByAuctionId(auction.getAuctionId()).isEmpty()) {
            throw new AuctionHasBidsException(auctionId);
        }
        auction.updateSchedule(
                command.startTime(),
                command.endTime(),
                new Money(command.startingPrice(), command.currency())
        );
        return auctionRepository.save(auction);
    }

    public void deleteAuction(String auctionId) {
        Auction auction = getAuction(auctionId);
        if (!bidRepository.findByAuctionId(auction.getAuctionId()).isEmpty()) {
            throw new AuctionHasBidsException(auctionId);
        }
        auctionRepository.deleteById(auction.getAuctionId());
    }

    public Auction activateAuction(String auctionId) {
        Auction auction = getAuction(auctionId);
        auction.activate();
        return auctionRepository.save(auction);
    }

    public Auction closeAuction(String auctionId) {
        Auction auction = getAuction(auctionId);
        List<com.ryannoah.auction.domain.core.auctionorchestration.Bid> bids = bidRepository.findByAuctionId(auction.getAuctionId());
        boolean sold = !bids.isEmpty();
        auction.close(sold);
        Auction saved = auctionRepository.save(auction);
        if (sold) {
            createInvoiceForWinningBid(saved, bids);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Auction getAuction(String auctionId) {
        return auctionRepository.findById(new AuctionId(auctionId))
                .orElseThrow(() -> new DomainNotFoundException("Auction not found: " + auctionId));
    }

    @Transactional(readOnly = true)
    public List<Auction> listAuctions() {
        return auctionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AuctionAggregate getAuctionAggregate(String auctionId) {
        Auction auction = getAuction(auctionId);
        return toAggregate(auction);
    }

    @Transactional(readOnly = true)
    public List<AuctionAggregate> listAuctionAggregates() {
        return listAuctions().stream().map(this::toAggregate).toList();
    }

    private void validateAuctionInvariant(String listingId, String sellerId) {
        ListingDomainClient.ListingResponse listing = listingDomainClient.getListing(listingId);
        UserDomainClient.UserResponse seller = userDomainClient.getUser(sellerId);

        if (!listing.sellerId().equals(sellerId)) {
            throw new AuctionInvariantViolationException("Auction seller must own listing: " + listingId);
        }
        if (!listing.published()) {
            throw new AuctionInvariantViolationException("Auction listing must be published before scheduling: " + listingId);
        }
        if (!seller.verified()) {
            throw new AuctionInvariantViolationException("Auction seller must be verified: " + sellerId);
        }
    }

    private void createInvoiceForWinningBid(
            Auction auction,
            List<com.ryannoah.auction.domain.core.auctionorchestration.Bid> bids
    ) {
        com.ryannoah.auction.domain.core.auctionorchestration.Bid winningBid = bids.stream()
                .max(Comparator.comparing(bid -> bid.getBidAmount().amount()))
                .orElseThrow(() -> new DomainNotFoundException("Winning bid not found for auction: " + auction.getAuctionId().value()));

        invoiceDomainClient.createInvoice(new InvoiceDomainClient.CreateInvoiceRequest(
                auction.getAuctionId().value(),
                winningBid.getBidderId().value(),
                auction.getSellerId().value(),
                LocalDateTime.now().plusDays(14),
                winningBid.getBidAmount().amount(),
                winningBid.getBidAmount().currency(),
                "CREDIT_CARD"
        ));
    }

    private AuctionAggregate toAggregate(Auction auction) {
        return new AuctionAggregate(
                auction,
                safeListing(auction.getListingId().value()).orElse(null),
                safeUser(auction.getSellerId().value()).orElse(null),
                safeInvoice(auction.getAuctionId().value()).orElse(null)
        );
    }

    private Optional<ListingDomainClient.ListingResponse> safeListing(String listingId) {
        try {
            return Optional.ofNullable(listingDomainClient.getListing(listingId));
        } catch (DownstreamServiceException exception) {
            return Optional.empty();
        }
    }

    private Optional<UserDomainClient.UserResponse> safeUser(String userId) {
        try {
            return Optional.ofNullable(userDomainClient.getUser(userId));
        } catch (DownstreamServiceException exception) {
            return Optional.empty();
        }
    }

    private Optional<InvoiceDomainClient.InvoiceResponse> safeInvoice(String auctionId) {
        try {
            InvoiceDomainClient.InvoiceResponse[] invoices = invoiceDomainClient.listInvoices();
            if (invoices == null) {
                return Optional.empty();
            }
            return Arrays.stream(invoices)
                    .filter(invoice -> auctionId.equals(invoice.auctionId()))
                    .findFirst();
        } catch (DownstreamServiceException exception) {
            return Optional.empty();
        }
    }

    public record CreateAuctionCommand(
            String listingId,
            String sellerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal startingPrice,
            String currency
    ) {
    }

    public record UpdateAuctionCommand(
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal startingPrice,
            String currency
    ) {
    }

    public record AuctionAggregate(
            Auction auction,
            ListingDomainClient.ListingResponse listing,
            UserDomainClient.UserResponse seller,
            InvoiceDomainClient.InvoiceResponse invoice
    ) {
    }
}
