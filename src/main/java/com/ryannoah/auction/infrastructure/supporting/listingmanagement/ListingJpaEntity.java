package com.ryannoah.auction.infrastructure.supporting.listingmanagement;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "listings")
public class ListingJpaEntity {

    @Id
    private String listingId;
    private String sellerId;
    private String title;
    private String description;
    private String category;
    private String listingCondition;
    private boolean published;

    public ListingJpaEntity() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getListingCondition() {
        return listingCondition;
    }

    public void setListingCondition(String listingCondition) {
        this.listingCondition = listingCondition;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
