package com.ryannoah.auction.api.supporting.listingmanagement;

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
class ListingControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededListings() {
        webTestClient.get()
                .uri("/api/v1/listings")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").value(length -> assertThat(((Number) length).intValue()).isGreaterThanOrEqualTo(2));
    }

    @Test
    void shouldCreateAndFetchListing() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-101",
                          "title": "Desk Lamp",
                          "description": "Adjustable lamp",
                          "category": "Home",
                          "condition": "GOOD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        JsonNode body = objectMapper.readTree(result.getResponseBody());
        String listingId = body.get("listingId").asText();
        assertThat(listingId).isNotBlank();

        webTestClient.get()
                .uri("/api/v1/listings/{id}", listingId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Desk Lamp");
    }

    @Test
    void shouldPublishListingAndRejectSecondPublish() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-202",
                          "title": "Router",
                          "description": "Wi-Fi 6 router",
                          "category": "Electronics",
                          "condition": "LIKE_NEW"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String listingId = objectMapper.readTree(result.getResponseBody()).get("listingId").asText();

        webTestClient.post()
                .uri("/api/v1/listings/{id}/publish", listingId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);

        webTestClient.post()
                .uri("/api/v1/listings/{id}/publish", listingId)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Listing is already published: " + listingId);
    }

    @Test
    void shouldUpdateAndDeleteListing() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-303",
                          "title": "Mechanical Keyboard",
                          "description": "Blue switches",
                          "category": "Computers",
                          "condition": "GOOD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String listingId = objectMapper.readTree(result.getResponseBody()).get("listingId").asText();

        webTestClient.put()
                .uri("/api/v1/listings/{id}", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-303",
                          "title": "Mechanical Keyboard Pro",
                          "description": "Blue switches and RGB",
                          "category": "Computers",
                          "condition": "LIKE_NEW"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Mechanical Keyboard Pro");

        webTestClient.delete()
                .uri("/api/v1/listings/{id}", listingId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/listings/{id}", listingId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldRejectInvalidListingPayload() {
        webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-404",
                          "title": "",
                          "description": "Bad payload",
                          "category": "Misc",
                          "condition": "GOOD"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldRejectInvalidListingCondition() {
        webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-405",
                          "title": "Bad Condition",
                          "description": "Bad enum",
                          "category": "Misc",
                          "condition": "BROKEN"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldReturnNotFoundForMissingListing() {
        webTestClient.get()
                .uri("/api/v1/listings/missing-listing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Listing not found: missing-listing");
    }

    @Test
    void shouldKeepPublishedListingPublishedWhenUpdated() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-500",
                          "title": "Published Item",
                          "description": "Publish me",
                          "category": "Office",
                          "condition": "GOOD"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String listingId = objectMapper.readTree(result.getResponseBody()).get("listingId").asText();

        webTestClient.post()
                .uri("/api/v1/listings/{id}/publish", listingId)
                .exchange()
                .expectStatus().isOk();

        webTestClient.put()
                .uri("/api/v1/listings/{id}", listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "sellerId": "user-500",
                          "title": "Published Item Updated",
                          "description": "Still published",
                          "category": "Office",
                          "condition": "LIKE_NEW"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.published").isEqualTo(true);
    }
}
