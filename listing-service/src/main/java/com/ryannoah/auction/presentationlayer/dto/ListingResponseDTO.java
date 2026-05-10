package com.ryannoah.auction.presentationlayer.dto;

public record ListingResponseDTO(
        String listingId,
        String sellerId,
        String title,
        String description,
        String category,
        String condition,
        boolean published
) {
}
