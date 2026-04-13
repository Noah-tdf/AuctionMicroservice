package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.supporting.paymentprocessing.Invoice;
import com.ryannoah.auction.domain.supporting.paymentprocessing.InvoiceId;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentMethod;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(InvoiceRepositoryAdapter.class)
class InvoiceRepositoryAdapterTest {

    @Autowired
    private InvoiceRepositoryAdapter repositoryAdapter;

    @Test
    void shouldSaveAndLoadInvoice() {
        Invoice saved = repositoryAdapter.save(Invoice.create(
                new AuctionId("auction-900"),
                new UserId("buyer-1"),
                new UserId("seller-1"),
                LocalDateTime.now().plusDays(3),
                new Money(new BigDecimal("123.45"), "CAD"),
                PaymentMethod.CREDIT_CARD
        ));

        Invoice loaded = repositoryAdapter.findById(saved.getInvoiceId()).orElseThrow();
        assertThat(loaded.getInvoiceId().value()).isEqualTo(saved.getInvoiceId().value());
        assertThat(loaded.getFinalSaleAmount().amount()).isEqualByComparingTo("123.45");
    }

    @Test
    void shouldReturnEmptyWhenInvoiceDoesNotExist() {
        assertThat(repositoryAdapter.findById(new InvoiceId("missing-invoice"))).isEmpty();
    }
}
