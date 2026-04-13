package com.ryannoah.auction.domain.supporting.paymentprocessing;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(InvoiceId invoiceId);

    List<Invoice> findAll();

    void deleteById(InvoiceId invoiceId);
}
