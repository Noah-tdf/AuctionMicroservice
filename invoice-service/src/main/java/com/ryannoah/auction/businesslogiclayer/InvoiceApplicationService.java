package com.ryannoah.auction.businesslogiclayer;

import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.utilities.DomainNotFoundException;
import com.ryannoah.auction.domain.InvoiceAlreadyPaidException;
import com.ryannoah.auction.domain.Invoice;
import com.ryannoah.auction.domain.InvoiceId;
import com.ryannoah.auction.domain.InvoiceRepository;
import com.ryannoah.auction.domain.PaymentGateway;
import com.ryannoah.auction.domain.PaymentMethod;
import com.ryannoah.auction.domain.PaymentStatus;
import com.ryannoah.auction.domain.UserId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceApplicationService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentGateway paymentGateway;

    public InvoiceApplicationService(
            InvoiceRepository invoiceRepository,
            PaymentGateway paymentGateway
    ) {
        this.invoiceRepository = invoiceRepository;
        this.paymentGateway = paymentGateway;
    }

    public Invoice createInvoice(CreateInvoiceCommand command) {
        Invoice invoice = Invoice.create(
                new AuctionId(command.auctionId()),
                new UserId(command.buyerId()),
                new UserId(command.sellerId()),
                command.dueDate(),
                new Money(command.finalSaleAmount(), command.currency()),
                PaymentMethod.valueOf(command.method())
        );
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(String invoiceId, UpdateInvoiceCommand command) {
        Invoice existing = getInvoice(invoiceId);
        if (existing.getStatus() == PaymentStatus.PAID) {
            throw new InvoiceAlreadyPaidException(invoiceId);
        }
        Invoice updated = new Invoice(
                existing.getInvoiceId(),
                existing.getAuctionId(),
                existing.getBuyerId(),
                existing.getSellerId(),
                existing.getIssueDate(),
                command.dueDate(),
                new Money(command.finalSaleAmount(), command.currency()),
                existing.getStatus(),
                PaymentMethod.valueOf(command.method())
        );
        return invoiceRepository.save(updated);
    }

    public void deleteInvoice(String invoiceId) {
        Invoice existing = getInvoice(invoiceId);
        if (existing.getStatus() == PaymentStatus.PAID) {
            throw new InvoiceAlreadyPaidException(invoiceId);
        }
        invoiceRepository.deleteById(existing.getInvoiceId());
    }

    public Invoice payInvoice(String invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new InvoiceAlreadyPaidException(invoiceId);
        }
        PaymentGateway.PaymentResult paymentResult = paymentGateway.charge(invoice);
        invoice.markPaid(paymentResult.successful());
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoice(String invoiceId) {
        return invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new DomainNotFoundException("Invoice not found: " + invoiceId));
    }

    public List<Invoice> listInvoices() {
        return invoiceRepository.findAll();
    }

    public record CreateInvoiceCommand(
            String auctionId,
            String buyerId,
            String sellerId,
            LocalDateTime dueDate,
            java.math.BigDecimal finalSaleAmount,
            String currency,
            String method
    ) {
    }

    public record UpdateInvoiceCommand(
            LocalDateTime dueDate,
            java.math.BigDecimal finalSaleAmount,
            String currency,
            String method
    ) {
    }
}
