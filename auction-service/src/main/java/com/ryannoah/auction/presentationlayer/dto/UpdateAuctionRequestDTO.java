package com.ryannoah.auction.presentationlayer.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateAuctionRequestDTO(
        @NotNull LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        @NotNull @Positive BigDecimal startingPrice,
        @NotBlank String currency
) {
}
