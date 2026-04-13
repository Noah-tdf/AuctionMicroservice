package com.ryannoah.auction.api.supporting.paymentprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class InvoiceControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededInvoices() {
        webTestClient.get()
                .uri("/api/v1/invoices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").value(length -> assertThat(((Number) length).intValue()).isGreaterThanOrEqualTo(2));
    }

    @Test
    void shouldCreateAndFetchInvoice() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "auctionId": "auction-901",
                          "buyerId": "buyer-901",
                          "sellerId": "seller-901",
                          "dueDate": "2026-05-01T10:15:30",
                          "finalSaleAmount": 345.67,
                          "currency": "CAD",
                          "method": "PAYPAL"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        JsonNode body = objectMapper.readTree(result.getResponseBody());
        String invoiceId = body.get("invoiceId").asText();
        assertThat(invoiceId).isNotBlank();

        webTestClient.get()
                .uri("/api/v1/invoices/{id}", invoiceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.buyerId").isEqualTo("buyer-901");
    }

    @Test
    void shouldPayPendingInvoice() {
        webTestClient.post()
                .uri("/api/v1/invoices/invoice-001/pay")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PAID");
    }

    @Test
    void shouldRejectChangesToPaidInvoice() {
        webTestClient.post()
                .uri("/api/v1/invoices/invoice-002/pay")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invoice is already paid: invoice-002");

        webTestClient.delete()
                .uri("/api/v1/invoices/invoice-002")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invoice is already paid: invoice-002");
    }

    @Test
    void shouldUpdateAndDeletePendingInvoice() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "auctionId": "auction-990",
                          "buyerId": "buyer-990",
                          "sellerId": "seller-990",
                          "dueDate": "2026-05-03T10:15:30",
                          "finalSaleAmount": 500.00,
                          "currency": "CAD",
                          "method": "CREDIT_CARD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String invoiceId = objectMapper.readTree(result.getResponseBody()).get("invoiceId").asText();

        webTestClient.put()
                .uri("/api/v1/invoices/{id}", invoiceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "dueDate": "2026-05-04T10:15:30",
                          "finalSaleAmount": 550.00,
                          "currency": "CAD",
                          "method": "PAYPAL"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.finalSaleAmount").isEqualTo(550.00);

        webTestClient.delete()
                .uri("/api/v1/invoices/{id}", invoiceId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/invoices/{id}", invoiceId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldRejectInvalidInvoicePayload() {
        webTestClient.post()
                .uri("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "auctionId": "auction-bad",
                          "buyerId": "buyer-bad",
                          "sellerId": "seller-bad",
                          "dueDate": "2025-01-01T10:15:30",
                          "finalSaleAmount": 10.00,
                          "currency": "CAD",
                          "method": "PAYPAL"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldReturnNotFoundForMissingInvoice() {
        webTestClient.get()
                .uri("/api/v1/invoices/missing-invoice")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invoice not found: missing-invoice");
    }

    @Test
    void shouldRejectInvalidPaymentMethod() {
        webTestClient.post()
                .uri("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "auctionId": "auction-bad-method",
                          "buyerId": "buyer-bad-method",
                          "sellerId": "seller-bad-method",
                          "dueDate": "2026-05-05T10:15:30",
                          "finalSaleAmount": 10.00,
                          "currency": "CAD",
                          "method": "WIRE"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }
}
