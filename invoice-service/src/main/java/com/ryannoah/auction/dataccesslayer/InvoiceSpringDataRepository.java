package com.ryannoah.auction.dataccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceSpringDataRepository extends MongoRepository<InvoiceJpaEntity, String> {
}
