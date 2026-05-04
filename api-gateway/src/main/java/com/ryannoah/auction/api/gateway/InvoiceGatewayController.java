package com.ryannoah.auction.api.gateway;

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
@RequestMapping("/api/v1/invoices")
public class InvoiceGatewayController {

    private final InvoiceDomainClient invoiceDomainClient;
    private final HypermediaSupport hypermediaSupport;

    public InvoiceGatewayController(
            InvoiceDomainClient invoiceDomainClient,
            HypermediaSupport hypermediaSupport
    ) {
        this.invoiceDomainClient = invoiceDomainClient;
        this.hypermediaSupport = hypermediaSupport;
    }

    @GetMapping
    Mono<ResponseEntity<JsonNode>> listInvoices() {
        return invoiceDomainClient.listInvoices()
                .map(items -> ResponseEntity.ok(hypermediaSupport.wrapCollection(items, Map.of(
                        "self", "/api/v1/invoices",
                        "create", "/api/v1/invoices"
                ))));
    }

    @GetMapping("/{invoiceId}")
    Mono<ResponseEntity<JsonNode>> getInvoice(@PathVariable String invoiceId) {
        return invoiceDomainClient.getInvoice(invoiceId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/invoices/" + invoiceId,
                        "update", "/api/v1/invoices/" + invoiceId,
                        "delete", "/api/v1/invoices/" + invoiceId,
                        "pay", "/api/v1/invoices/" + invoiceId + "/pay"
                ))));
    }

    @PostMapping
    Mono<ResponseEntity<JsonNode>> createInvoice(@RequestBody JsonNode request) {
        return invoiceDomainClient.createInvoice(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(hypermediaSupport.addLinks(body, Map.of(
                        "collection", "/api/v1/invoices"
                ))));
    }

    @PutMapping("/{invoiceId}")
    Mono<ResponseEntity<JsonNode>> updateInvoice(@PathVariable String invoiceId, @RequestBody JsonNode request) {
        return invoiceDomainClient.updateInvoice(invoiceId, request)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/invoices/" + invoiceId,
                        "pay", "/api/v1/invoices/" + invoiceId + "/pay"
                ))));
    }

    @PostMapping("/{invoiceId}/pay")
    Mono<ResponseEntity<JsonNode>> payInvoice(@PathVariable String invoiceId) {
        return invoiceDomainClient.payInvoice(invoiceId)
                .map(body -> ResponseEntity.ok(hypermediaSupport.addLinks(body, Map.of(
                        "self", "/api/v1/invoices/" + invoiceId,
                        "collection", "/api/v1/invoices"
                ))));
    }

    @DeleteMapping("/{invoiceId}")
    Mono<ResponseEntity<Void>> deleteInvoice(@PathVariable String invoiceId) {
        return invoiceDomainClient.deleteInvoice(invoiceId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
