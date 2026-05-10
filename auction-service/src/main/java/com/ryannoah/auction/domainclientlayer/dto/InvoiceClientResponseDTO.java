package com.ryannoah.auction.domainclientlayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceClientResponseDTO(
        String invoiceId,
        String auctionId,
        String buyerId,
        String sellerId,
        LocalDateTime issueDate,
        LocalDateTime dueDate,
        BigDecimal finalSaleAmount,
        String currency,
        String status,
        String method
) {
}
