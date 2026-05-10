package com.ryannoah.auction.dataccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingSpringDataRepository extends JpaRepository<ListingJpaEntity, String> {
}
