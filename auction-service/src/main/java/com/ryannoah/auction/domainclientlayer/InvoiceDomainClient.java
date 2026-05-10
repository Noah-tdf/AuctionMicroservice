package com.ryannoah.auction.domainclientlayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class InvoiceDomainClient extends AbstractHttpDomainClient {

    private final String invoiceServiceBaseUrl;

    public InvoiceDomainClient(
            WebClient webClient,
            @Value("${services.invoice-service.base-url}") String invoiceServiceBaseUrl
    ) {
        super(webClient);
        this.invoiceServiceBaseUrl = invoiceServiceBaseUrl;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        return postObject(invoiceServiceBaseUrl, "/api/v1/invoices", request, InvoiceResponse.class);
    }

    public InvoiceResponse[] listInvoices() {
        return getObject(invoiceServiceBaseUrl, "/api/v1/invoices", InvoiceResponse[].class);
    }

    public record CreateInvoiceRequest(
            String auctionId,
            String buyerId,
            String sellerId,
            LocalDateTime dueDate,
            BigDecimal finalSaleAmount,
            String currency,
            String method
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvoiceResponse(
            String invoiceId,
            String auctionId,
            String buyerId,
            String sellerId,
            LocalDateTime issueDate,
            LocalDateTime dueDate,
            BigDecimal finalSaleAmount,
            String currency,
            String status,
            String method
    ) {
    }
}
