package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuctionDomainClient extends AbstractDomainClient {

    private final String auctionServiceBaseUrl;

    public AuctionDomainClient(
            WebClient webClient,
            @Value("${services.auction-service.base-url}") String auctionServiceBaseUrl
    ) {
        super(webClient);
        this.auctionServiceBaseUrl = auctionServiceBaseUrl;
    }

    public Mono<ArrayNode> listAuctions() {
        return fetchCollection(auctionServiceBaseUrl, "/api/v1/auctions");
    }

    public Mono<JsonNode> getAuction(String auctionId) {
        return fetchObject(auctionServiceBaseUrl, "/api/v1/auctions/" + auctionId);
    }

    public Mono<JsonNode> createAuction(JsonNode request) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.POST, "/api/v1/auctions", request);
    }

    public Mono<JsonNode> updateAuction(String auctionId, JsonNode request) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.PUT, "/api/v1/auctions/" + auctionId, request);
    }

    public Mono<Void> deleteAuction(String auctionId) {
        return delete(auctionServiceBaseUrl, "/api/v1/auctions/" + auctionId);
    }

    public Mono<JsonNode> activateAuction(String auctionId) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/activate", null);
    }

    public Mono<JsonNode> closeAuction(String auctionId) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/close", null);
    }

    public Mono<ArrayNode> listBidsByAuction(String auctionId) {
        return fetchCollection(auctionServiceBaseUrl, "/api/v1/auctions/" + auctionId + "/bids");
    }

    public Mono<ArrayNode> listBids() {
        return fetchCollection(auctionServiceBaseUrl, "/api/v1/bids");
    }

    public Mono<JsonNode> placeBid(String auctionId, JsonNode request) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/bids", request);
    }

    public Mono<JsonNode> updateBid(String auctionId, String bidId, JsonNode request) {
        return exchangeForObject(auctionServiceBaseUrl, HttpMethod.PUT, "/api/v1/auctions/" + auctionId + "/bids/" + bidId, request);
    }

    public Mono<Void> deleteBid(String auctionId, String bidId) {
        return delete(auctionServiceBaseUrl, "/api/v1/auctions/" + auctionId + "/bids/" + bidId);
    }
}
