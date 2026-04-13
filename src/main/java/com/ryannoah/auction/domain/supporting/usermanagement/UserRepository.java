package com.ryannoah.auction.domain.supporting.usermanagement;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId userId);

    List<User> findAll();

    void deleteById(UserId userId);
}
