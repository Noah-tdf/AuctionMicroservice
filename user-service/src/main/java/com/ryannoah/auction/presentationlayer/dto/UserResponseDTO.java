package com.ryannoah.auction.presentationlayer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponseDTO(
        String userId,
        String username,
        String email,
        LocalDateTime registrationDate,
        boolean verified,
        BigDecimal rating,
        int totalReviews,
        AddressResponseDTO address
) {
}
