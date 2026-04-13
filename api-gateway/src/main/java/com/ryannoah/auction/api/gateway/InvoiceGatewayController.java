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
@RequestMapping("/api/v1/invoices")
public class InvoiceGatewayController {

    private final WebClient webClient;
    private final HypermediaSupport hypermediaSupport;
    private final String invoiceServiceBaseUrl;

    public InvoiceGatewayController(
            WebClient webClient,
            HypermediaSupport hypermediaSupport,
            @Value("${services.invoice-service.base-url}") String invoiceServiceBaseUrl
    ) {
        this.webClient = webClient;
        this.hypermediaSupport = hypermediaSupport;
        this.invoiceServiceBaseUrl = invoiceServiceBaseUrl;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listInvoices() {
        return webClient.get()
                .uri(invoiceServiceBaseUrl + "/api/v1/invoices")
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/invoices",
                        "create", "/api/v1/invoices"
                ))));
    }

    @GetMapping("/{invoiceId}")
    Mono<ResponseEntity<JsonNode>> getInvoice(@PathVariable String invoiceId) {
        return forwardWithLinks(HttpMethod.GET, "/api/v1/invoices/" + invoiceId, null, Map.of(
                "self", "/api/v1/invoices/" + invoiceId,
                "update", "/api/v1/invoices/" + invoiceId,
                "delete", "/api/v1/invoices/" + invoiceId,
                "pay", "/api/v1/invoices/" + invoiceId + "/pay"
        ));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createInvoice(@RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/invoices", request, Map.of(
                "collection", "/api/v1/invoices"
        ), HttpStatus.CREATED);
    }

    @PutMapping("/{invoiceId}")
    Mono<ResponseEntity<JsonNode>> updateInvoice(@PathVariable String invoiceId, @RequestBody JsonNode request) {
        return forwardWithLinks(HttpMethod.PUT, "/api/v1/invoices/" + invoiceId, request, Map.of(
                "self", "/api/v1/invoices/" + invoiceId,
                "pay", "/api/v1/invoices/" + invoiceId + "/pay"
        ));
    }

    @PostMapping("/{invoiceId}/pay")
    Mono<ResponseEntity<JsonNode>> payInvoice(@PathVariable String invoiceId) {
        return forwardWithLinks(HttpMethod.POST, "/api/v1/invoices/" + invoiceId + "/pay", null, Map.of(
                "self", "/api/v1/invoices/" + invoiceId,
                "collection", "/api/v1/invoices"
        ));
    }

    @DeleteMapping("/{invoiceId}")
    Mono<ResponseEntity<Void>> deleteInvoice(@PathVariable String invoiceId) {
        return webClient.delete()
                .uri(invoiceServiceBaseUrl + "/api/v1/invoices/{id}", invoiceId)
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
        WebClient.RequestBodySpec spec = webClient.method(method).uri(invoiceServiceBaseUrl + path);
        WebClient.RequestHeadersSpec<?> headersSpec = request == null ? spec : spec.bodyValue(request);
        return headersSpec.retrieve()
                .bodyToMono(JsonNode.class)
                .map(body -> ResponseEntity.status(expectedStatus).body(hypermediaSupport.addLinks(body, links)));
    }
}
