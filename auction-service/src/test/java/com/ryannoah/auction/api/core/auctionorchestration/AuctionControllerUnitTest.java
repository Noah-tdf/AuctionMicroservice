package com.ryannoah.auction.api.core.auctionorchestration;

import com.ryannoah.auction.api.shared.GlobalExceptionHandler;
import com.ryannoah.auction.application.core.auctionorchestration.AuctionApplicationService;
import com.ryannoah.auction.application.core.auctionorchestration.BidApplicationService;
import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
@Import(GlobalExceptionHandler.class)
class AuctionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionApplicationService auctionApplicationService;

    @MockBean
    private BidApplicationService bidApplicationService;

    @Test
    void shouldReturnCreatedWhenServiceCreatesAuction() throws Exception {
        Auction auction = auction();
        when(auctionApplicationService.createAuction(any())).thenReturn(auction);
        when(auctionApplicationService.getAuctionAggregate(auction.getAuctionId().value()))
                .thenReturn(new AuctionApplicationService.AuctionAggregate(auction, null, null, null));
        when(bidApplicationService.listBids(auction.getAuctionId().value())).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": "listing-unit",
                                  "sellerId": "seller-unit",
                                  "startTime": "2026-06-01T10:00:00",
                                  "endTime": "2026-06-08T10:00:00",
                                  "startingPrice": 100.00,
                                  "currency": "CAD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listingId").value("listing-unit"));
    }

    @Test
    void shouldReturnNotFoundWhenServiceCannotFindAuction() throws Exception {
        when(auctionApplicationService.getAuctionAggregate("missing")).thenThrow(new DomainNotFoundException("Auction not found: missing"));

        mockMvc.perform(get("/api/v1/auctions/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Auction not found: missing"));
    }

    private Auction auction() {
        return Auction.schedule(
                new ListingId("listing-unit"),
                new UserId("seller-unit"),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("100.00"), "CAD")
        );
    }
}
