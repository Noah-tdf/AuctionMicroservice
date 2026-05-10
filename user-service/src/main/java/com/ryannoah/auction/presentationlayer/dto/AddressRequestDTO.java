package com.ryannoah.auction.presentationlayer.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequestDTO(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String zipCode,
        @NotBlank String country
) {
}
