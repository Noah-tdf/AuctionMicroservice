package com.ryannoah.auction.domainclientlayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressClientResponseDTO(
        String street,
        String city,
        String zipCode,
        String country
) {
}
