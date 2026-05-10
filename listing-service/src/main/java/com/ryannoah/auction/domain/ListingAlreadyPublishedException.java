package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;

public class ListingAlreadyPublishedException extends DomainConflictException {

    public ListingAlreadyPublishedException(String listingId) {
        super("Listing is already published: " + listingId);
    }
}
