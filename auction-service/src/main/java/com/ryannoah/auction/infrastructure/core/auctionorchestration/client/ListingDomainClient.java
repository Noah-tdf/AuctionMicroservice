package com.ryannoah.auction.infrastructure.core.auctionorchestration.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ListingDomainClient extends AbstractHttpDomainClient {

    private final String listingServiceBaseUrl;

    public ListingDomainClient(
            WebClient webClient,
            @Value("${services.listing-service.base-url}") String listingServiceBaseUrl
    ) {
        super(webClient);
        this.listingServiceBaseUrl = listingServiceBaseUrl;
    }

    public ListingResponse getListing(String listingId) {
        return getObject(listingServiceBaseUrl, "/api/v1/listings/" + listingId, ListingResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListingResponse(
            String listingId,
            String sellerId,
            String title,
            String description,
            String category,
            String condition,
            boolean published
    ) {
    }
}
