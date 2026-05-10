package com.ryannoah.auction.dataccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuctionSpringDataRepository extends MongoRepository<AuctionDocument, String> {
}
