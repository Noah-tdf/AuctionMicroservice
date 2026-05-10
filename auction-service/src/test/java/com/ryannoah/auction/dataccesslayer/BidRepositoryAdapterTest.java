package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Bid;
import com.ryannoah.auction.domain.BidId;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BidRepositoryAdapterTest {

    @Mock
    private BidSpringDataRepository springDataRepository;
    private BidRepositoryAdapter bidRepositoryAdapter;
    private Map<String, BidDocument> bids;

    @BeforeEach
    void setUp() {
        bids = new LinkedHashMap<>();
        bidRepositoryAdapter = new BidRepositoryAdapter(springDataRepository);
        when(springDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(bids.values()));
        when(springDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(bids.get(invocation.getArgument(0)))
        );
        when(springDataRepository.findByAuctionIdOrderByBidTimeAsc(any(String.class))).thenAnswer(invocation ->
                bids.values().stream()
                        .filter(bid -> bid.getAuctionId().equals(invocation.getArgument(0)))
                        .sorted(Comparator.comparing(BidDocument::getBidTime))
                        .toList()
        );
        when(springDataRepository.save(any(BidDocument.class))).thenAnswer(invocation -> {
            BidDocument document = invocation.getArgument(0);
            bids.put(document.getBidId(), document);
            return document;
        });
        doAnswer(invocation -> {
            bids.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(springDataRepository).deleteById(any(String.class));
    }

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
