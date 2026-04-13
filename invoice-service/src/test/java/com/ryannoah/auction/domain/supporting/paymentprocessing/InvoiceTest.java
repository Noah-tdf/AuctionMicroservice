package com.ryannoah.auction.domain.supporting.paymentprocessing;

import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.shared.DomainConflictException;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    @Test
    void shouldRejectFailedPayment() {
        Invoice invoice = Invoice.create(
                new AuctionId("auction-1"),
                new UserId("buyer-1"),
                new UserId("seller-1"),
                LocalDateTime.now().plusDays(1),
                new Money(new BigDecimal("12.00"), "CAD"),
                PaymentMethod.CREDIT_CARD
        );

        assertThatThrownBy(() -> invoice.markPaid(false))
                .isInstanceOf(DomainConflictException.class)
                .hasMessage("Invoice can only be marked PAID when the transaction succeeds");
    }
}
