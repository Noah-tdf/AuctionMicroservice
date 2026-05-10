package com.ryannoah.auction.domainclientlayer;

import com.ryannoah.auction.domainclientlayer.dto.ListingClientResponseDTO;
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

    public ListingClientResponseDTO getListing(String listingId) {
        return getObject(listingServiceBaseUrl, "/api/v1/listings/" + listingId, ListingClientResponseDTO.class);
    }
}
