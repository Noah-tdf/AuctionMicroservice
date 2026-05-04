package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuctionRepositoryAdapterTest {

    @Autowired
    private AuctionRepositoryAdapter auctionRepositoryAdapter;

    @Test
    void shouldSaveAndFindAuction() {
        Auction auction = Auction.schedule(
                new ListingId("listing-repository"),
                new UserId("user-repository"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2),
                new Money(new BigDecimal("99.00"), "CAD")
        );

        Auction saved = auctionRepositoryAdapter.save(auction);

        assertThat(auctionRepositoryAdapter.findById(saved.getAuctionId()))
                .isPresent()
                .get()
                .extracting(found -> found.getListingId().value())
                .isEqualTo("listing-repository");
    }

    @Test
    void shouldReturnEmptyForMissingAuction() {
        assertThat(auctionRepositoryAdapter.findById(new AuctionId("missing-auction"))).isEmpty();
    }
}
