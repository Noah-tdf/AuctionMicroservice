package com.ryannoah.auction.presentationlayer.dto;

public record AddressResponseDTO(
        String street,
        String city,
        String zipCode,
        String country
) {
}
