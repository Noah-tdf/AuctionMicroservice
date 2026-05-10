package com.ryannoah.auction.presentationlayer.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateListingRequestDTO(
        @NotBlank String sellerId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String category,
        @NotBlank String condition
) {
}
