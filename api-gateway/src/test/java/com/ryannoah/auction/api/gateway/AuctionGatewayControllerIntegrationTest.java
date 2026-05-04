package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(AuctionGatewayController.class)
@Import({HypermediaSupport.class, GatewayGlobalExceptionHandler.class})
class AuctionGatewayControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionDomainClient auctionDomainClient;

    @Test
    void shouldReturnAuctionCollectionFromMockedDomainClient() {
        ArrayNode auctions = objectMapper.createArrayNode();
        auctions.add(objectMapper.createObjectNode().put("auctionId", "auction-integration"));
        when(auctionDomainClient.listAuctions()).thenReturn(Mono.just(auctions));

        webTestClient.get()
                .uri("/api/v1/auctions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].auctionId").isEqualTo("auction-integration");
    }

    @Test
    void shouldReturnDownstreamNotFoundFromMockedDomainClient() {
        when(auctionDomainClient.getAuction("missing")).thenReturn(Mono.error(
                new DownstreamServiceException(HttpStatus.NOT_FOUND, "Auction not found: missing", "/api/v1/auctions/missing")
        ));

        webTestClient.get()
                .uri("/api/v1/auctions/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Auction not found: missing");
    }
}
