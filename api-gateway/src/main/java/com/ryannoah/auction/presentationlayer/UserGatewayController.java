package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.datamappinglayer.*;
import com.ryannoah.auction.domainclientlayer.*;

import com.fasterxml.jackson.databind.JsonNode;
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
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserGatewayController {

    private final UserDomainClient userDomainClient;
    private final HypermediaSupport hypermediaSupport;

    public UserGatewayController(
            UserDomainClient userDomainClient,
            HypermediaSupport hypermediaSupport
    ) {
        this.userDomainClient = userDomainClient;
        this.hypermediaSupport = hypermediaSupport;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listUsers() {
        return userDomainClient.listUsers()
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/users",
                        "create", "/api/v1/users"
                ))));
    }

    @GetMapping("/{userId}")
    Mono<ResponseEntity<JsonNode>> getUser(@PathVariable String userId) {
        return userDomainClient.getUser(userId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/users/" + userId,
                        "update", "/api/v1/users/" + userId,
                        "delete", "/api/v1/users/" + userId
                ))));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createUser(@RequestBody JsonNode request) {
        return userDomainClient.createUser(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(hypermediaSupport.addLinks(body, Map.of(
                        "collection", "/api/v1/users"
                ))));
    }

    @PutMapping("/{userId}")
    Mono<ResponseEntity<JsonNode>> updateUser(@PathVariable String userId, @RequestBody JsonNode request) {
        return userDomainClient.updateUser(userId, request)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/users/" + userId,
                        "collection", "/api/v1/users"
                ))));
    }

    @DeleteMapping("/{userId}")
    Mono<ResponseEntity<Void>> deleteUser(@PathVariable String userId) {
        return userDomainClient.deleteUser(userId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
