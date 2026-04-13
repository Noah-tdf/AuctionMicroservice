create table auctions (
    auction_id varchar(64) primary key,
    listing_id varchar(64) not null,
    seller_id varchar(64) not null,
    start_time timestamp not null,
    end_time timestamp not null,
    starting_price_amount numeric(19, 2) not null,
    currency varchar(10) not null,
    current_price_amount numeric(19, 2) not null,
    status varchar(30) not null
);

create table bids (
    bid_id varchar(64) primary key,
    auction_id varchar(64) not null,
    bidder_id varchar(64) not null,
    bid_amount numeric(19, 2) not null,
    currency varchar(10) not null,
    bid_time timestamp not null
);

create index idx_bids_auction_id on bids (auction_id);
