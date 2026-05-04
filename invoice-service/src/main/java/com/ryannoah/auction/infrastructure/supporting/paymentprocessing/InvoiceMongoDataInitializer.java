package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile("!test")
public class InvoiceMongoDataInitializer {

    @Bean
    CommandLineRunner seedInvoices(InvoiceSpringDataRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(seedInvoice(
                    "invoice-001",
                    "auction-001",
                    "user-005",
                    "user-001",
                    LocalDateTime.of(2026, 3, 12, 9, 0),
                    LocalDateTime.of(2026, 6, 25, 9, 0),
                    new BigDecimal("410.00"),
                    "CAD",
                    "PENDING",
                    "CREDIT_CARD"
            ));
            repository.save(seedInvoice(
                    "invoice-002",
                    "auction-002",
                    "user-007",
                    "user-002",
                    LocalDateTime.of(2026, 3, 12, 9, 15),
                    LocalDateTime.of(2026, 6, 25, 9, 15),
                    new BigDecimal("975.00"),
                    "CAD",
                    "PAID",
                    "PAYPAL"
            ));
        };
    }

    private InvoiceJpaEntity seedInvoice(
            String invoiceId,
            String auctionId,
            String buyerId,
            String sellerId,
            LocalDateTime issueDate,
            LocalDateTime dueDate,
            BigDecimal finalSaleAmount,
            String currency,
            String status,
            String method
    ) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setInvoiceId(invoiceId);
        entity.setAuctionId(auctionId);
        entity.setBuyerId(buyerId);
        entity.setSellerId(sellerId);
        entity.setIssueDate(issueDate);
        entity.setDueDate(dueDate);
        entity.setFinalSaleAmount(finalSaleAmount);
        entity.setCurrency(currency);
        entity.setStatus(status);
        entity.setMethod(method);
        return entity;
    }
}
