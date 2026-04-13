create table users (
    user_id varchar(64) primary key,
    username varchar(255) not null,
    email varchar(255) not null,
    registration_date datetime not null,
    verified boolean not null,
    rating numeric(10, 2) not null,
    total_reviews integer not null,
    street varchar(255) not null,
    city varchar(255) not null,
    zip_code varchar(50) not null,
    country varchar(100) not null
);

create table listings (
    listing_id varchar(64) primary key,
    seller_id varchar(64) not null,
    title varchar(255) not null,
    description text not null,
    category varchar(255) not null,
    listing_condition varchar(50) not null,
    published boolean not null
);

create table auctions (
    auction_id varchar(64) primary key,
    listing_id varchar(64) not null,
    seller_id varchar(64) not null,
    start_time datetime not null,
    end_time datetime not null,
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
    bid_time datetime not null
);

create index idx_bids_auction_id on bids (auction_id);

create table invoices (
    invoice_id varchar(64) primary key,
    auction_id varchar(64) not null,
    buyer_id varchar(64) not null,
    seller_id varchar(64) not null,
    issue_date datetime not null,
    due_date datetime not null,
    final_sale_amount numeric(19, 2) not null,
    currency varchar(10) not null,
    status varchar(30) not null,
    method varchar(30) not null
);
