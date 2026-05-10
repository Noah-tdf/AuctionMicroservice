package com.ryannoah.auction.presentationlayer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBidRequestDTO(
        @NotNull @Positive BigDecimal bidAmount,
        @NotBlank String currency
) {
}
