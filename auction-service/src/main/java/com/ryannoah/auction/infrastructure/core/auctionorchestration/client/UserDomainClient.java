package com.ryannoah.auction.infrastructure.core.auctionorchestration.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class UserDomainClient extends AbstractHttpDomainClient {

    private final String userServiceBaseUrl;

    public UserDomainClient(
            WebClient webClient,
            @Value("${services.user-service.base-url}") String userServiceBaseUrl
    ) {
        super(webClient);
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public UserResponse getUser(String userId) {
        return getObject(userServiceBaseUrl, "/api/v1/users/" + userId, UserResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserResponse(
            String userId,
            String username,
            String email,
            LocalDateTime registrationDate,
            @JsonAlias({"isVerified", "verified"}) boolean verified,
            BigDecimal rating,
            int totalReviews,
            AddressResponse address
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressResponse(
            String street,
            String city,
            String zipCode,
            String country
    ) {
    }
}
