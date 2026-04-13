package com.ryannoah.auction.api.shared;

import com.ryannoah.auction.domain.shared.DomainConflictException;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.shared.DomainValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceSharedComponentsTest {

    private final SystemController systemController = new SystemController();
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldExposeRootStatus() {
        assertThat(systemController.root()).containsEntry("service", "invoice-service");
    }

    @Test
    void shouldBuildValidationResponses() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/invoices");

        var domainValidation = exceptionHandler.handleDomainValidation(new DomainValidationException("bad data"), request);
        var notFound = exceptionHandler.handleNotFound(new DomainNotFoundException("missing"), request);
        var conflict = exceptionHandler.handleConflict(new DomainConflictException("paid"), request);
        var methodNotAllowed = exceptionHandler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PATCH"),
                request
        );
        var unexpected = exceptionHandler.handleUnexpected(new RuntimeException("boom"), request);

        assertThat(domainValidation.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(methodNotAllowed.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(unexpected.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
