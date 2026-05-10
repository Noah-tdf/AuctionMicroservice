package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.datamappinglayer.*;
import com.ryannoah.auction.domainclientlayer.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bids")
public class BidGatewayController {

    private final AuctionDomainClient auctionDomainClient;
    private final HypermediaSupport hypermediaSupport;

    public BidGatewayController(
            AuctionDomainClient auctionDomainClient,
            HypermediaSupport hypermediaSupport
    ) {
        this.auctionDomainClient = auctionDomainClient;
        this.hypermediaSupport = hypermediaSupport;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listBids() {
        return auctionDomainClient.listBids()
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/bids"
                ))));
    }
}
