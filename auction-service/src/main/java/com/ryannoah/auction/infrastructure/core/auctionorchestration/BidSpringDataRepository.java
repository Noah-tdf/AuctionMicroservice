package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidSpringDataRepository extends JpaRepository<BidJpaEntity, String> {

    List<BidJpaEntity> findByAuctionIdOrderByBidTimeAsc(String auctionId);
}
