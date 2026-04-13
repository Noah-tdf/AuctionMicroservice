package com.ryannoah.auction.api.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class GatewayHomeController {

    @GetMapping("/")
    Mono<Map<String, Object>> root() {
        return Mono.just(Map.of(
                "service", "api-gateway",
                "status", "ok",
                "message", "Auction API gateway is running",
                "_links", Map.of(
                        "users", Map.of("href", "/api/v1/users"),
                        "listings", Map.of("href", "/api/v1/listings"),
                        "invoices", Map.of("href", "/api/v1/invoices"),
                        "auctions", Map.of("href", "/api/v1/auctions"),
                        "bids", Map.of("href", "/api/v1/bids")
                )
        ));
    }
}
