package com.ryannoah.auction.presentationlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryannoah.auction.dataccesslayer.InvoiceJpaEntity;
import com.ryannoah.auction.dataccesslayer.InvoiceSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class InvoiceControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceSpringDataRepository invoiceSpringDataRepository;

    private Map<String, InvoiceJpaEntity> invoices;

    @BeforeEach
    void setUp() {
        invoices = new LinkedHashMap<>();
        invoices.put("invoice-001", invoice("invoice-001", "auction-001", "user-005", "user-001", "PENDING", "CREDIT_CARD"));
        invoices.put("invoice-002", invoice("invoice-002", "auction-002", "user-007", "user-002", "PAID", "PAYPAL"));

        when(invoiceSpringDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(invoices.values()));
        when(invoiceSpringDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(invoices.get(invocation.getArgument(0)))
        );
        when(invoiceSpringDataRepository.save(any(InvoiceJpaEntity.class))).thenAnswer(invocation -> {
            InvoiceJpaEntity entity = invocation.getArgument(0);
            invoices.put(entity.getInvoiceId(), entity);
            return entity;
        });
        doAnswer(invocation -> {
            invoices.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(invoiceSpringDataRepository).deleteById(any(String.class));
    }

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
                          "dueDate": "2026-06-01T10:15:30",
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
                          "dueDate": "2026-06-03T10:15:30",
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
                          "dueDate": "2026-06-04T10:15:30",
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

    private InvoiceJpaEntity invoice(String invoiceId, String auctionId, String buyerId, String sellerId, String status, String method) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setInvoiceId(invoiceId);
        entity.setAuctionId(auctionId);
        entity.setBuyerId(buyerId);
        entity.setSellerId(sellerId);
        entity.setIssueDate(LocalDateTime.of(2026, 3, 12, 9, 0));
        entity.setDueDate(LocalDateTime.of(2026, 6, 25, 9, 0));
        entity.setFinalSaleAmount(new BigDecimal("410.00"));
        entity.setCurrency("CAD");
        entity.setStatus(status);
        entity.setMethod(method);
        return entity;
    }
}
