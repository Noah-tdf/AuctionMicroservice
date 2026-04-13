package com.ryannoah.auction.domain.supporting.listingmanagement;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(ListingId listingId);

    List<Listing> findAll();

    void deleteById(ListingId listingId);
}
