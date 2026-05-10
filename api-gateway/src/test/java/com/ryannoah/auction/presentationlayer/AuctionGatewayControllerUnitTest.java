package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.datamappinglayer.*;
import com.ryannoah.auction.domainclientlayer.*;
import com.ryannoah.auction.utilities.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuctionGatewayControllerUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuctionDomainClient auctionDomainClient = mock(AuctionDomainClient.class);
    private final WebTestClient webTestClient = WebTestClient.bindToController(
                    new AuctionGatewayController(auctionDomainClient, new HypermediaSupport(objectMapper))
            )
            .controllerAdvice(new GatewayGlobalExceptionHandler())
            .build();

    @Test
    void shouldWrapAuctionCollectionWithHateoasLinks() {
        ArrayNode auctions = objectMapper.createArrayNode();
        auctions.add(objectMapper.createObjectNode().put("auctionId", "auction-1"));
        when(auctionDomainClient.listAuctions()).thenReturn(Mono.just(auctions));

        webTestClient.get()
                .uri("/api/v1/auctions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].auctionId").isEqualTo("auction-1")
                .jsonPath("$._links.self.href").isEqualTo("/api/v1/auctions");
    }

    @Test
    void shouldMapDownstreamErrorsThroughGlobalExceptionHandler() {
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

    @Test
    void shouldAddLinksToCreatedAuction() {
        ObjectNode auction = objectMapper.createObjectNode().put("auctionId", "auction-2");
        when(auctionDomainClient.createAuction(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.just(auction));

        webTestClient.post()
                .uri("/api/v1/auctions")
                .bodyValue(objectMapper.createObjectNode().put("listingId", "listing-2"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.auctionId").isEqualTo("auction-2")
                .jsonPath("$._links.collection.href").isEqualTo("/api/v1/auctions");
    }
}
