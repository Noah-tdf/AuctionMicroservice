package com.ryannoah.auction.application.supporting.paymentprocessing;

import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionStatus;
import com.ryannoah.auction.domain.shared.DomainConflictException;
import com.ryannoah.auction.domain.shared.DomainNotFoundException;
import com.ryannoah.auction.domain.supporting.paymentprocessing.Invoice;
import com.ryannoah.auction.domain.supporting.paymentprocessing.InvoiceId;
import com.ryannoah.auction.domain.supporting.paymentprocessing.InvoiceRepository;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentGateway;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentMethod;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentStatus;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InvoiceApplicationService {

    private final InvoiceRepository invoiceRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;

    public InvoiceApplicationService(
            InvoiceRepository invoiceRepository,
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            PaymentGateway paymentGateway
    ) {
        this.invoiceRepository = invoiceRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.paymentGateway = paymentGateway;
    }

    public Invoice createInvoice(CreateInvoiceCommand command) {
        Auction auction = auctionRepository.findById(new AuctionId(command.auctionId()))
                .orElseThrow(() -> new DomainNotFoundException("Auction not found: " + command.auctionId()));
        if (auction.getStatus() != AuctionStatus.SOLD) {
            throw new DomainConflictException("Invoices can only be created for SOLD auctions");
        }

        UserId buyerId = new UserId(command.buyerId());
        userRepository.findById(buyerId)
                .orElseThrow(() -> new DomainNotFoundException("Buyer not found: " + command.buyerId()));
        userRepository.findById(auction.getSellerId())
                .orElseThrow(() -> new DomainNotFoundException("Seller not found: " + auction.getSellerId().value()));

        Invoice invoice = Invoice.create(
                auction.getAuctionId(),
                buyerId,
                auction.getSellerId(),
                command.dueDate(),
                auction.getCurrentPrice(),
                PaymentMethod.valueOf(command.method())
        );
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(String invoiceId, UpdateInvoiceCommand command) {
        Invoice existing = getInvoice(invoiceId);
        if (existing.getStatus() == PaymentStatus.PAID) {
            throw new DomainConflictException("Paid invoices cannot be updated");
        }
        Invoice updated = new Invoice(
                existing.getInvoiceId(),
                existing.getAuctionId(),
                existing.getBuyerId(),
                existing.getSellerId(),
                existing.getIssueDate(),
                command.dueDate(),
                existing.getFinalSaleAmount(),
                existing.getStatus(),
                PaymentMethod.valueOf(command.method())
        );
        return invoiceRepository.save(updated);
    }

    public void deleteInvoice(String invoiceId) {
        Invoice existing = getInvoice(invoiceId);
        if (existing.getStatus() == PaymentStatus.PAID) {
            throw new DomainConflictException("Paid invoices cannot be deleted");
        }
        invoiceRepository.deleteById(existing.getInvoiceId());
    }

    public Invoice payInvoice(String invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        if (invoice.getStatus() == PaymentStatus.PAID) {
            throw new DomainConflictException("Invoice is already PAID");
        }
        PaymentGateway.PaymentResult paymentResult = paymentGateway.charge(invoice);
        invoice.markPaid(paymentResult.successful());
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(String invoiceId) {
        return invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new DomainNotFoundException("Invoice not found: " + invoiceId));
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoices() {
        return invoiceRepository.findAll();
    }

    public record CreateInvoiceCommand(
            String auctionId,
            String buyerId,
            LocalDateTime dueDate,
            String method
    ) {
    }

    public record UpdateInvoiceCommand(
            LocalDateTime dueDate,
            String method
    ) {
    }
}
