package com.ryannoah.auction.domainclientlayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ListingClientResponseDTO(
        String listingId,
        String sellerId,
        String title,
        String description,
        String category,
        String condition,
        boolean published
) {
}
