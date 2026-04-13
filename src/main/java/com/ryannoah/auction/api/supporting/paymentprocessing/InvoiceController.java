package com.ryannoah.auction.api.supporting.paymentprocessing;

import com.ryannoah.auction.application.supporting.paymentprocessing.InvoiceApplicationService;
import com.ryannoah.auction.domain.supporting.paymentprocessing.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceApplicationService invoiceApplicationService;

    public InvoiceController(InvoiceApplicationService invoiceApplicationService) {
        this.invoiceApplicationService = invoiceApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return toResponse(invoiceApplicationService.createInvoice(
                new InvoiceApplicationService.CreateInvoiceCommand(
                        request.auctionId(),
                        request.buyerId(),
                        request.dueDate(),
                        request.method()
                )
        ));
    }

    @PutMapping("/{invoiceId}")
    public EntityModel<InvoiceResponse> updateInvoice(@PathVariable String invoiceId, @Valid @RequestBody UpdateInvoiceRequest request) {
        return toResponse(invoiceApplicationService.updateInvoice(
                invoiceId,
                new InvoiceApplicationService.UpdateInvoiceCommand(request.dueDate(), request.method())
        ));
    }

    @DeleteMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable String invoiceId) {
        invoiceApplicationService.deleteInvoice(invoiceId);
    }

    @PostMapping("/{invoiceId}/pay")
    public EntityModel<InvoiceResponse> payInvoice(@PathVariable String invoiceId) {
        return toResponse(invoiceApplicationService.payInvoice(invoiceId));
    }

    @GetMapping("/{invoiceId}")
    public EntityModel<InvoiceResponse> getInvoice(@PathVariable String invoiceId) {
        return toResponse(invoiceApplicationService.getInvoice(invoiceId));
    }

    @GetMapping
    public CollectionModel<EntityModel<InvoiceResponse>> listInvoices() {
        return CollectionModel.of(
                invoiceApplicationService.listInvoices().stream().map(this::toResponse).toList(),
                linkTo(methodOn(InvoiceController.class).listInvoices()).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).createInvoice(null)).withRel("create")
        );
    }

    private EntityModel<InvoiceResponse> toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse(
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
        EntityModel<InvoiceResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(InvoiceController.class).getInvoice(response.invoiceId())).withSelfRel(),
                linkTo(methodOn(InvoiceController.class).createInvoice(null)).withRel("create"),
                linkTo(methodOn(InvoiceController.class).updateInvoice(response.invoiceId(), null)).withRel("update"),
                linkTo(InvoiceController.class).slash(response.invoiceId()).withRel("delete")
        );
        if (!"PAID".equals(response.status())) {
            model.add(linkTo(methodOn(InvoiceController.class).payInvoice(response.invoiceId())).withRel("pay"));
        }
        return model;
    }

    public record CreateInvoiceRequest(
            @NotBlank String auctionId,
            @NotBlank String buyerId,
            @NotNull @Future LocalDateTime dueDate,
            @NotBlank String method
    ) {
    }

    public record UpdateInvoiceRequest(
            @NotNull @Future LocalDateTime dueDate,
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
