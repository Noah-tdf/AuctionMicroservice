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
import com.ryannoah.auction.domain.UserId;
import com.ryannoah.auction.domainclientlayer.UserDomainClient;
import com.ryannoah.auction.domainclientlayer.dto.UserClientResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class BidApplicationService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserDomainClient userDomainClient;

    public BidApplicationService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            UserDomainClient userDomainClient
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userDomainClient = userDomainClient;
    }

    public Bid placeBid(PlaceBidCommand command) {
        Auction auction = auctionRepository.findById(new AuctionId(command.auctionId()))
                .orElseThrow(() -> new DomainNotFoundException("Auction not found: " + command.auctionId()));
        validateBidder(auction, command.bidderId());
        Bid bid = Bid.create(auction.getAuctionId(), new UserId(command.bidderId()), new Money(command.bidAmount(), command.currency()));
        auction.acceptBid(bid);
        auctionRepository.save(auction);
        return bidRepository.save(bid);
    }

    public Bid updateBid(String auctionId, String bidId, UpdateBidCommand command) {
        Auction auction = getAuctionOrThrow(auctionId);
        Bid existing = getBidOrThrow(bidId);
        ensureAuctionMatches(existing, auctionId);

        List<Bid> remainingBids = bidRepository.findByAuctionId(auction.getAuctionId()).stream()
                .filter(bid -> !bid.getBidId().equals(existing.getBidId()))
                .toList();
        Money baseline = highestBidOrStartingPrice(auction, remainingBids);
        Money updatedAmount = new Money(command.bidAmount(), command.currency());
        if (!updatedAmount.isGreaterThan(baseline)) {
            throw new DomainConflictException("Updated bid amount must remain strictly greater than the current auction price baseline");
        }

        Bid updated = existing.withAmount(updatedAmount);
        Bid saved = bidRepository.save(updated);
        auction.synchronizeCurrentPrice(updatedAmount);
        auctionRepository.save(auction);
        return saved;
    }

    public void deleteBid(String auctionId, String bidId) {
        Auction auction = getAuctionOrThrow(auctionId);
        Bid existing = getBidOrThrow(bidId);
        ensureAuctionMatches(existing, auctionId);

        bidRepository.deleteById(existing.getBidId());
        List<Bid> remainingBids = bidRepository.findByAuctionId(auction.getAuctionId());
        auction.synchronizeCurrentPrice(highestBidOrStartingPrice(auction, remainingBids));
        auctionRepository.save(auction);
    }

    @Transactional(readOnly = true)
    public List<Bid> listBids(String auctionId) {
        getAuctionOrThrow(auctionId);
        return bidRepository.findByAuctionId(new AuctionId(auctionId));
    }

    @Transactional(readOnly = true)
    public List<Bid> listAllBids() {
        return bidRepository.findAll();
    }

    private Auction getAuctionOrThrow(String auctionId) {
        return auctionRepository.findById(new AuctionId(auctionId))
                .orElseThrow(() -> new DomainNotFoundException("Auction not found: " + auctionId));
    }

    private Bid getBidOrThrow(String bidId) {
        return bidRepository.findById(new BidId(bidId))
                .orElseThrow(() -> new DomainNotFoundException("Bid not found: " + bidId));
    }

    private void ensureAuctionMatches(Bid bid, String auctionId) {
        if (!bid.getAuctionId().value().equals(auctionId)) {
            throw new DomainConflictException("Bid does not belong to the specified auction");
        }
    }

    private void validateBidder(Auction auction, String bidderId) {
        if (auction.getSellerId().value().equals(bidderId)) {
            throw new AuctionInvariantViolationException("Auction seller cannot bid on their own auction: " + bidderId);
        }
        UserClientResponseDTO bidder = userDomainClient.getUser(bidderId);
        if (!bidder.verified()) {
            throw new AuctionInvariantViolationException("Auction bidder must be verified: " + bidderId);
        }
    }

    private Money highestBidOrStartingPrice(Auction auction, List<Bid> bids) {
        return bids.stream()
                .map(Bid::getBidAmount)
                .max(Comparator.comparing(Money::amount))
                .orElse(auction.getStartingPrice());
    }

    public record PlaceBidCommand(
            String auctionId,
            String bidderId,
            BigDecimal bidAmount,
            String currency
    ) {
    }

    public record UpdateBidCommand(
            BigDecimal bidAmount,
            String currency
    ) {
    }
}
