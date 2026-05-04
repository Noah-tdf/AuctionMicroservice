package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayControllersHateoasTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuctionDomainClient auctionDomainClient = mock(AuctionDomainClient.class);
    private final UserDomainClient userDomainClient = mock(UserDomainClient.class);
    private final ListingDomainClient listingDomainClient = mock(ListingDomainClient.class);
    private final InvoiceDomainClient invoiceDomainClient = mock(InvoiceDomainClient.class);
    private final WebTestClient webTestClient = WebTestClient.bindToController(
                    new GatewayHomeController(),
                    new AuctionGatewayController(auctionDomainClient, new HypermediaSupport(objectMapper)),
                    new BidGatewayController(auctionDomainClient, new HypermediaSupport(objectMapper)),
                    new UserGatewayController(userDomainClient, new HypermediaSupport(objectMapper)),
                    new ListingGatewayController(listingDomainClient, new HypermediaSupport(objectMapper)),
                    new InvoiceGatewayController(invoiceDomainClient, new HypermediaSupport(objectMapper))
            )
            .controllerAdvice(new GatewayGlobalExceptionHandler())
            .build();

    @BeforeEach
    void setUp() {
        when(auctionDomainClient.listBids()).thenReturn(Mono.just(array("bidId", "bid-1")));
        when(auctionDomainClient.listBidsByAuction("auction-1")).thenReturn(Mono.just(array("bidId", "bid-1")));
        when(auctionDomainClient.placeBid(eq("auction-1"), any())).thenReturn(Mono.just(object("bidId", "bid-2")));
        when(auctionDomainClient.updateBid(eq("auction-1"), eq("bid-2"), any())).thenReturn(Mono.just(object("bidId", "bid-2")));
        when(auctionDomainClient.deleteBid("auction-1", "bid-2")).thenReturn(Mono.empty());
        when(auctionDomainClient.activateAuction("auction-1")).thenReturn(Mono.just(object("auctionId", "auction-1")));
        when(auctionDomainClient.closeAuction("auction-1")).thenReturn(Mono.just(object("status", "SOLD")));
        when(auctionDomainClient.updateAuction(eq("auction-1"), any())).thenReturn(Mono.just(object("auctionId", "auction-1")));
        when(auctionDomainClient.deleteAuction("auction-1")).thenReturn(Mono.empty());

        when(userDomainClient.listUsers()).thenReturn(Mono.just(array("userId", "user-1")));
        when(userDomainClient.getUser("user-1")).thenReturn(Mono.just(object("userId", "user-1")));
        when(userDomainClient.createUser(any())).thenReturn(Mono.just(object("userId", "user-2")));
        when(userDomainClient.updateUser(eq("user-1"), any())).thenReturn(Mono.just(object("userId", "user-1")));
        when(userDomainClient.deleteUser("user-1")).thenReturn(Mono.empty());

        when(listingDomainClient.listListings()).thenReturn(Mono.just(array("listingId", "listing-1")));
        when(listingDomainClient.getListing("listing-1")).thenReturn(Mono.just(object("listingId", "listing-1")));
        when(listingDomainClient.createListing(any())).thenReturn(Mono.just(object("listingId", "listing-2")));
        when(listingDomainClient.updateListing(eq("listing-1"), any())).thenReturn(Mono.just(object("listingId", "listing-1")));
        when(listingDomainClient.publishListing("listing-1")).thenReturn(Mono.just(object("published", "true")));
        when(listingDomainClient.deleteListing("listing-1")).thenReturn(Mono.empty());

        when(invoiceDomainClient.listInvoices()).thenReturn(Mono.just(array("invoiceId", "invoice-1")));
        when(invoiceDomainClient.getInvoice("invoice-1")).thenReturn(Mono.just(object("invoiceId", "invoice-1")));
        when(invoiceDomainClient.createInvoice(any())).thenReturn(Mono.just(object("invoiceId", "invoice-2")));
        when(invoiceDomainClient.updateInvoice(eq("invoice-1"), any())).thenReturn(Mono.just(object("invoiceId", "invoice-1")));
        when(invoiceDomainClient.payInvoice("invoice-1")).thenReturn(Mono.just(object("status", "PAID")));
        when(invoiceDomainClient.deleteInvoice("invoice-1")).thenReturn(Mono.empty());
    }

    @Test
    void shouldExposeGatewayHome() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo("api-gateway")
                .jsonPath("$._links.auctions.href").isEqualTo("/api/v1/auctions");
    }

    @Test
    void shouldRouteBidEndpoints() {
        webTestClient.get().uri("/api/v1/bids").exchange().expectStatus().isOk().expectBody().jsonPath("$.items[0].bidId").isEqualTo("bid-1");
        webTestClient.get().uri("/api/v1/auctions/auction-1/bids").exchange().expectStatus().isOk().expectBody().jsonPath("$.items[0].bidId").isEqualTo("bid-1");
        webTestClient.post().uri("/api/v1/auctions/auction-1/bids").bodyValue(object("bidderId", "user-2")).exchange().expectStatus().isCreated();
        webTestClient.put().uri("/api/v1/auctions/auction-1/bids/bid-2").bodyValue(object("bidAmount", "125")).exchange().expectStatus().isOk();
        webTestClient.delete().uri("/api/v1/auctions/auction-1/bids/bid-2").exchange().expectStatus().isNoContent();
    }

    @Test
    void shouldRouteAuctionStateEndpoints() {
        webTestClient.post().uri("/api/v1/auctions/auction-1/activate").exchange().expectStatus().isOk();
        webTestClient.post().uri("/api/v1/auctions/auction-1/close").exchange().expectStatus().isOk();
        webTestClient.put().uri("/api/v1/auctions/auction-1").bodyValue(object("currency", "CAD")).exchange().expectStatus().isOk();
        webTestClient.delete().uri("/api/v1/auctions/auction-1").exchange().expectStatus().isNoContent();
    }

    @Test
    void shouldRouteUserEndpoints() {
        webTestClient.get().uri("/api/v1/users").exchange().expectStatus().isOk().expectBody().jsonPath("$.items[0].userId").isEqualTo("user-1");
        webTestClient.get().uri("/api/v1/users/user-1").exchange().expectStatus().isOk().expectBody().jsonPath("$._links.update.href").isEqualTo("/api/v1/users/user-1");
        webTestClient.post().uri("/api/v1/users").bodyValue(object("username", "user")).exchange().expectStatus().isCreated();
        webTestClient.put().uri("/api/v1/users/user-1").bodyValue(object("username", "user")).exchange().expectStatus().isOk();
        webTestClient.delete().uri("/api/v1/users/user-1").exchange().expectStatus().isNoContent();
    }

    @Test
    void shouldRouteListingEndpoints() {
        webTestClient.get().uri("/api/v1/listings").exchange().expectStatus().isOk().expectBody().jsonPath("$.items[0].listingId").isEqualTo("listing-1");
        webTestClient.get().uri("/api/v1/listings/listing-1").exchange().expectStatus().isOk().expectBody().jsonPath("$._links.publish.href").isEqualTo("/api/v1/listings/listing-1/publish");
        webTestClient.post().uri("/api/v1/listings").bodyValue(object("title", "listing")).exchange().expectStatus().isCreated();
        webTestClient.put().uri("/api/v1/listings/listing-1").bodyValue(object("title", "listing")).exchange().expectStatus().isOk();
        webTestClient.post().uri("/api/v1/listings/listing-1/publish").exchange().expectStatus().isOk();
        webTestClient.delete().uri("/api/v1/listings/listing-1").exchange().expectStatus().isNoContent();
    }

    @Test
    void shouldRouteInvoiceEndpoints() {
        webTestClient.get().uri("/api/v1/invoices").exchange().expectStatus().isOk().expectBody().jsonPath("$.items[0].invoiceId").isEqualTo("invoice-1");
        webTestClient.get().uri("/api/v1/invoices/invoice-1").exchange().expectStatus().isOk().expectBody().jsonPath("$._links.pay.href").isEqualTo("/api/v1/invoices/invoice-1/pay");
        webTestClient.post().uri("/api/v1/invoices").bodyValue(object("method", "CREDIT_CARD")).exchange().expectStatus().isCreated();
        webTestClient.put().uri("/api/v1/invoices/invoice-1").bodyValue(object("method", "PAYPAL")).exchange().expectStatus().isOk();
        webTestClient.post().uri("/api/v1/invoices/invoice-1/pay").exchange().expectStatus().isOk();
        webTestClient.delete().uri("/api/v1/invoices/invoice-1").exchange().expectStatus().isNoContent();
    }

    private ArrayNode array(String field, String value) {
        ArrayNode array = objectMapper.createArrayNode();
        array.add(object(field, value));
        return array;
    }

    private ObjectNode object(String field, String value) {
        return objectMapper.createObjectNode().put(field, value);
    }
}
