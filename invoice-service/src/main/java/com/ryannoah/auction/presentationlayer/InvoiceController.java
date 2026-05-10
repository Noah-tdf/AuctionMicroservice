package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.InvoiceApplicationService;
import com.ryannoah.auction.domain.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceApplicationService invoiceApplicationService;

    public InvoiceController(InvoiceApplicationService invoiceApplicationService) {
        this.invoiceApplicationService = invoiceApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return toResponse(invoiceApplicationService.createInvoice(
                new InvoiceApplicationService.CreateInvoiceCommand(
                        request.auctionId(),
                        request.buyerId(),
                        request.sellerId(),
                        request.dueDate(),
                        request.finalSaleAmount(),
                        request.currency(),
                        request.method()
                )
        ));
    }

    @PutMapping("/{invoiceId}")
    public InvoiceResponse updateInvoice(@PathVariable String invoiceId, @Valid @RequestBody UpdateInvoiceRequest request) {
        return toResponse(invoiceApplicationService.updateInvoice(
                invoiceId,
                new InvoiceApplicationService.UpdateInvoiceCommand(
                        request.dueDate(),
                        request.finalSaleAmount(),
                        request.currency(),
                        request.method()
                )
        ));
    }

    @DeleteMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable String invoiceId) {
        invoiceApplicationService.deleteInvoice(invoiceId);
    }

    @PostMapping("/{invoiceId}/pay")
    public InvoiceResponse payInvoice(@PathVariable String invoiceId) {
        return toResponse(invoiceApplicationService.payInvoice(invoiceId));
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse getInvoice(@PathVariable String invoiceId) {
        return toResponse(invoiceApplicationService.getInvoice(invoiceId));
    }

    @GetMapping
    public java.util.List<InvoiceResponse> listInvoices() {
        return invoiceApplicationService.listInvoices().stream().map(this::toResponse).toList();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getInvoiceId().value(),
                invoice.getAuctionId().value(),
                invoice.getBuyerId().value(),
                invoice.getSellerId().value(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getFinalSaleAmount().amount(),
                invoice.getFinalSaleAmount().currency(),
                invoice.getStatus().name(),
                invoice.getMethod().name()
        );
    }

    public record CreateInvoiceRequest(
            @NotBlank String auctionId,
            @NotBlank String buyerId,
            @NotBlank String sellerId,
            @NotNull @Future LocalDateTime dueDate,
            @NotNull BigDecimal finalSaleAmount,
            @NotBlank String currency,
            @NotBlank String method
    ) {
    }

    public record UpdateInvoiceRequest(
            @NotNull @Future LocalDateTime dueDate,
            @NotNull BigDecimal finalSaleAmount,
            @NotBlank String currency,
            @NotBlank String method
    ) {
    }

    public record InvoiceResponse(
            String invoiceId,
            String auctionId,
            String buyerId,
            String sellerId,
            LocalDateTime issueDate,
            LocalDateTime dueDate,
            BigDecimal finalSaleAmount,
            String currency,
            String status,
            String method
    ) {
    }
}
