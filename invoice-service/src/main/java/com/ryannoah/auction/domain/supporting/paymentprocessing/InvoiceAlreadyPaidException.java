package com.ryannoah.auction.domain.supporting.paymentprocessing;

import com.ryannoah.auction.domain.shared.DomainConflictException;

public class InvoiceAlreadyPaidException extends DomainConflictException {

    public InvoiceAlreadyPaidException(String invoiceId) {
        super("Invoice is already paid: " + invoiceId);
    }
}
