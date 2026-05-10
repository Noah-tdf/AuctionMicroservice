package com.ryannoah.auction.dataccesslayer;

import com.ryannoah.auction.domain.AuctionId;
import com.ryannoah.auction.domain.Bid;
import com.ryannoah.auction.domain.BidId;
import com.ryannoah.auction.domain.BidRepository;
import com.ryannoah.auction.domain.Money;
import com.ryannoah.auction.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BidRepositoryAdapter implements BidRepository {

    private final BidSpringDataRepository repository;

    public BidRepositoryAdapter(BidSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Bid save(Bid bid) {
        return toDomain(repository.save(toDocument(bid)));
    }

    @Override
    public List<Bid> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Bid> findByAuctionId(AuctionId auctionId) {
        return repository.findByAuctionIdOrderByBidTimeAsc(auctionId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Bid> findById(BidId bidId) {
        return repository.findById(bidId.value()).map(this::toDomain);
    }

    @Override
    public void deleteById(BidId bidId) {
        repository.deleteById(bidId.value());
    }

    private BidDocument toDocument(Bid bid) {
        BidDocument entity = new BidDocument();
        entity.setBidId(bid.getBidId().value());
        entity.setAuctionId(bid.getAuctionId().value());
        entity.setBidderId(bid.getBidderId().value());
        entity.setBidAmount(bid.getBidAmount().amount());
        entity.setCurrency(bid.getBidAmount().currency());
        entity.setBidTime(bid.getBidTime());
        return entity;
    }

    private Bid toDomain(BidDocument entity) {
        return new Bid(
                new BidId(entity.getBidId()),
                new AuctionId(entity.getAuctionId()),
                new UserId(entity.getBidderId()),
                new Money(entity.getBidAmount(), entity.getCurrency()),
                entity.getBidTime()
        );
    }
}
