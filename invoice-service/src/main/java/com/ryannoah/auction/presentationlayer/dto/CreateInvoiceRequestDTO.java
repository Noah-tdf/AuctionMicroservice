package com.ryannoah.auction.presentationlayer.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateInvoiceRequestDTO(
        @NotBlank String auctionId,
        @NotBlank String buyerId,
        @NotBlank String sellerId,
        @NotNull @Future LocalDateTime dueDate,
        @NotNull BigDecimal finalSaleAmount,
        @NotBlank String currency,
        @NotBlank String method
) {
}
