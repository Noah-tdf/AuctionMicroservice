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
@RequestMapping("/api/v1/listings")
public class ListingGatewayController {

    private final WebClient webClient;
    private final HypermediaSupport hypermediaSupport;
    private final String listingServiceBaseUrl;

    public ListingGatewayController(
            WebClient webClient,
            HypermediaSupport hypermediaSupport,
            @Value("${services.listing-service.base-url}") String listingServiceBaseUrl
    ) {
        this.webClient = webClient;
        this.hypermediaSupport = hypermediaSupport;
        this.listingServiceBaseUrl = listingServiceBaseUrl;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listListings() {
        return webClient.get()
                .uri(listingServiceBaseUrl + "/api/v1/listings")
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/listings",
                        "create", "/api/v1/listings"
                ))));
    }

    @GetMapping("/{listingId}")
    Mono<ResponseEntity<JsonNode>> getListing(@PathVariable String listingId) {
        return forwardWithLinks(HttpMethod.GET, "/api/v1/listings/" + listingId, null, Map.of(
                "self", "/api/v1/listings/" + listingId,
                "update", "/api/v1/listings/" + listingId,
                "delete", "/api/v1/listings/" + listingId,
                "publish", "/api/v1/listings/" + listingId + "/publish"
        ));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createListing(@RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/listings", request, Map.of(
                "collection", "/api/v1/listings"
        ), HttpStatus.CREATED);
    }

    @PutMapping("/{listingId}")
    Mono<ResponseEntity<JsonNode>> updateListing(@PathVariable String listingId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.PUT, "/api/v1/listings/" + listingId, request, Map.of(
                "self", "/api/v1/listings/" + listingId,
                "publish", "/api/v1/listings/" + listingId + "/publish"
        ));
    }

    @PostMapping("/{listingId}/publish")
    Mono<ResponseEntity<JsonNode>> publishListing(@PathVariable String listingId) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/listings/" + listingId + "/publish", null, Map.of(
                "self", "/api/v1/listings/" + listingId,
                "collection", "/api/v1/listings"
        ));
    }

    @DeleteMapping("/{listingId}")
    Mono<ResponseEntity<Void>> deleteListing(@PathVariable String listingId) {
        return webClient.delete()
                .uri(listingServiceBaseUrl + "/api/v1/listings/{id}", listingId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.noContent().build());
    }

    private Mono<ResponseEntity<JsonNode>> forwardWithLinks(HttpMethod method, String path, JsonNode request, Map<String, String> links) {
        return forwardWithLinks(method, path, request, links, HttpStatus.OK);
    }

    private Mono<ResponseEntity<JsonNode>> forwardWithLinks(
            HttpMethod method,
            String path,
            JsonNode request,
            Map<String, String> links,
            HttpStatus expectedStatus
    ) {
        WebClient.RequestBodySpec spec = webClient.method(method).uri(listingServiceBaseUrl + path);
        WebClient.RequestHeadersSpec<?> headersSpec = request == null ? spec : spec.bodyValue(request);
        return headersSpec.retrieve()
                .bodyToMono(JsonNode.class)
                .map(body -> ResponseEntity.status(expectedStatus).body(hypermediaSupport.addLinks(body, links)));
    }
}
