package com.ryannoah.auction.domain.core.auctionorchestration;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {

    Auction save(Auction auction);

    Optional<Auction> findById(AuctionId auctionId);

    List<Auction> findAll();

    void deleteById(AuctionId auctionId);
}
