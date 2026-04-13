package com.ryannoah.auction.application.supporting.listingmanagement;

import com.ryannoah.auction.domain.shared.DomainConflictException;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.supporting.listingmanagement.Condition;
import com.ryannoah.auction.domain.supporting.listingmanagement.Listing;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingRepository;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ListingApplicationService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingApplicationService(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public Listing createListing(CreateListingCommand command) {
        UserId sellerId = new UserId(command.sellerId());
        userRepository.findById(sellerId)
                .orElseThrow(() -> new DomainNotFoundException("Seller not found: " + command.sellerId()));

        Listing listing = Listing.create(
                sellerId,
                command.title(),
                command.description(),
                command.category(),
                Condition.valueOf(command.condition())
        );
        return listingRepository.save(listing);
    }

    public Listing updateListing(String listingId, UpdateListingCommand command) {
        Listing existing = getListing(listingId);
        UserId sellerId = new UserId(command.sellerId());
        userRepository.findById(sellerId)
                .orElseThrow(() -> new DomainNotFoundException("Seller not found: " + command.sellerId()));

        Listing updated = new Listing(
                existing.getListingId(),
                sellerId,
                command.title(),
                command.description(),
                command.category(),
                Condition.valueOf(command.condition()),
                existing.isPublished()
        );
        if (existing.isPublished()) {
            updated.publish();
        }
        return listingRepository.save(updated);
    }

    public void deleteListing(String listingId) {
        Listing listing = getListing(listingId);
        listingRepository.deleteById(listing.getListingId());
    }

    public Listing publishListing(String listingId) {
        Listing listing = getListing(listingId);
        if (listing.isPublished()) {
            throw new DomainConflictException("Listing is already published");
        }
        listing.publish();
        return listingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public Listing getListing(String listingId) {
        return listingRepository.findById(new ListingId(listingId))
                .orElseThrow(() -> new DomainNotFoundException("Listing not found: " + listingId));
    }

    @Transactional(readOnly = true)
    public List<Listing> listListings() {
        return listingRepository.findAll();
    }

    public record CreateListingCommand(
            String sellerId,
            String title,
            String description,
            String category,
            String condition
    ) {
    }

    public record UpdateListingCommand(
            String sellerId,
            String title,
            String description,
            String category,
            String condition
    ) {
    }
}
