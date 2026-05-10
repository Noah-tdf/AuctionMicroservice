package com.ryannoah.auction.domain;

import java.util.List;
import java.util.Optional;

public interface BidRepository {

    Bid save(Bid bid);

    List<Bid> findAll();

    List<Bid> findByAuctionId(AuctionId auctionId);

    Optional<Bid> findById(BidId bidId);

    void deleteById(BidId bidId);
}
