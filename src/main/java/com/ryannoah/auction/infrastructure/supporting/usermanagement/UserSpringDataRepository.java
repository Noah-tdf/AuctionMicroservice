package com.ryannoah.auction.infrastructure.supporting.usermanagement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, String> {
}
