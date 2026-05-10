package com.ryannoah.auction.utilities;

import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.utilities.DomainValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ListingSharedComponentsTest {

    private final SystemController systemController = new SystemController();
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldExposeRootStatus() {
        assertThat(systemController.root()).containsEntry("service", "listing-service");
    }

    @Test
    void shouldBuildConflictResponse() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/listings");

        var response = exceptionHandler.handleConflict(new DomainConflictException("duplicate"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("duplicate");
    }

    @Test
    void shouldBuildValidationResponse() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/listings");

        var response = exceptionHandler.handleDomainValidation(new DomainValidationException("bad data"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("bad data");
    }
}
