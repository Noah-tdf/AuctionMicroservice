package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bids")
public class BidGatewayController {

    private final WebClient webClient;
    private final HypermediaSupport hypermediaSupport;
    private final String auctionServiceBaseUrl;

    public BidGatewayController(
            WebClient webClient,
            HypermediaSupport hypermediaSupport,
            @Value("${services.auction-service.base-url}") String auctionServiceBaseUrl
    ) {
        this.webClient = webClient;
        this.hypermediaSupport = hypermediaSupport;
        this.auctionServiceBaseUrl = auctionServiceBaseUrl;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listBids() {
        return webClient.get()
                .uri(auctionServiceBaseUrl + "/api/v1/bids")
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/bids"
                ))));
    }
}
