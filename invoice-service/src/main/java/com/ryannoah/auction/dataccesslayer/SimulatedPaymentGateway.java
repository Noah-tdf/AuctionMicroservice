package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.Invoice;
import com.ryannoah.auction.domain.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(Invoice invoice) {
        boolean success = invoice.getFinalSaleAmount().amount().signum() > 0;
        return new PaymentResult(success, UUID.randomUUID().toString());
    }
}
