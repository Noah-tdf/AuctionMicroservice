package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.datamappinglayer.*;
import com.ryannoah.auction.domainclientlayer.*;

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
@RequestMapping("/api/v1/listings")
public class ListingGatewayController {

    private final ListingDomainClient listingDomainClient;
    private final HypermediaSupport hypermediaSupport;

    public ListingGatewayController(
            ListingDomainClient listingDomainClient,
            HypermediaSupport hypermediaSupport
    ) {
        this.listingDomainClient = listingDomainClient;
        this.hypermediaSupport = hypermediaSupport;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listListings() {
        return listingDomainClient.listListings()
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/listings",
                        "create", "/api/v1/listings"
                ))));
    }

    @GetMapping("/{listingId}")
    Mono<ResponseEntity<JsonNode>> getListing(@PathVariable String listingId) {
        return listingDomainClient.getListing(listingId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/listings/" + listingId,
                        "update", "/api/v1/listings/" + listingId,
                        "delete", "/api/v1/listings/" + listingId,
                        "publish", "/api/v1/listings/" + listingId + "/publish"
                ))));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createListing(@RequestBody JsonNode request) {
        return listingDomainClient.createListing(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(hypermediaSupport.addLinks(body, Map.of(
                        "collection", "/api/v1/listings"
                ))));
    }

    @PutMapping("/{listingId}")
    Mono<ResponseEntity<JsonNode>> updateListing(@PathVariable String listingId, @RequestBody JsonNode request) {
        return listingDomainClient.updateListing(listingId, request)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/listings/" + listingId,
                        "publish", "/api/v1/listings/" + listingId + "/publish"
                ))));
    }

    @PostMapping("/{listingId}/publish")
    Mono<ResponseEntity<JsonNode>> publishListing(@PathVariable String listingId) {
        return listingDomainClient.publishListing(listingId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/listings/" + listingId,
                        "collection", "/api/v1/listings"
                ))));
    }

    @DeleteMapping("/{listingId}")
    Mono<ResponseEntity<Void>> deleteListing(@PathVariable String listingId) {
        return listingDomainClient.deleteListing(listingId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
