package com.ryannoah.auction.presentationlayer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponseDTO(
        String bidId,
        String auctionId,
        String bidderId,
        BigDecimal bidAmount,
        String currency,
        LocalDateTime bidTime
) {
}
