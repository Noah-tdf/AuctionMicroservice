package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auctions")
public class AuctionGatewayController {

    private final WebClient webClient;
    private final HypermediaSupport hypermediaSupport;
    private final String auctionServiceBaseUrl;

    public AuctionGatewayController(
            WebClient webClient,
            HypermediaSupport hypermediaSupport,
            @Value("${services.auction-service.base-url}") String auctionServiceBaseUrl
    ) {
        this.webClient = webClient;
        this.hypermediaSupport = hypermediaSupport;
        this.auctionServiceBaseUrl = auctionServiceBaseUrl;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listAuctions() {
        return webClient.get()
                .uri(auctionServiceBaseUrl + "/api/v1/auctions")
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/auctions",
                        "create", "/api/v1/auctions"
                ))));
    }

    @GetMapping("/{auctionId}")
    Mono<ResponseEntity<JsonNode>> getAuction(@PathVariable String auctionId) {
        return forwardWithLinks(HttpMethod.GET, "/api/v1/auctions/" + auctionId, null, Map.of(
                "self", "/api/v1/auctions/" + auctionId,
                "bids", "/api/v1/auctions/" + auctionId + "/bids",
                "activate", "/api/v1/auctions/" + auctionId + "/activate",
                "close", "/api/v1/auctions/" + auctionId + "/close"
        ));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createAuction(@RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/auctions", request, Map.of(
                "collection", "/api/v1/auctions"
        ), HttpStatus.CREATED);
    }

    @PutMapping("/{auctionId}")
    Mono<ResponseEntity<JsonNode>> updateAuction(@PathVariable String auctionId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.PUT, "/api/v1/auctions/" + auctionId, request, Map.of(
                "self", "/api/v1/auctions/" + auctionId,
                "bids", "/api/v1/auctions/" + auctionId + "/bids"
        ));
    }

    @DeleteMapping("/{auctionId}")
    Mono<ResponseEntity<Void>> deleteAuction(@PathVariable String auctionId) {
        return webClient.delete()
                .uri(auctionServiceBaseUrl + "/api/v1/auctions/{id}", auctionId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{auctionId}/activate")
    Mono<ResponseEntity<JsonNode>> activateAuction(@PathVariable String auctionId) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/activate", null, Map.of(
                "self", "/api/v1/auctions/" + auctionId
        ));
    }

    @PostMapping("/{auctionId}/close")
    Mono<ResponseEntity<JsonNode>> closeAuction(@PathVariable String auctionId) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/close", null, Map.of(
                "self", "/api/v1/auctions/" + auctionId
        ));
    }

    @GetMapping("/{auctionId}/bids")
    Mono<ResponseEntity<JsonNode>> listBidsByAuction(@PathVariable String auctionId) {
        return webClient.get()
                .uri(auctionServiceBaseUrl + "/api/v1/auctions/{id}/bids", auctionId)
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/auctions/" + auctionId + "/bids",
                        "auction", "/api/v1/auctions/" + auctionId
                ))));
    }

    @PostMapping("/{auctionId}/bids")
    Mono<ResponseEntity<JsonNode>> placeBid(@PathVariable String auctionId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/auctions/" + auctionId + "/bids", request, Map.of(
                "auction", "/api/v1/auctions/" + auctionId,
                "collection", "/api/v1/auctions/" + auctionId + "/bids"
        ), HttpStatus.CREATED);
    }

    @PutMapping("/{auctionId}/bids/{bidId}")
    Mono<ResponseEntity<JsonNode>> updateBid(@PathVariable String auctionId, @PathVariable String bidId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.PUT, "/api/v1/auctions/" + auctionId + "/bids/" + bidId, request, Map.of(
                "auction", "/api/v1/auctions/" + auctionId,
                "collection", "/api/v1/auctions/" + auctionId + "/bids"
        ));
    }

    @DeleteMapping("/{auctionId}/bids/{bidId}")
    Mono<ResponseEntity<Void>> deleteBid(@PathVariable String auctionId, @PathVariable String bidId) {
        return webClient.delete()
                .uri(auctionServiceBaseUrl + "/api/v1/auctions/{auctionId}/bids/{bidId}", auctionId, bidId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.noContent().build());
    }

    private Mono<ResponseEntity<JsonNode>> forwardWithLinks(HttpMethod method, String path, JsonNode request, Map<String, String> links) {
        return forwardWithLinks(method, path, request, links, HttpStatus.OK);
    }

    private Mono<ResponseEntity<JsonNode>> forwardWithLinks(HttpMethod method, String path, JsonNode request, Map<String, String> links, HttpStatus expectedStatus) {
        WebClient.RequestBodySpec spec = webClient.method(method).uri(auctionServiceBaseUrl + path);
        WebClient.RequestHeadersSpec<?> headersSpec = request == null ? spec : spec.bodyValue(request);
        return headersSpec.retrieve()
                .bodyToMono(JsonNode.class)
                .map(body -> ResponseEntity.status(expectedStatus).body(hypermediaSupport.addLinks(body, links)));
    }
}
