package com.ryannoah.auction.domain;

import java.time.LocalDateTime;

public class User {

    private final UserId userId;
    private final String username;
    private final Email email;
    private final LocalDateTime registrationDate;
    private final boolean isVerified;
    private final UserRating rating;
    private final Address address;

    public User(
            UserId userId,
            String username,
            Email email,
            LocalDateTime registrationDate,
            boolean isVerified,
            UserRating rating,
            Address address
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.registrationDate = registrationDate;
        this.isVerified = isVerified;
        this.rating = rating;
        this.address = address;
    }

    public static User create(String username, Email email, boolean isVerified, Address address) {
        return new User(UserId.newId(), username, email, LocalDateTime.now(), isVerified, UserRating.unrated(), address);
    }

    public UserId getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public UserRating getRating() {
        return rating;
    }

    public Address getAddress() {
        return address;
    }
}
