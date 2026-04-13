package com.ryannoah.auction.application.core.auctionorchestration;

import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.BidRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.shared.DomainConflictException;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.supporting.listingmanagement.Listing;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuctionApplicationService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;

    public AuctionApplicationService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            ListingRepository listingRepository
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.listingRepository = listingRepository;
    }

    public Auction createAuction(CreateAuctionCommand command) {
        Listing listing = listingRepository.findById(new ListingId(command.listingId()))
                .orElseThrow(() -> new DomainNotFoundException("Listing not found: " + command.listingId()));
        if (!listing.isPublished()) {
            throw new DomainConflictException("Listing must be published before creating an auction");
        }

        Auction auction = Auction.schedule(
                listing.getListingId(),
                listing.getSellerId(),
                command.startTime(),
                command.endTime(),
                new Money(command.startingPrice(), command.currency())
        );
        return auctionRepository.save(auction);
    }

    public Auction updateAuction(String auctionId, UpdateAuctionCommand command) {
        Auction auction = getAuction(auctionId);
        if (!bidRepository.findByAuctionId(auction.getAuctionId()).isEmpty()) {
            throw new DomainConflictException("Auctions with bids cannot be updated");
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
            throw new DomainConflictException("Auctions with bids cannot be deleted");
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
        boolean sold = !bidRepository.findByAuctionId(auction.getAuctionId()).isEmpty();
        auction.close(sold);
        return auctionRepository.save(auction);
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

    public record CreateAuctionCommand(
            String listingId,
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
}
