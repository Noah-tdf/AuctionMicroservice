package com.ryannoah.auction.api.supporting.listingmanagement;

import com.ryannoah.auction.application.supporting.listingmanagement.ListingApplicationService;
import com.ryannoah.auction.domain.supporting.listingmanagement.Listing;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingApplicationService listingApplicationService;

    public ListingController(ListingApplicationService listingApplicationService) {
        this.listingApplicationService = listingApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponse createListing(@Valid @RequestBody CreateListingRequest request) {
        return toResponse(listingApplicationService.createListing(toCreateCommand(request)));
    }

    @PutMapping("/{listingId}")
    public ListingResponse updateListing(@PathVariable String listingId, @Valid @RequestBody UpdateListingRequest request) {
        return toResponse(listingApplicationService.updateListing(listingId, toUpdateCommand(request)));
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@PathVariable String listingId) {
        listingApplicationService.deleteListing(listingId);
    }

    @PostMapping("/{listingId}/publish")
    public ListingResponse publishListing(@PathVariable String listingId) {
        return toResponse(listingApplicationService.publishListing(listingId));
    }

    @GetMapping("/{listingId}")
    public ListingResponse getListing(@PathVariable String listingId) {
        return toResponse(listingApplicationService.getListing(listingId));
    }

    @GetMapping
    public java.util.List<ListingResponse> listListings() {
        return listingApplicationService.listListings().stream().map(this::toResponse).toList();
    }

    private ListingApplicationService.CreateListingCommand toCreateCommand(CreateListingRequest request) {
        return new ListingApplicationService.CreateListingCommand(
                request.sellerId(),
                request.title(),
                request.description(),
                request.category(),
                request.condition()
        );
    }

    private ListingApplicationService.UpdateListingCommand toUpdateCommand(UpdateListingRequest request) {
        return new ListingApplicationService.UpdateListingCommand(
                request.sellerId(),
                request.title(),
                request.description(),
                request.category(),
                request.condition()
        );
    }

    private ListingResponse toResponse(Listing listing) {
        return new ListingResponse(
                listing.getListingId().value(),
                listing.getSellerId().value(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCategory(),
                listing.getCondition().name(),
                listing.isPublished()
        );
    }

    public record CreateListingRequest(
            @NotBlank String sellerId,
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String category,
            @NotBlank String condition
    ) {
    }

    public record UpdateListingRequest(
            @NotBlank String sellerId,
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String category,
            @NotBlank String condition
    ) {
    }

    public record ListingResponse(
            String listingId,
            String sellerId,
            String title,
            String description,
            String category,
            String condition,
            boolean published
    ) {
    }
}
