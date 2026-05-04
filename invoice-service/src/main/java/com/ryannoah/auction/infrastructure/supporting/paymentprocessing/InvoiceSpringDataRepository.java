package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceSpringDataRepository extends MongoRepository<InvoiceJpaEntity, String> {
}
