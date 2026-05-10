package com.ryannoah.auction.domainclientlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InvoiceDomainClient extends AbstractDomainClient {

    private final String invoiceServiceBaseUrl;

    public InvoiceDomainClient(
            WebClient webClient,
            @Value("${services.invoice-service.base-url}") String invoiceServiceBaseUrl
    ) {
        super(webClient);
        this.invoiceServiceBaseUrl = invoiceServiceBaseUrl;
    }

    public Mono<ArrayNode> listInvoices() {
        return fetchCollection(invoiceServiceBaseUrl, "/api/v1/invoices");
    }

    public Mono<JsonNode> getInvoice(String invoiceId) {
        return fetchObject(invoiceServiceBaseUrl, "/api/v1/invoices/" + invoiceId);
    }

    public Mono<JsonNode> createInvoice(JsonNode request) {
        return exchangeForObject(invoiceServiceBaseUrl, HttpMethod.POST, "/api/v1/invoices", request);
    }

    public Mono<JsonNode> updateInvoice(String invoiceId, JsonNode request) {
        return exchangeForObject(invoiceServiceBaseUrl, HttpMethod.PUT, "/api/v1/invoices/" + invoiceId, request);
    }

    public Mono<JsonNode> payInvoice(String invoiceId) {
        return exchangeForObject(invoiceServiceBaseUrl, HttpMethod.POST, "/api/v1/invoices/" + invoiceId + "/pay", null);
    }

    public Mono<Void> deleteInvoice(String invoiceId) {
        return delete(invoiceServiceBaseUrl, "/api/v1/invoices/" + invoiceId);
    }
}
