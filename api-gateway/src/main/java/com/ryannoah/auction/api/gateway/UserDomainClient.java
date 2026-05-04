package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserDomainClient extends AbstractDomainClient {

    private final String userServiceBaseUrl;

    public UserDomainClient(
            WebClient webClient,
            @Value("${services.user-service.base-url}") String userServiceBaseUrl
    ) {
        super(webClient);
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public Mono<ArrayNode> listUsers() {
        return fetchCollection(userServiceBaseUrl, "/api/v1/users");
    }

    public Mono<JsonNode> getUser(String userId) {
        return fetchObject(userServiceBaseUrl, "/api/v1/users/" + userId);
    }

    public Mono<JsonNode> createUser(JsonNode request) {
        return exchangeForObject(userServiceBaseUrl, HttpMethod.POST, "/api/v1/users", request);
    }

    public Mono<JsonNode> updateUser(String userId, JsonNode request) {
        return exchangeForObject(userServiceBaseUrl, HttpMethod.PUT, "/api/v1/users/" + userId, request);
    }

    public Mono<Void> deleteUser(String userId) {
        return delete(userServiceBaseUrl, "/api/v1/users/" + userId);
    }
}
