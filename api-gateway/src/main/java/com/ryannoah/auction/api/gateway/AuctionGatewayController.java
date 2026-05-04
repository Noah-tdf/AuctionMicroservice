package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
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
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionGatewayController {

    private final AuctionDomainClient auctionDomainClient;
    private final HypermediaSupport hypermediaSupport;

    public AuctionGatewayController(
            AuctionDomainClient auctionDomainClient,
            HypermediaSupport hypermediaSupport
    ) {
        this.auctionDomainClient = auctionDomainClient;
        this.hypermediaSupport = hypermediaSupport;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listAuctions() {
        return auctionDomainClient.listAuctions()
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/auctions",
                        "create", "/api/v1/auctions"
                ))));
    }

    @GetMapping("/{auctionId}")
    Mono<ResponseEntity<JsonNode>> getAuction(@PathVariable String auctionId) {
        return auctionDomainClient.getAuction(auctionId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/auctions/" + auctionId,
                        "bids", "/api/v1/auctions/" + auctionId + "/bids",
                        "activate", "/api/v1/auctions/" + auctionId + "/activate",
                        "close", "/api/v1/auctions/" + auctionId + "/close"
                ))));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createAuction(@RequestBody JsonNode request) {
        return auctionDomainClient.createAuction(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(hypermediaSupport.addLinks(body, Map.of(
                        "collection", "/api/v1/auctions"
                ))));
    }

    @PutMapping("/{auctionId}")
    Mono<ResponseEntity<JsonNode>> updateAuction(@PathVariable String auctionId, @RequestBody JsonNode request) {
        return auctionDomainClient.updateAuction(auctionId, request)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/auctions/" + auctionId,
                        "bids", "/api/v1/auctions/" + auctionId + "/bids"
                ))));
    }

    @DeleteMapping("/{auctionId}")
    Mono<ResponseEntity<Void>> deleteAuction(@PathVariable String auctionId) {
        return auctionDomainClient.deleteAuction(auctionId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/{auctionId}/activate")
    Mono<ResponseEntity<JsonNode>> activateAuction(@PathVariable String auctionId) {
        return auctionDomainClient.activateAuction(auctionId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/auctions/" + auctionId
                ))));
    }

    @PostMapping("/{auctionId}/close")
    Mono<ResponseEntity<JsonNode>> closeAuction(@PathVariable String auctionId) {
        return auctionDomainClient.closeAuction(auctionId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/auctions/" + auctionId
                ))));
    }

    @GetMapping("/{auctionId}/bids")
    Mono<ResponseEntity<JsonNode>> listBidsByAuction(@PathVariable String auctionId) {
        return auctionDomainClient.listBidsByAuction(auctionId)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/auctions/" + auctionId + "/bids",
                        "auction", "/api/v1/auctions/" + auctionId
                ))));
    }

    @PostMapping("/{auctionId}/bids")
    Mono<ResponseEntity<JsonNode>> placeBid(@PathVariable String auctionId, @RequestBody JsonNode request) {
        return auctionDomainClient.placeBid(auctionId, request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(hypermediaSupport.addLinks(body, Map.of(
                        "auction", "/api/v1/auctions/" + auctionId,
                        "collection", "/api/v1/auctions/" + auctionId + "/bids"
                ))));
    }

    @PutMapping("/{auctionId}/bids/{bidId}")
    Mono<ResponseEntity<JsonNode>> updateBid(@PathVariable String auctionId, @PathVariable String bidId, @RequestBody JsonNode request) {
        return auctionDomainClient.updateBid(auctionId, bidId, request)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "auction", "/api/v1/auctions/" + auctionId,
                        "collection", "/api/v1/auctions/" + auctionId + "/bids"
                ))));
    }

    @DeleteMapping("/{auctionId}/bids/{bidId}")
    Mono<ResponseEntity<Void>> deleteBid(@PathVariable String auctionId, @PathVariable String bidId) {
        return auctionDomainClient.deleteBid(auctionId, bidId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
