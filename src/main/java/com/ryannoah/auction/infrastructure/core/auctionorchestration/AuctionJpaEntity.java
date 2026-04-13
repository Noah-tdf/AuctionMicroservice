package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
public class AuctionJpaEntity {

    @Id
    private String auctionId;
    private String listingId;
    private String sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startingPriceAmount;
    private String currency;
    private BigDecimal currentPriceAmount;
    private String status;

    public AuctionJpaEntity() {
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getStartingPriceAmount() {
        return startingPriceAmount;
    }

    public void setStartingPriceAmount(BigDecimal startingPriceAmount) {
        this.startingPriceAmount = startingPriceAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getCurrentPriceAmount() {
        return currentPriceAmount;
    }

    public void setCurrentPriceAmount(BigDecimal currentPriceAmount) {
        this.currentPriceAmount = currentPriceAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
