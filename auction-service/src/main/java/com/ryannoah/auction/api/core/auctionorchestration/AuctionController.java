package com.ryannoah.auction.api.core.auctionorchestration;

import com.ryannoah.auction.application.core.auctionorchestration.AuctionApplicationService;
import com.ryannoah.auction.application.core.auctionorchestration.BidApplicationService;
import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.Bid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionController {

    private final AuctionApplicationService auctionApplicationService;
    private final BidApplicationService bidApplicationService;

    public AuctionController(
            AuctionApplicationService auctionApplicationService,
            BidApplicationService bidApplicationService
    ) {
        this.auctionApplicationService = auctionApplicationService;
        this.bidApplicationService = bidApplicationService;
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(@Valid @RequestBody CreateAuctionRequest request) {
        Auction auction = auctionApplicationService.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                request.listingId(),
                request.sellerId(),
                request.startTime(),
                request.endTime(),
                request.startingPrice(),
                request.currency()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<AuctionResponse> updateAuction(@PathVariable String auctionId, @Valid @RequestBody UpdateAuctionRequest request) {
        Auction auction = auctionApplicationService.updateAuction(
                auctionId,
                new AuctionApplicationService.UpdateAuctionCommand(
                        request.startTime(),
                        request.endTime(),
                        request.startingPrice(),
                        request.currency()
                )
        );
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable String auctionId) {
        auctionApplicationService.deleteAuction(auctionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{auctionId}/activate")
    public ResponseEntity<AuctionResponse> activateAuction(@PathVariable String auctionId) {
        Auction auction = auctionApplicationService.activateAuction(auctionId);
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @PostMapping("/{auctionId}/close")
    public ResponseEntity<AuctionResponse> closeAuction(@PathVariable String auctionId) {
        Auction auction = auctionApplicationService.closeAuction(auctionId);
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable String auctionId) {
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auctionId)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> listAuctions() {
        return ResponseEntity.ok(auctionApplicationService.listAuctionAggregates().stream().map(this::toAuctionResponse).toList());
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponse> placeBid(@PathVariable String auctionId, @Valid @RequestBody PlaceBidRequest request) {
        Bid bid = bidApplicationService.placeBid(new BidApplicationService.PlaceBidCommand(
                auctionId,
                request.bidderId(),
                request.bidAmount(),
                request.currency()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toBidResponse(bid));
    }

    @PutMapping("/{auctionId}/bids/{bidId}")
    public ResponseEntity<BidResponse> updateBid(
            @PathVariable String auctionId,
            @PathVariable String bidId,
            @Valid @RequestBody UpdateBidRequest request
    ) {
        Bid bid = bidApplicationService.updateBid(
                auctionId,
                bidId,
                new BidApplicationService.UpdateBidCommand(request.bidAmount(), request.currency())
        );
        return ResponseEntity.ok(toBidResponse(bid));
    }

    @DeleteMapping("/{auctionId}/bids/{bidId}")
    public ResponseEntity<Void> deleteBid(@PathVariable String auctionId, @PathVariable String bidId) {
        bidApplicationService.deleteBid(auctionId, bidId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponse>> listBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(bidApplicationService.listBids(auctionId).stream().map(this::toBidResponse).toList());
    }

    private AuctionResponse toAuctionResponse(AuctionApplicationService.AuctionAggregate aggregate) {
        Auction auction = aggregate.auction();
        return new AuctionResponse(
                auction.getAuctionId().value(),
                auction.getListingId().value(),
                auction.getSellerId().value(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStartingPrice().amount(),
                auction.getCurrentPrice().amount(),
                auction.getStartingPrice().currency(),
                auction.getStatus().name(),
                aggregate.listing(),
                aggregate.seller(),
                aggregate.invoice(),
                bidApplicationService.listBids(auction.getAuctionId().value()).stream().map(this::toBidResponse).toList()
        );
    }

    private BidResponse toBidResponse(Bid bid) {
        return new BidResponse(
                bid.getBidId().value(),
                bid.getAuctionId().value(),
                bid.getBidderId().value(),
                bid.getBidAmount().amount(),
                bid.getBidAmount().currency(),
                bid.getBidTime()
        );
    }

    public record CreateAuctionRequest(
            @NotBlank String listingId,
            @NotBlank String sellerId,
            @NotNull LocalDateTime startTime,
            @NotNull @Future LocalDateTime endTime,
            @NotNull @Positive BigDecimal startingPrice,
            @NotBlank String currency
    ) {
    }

    public record UpdateAuctionRequest(
            @NotNull LocalDateTime startTime,
            @NotNull @Future LocalDateTime endTime,
            @NotNull @Positive BigDecimal startingPrice,
            @NotBlank String currency
    ) {
    }

    public record PlaceBidRequest(
            @NotBlank String bidderId,
            @NotNull @Positive BigDecimal bidAmount,
            @NotBlank String currency
    ) {
    }

    public record UpdateBidRequest(
            @NotNull @Positive BigDecimal bidAmount,
            @NotBlank String currency
    ) {
    }

    public record AuctionResponse(
            String auctionId,
            String listingId,
            String sellerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            String currency,
            String status,
            Object listing,
            Object seller,
            Object invoice,
            List<BidResponse> bids
    ) {
    }

    public record BidResponse(
            String bidId,
            String auctionId,
            String bidderId,
            BigDecimal bidAmount,
            String currency,
            LocalDateTime bidTime
    ) {
    }
}
