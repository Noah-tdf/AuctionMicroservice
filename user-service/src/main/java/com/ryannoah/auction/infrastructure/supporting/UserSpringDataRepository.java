package com.ryannoah.auction.infrastructure.supporting.usermanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
}
