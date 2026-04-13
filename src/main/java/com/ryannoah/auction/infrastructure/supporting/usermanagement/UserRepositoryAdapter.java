package com.ryannoah.auction.infrastructure.supporting.usermanagement;

import com.ryannoah.auction.domain.supporting.usermanagement.Address;
import com.ryannoah.auction.domain.supporting.usermanagement.Email;
import com.ryannoah.auction.domain.supporting.usermanagement.User;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserRating;
import com.ryannoah.auction.domain.supporting.usermanagement.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserSpringDataRepository repository;

    public UserRepositoryAdapter(UserSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return repository.findById(userId.value()).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UserId userId) {
        repository.deleteById(userId.value());
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUserId(user.getUserId().value());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail().address());
        entity.setRegistrationDate(user.getRegistrationDate());
        entity.setVerified(user.isVerified());
        entity.setRating(user.getRating().rating());
        entity.setTotalReviews(user.getRating().totalReviews());
        entity.setStreet(user.getAddress().street());
        entity.setCity(user.getAddress().city());
        entity.setZipCode(user.getAddress().zipCode());
        entity.setCountry(user.getAddress().country());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                new UserId(entity.getUserId()),
                entity.getUsername(),
                new Email(entity.getEmail()),
                entity.getRegistrationDate(),
                entity.isVerified(),
                new UserRating(entity.getRating(), entity.getTotalReviews()),
                new Address(entity.getStreet(), entity.getCity(), entity.getZipCode(), entity.getCountry())
        );
    }
}
