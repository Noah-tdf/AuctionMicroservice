package com.ryannoah.auction.presentationlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryannoah.auction.dataccesslayer.AuctionDocument;
import com.ryannoah.auction.dataccesslayer.AuctionSpringDataRepository;
import com.ryannoah.auction.dataccesslayer.BidDocument;
import com.ryannoah.auction.dataccesslayer.BidSpringDataRepository;
import com.ryannoah.auction.domainclientlayer.InvoiceDomainClient;
import com.ryannoah.auction.domainclientlayer.ListingDomainClient;
import com.ryannoah.auction.domainclientlayer.UserDomainClient;
import com.ryannoah.auction.domainclientlayer.dto.InvoiceClientResponseDTO;
import com.ryannoah.auction.domainclientlayer.dto.ListingClientResponseDTO;
import com.ryannoah.auction.domainclientlayer.dto.UserClientResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuctionControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuctionSpringDataRepository auctionSpringDataRepository;

    @MockitoBean
    private BidSpringDataRepository bidSpringDataRepository;

    private Map<String, AuctionDocument> auctions;
    private Map<String, BidDocument> bids;

    @MockitoBean
    private UserDomainClient userDomainClient;

    @MockitoBean
    private ListingDomainClient listingDomainClient;

    @MockitoBean
    private InvoiceDomainClient invoiceDomainClient;

    @BeforeEach
    void setUp() {
        auctions = new LinkedHashMap<>();
        bids = new LinkedHashMap<>();
        stubAuctionRepository();
        stubBidRepository();
        when(listingDomainClient.getListing(anyString())).thenAnswer(invocation -> publishedListing(invocation.getArgument(0), "user-001"));
        when(userDomainClient.getUser(anyString())).thenAnswer(invocation -> verifiedUser(invocation.getArgument(0)));
        when(invoiceDomainClient.listInvoices()).thenReturn(new InvoiceClientResponseDTO[0]);
    }

    private void stubAuctionRepository() {
        when(auctionSpringDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(auctions.values()));
        when(auctionSpringDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(auctions.get(invocation.getArgument(0)))
        );
        when(auctionSpringDataRepository.save(any(AuctionDocument.class))).thenAnswer(invocation -> {
            AuctionDocument document = invocation.getArgument(0);
            auctions.put(document.getAuctionId(), document);
            return document;
        });
        doAnswer(invocation -> {
            auctions.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(auctionSpringDataRepository).deleteById(any(String.class));
    }

    private void stubBidRepository() {
        when(bidSpringDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(bids.values()));
        when(bidSpringDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(bids.get(invocation.getArgument(0)))
        );
        when(bidSpringDataRepository.findByAuctionIdOrderByBidTimeAsc(any(String.class))).thenAnswer(invocation ->
                bids.values().stream()
                        .filter(bid -> bid.getAuctionId().equals(invocation.getArgument(0)))
                        .sorted(Comparator.comparing(BidDocument::getBidTime))
                        .toList()
        );
        when(bidSpringDataRepository.save(any(BidDocument.class))).thenAnswer(invocation -> {
            BidDocument document = invocation.getArgument(0);
            bids.put(document.getBidId(), document);
            return document;
        });
        doAnswer(invocation -> {
            bids.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(bidSpringDataRepository).deleteById(any(String.class));
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
        when(listingDomainClient.getListing("listing-draft")).thenReturn(new ListingClientResponseDTO(
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

    private ListingClientResponseDTO publishedListing(String listingId, String sellerId) {
        return new ListingClientResponseDTO(listingId, sellerId, "Published", "Published listing", "Office", "GOOD", true);
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

    private UserClientResponseDTO verifiedUser(String userId) {
        return new UserClientResponseDTO(userId, "user", "user@example.com", LocalDateTime.now(), true, BigDecimal.ZERO, 0, null);
    }
}
