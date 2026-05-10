package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.BidApplicationService;
import com.ryannoah.auction.datamappinglayer.BidMapper;
import com.ryannoah.auction.presentationlayer.dto.BidResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bids")
public class BidController {

    private final BidApplicationService bidApplicationService;
    private final BidMapper bidMapper;

    public BidController(BidApplicationService bidApplicationService, BidMapper bidMapper) {
        this.bidApplicationService = bidApplicationService;
        this.bidMapper = bidMapper;
    }

    @GetMapping
    public ResponseEntity<java.util.List<BidResponseDTO>> listBids() {
        return ResponseEntity.ok(bidApplicationService.listAllBids().stream().map(bidMapper::toResponseDTO).toList());
    }
}
