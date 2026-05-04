package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.Bid;
import com.ryannoah.auction.domain.core.auctionorchestration.BidId;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BidRepositoryAdapterTest {

    @Autowired
    private BidRepositoryAdapter bidRepositoryAdapter;

    @Test
    void shouldSaveFindListAndDeleteBid() {
        AuctionId auctionId = new AuctionId("auction-repo-bid");
        Bid saved = bidRepositoryAdapter.save(Bid.create(
                auctionId,
                new UserId("bidder-repo"),
                new Money(new BigDecimal("125.00"), "CAD")
        ));

        assertThat(bidRepositoryAdapter.findById(saved.getBidId())).isPresent();
        assertThat(bidRepositoryAdapter.findByAuctionId(auctionId)).hasSize(1);
        assertThat(bidRepositoryAdapter.findAll()).isNotEmpty();

        bidRepositoryAdapter.deleteById(saved.getBidId());

        assertThat(bidRepositoryAdapter.findById(saved.getBidId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyForMissingBid() {
        assertThat(bidRepositoryAdapter.findById(new BidId("missing-bid"))).isEmpty();
    }
}
