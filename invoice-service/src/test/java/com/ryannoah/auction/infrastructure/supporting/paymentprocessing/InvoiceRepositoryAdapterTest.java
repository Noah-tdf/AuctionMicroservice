package com.ryannoah.auction.infrastructure.supporting.paymentprocessing;

import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.supporting.paymentprocessing.Invoice;
import com.ryannoah.auction.domain.supporting.paymentprocessing.InvoiceId;
import com.ryannoah.auction.domain.supporting.paymentprocessing.PaymentMethod;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InvoiceRepositoryAdapterTest {

    @Mock
    private InvoiceSpringDataRepository springDataRepository;

    private InvoiceRepositoryAdapter repositoryAdapter;
    private Map<String, InvoiceJpaEntity> invoices;

    @BeforeEach
    void setUp() {
        invoices = new LinkedHashMap<>();
        repositoryAdapter = new InvoiceRepositoryAdapter(springDataRepository);
        when(springDataRepository.findAll()).thenAnswer(invocation -> List.copyOf(invoices.values()));
        when(springDataRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(invoices.get(invocation.getArgument(0)))
        );
        when(springDataRepository.save(any(InvoiceJpaEntity.class))).thenAnswer(invocation -> {
            InvoiceJpaEntity entity = invocation.getArgument(0);
            invoices.put(entity.getInvoiceId(), entity);
            return entity;
        });
        doAnswer(invocation -> {
            invoices.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(springDataRepository).deleteById(any(String.class));
    }

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
        assertThat(repositoryAdapter.findAll()).hasSize(1);

        repositoryAdapter.deleteById(saved.getInvoiceId());

        assertThat(repositoryAdapter.findById(saved.getInvoiceId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenInvoiceDoesNotExist() {
        assertThat(repositoryAdapter.findById(new InvoiceId("missing-invoice"))).isEmpty();
    }
}
