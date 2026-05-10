package com.ryannoah.auction.domainclientlayer;

import com.ryannoah.auction.utilities.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

public abstract class AbstractDomainClient {

    private final WebClient webClient;

    protected AbstractDomainClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected Mono<JsonNode> fetchObject(String baseUrl, String path) {
        return exchangeForObject(baseUrl, HttpMethod.GET, path, null);
    }

    protected Mono<ArrayNode> fetchCollection(String baseUrl, String path) {
        return webClient.get()
                .uri(baseUrl + path)
                .exchangeToMono(response -> handleArrayResponse(response, path))
                .onErrorMap(WebClientRequestException.class, exception -> serviceUnavailable(baseUrl, path, exception));
    }

    protected Mono<JsonNode> exchangeForObject(String baseUrl, HttpMethod method, String path, JsonNode requestBody) {
        WebClient.RequestBodySpec spec = webClient.method(method).uri(baseUrl + path);
        WebClient.RequestHeadersSpec<?> headersSpec = requestBody == null ? spec : spec.bodyValue(requestBody);
        return headersSpec
                .exchangeToMono(response -> handleObjectResponse(response, path))
                .onErrorMap(WebClientRequestException.class, exception -> serviceUnavailable(baseUrl, path, exception));
    }

    protected Mono<Void> delete(String baseUrl, String path) {
        return webClient.delete()
                .uri(baseUrl + path)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.<Void>empty();
                    }
                    return handleErrorResponse(response, path);
                })
                .onErrorMap(WebClientRequestException.class, exception -> serviceUnavailable(baseUrl, path, exception));
    }

    private Mono<JsonNode> handleObjectResponse(ClientResponse response, String path) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(JsonNode.class);
        }
        return handleErrorResponse(response, path);
    }

    private Mono<ArrayNode> handleArrayResponse(ClientResponse response, String path) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(ArrayNode.class);
        }
        return handleErrorResponse(response, path);
    }

    private <T> Mono<T> handleErrorResponse(ClientResponse response, String path) {
        HttpStatus status = HttpStatus.valueOf(response.statusCode().value());
        return response.bodyToMono(String.class)
                .defaultIfEmpty(status.getReasonPhrase())
                .flatMap(body -> Mono.error(new DownstreamServiceException(status, extractMessage(body), path)));
    }

    private DownstreamServiceException serviceUnavailable(String baseUrl, String path, WebClientRequestException exception) {
        return new DownstreamServiceException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Downstream service unavailable for " + baseUrl + path + ": " + exception.getMessage(),
                path
        );
    }

    private String extractMessage(String body) {
        String trimmed = body == null ? "" : body.trim();
        if (trimmed.isEmpty()) {
            return "Downstream service returned an empty error response";
        }
        int messageIndex = trimmed.indexOf("\"message\"");
        if (messageIndex < 0) {
            return trimmed;
        }

        int colonIndex = trimmed.indexOf(':', messageIndex);
        int firstQuote = trimmed.indexOf('"', colonIndex + 1);
        int secondQuote = trimmed.indexOf('"', firstQuote + 1);
        if (colonIndex < 0 || firstQuote < 0 || secondQuote < 0) {
            return trimmed;
        }
        return trimmed.substring(firstQuote + 1, secondQuote);
    }
}
