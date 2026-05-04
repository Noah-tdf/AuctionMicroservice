package com.ryannoah.auction.infrastructure.core.auctionorchestration.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

public abstract class AbstractHttpDomainClient {

    private final WebClient webClient;

    protected AbstractHttpDomainClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> T getObject(String baseUrl, String path, Class<T> responseType) {
        return exchange(baseUrl, HttpMethod.GET, path, null, responseType);
    }

    protected <T> T postObject(String baseUrl, String path, Object body, Class<T> responseType) {
        return exchange(baseUrl, HttpMethod.POST, path, body, responseType);
    }

    protected <T> T exchange(String baseUrl, HttpMethod method, String path, Object body, Class<T> responseType) {
        WebClient.RequestBodySpec request = webClient.method(method).uri(baseUrl + path);
        WebClient.RequestHeadersSpec<?> headers = body == null ? request : request.bodyValue(body);
        return headers
                .exchangeToMono(response -> handleResponse(response, path, responseType))
                .onErrorMap(WebClientRequestException.class, exception -> serviceUnavailable(baseUrl, path, exception))
                .block();
    }

    private <T> Mono<T> handleResponse(ClientResponse response, String path, Class<T> responseType) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(responseType);
        }
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
