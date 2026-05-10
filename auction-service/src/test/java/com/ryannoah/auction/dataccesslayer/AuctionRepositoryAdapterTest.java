package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.Auction;
import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.domain.ListingId;
import com.ryannoah.auction.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class AuctionRepositoryAdapterTest {

    @Mock
    private AuctionSpringDataRepository springDataRepository;
    private AuctionRepositoryAdapter auctionRepositoryAdapter;
    private Map<String, AuctionDocument> auctions;

    @BeforeEach
    void setUp() {
        auctions = new LinkedHashMap<>();
        auctionRepositoryAdapter = new AuctionRepositoryAdapter(springDataRepository);
        when(springDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(auctions.values()));
        when(springDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(auctions.get(invocation.getArgument(0)))
        );
        when(springDataRepository.save(any(AuctionDocument.class))).thenAnswer(invocation -> {
            AuctionDocument document = invocation.getArgument(0);
            auctions.put(document.getAuctionId(), document);
            return document;
        });
        doAnswer(invocation -> {
            auctions.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(springDataRepository).deleteById(any(String.class));
    }

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
