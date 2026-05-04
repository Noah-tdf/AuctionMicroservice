package com.ryannoah.auction.api.core.auctionorchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.InvoiceDomainClient;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.ListingDomainClient;
import com.ryannoah.auction.infrastructure.core.auctionorchestration.client.UserDomainClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuctionControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDomainClient userDomainClient;

    @MockBean
    private ListingDomainClient listingDomainClient;

    @MockBean
    private InvoiceDomainClient invoiceDomainClient;

    @BeforeEach
    void setUp() {
        when(listingDomainClient.getListing(anyString())).thenAnswer(invocation -> publishedListing(invocation.getArgument(0), "user-001"));
        when(userDomainClient.getUser(anyString())).thenAnswer(invocation -> verifiedUser(invocation.getArgument(0)));
        when(invoiceDomainClient.listInvoices()).thenReturn(new InvoiceDomainClient.InvoiceResponse[0]);
    }

    @Test
    void shouldCreateAndFetchAuctionAggregate() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "listingId": "listing-new",
                          "sellerId": "user-001",
                          "startTime": "2026-06-01T10:00:00",
                          "endTime": "2026-06-08T10:00:00",
                          "startingPrice": 100.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.listingId").isEqualTo("listing-new")
                .jsonPath("$.listing.published").isEqualTo(true)
                .returnResult();

        JsonNode body = objectMapper.readTree(result.getResponseBody());

        webTestClient.get()
                .uri("/api/v1/auctions/{id}", body.get("auctionId").asText())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.seller.verified").isEqualTo(true);
    }

    @Test
    void shouldRejectAuctionWhenListingIsNotPublished() {
        when(listingDomainClient.getListing("listing-draft")).thenReturn(new ListingDomainClient.ListingResponse(
                "listing-draft", "user-001", "Draft", "Draft listing", "Office", "GOOD", false
        ));

        webTestClient.post()
                .uri("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "listingId": "listing-draft",
                          "sellerId": "user-001",
                          "startTime": "2026-06-01T10:00:00",
                          "endTime": "2026-06-08T10:00:00",
                          "startingPrice": 100.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Auction listing must be published before scheduling: listing-draft");
    }

    @Test
    void shouldReturnNotFoundForMissingAuction() {
        webTestClient.get()
                .uri("/api/v1/auctions/missing-auction")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Auction not found: missing-auction");
    }

    @Test
    void shouldRejectInvalidAuctionPayloadAndUnsupportedMethod() {
        webTestClient.post()
                .uri("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "listingId": "",
                          "sellerId": "user-001",
                          "startTime": "2026-06-01T10:00:00",
                          "endTime": "2026-06-08T10:00:00",
                          "startingPrice": 100.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();

        webTestClient.patch()
                .uri("/api/v1/auctions")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldListUpdateActivateCloseAndDeleteAuction() throws Exception {
        String auctionId = createAuction("listing-workflow", "user-001");

        webTestClient.get()
                .uri("/api/v1/auctions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].auctionId").exists();

        webTestClient.put()
                .uri("/api/v1/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "startTime": "2026-06-02T10:00:00",
                          "endTime": "2026-06-09T10:00:00",
                          "startingPrice": 125.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.startingPrice").isEqualTo(125.00);

        webTestClient.post()
                .uri("/api/v1/auctions/{id}/activate", auctionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ACTIVE");

        webTestClient.get()
                .uri("/api/v1/auctions/{id}/bids", auctionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);

        webTestClient.post()
                .uri("/api/v1/auctions/{id}/close", auctionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CLOSED");

        String deletableAuctionId = createAuction("listing-delete-controller", "user-001");
        webTestClient.delete()
                .uri("/api/v1/auctions/{id}", deletableAuctionId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldListAllBidsFromBidController() {
        webTestClient.get()
                .uri("/api/v1/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").exists();
    }

    @Test
    void shouldPlaceUpdateAndDeleteBidThroughAuctionController() throws Exception {
        String auctionId = createAuction("listing-bid-controller", "user-001");
        webTestClient.post()
                .uri("/api/v1/auctions/{id}/activate", auctionId)
                .exchange()
                .expectStatus().isOk();

        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/auctions/{id}/bids", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "bidderId": "user-777",
                          "bidAmount": 125.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.bidderId").isEqualTo("user-777")
                .returnResult();
        String bidId = objectMapper.readTree(result.getResponseBody()).get("bidId").asText();

        webTestClient.put()
                .uri("/api/v1/auctions/{auctionId}/bids/{bidId}", auctionId, bidId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "bidAmount": 150.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.bidAmount").isEqualTo(150.00);

        webTestClient.delete()
                .uri("/api/v1/auctions/{auctionId}/bids/{bidId}", auctionId, bidId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldExposeSystemStatus() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").exists();
    }

    @Test
    void shouldRejectSellerBiddingOnOwnAuction() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "listingId": "listing-own-bid",
                          "sellerId": "user-001",
                          "startTime": "2026-06-01T10:00:00",
                          "endTime": "2026-06-08T10:00:00",
                          "startingPrice": 100.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();
        String auctionId = objectMapper.readTree(result.getResponseBody()).get("auctionId").asText();

        webTestClient.post()
                .uri("/api/v1/auctions/{id}/activate", auctionId)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/v1/auctions/{id}/bids", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "bidderId": "user-001",
                          "bidAmount": 500.00,
                          "currency": "CAD"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Auction seller cannot bid on their own auction: user-001");
    }

    private ListingDomainClient.ListingResponse publishedListing(String listingId, String sellerId) {
        return new ListingDomainClient.ListingResponse(listingId, sellerId, "Published", "Published listing", "Office", "GOOD", true);
    }

    private String createAuction(String listingId, String sellerId) throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "listingId": "%s",
                          "sellerId": "%s",
                          "startTime": "2026-06-01T10:00:00",
                          "endTime": "2026-06-08T10:00:00",
                          "startingPrice": 100.00,
                          "currency": "CAD"
                        }
                        """.formatted(listingId, sellerId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();
        return objectMapper.readTree(result.getResponseBody()).get("auctionId").asText();
    }

    private UserDomainClient.UserResponse verifiedUser(String userId) {
        return new UserDomainClient.UserResponse(userId, "user", "user@example.com", LocalDateTime.now(), true, BigDecimal.ZERO, 0, null);
    }
}
