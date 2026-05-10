package com.ryannoah.auction.domainclientlayer;

import com.ryannoah.auction.domainclientlayer.dto.CreateInvoiceClientRequestDTO;
import com.ryannoah.auction.domainclientlayer.dto.InvoiceClientResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    public InvoiceClientResponseDTO createInvoice(CreateInvoiceClientRequestDTO request) {
        return postObject(invoiceServiceBaseUrl, "/api/v1/invoices", request, InvoiceClientResponseDTO.class);
    }

    public InvoiceClientResponseDTO[] listInvoices() {
        return getObject(invoiceServiceBaseUrl, "/api/v1/invoices", InvoiceClientResponseDTO[].class);
    }
}
