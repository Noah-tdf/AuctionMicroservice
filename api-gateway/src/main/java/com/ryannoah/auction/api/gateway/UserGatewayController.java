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
@RequestMapping("/api/v1/users")
public class UserGatewayController {

    private final WebClient webClient;
    private final HypermediaSupport hypermediaSupport;
    private final String userServiceBaseUrl;

    public UserGatewayController(
            WebClient webClient,
            HypermediaSupport hypermediaSupport,
            @Value("${services.user-service.base-url}") String userServiceBaseUrl
    ) {
        this.webClient = webClient;
        this.hypermediaSupport = hypermediaSupport;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listUsers() {
        return webClient.get()
                .uri(userServiceBaseUrl + "/api/v1/users")
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/users",
                        "create", "/api/v1/users"
                ))));
    }

    @GetMapping("/{userId}")
    Mono<ResponseEntity<JsonNode>> getUser(@PathVariable String userId) {
        return forwardWithLinks(HttpMethod.GET, "/api/v1/users/" + userId, null, Map.of(
                "self", "/api/v1/users/" + userId,
                "update", "/api/v1/users/" + userId,
                "delete", "/api/v1/users/" + userId
        ));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createUser(@RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/users", request, Map.of(
                "collection", "/api/v1/users"
        ), HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    Mono<ResponseEntity<JsonNode>> updateUser(@PathVariable String userId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.PUT, "/api/v1/users/" + userId, request, Map.of(
                "self", "/api/v1/users/" + userId,
                "collection", "/api/v1/users"
        ));
    }

    @DeleteMapping("/{userId}")
    Mono<ResponseEntity<Void>> deleteUser(@PathVariable String userId) {
        return webClient.delete()
                .uri(userServiceBaseUrl + "/api/v1/users/{id}", userId)
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
        WebClient.RequestBodySpec spec = webClient.method(method).uri(userServiceBaseUrl + path);
        WebClient.RequestHeadersSpec<?> headersSpec = request == null ? spec : spec.bodyValue(request);
        return headersSpec.retrieve()
                .bodyToMono(JsonNode.class)
                .map(body -> ResponseEntity.status(expectedStatus).body(hypermediaSupport.addLinks(body, links)));
    }
}
