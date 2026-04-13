package com.ryannoah.auction.infrastructure.supporting.listingmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingSpringDataRepository extends JpaRepository<ListingJpaEntity, String> {
}
