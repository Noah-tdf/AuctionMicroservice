package com.ryannoah.auction.presentationlayer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponseDTO(
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
