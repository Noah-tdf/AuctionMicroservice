package com.ryannoah.auction.domain;

import java.math.BigDecimal;

public record UserRating(BigDecimal rating, int totalReviews) {

    public static UserRating unrated() {
        return new UserRating(BigDecimal.ZERO, 0);
    }
}
