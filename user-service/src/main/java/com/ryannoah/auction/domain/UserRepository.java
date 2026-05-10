package com.ryannoah.auction.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(UserId userId);
}
