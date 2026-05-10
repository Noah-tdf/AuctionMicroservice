package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.ListingApplicationService;
import com.ryannoah.auction.datamappinglayer.ListingMapper;
import com.ryannoah.auction.presentationlayer.dto.CreateListingRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.ListingResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateListingRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingApplicationService listingApplicationService;
    private final ListingMapper listingMapper;

    public ListingController(ListingApplicationService listingApplicationService, ListingMapper listingMapper) {
        this.listingApplicationService = listingApplicationService;
        this.listingMapper = listingMapper;
    }

    @PostMapping
    public ResponseEntity<ListingResponseDTO> createListing(@Valid @RequestBody CreateListingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingMapper.toResponseDTO(listingApplicationService.createListing(listingMapper.toCreateCommand(request))));
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingResponseDTO> updateListing(@PathVariable String listingId, @Valid @RequestBody UpdateListingRequestDTO request) {
        return ResponseEntity.ok(listingMapper.toResponseDTO(listingApplicationService.updateListing(listingId, listingMapper.toUpdateCommand(request))));
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable String listingId) {
        listingApplicationService.deleteListing(listingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listingId}/publish")
    public ResponseEntity<ListingResponseDTO> publishListing(@PathVariable String listingId) {
        return ResponseEntity.ok(listingMapper.toResponseDTO(listingApplicationService.publishListing(listingId)));
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingResponseDTO> getListing(@PathVariable String listingId) {
        return ResponseEntity.ok(listingMapper.toResponseDTO(listingApplicationService.getListing(listingId)));
    }

    @GetMapping
    public ResponseEntity<java.util.List<ListingResponseDTO>> listListings() {
        return ResponseEntity.ok(listingApplicationService.listListings().stream().map(listingMapper::toResponseDTO).toList());
    }
}
