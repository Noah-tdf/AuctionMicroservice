package com.ryannoah.auction.domainclientlayer;

import com.ryannoah.auction.domainclientlayer.dto.UserClientResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    public UserClientResponseDTO getUser(String userId) {
        return getObject(userServiceBaseUrl, "/api/v1/users/" + userId, UserClientResponseDTO.class);
    }
}
