package com.ryannoah.auction.presentationlayer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionResponseDTO(
        String auctionId,
        String listingId,
        String sellerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        String currency,
        String status,
        Object listing,
        Object seller,
        Object invoice,
        List<BidResponseDTO> bids
) {
}
