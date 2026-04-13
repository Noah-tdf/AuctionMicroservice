package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionSpringDataRepository extends JpaRepository<AuctionJpaEntity, String> {
}
