package com.ryannoah.auction.domain.supporting.paymentprocessing;

public interface PaymentGateway {

    PaymentResult charge(Invoice invoice);

    record PaymentResult(boolean successful, String reference) {
    }
}
