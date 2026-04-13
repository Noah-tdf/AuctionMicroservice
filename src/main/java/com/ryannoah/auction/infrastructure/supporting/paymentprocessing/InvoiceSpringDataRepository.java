package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSpringDataRepository extends JpaRepository<InvoiceJpaEntity, String> {
}
