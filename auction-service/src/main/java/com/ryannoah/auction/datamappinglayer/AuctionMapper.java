package com.ryannoah.auction.datamappinglayer;

import com.ryannoah.auction.businesslogiclayer.AuctionApplicationService;
import com.ryannoah.auction.domain.Auction;
import com.ryannoah.auction.presentationlayer.dto.AuctionResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.BidResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.CreateAuctionRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateAuctionRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuctionMapper {

    public AuctionApplicationService.CreateAuctionCommand toCreateCommand(CreateAuctionRequestDTO request) {
        return new AuctionApplicationService.CreateAuctionCommand(
                request.listingId(),
                request.sellerId(),
                request.startTime(),
                request.endTime(),
                request.startingPrice(),
                request.currency()
        );
    }

    public AuctionApplicationService.UpdateAuctionCommand toUpdateCommand(UpdateAuctionRequestDTO request) {
        return new AuctionApplicationService.UpdateAuctionCommand(
                request.startTime(),
                request.endTime(),
                request.startingPrice(),
                request.currency()
        );
    }

    public AuctionResponseDTO toResponseDTO(AuctionApplicationService.AuctionAggregate aggregate, List<BidResponseDTO> bids) {
        Auction auction = aggregate.auction();
        return new AuctionResponseDTO(
                auction.getAuctionId().value(),
                auction.getListingId().value(),
                auction.getSellerId().value(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStartingPrice().amount(),
                auction.getCurrentPrice().amount(),
                auction.getStartingPrice().currency(),
                auction.getStatus().name(),
                aggregate.listing(),
                aggregate.seller(),
                aggregate.invoice(),
                bids
        );
    }
}
