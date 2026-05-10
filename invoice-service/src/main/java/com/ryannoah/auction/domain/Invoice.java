package com.ryannoah.auction.domain;

import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.utilities.DomainConflictException;
import com.ryannoah.auction.domain.UserId;

import java.time.LocalDateTime;

public class Invoice {

    private final InvoiceId invoiceId;
    private final AuctionId auctionId;
    private final UserId buyerId;
    private final UserId sellerId;
    private final LocalDateTime issueDate;
    private final LocalDateTime dueDate;
    private final Money finalSaleAmount;
    private PaymentStatus status;
    private final PaymentMethod method;

    public Invoice(
            InvoiceId invoiceId,
            AuctionId auctionId,
            UserId buyerId,
            UserId sellerId,
            LocalDateTime issueDate,
            LocalDateTime dueDate,
            Money finalSaleAmount,
            PaymentStatus status,
            PaymentMethod method
    ) {
        finalSaleAmount.ensurePositive("finalSaleAmount");
        this.invoiceId = invoiceId;
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.finalSaleAmount = finalSaleAmount;
        this.status = status;
        this.method = method;
    }

    public static Invoice create(
            AuctionId auctionId,
            UserId buyerId,
            UserId sellerId,
            LocalDateTime dueDate,
            Money finalSaleAmount,
            PaymentMethod method
    ) {
        return new Invoice(
                InvoiceId.newId(),
                auctionId,
                buyerId,
                sellerId,
                LocalDateTime.now(),
                dueDate,
                finalSaleAmount,
                PaymentStatus.PENDING,
                method
        );
    }

    public void markPaid(boolean transactionSuccessful) {
        if (!transactionSuccessful) {
            status = PaymentStatus.ERROR;
            throw new DomainConflictException("Invoice can only be marked PAID when the transaction succeeds");
        }
        status = PaymentStatus.PAID;
    }

    public InvoiceId getInvoiceId() {
        return invoiceId;
    }

    public AuctionId getAuctionId() {
        return auctionId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public UserId getSellerId() {
        return sellerId;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Money getFinalSaleAmount() {
        return finalSaleAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getMethod() {
        return method;
    }
}
