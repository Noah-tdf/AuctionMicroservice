package com.ryannoah.auction.domain.supporting.listingmanagement;

import com.ryannoah.auction.domain.shared.DomainValidationException;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;

public class Listing {

    private final ListingId listingId;
    private final UserId sellerId;
    private final String title;
    private final String description;
    private final String category;
    private final Condition condition;
    private boolean published;

    public Listing(
            ListingId listingId,
            UserId sellerId,
            String title,
            String description,
            String category,
            Condition condition,
            boolean published
    ) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.published = published;
    }

    public static Listing create(UserId sellerId, String title, String description, String category, Condition condition) {
        return new Listing(ListingId.newId(), sellerId, title, description, category, condition, false);
    }

    public void publish() {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("Listing title must not be empty");
        }
        if (description == null || description.isBlank()) {
            throw new DomainValidationException("Listing description must not be empty");
        }
        if (sellerId == null || sellerId.value().isBlank()) {
            throw new DomainValidationException("Listing sellerId must be valid");
        }
        published = true;
    }

    public ListingId getListingId() {
        return listingId;
    }

    public UserId getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Condition getCondition() {
        return condition;
    }

    public boolean isPublished() {
        return published;
    }
}
