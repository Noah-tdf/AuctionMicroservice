package com.ryannoah.auction.domain;

public interface PaymentGateway {

    PaymentResult charge(Invoice invoice);

    record PaymentResult(boolean successful, String reference) {
    }
}
