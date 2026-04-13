package com.ryannoah.auction.infrastructure.supporting.listingmanagement;

import com.ryannoah.auction.domain.supporting.listingmanagement.Condition;
import com.ryannoah.auction.domain.supporting.listingmanagement.Listing;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingId;
import com.ryannoah.auction.domain.supporting.listingmanagement.ListingRepository;
import com.ryannoah.auction.domain.supporting.usermanagement.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListingRepositoryAdapter implements ListingRepository {

    private final ListingSpringDataRepository repository;

    public ListingRepositoryAdapter(ListingSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Listing save(Listing listing) {
        return toDomain(repository.save(toEntity(listing)));
    }

    @Override
    public Optional<Listing> findById(ListingId listingId) {
        return repository.findById(listingId.value()).map(this::toDomain);
    }

    @Override
    public List<Listing> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(ListingId listingId) {
        repository.deleteById(listingId.value());
    }

    private ListingJpaEntity toEntity(Listing listing) {
        ListingJpaEntity entity = new ListingJpaEntity();
        entity.setListingId(listing.getListingId().value());
        entity.setSellerId(listing.getSellerId().value());
        entity.setTitle(listing.getTitle());
        entity.setDescription(listing.getDescription());
        entity.setCategory(listing.getCategory());
        entity.setListingCondition(listing.getCondition().name());
        entity.setPublished(listing.isPublished());
        return entity;
    }

    private Listing toDomain(ListingJpaEntity entity) {
        return new Listing(
                new ListingId(entity.getListingId()),
                new UserId(entity.getSellerId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCategory(),
                Condition.valueOf(entity.getListingCondition()),
                entity.isPublished()
        );
    }
}
