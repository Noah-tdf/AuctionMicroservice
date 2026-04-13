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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<AuctionResponse> createAuction(@Valid @RequestBody CreateAuctionRequest request) {
        Auction auction = auctionApplicationService.createAuction(new AuctionApplicationService.CreateAuctionCommand(
                request.listingId(),
                request.startTime(),
                request.endTime(),
                request.startingPrice(),
                request.currency()
        ));
        return toAuctionResponse(auction);
    }

    @PutMapping("/{auctionId}")
    public EntityModel<AuctionResponse> updateAuction(@PathVariable String auctionId, @Valid @RequestBody UpdateAuctionRequest request) {
        Auction auction = auctionApplicationService.updateAuction(
                auctionId,
                new AuctionApplicationService.UpdateAuctionCommand(
                        request.startTime(),
                        request.endTime(),
                        request.startingPrice(),
                        request.currency()
                )
        );
        return toAuctionResponse(auction);
    }

    @DeleteMapping("/{auctionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuction(@PathVariable String auctionId) {
        auctionApplicationService.deleteAuction(auctionId);
    }

    @PostMapping("/{auctionId}/activate")
    public EntityModel<AuctionResponse> activateAuction(@PathVariable String auctionId) {
        return toAuctionResponse(auctionApplicationService.activateAuction(auctionId));
    }

    @PostMapping("/{auctionId}/close")
    public EntityModel<AuctionResponse> closeAuction(@PathVariable String auctionId) {
        return toAuctionResponse(auctionApplicationService.closeAuction(auctionId));
    }

    @GetMapping("/{auctionId}")
    public EntityModel<AuctionResponse> getAuction(@PathVariable String auctionId) {
        return toAuctionResponse(auctionApplicationService.getAuction(auctionId));
    }

    @GetMapping
    public CollectionModel<EntityModel<AuctionResponse>> listAuctions() {
        return CollectionModel.of(
                auctionApplicationService.listAuctions().stream().map(this::toAuctionResponse).toList(),
                linkTo(methodOn(AuctionController.class).listAuctions()).withSelfRel(),
                linkTo(methodOn(AuctionController.class).createAuction(null)).withRel("create")
        );
    }

    @PostMapping("/{auctionId}/bids")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<BidResponse> placeBid(@PathVariable String auctionId, @Valid @RequestBody PlaceBidRequest request) {
        Bid bid = bidApplicationService.placeBid(new BidApplicationService.PlaceBidCommand(
                auctionId,
                request.bidderId(),
                request.bidAmount(),
                request.currency()
        ));
        return toBidResponse(bid);
    }

    @PutMapping("/{auctionId}/bids/{bidId}")
    public EntityModel<BidResponse> updateBid(
            @PathVariable String auctionId,
            @PathVariable String bidId,
            @Valid @RequestBody UpdateBidRequest request
    ) {
        Bid bid = bidApplicationService.updateBid(
                auctionId,
                bidId,
                new BidApplicationService.UpdateBidCommand(request.bidAmount(), request.currency())
        );
        return toBidResponse(bid);
    }

    @DeleteMapping("/{auctionId}/bids/{bidId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBid(@PathVariable String auctionId, @PathVariable String bidId) {
        bidApplicationService.deleteBid(auctionId, bidId);
    }

    @GetMapping("/{auctionId}/bids")
    public CollectionModel<EntityModel<BidResponse>> listBids(@PathVariable String auctionId) {
        List<EntityModel<BidResponse>> bids = bidApplicationService.listBids(auctionId).stream().map(this::toBidResponse).toList();
        return CollectionModel.of(
                bids,
                linkTo(methodOn(AuctionController.class).listBids(auctionId)).withSelfRel(),
                linkTo(methodOn(AuctionController.class).getAuction(auctionId)).withRel("auction")
        );
    }

    private EntityModel<AuctionResponse> toAuctionResponse(Auction auction) {
        AuctionResponse response = new AuctionResponse(
                auction.getAuctionId().value(),
                auction.getListingId().value(),
                auction.getSellerId().value(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStartingPrice().amount(),
                auction.getCurrentPrice().amount(),
                auction.getStartingPrice().currency(),
                auction.getStatus().name()
        );
        EntityModel<AuctionResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(AuctionController.class).getAuction(response.auctionId())).withSelfRel(),
                linkTo(methodOn(AuctionController.class).listBids(response.auctionId())).withRel("bids"),
                linkTo(methodOn(AuctionController.class).createAuction(null)).withRel("create"),
                linkTo(methodOn(AuctionController.class).updateAuction(response.auctionId(), null)).withRel("update"),
                linkTo(AuctionController.class).slash(response.auctionId()).withRel("delete")
        );
        if ("SCHEDULED".equals(response.status())) {
            model.add(linkTo(methodOn(AuctionController.class).activateAuction(response.auctionId())).withRel("activate"));
        }
        if ("ACTIVE".equals(response.status())) {
            model.add(linkTo(methodOn(AuctionController.class).closeAuction(response.auctionId())).withRel("close"));
        }
        return model;
    }

    private EntityModel<BidResponse> toBidResponse(Bid bid) {
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
                linkTo(methodOn(AuctionController.class).listBids(response.auctionId())).withRel("auctionBids"),
                linkTo(methodOn(AuctionController.class).getAuction(response.auctionId())).withRel("auction"),
                linkTo(methodOn(AuctionController.class).updateBid(response.auctionId(), response.bidId(), null)).withRel("update"),
                linkTo(AuctionController.class).slash(response.auctionId()).slash("bids").slash(response.bidId()).withRel("delete")
        );
    }

    public record CreateAuctionRequest(
            @NotBlank String listingId,
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
            String status
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
