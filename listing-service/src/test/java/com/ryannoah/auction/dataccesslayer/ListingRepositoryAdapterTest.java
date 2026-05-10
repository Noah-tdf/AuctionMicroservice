package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.Condition;
import com.ryannoah.auction.domain.Listing;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ListingRepositoryAdapter.class)
class ListingRepositoryAdapterTest {

    @Autowired
    private ListingRepositoryAdapter repositoryAdapter;

    @Test
    void shouldSaveAndLoadListing() {
        Listing saved = repositoryAdapter.save(Listing.create(
                new UserId("seller-123"),
                "Vintage Camera",
                "Film camera in good condition",
                "Photography",
                Condition.GOOD
        ));

        Listing loaded = repositoryAdapter.findById(saved.getListingId()).orElseThrow();
        assertThat(loaded.getListingId().value()).isEqualTo(saved.getListingId().value());
        assertThat(loaded.getTitle()).isEqualTo("Vintage Camera");
    }

    @Test
    void shouldReturnEmptyWhenListingDoesNotExist() {
        assertThat(repositoryAdapter.findById(new ListingId("missing-listing"))).isEmpty();
    }
}
