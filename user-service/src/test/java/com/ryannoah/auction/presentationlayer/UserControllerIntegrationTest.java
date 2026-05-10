package com.ryannoah.auction.presentationlayer;

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
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededUsers() {
        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").value(length -> assertThat(((Number) length).intValue()).isGreaterThanOrEqualTo(2));
    }

    @Test
    void shouldCreateAndFetchUser() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "username": "controller-user",
                          "email": "controller-user@example.com",
                          "verified": true,
                          "address": {
                            "street": "10 Rue Test",
                            "city": "Montreal",
                            "zipCode": "H2H2H2",
                            "country": "Canada"
                          }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        JsonNode body = objectMapper.readTree(result.getResponseBody());
        String userId = body.get("userId").asText();
        assertThat(userId).isNotBlank();

        webTestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("controller-user@example.com");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        String payload = """
                {
                  "username": "duplicate-user",
                  "email": "seller.alpha@example.com",
                  "verified": true,
                  "address": {
                    "street": "12 Main",
                    "city": "Toronto",
                    "zipCode": "M1M1M1",
                    "country": "Canada"
                  }
                }
                """;

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email already exists: seller.alpha@example.com");
    }

    @Test
    void shouldUpdateAndDeleteUser() throws Exception {
        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "username": "update-user",
                          "email": "update-user@example.com",
                          "verified": false,
                          "address": {
                            "street": "15 Queen",
                            "city": "Ottawa",
                            "zipCode": "K1K1K1",
                            "country": "Canada"
                          }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        String userId = objectMapper.readTree(result.getResponseBody()).get("userId").asText();

        webTestClient.put()
                .uri("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "username": "update-user-final",
                          "email": "update-user-final@example.com",
                          "verified": true,
                          "address": {
                            "street": "16 Queen",
                            "city": "Ottawa",
                            "zipCode": "K1K1K1",
                            "country": "Canada"
                          }
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("update-user-final");

        webTestClient.delete()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
