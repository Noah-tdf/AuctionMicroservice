package com.ryannoah.auction.domain;

import com.ryannoah.auction.utilities.DomainConflictException;

public class InvoiceAlreadyPaidException extends DomainConflictException {

    public InvoiceAlreadyPaidException(String invoiceId) {
        super("Invoice is already paid: " + invoiceId);
    }
}
