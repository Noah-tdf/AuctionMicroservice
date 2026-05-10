package com.ryannoah.auction.domainclientlayer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateInvoiceClientRequestDTO(
        String auctionId,
        String buyerId,
        String sellerId,
        LocalDateTime dueDate,
        BigDecimal finalSaleAmount,
        String currency,
        String method
) {
}
