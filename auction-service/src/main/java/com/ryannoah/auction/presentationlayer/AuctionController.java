package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.AuctionApplicationService;
import com.ryannoah.auction.businesslogiclayer.BidApplicationService;
import com.ryannoah.auction.datamappinglayer.AuctionMapper;
import com.ryannoah.auction.datamappinglayer.BidMapper;
import com.ryannoah.auction.domain.Auction;
import com.ryannoah.auction.domain.Bid;
import com.ryannoah.auction.presentationlayer.dto.AuctionResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.BidResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.CreateAuctionRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.PlaceBidRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateAuctionRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateBidRequestDTO;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionController {

    private final AuctionApplicationService auctionApplicationService;
    private final BidApplicationService bidApplicationService;
    private final AuctionMapper auctionMapper;
    private final BidMapper bidMapper;

    public AuctionController(
            AuctionApplicationService auctionApplicationService,
            BidApplicationService bidApplicationService,
            AuctionMapper auctionMapper,
            BidMapper bidMapper
    ) {
        this.auctionApplicationService = auctionApplicationService;
        this.bidApplicationService = bidApplicationService;
        this.auctionMapper = auctionMapper;
        this.bidMapper = bidMapper;
    }

    @PostMapping
    public ResponseEntity<AuctionResponseDTO> createAuction(@Valid @RequestBody CreateAuctionRequestDTO request) {
        Auction auction = auctionApplicationService.createAuction(auctionMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<AuctionResponseDTO> updateAuction(@PathVariable String auctionId, @Valid @RequestBody UpdateAuctionRequestDTO request) {
        Auction auction = auctionApplicationService.updateAuction(
                auctionId,
                auctionMapper.toUpdateCommand(request)
        );
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable String auctionId) {
        auctionApplicationService.deleteAuction(auctionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{auctionId}/activate")
    public ResponseEntity<AuctionResponseDTO> activateAuction(@PathVariable String auctionId) {
        Auction auction = auctionApplicationService.activateAuction(auctionId);
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @PostMapping("/{auctionId}/close")
    public ResponseEntity<AuctionResponseDTO> closeAuction(@PathVariable String auctionId) {
        Auction auction = auctionApplicationService.closeAuction(auctionId);
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value())));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponseDTO> getAuction(@PathVariable String auctionId) {
        return ResponseEntity.ok(toAuctionResponse(auctionApplicationService.getAuctionAggregate(auctionId)));
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponseDTO>> listAuctions() {
        return ResponseEntity.ok(auctionApplicationService.listAuctionAggregates().stream().map(this::toAuctionResponse).toList());
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<BidResponseDTO> placeBid(@PathVariable String auctionId, @Valid @RequestBody PlaceBidRequestDTO request) {
        Bid bid = bidApplicationService.placeBid(bidMapper.toPlaceCommand(auctionId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toBidResponse(bid));
    }

    @PutMapping("/{auctionId}/bids/{bidId}")
    public ResponseEntity<BidResponseDTO> updateBid(
            @PathVariable String auctionId,
            @PathVariable String bidId,
            @Valid @RequestBody UpdateBidRequestDTO request
    ) {
        Bid bid = bidApplicationService.updateBid(
                auctionId,
                bidId,
                bidMapper.toUpdateCommand(request)
        );
        return ResponseEntity.ok(toBidResponse(bid));
    }

    @DeleteMapping("/{auctionId}/bids/{bidId}")
    public ResponseEntity<Void> deleteBid(@PathVariable String auctionId, @PathVariable String bidId) {
        bidApplicationService.deleteBid(auctionId, bidId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponseDTO>> listBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(bidApplicationService.listBids(auctionId).stream().map(this::toBidResponse).toList());
    }

    private AuctionResponseDTO toAuctionResponse(AuctionApplicationService.AuctionAggregate aggregate) {
        Auction auction = aggregate.auction();
        List<BidResponseDTO> bids = bidApplicationService.listBids(auction.getAuctionId().value()).stream().map(this::toBidResponse).toList();
        return auctionMapper.toResponseDTO(aggregate, bids);
    }

    private BidResponseDTO toBidResponse(Bid bid) {
        return bidMapper.toResponseDTO(bid);
    }
}
