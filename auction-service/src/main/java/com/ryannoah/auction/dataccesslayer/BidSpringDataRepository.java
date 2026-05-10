package com.ryannoah.auction.dataccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BidSpringDataRepository extends MongoRepository<BidDocument, String> {

    List<BidDocument> findByAuctionIdOrderByBidTimeAsc(String auctionId);
}
