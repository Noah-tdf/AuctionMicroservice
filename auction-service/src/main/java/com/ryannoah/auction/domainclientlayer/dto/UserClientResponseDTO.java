package com.ryannoah.auction.domainclientlayer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserClientResponseDTO(
        String userId,
        String username,
        String email,
        LocalDateTime registrationDate,
        @JsonAlias({"isVerified", "verified"}) boolean verified,
        BigDecimal rating,
        int totalReviews,
        AddressClientResponseDTO address
) {
}
