package com.ryannoah.auction.datamappinglayer;

import com.ryannoah.auction.businesslogiclayer.BidApplicationService;
import com.ryannoah.auction.domain.Bid;
import com.ryannoah.auction.presentationlayer.dto.BidResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.PlaceBidRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateBidRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class BidMapper {

    public BidApplicationService.PlaceBidCommand toPlaceCommand(String auctionId, PlaceBidRequestDTO request) {
        return new BidApplicationService.PlaceBidCommand(
                auctionId,
                request.bidderId(),
                request.bidAmount(),
                request.currency()
        );
    }

    public BidApplicationService.UpdateBidCommand toUpdateCommand(UpdateBidRequestDTO request) {
        return new BidApplicationService.UpdateBidCommand(request.bidAmount(), request.currency());
    }

    public BidResponseDTO toResponseDTO(Bid bid) {
        return new BidResponseDTO(
                bid.getBidId().value(),
                bid.getAuctionId().value(),
                bid.getBidderId().value(),
                bid.getBidAmount().amount(),
                bid.getBidAmount().currency(),
                bid.getBidTime()
        );
    }
}
