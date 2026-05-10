package com.ryannoah.auction.domainclientlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ListingDomainClient extends AbstractDomainClient {

    private final String listingServiceBaseUrl;

    public ListingDomainClient(
            WebClient webClient,
            @Value("${services.listing-service.base-url}") String listingServiceBaseUrl
    ) {
        super(webClient);
        this.listingServiceBaseUrl = listingServiceBaseUrl;
    }

    public Mono<ArrayNode> listListings() {
        return fetchCollection(listingServiceBaseUrl, "/api/v1/listings");
    }

    public Mono<JsonNode> getListing(String listingId) {
        return fetchObject(listingServiceBaseUrl, "/api/v1/listings/" + listingId);
    }

    public Mono<JsonNode> createListing(JsonNode request) {
        return exchangeForObject(listingServiceBaseUrl, HttpMethod.POST, "/api/v1/listings", request);
    }

    public Mono<JsonNode> updateListing(String listingId, JsonNode request) {
        return exchangeForObject(listingServiceBaseUrl, HttpMethod.PUT, "/api/v1/listings/" + listingId, request);
    }

    public Mono<JsonNode> publishListing(String listingId) {
        return exchangeForObject(listingServiceBaseUrl, HttpMethod.POST, "/api/v1/listings/" + listingId + "/publish", null);
    }

    public Mono<Void> deleteListing(String listingId) {
        return delete(listingServiceBaseUrl, "/api/v1/listings/" + listingId);
    }
}
