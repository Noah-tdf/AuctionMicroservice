package com.ryannoah.auction.api.core.auctionorchestration;

import com.ryannoah.auction.application.core.auctionorchestration.BidApplicationService;
import com.ryannoah.auction.domain.core.auctionorchestration.Bid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bids")
public class BidController {

    private final BidApplicationService bidApplicationService;

    public BidController(BidApplicationService bidApplicationService) {
        this.bidApplicationService = bidApplicationService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<BidResponse>> listBids() {
        return ResponseEntity.ok(bidApplicationService.listAllBids().stream().map(this::toResponse).toList());
    }

    private BidResponse toResponse(Bid bid) {
        return new BidResponse(
                bid.getBidId().value(),
                bid.getAuctionId().value(),
                bid.getBidderId().value(),
                bid.getBidAmount().amount(),
                bid.getBidAmount().currency(),
                bid.getBidTime()
        );
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
