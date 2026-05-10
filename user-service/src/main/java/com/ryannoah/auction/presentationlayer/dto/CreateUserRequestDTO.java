package com.ryannoah.auction.presentationlayer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequestDTO(
        @NotBlank String username,
        @Email @NotBlank String email,
        @JsonAlias({"isVerified", "verified"}) boolean verified,
        @Valid @NotNull AddressRequestDTO address
) {
}
