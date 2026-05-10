package com.ryannoah.auction.businesslogiclayer;

import com.ryannoah.auction.utilities.DomainNotFoundException;
import com.ryannoah.auction.domain.Address;
import com.ryannoah.auction.domain.DuplicateEmailException;
import com.ryannoah.auction.domain.Email;
import com.ryannoah.auction.domain.User;
import com.ryannoah.auction.domain.UserId;
import com.ryannoah.auction.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserCommand command) {
        ensureEmailIsUnique(command.email(), null);
        User user = User.create(
                command.username(),
                new Email(command.email()),
                command.isVerified(),
                new Address(command.street(), command.city(), command.zipCode(), command.country())
        );
        return userRepository.save(user);
    }

    public User updateUser(String userId, UpdateUserCommand command) {
        User existing = getUser(userId);
        ensureEmailIsUnique(command.email(), existing.getUserId().value());
        User updated = new User(
                existing.getUserId(),
                command.username(),
                new Email(command.email()),
                existing.getRegistrationDate(),
                command.isVerified(),
                existing.getRating(),
                new Address(command.street(), command.city(), command.zipCode(), command.country())
        );
        return userRepository.save(updated);
    }

    public void deleteUser(String userId) {
        User existing = getUser(userId);
        userRepository.deleteById(existing.getUserId());
    }

    @Transactional(readOnly = true)
    public User getUser(String userId) {
        return userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new DomainNotFoundException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    private void ensureEmailIsUnique(String email, String currentUserId) {
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getUserId().value().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(email);
                });
    }

    public record CreateUserCommand(
            String username,
            String email,
            boolean isVerified,
            String street,
            String city,
            String zipCode,
            String country
    ) {
    }

    public record UpdateUserCommand(
            String username,
            String email,
            boolean isVerified,
            String street,
            String city,
            String zipCode,
            String country
    ) {
    }
}
