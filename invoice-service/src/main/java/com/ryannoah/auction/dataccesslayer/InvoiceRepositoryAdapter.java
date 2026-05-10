package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.domain.Invoice;
import com.ryannoah.auction.domain.InvoiceId;
import com.ryannoah.auction.domain.InvoiceRepository;
import com.ryannoah.auction.domain.PaymentMethod;
import com.ryannoah.auction.domain.PaymentStatus;
import com.ryannoah.auction.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceSpringDataRepository repository;

    public InvoiceRepositoryAdapter(InvoiceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return toDomain(repository.save(toEntity(invoice)));
    }

    @Override
    public Optional<Invoice> findById(InvoiceId invoiceId) {
        return repository.findById(invoiceId.value()).map(this::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(InvoiceId invoiceId) {
        repository.deleteById(invoiceId.value());
    }

    private InvoiceJpaEntity toEntity(Invoice invoice) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setInvoiceId(invoice.getInvoiceId().value());
        entity.setAuctionId(invoice.getAuctionId().value());
        entity.setBuyerId(invoice.getBuyerId().value());
        entity.setSellerId(invoice.getSellerId().value());
        entity.setIssueDate(invoice.getIssueDate());
        entity.setDueDate(invoice.getDueDate());
        entity.setFinalSaleAmount(invoice.getFinalSaleAmount().amount());
        entity.setCurrency(invoice.getFinalSaleAmount().currency());
        entity.setStatus(invoice.getStatus().name());
        entity.setMethod(invoice.getMethod().name());
        return entity;
    }

    private Invoice toDomain(InvoiceJpaEntity entity) {
        return new Invoice(
                new InvoiceId(entity.getInvoiceId()),
                new AuctionId(entity.getAuctionId()),
                new UserId(entity.getBuyerId()),
                new UserId(entity.getSellerId()),
                entity.getIssueDate(),
                entity.getDueDate(),
                new Money(entity.getFinalSaleAmount(), entity.getCurrency()),
                PaymentStatus.valueOf(entity.getStatus()),
                PaymentMethod.valueOf(entity.getMethod())
        );
    }
}
