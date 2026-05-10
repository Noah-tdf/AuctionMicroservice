package com.ryannoah.auction.datamappinglayer;

import com.ryannoah.auction.businesslogiclayer.ListingApplicationService;
import com.ryannoah.auction.domain.Listing;
import com.ryannoah.auction.presentationlayer.dto.CreateListingRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.ListingResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateListingRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingApplicationService.CreateListingCommand toCreateCommand(CreateListingRequestDTO request) {
        return new ListingApplicationService.CreateListingCommand(
                request.sellerId(),
                request.title(),
                request.description(),
                request.category(),
                request.condition()
        );
    }

    public ListingApplicationService.UpdateListingCommand toUpdateCommand(UpdateListingRequestDTO request) {
        return new ListingApplicationService.UpdateListingCommand(
                request.sellerId(),
                request.title(),
                request.description(),
                request.category(),
                request.condition()
        );
    }

    public ListingResponseDTO toResponseDTO(Listing listing) {
        return new ListingResponseDTO(
                listing.getListingId().value(),
                listing.getSellerId().value(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCategory(),
                listing.getCondition().name(),
                listing.isPublished()
        );
    }
}
