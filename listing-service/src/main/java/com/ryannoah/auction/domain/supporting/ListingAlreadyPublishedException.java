package com.ryannoah.auction.domain.supporting.listingmanagement;

import com.ryannoah.auction.domain.shared.DomainConflictException;

public class ListingAlreadyPublishedException extends DomainConflictException {

    public ListingAlreadyPublishedException(String listingId) {
        super("Listing is already published: " + listingId);
    }
}
