package com.ryannoah.auction.api.core.auctionorchestration;

import com.ryannoah.auction.application.core.auctionorchestration.BidApplicationService;
import com.ryannoah.auction.domain.core.auctionorchestration.Bid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/bids")
public class BidController {

    private final BidApplicationService bidApplicationService;

    public BidController(BidApplicationService bidApplicationService) {
        this.bidApplicationService = bidApplicationService;
    }

    @GetMapping
    public CollectionModel<EntityModel<BidResponse>> listBids() {
        return CollectionModel.of(
                bidApplicationService.listAllBids().stream().map(this::toResponse).toList(),
                linkTo(methodOn(BidController.class).listBids()).withSelfRel()
        );
    }

    private EntityModel<BidResponse> toResponse(Bid bid) {
        BidResponse response = new BidResponse(
                bid.getBidId().value(),
                bid.getAuctionId().value(),
                bid.getBidderId().value(),
                bid.getBidAmount().amount(),
                bid.getBidAmount().currency(),
                bid.getBidTime()
        );
        return EntityModel.of(
                response,
                linkTo(BidController.class).slash(response.bidId()).withSelfRel(),
                linkTo(AuctionController.class).slash(response.auctionId()).withRel("auction")
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
