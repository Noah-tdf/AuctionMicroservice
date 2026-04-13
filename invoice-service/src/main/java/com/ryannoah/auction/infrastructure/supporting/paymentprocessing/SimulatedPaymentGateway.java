package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import com.ryannoah.auction.domain.supporting.paymentprocessing.Invoice;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentGateway;
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
