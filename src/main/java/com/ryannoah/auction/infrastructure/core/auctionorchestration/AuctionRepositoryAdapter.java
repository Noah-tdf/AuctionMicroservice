package com.ryannoah.auction.infrastructure.core.auctionorchestration;

import com.ryannoah.auction.domain.core.auctionorchestration.Auction;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionId;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionRepository;
import com.ryannoah.auction.domain.core.auctionorchestration.AuctionStatus;
import com.ryannoah.auction.domain.core.auctionorchestration.Money;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuctionRepositoryAdapter implements AuctionRepository {

    private final AuctionSpringDataRepository repository;

    public AuctionRepositoryAdapter(AuctionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Auction save(Auction auction) {
        return toDomain(repository.save(toEntity(auction)));
    }

    @Override
    public Optional<Auction> findById(AuctionId auctionId) {
        return repository.findById(auctionId.value()).map(this::toDomain);
    }

    @Override
    public List<Auction> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(AuctionId auctionId) {
        repository.deleteById(auctionId.value());
    }

    private AuctionJpaEntity toEntity(Auction auction) {
        AuctionJpaEntity entity = new AuctionJpaEntity();
        entity.setAuctionId(auction.getAuctionId().value());
        entity.setListingId(auction.getListingId().value());
        entity.setSellerId(auction.getSellerId().value());
        entity.setStartTime(auction.getStartTime());
        entity.setEndTime(auction.getEndTime());
        entity.setStartingPriceAmount(auction.getStartingPrice().amount());
        entity.setCurrency(auction.getStartingPrice().currency());
        entity.setCurrentPriceAmount(auction.getCurrentPrice().amount());
        entity.setStatus(auction.getStatus().name());
        return entity;
    }

    private Auction toDomain(AuctionJpaEntity entity) {
        return new Auction(
                new AuctionId(entity.getAuctionId()),
                new ListingId(entity.getListingId()),
                new UserId(entity.getSellerId()),
                entity.getStartTime(),
                entity.getEndTime(),
                new Money(entity.getStartingPriceAmount(), entity.getCurrency()),
                new Money(entity.getCurrentPriceAmount(), entity.getCurrency()),
                AuctionStatus.valueOf(entity.getStatus())
        );
    }
}
