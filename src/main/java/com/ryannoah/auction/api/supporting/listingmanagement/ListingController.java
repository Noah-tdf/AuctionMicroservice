package com.ryannoah.auction.api.supporting.listingmanagement;

import com.ryannoah.auction.application.supporting.listingmanagement.ListingApplicationService;
import com.ryannoah.auction.domain.supporting.listingmanagement.Listing;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingApplicationService listingApplicationService;

    public ListingController(ListingApplicationService listingApplicationService) {
        this.listingApplicationService = listingApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<ListingResponse> createListing(@Valid @RequestBody CreateListingRequest request) {
        return toResponse(listingApplicationService.createListing(toCreateCommand(request)));
    }

    @PutMapping("/{listingId}")
    public EntityModel<ListingResponse> updateListing(@PathVariable String listingId, @Valid @RequestBody UpdateListingRequest request) {
        return toResponse(listingApplicationService.updateListing(listingId, toUpdateCommand(request)));
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListing(@PathVariable String listingId) {
        listingApplicationService.deleteListing(listingId);
    }

    @PostMapping("/{listingId}/publish")
    public EntityModel<ListingResponse> publishListing(@PathVariable String listingId) {
        return toResponse(listingApplicationService.publishListing(listingId));
    }

    @GetMapping("/{listingId}")
    public EntityModel<ListingResponse> getListing(@PathVariable String listingId) {
        return toResponse(listingApplicationService.getListing(listingId));
    }

    @GetMapping
    public CollectionModel<EntityModel<ListingResponse>> listListings() {
        return CollectionModel.of(
                listingApplicationService.listListings().stream().map(this::toResponse).toList(),
                linkTo(methodOn(ListingController.class).listListings()).withSelfRel(),
                linkTo(methodOn(ListingController.class).createListing(null)).withRel("create")
        );
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

    private EntityModel<ListingResponse> toResponse(Listing listing) {
        ListingResponse response = new ListingResponse(
                listing.getListingId().value(),
                listing.getSellerId().value(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCategory(),
                listing.getCondition().name(),
                listing.isPublished()
        );
        EntityModel<ListingResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(ListingController.class).getListing(response.listingId())).withSelfRel(),
                linkTo(methodOn(ListingController.class).createListing(null)).withRel("create"),
                linkTo(methodOn(ListingController.class).updateListing(response.listingId(), null)).withRel("update"),
                linkTo(ListingController.class).slash(response.listingId()).withRel("delete")
        );
        if (!response.published()) {
            model.add(linkTo(methodOn(ListingController.class).publishListing(response.listingId())).withRel("publish"));
        }
        return model;
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
