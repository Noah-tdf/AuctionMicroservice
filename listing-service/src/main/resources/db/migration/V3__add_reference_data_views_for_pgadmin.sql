create table users (
    user_id varchar(64) primary key,
    username varchar(255) not null,
    email varchar(255) not null unique,
    registration_date timestamp not null,
    verified boolean not null,
    rating numeric(10, 2) not null,
    total_reviews integer not null,
    street varchar(255) not null,
    city varchar(255) not null,
    zip_code varchar(50) not null,
    country varchar(100) not null
);

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

create table invoices (
    invoice_id varchar(64) primary key,
    auction_id varchar(64) not null,
    buyer_id varchar(64) not null,
    seller_id varchar(64) not null,
    issue_date timestamp not null,
    due_date timestamp not null,
    final_sale_amount numeric(19, 2) not null,
    currency varchar(10) not null,
    status varchar(30) not null,
    method varchar(30) not null
);

insert into users (user_id, username, email, registration_date, verified, rating, total_reviews, street, city, zip_code, country) values
('user-001', 'seller-alpha', 'seller.alpha@example.com', '2026-03-01 09:00:00', true, 4.80, 24, '101 King St W', 'Toronto', 'M5H1J9', 'Canada'),
('user-002', 'buyer-beta', 'buyer.beta@example.com', '2026-03-01 10:00:00', true, 4.55, 15, '505 Bloor St W', 'Toronto', 'M5S1X9', 'Canada');

insert into auctions (auction_id, listing_id, seller_id, start_time, end_time, starting_price_amount, currency, current_price_amount, status) values
('auction-001', 'listing-001', 'user-001', '2026-04-03 09:00:00', '2026-04-10 21:00:00', 350.00, 'CAD', 410.00, 'ACTIVE'),
('auction-002', 'listing-002', 'user-002', '2026-04-03 10:00:00', '2026-04-11 21:00:00', 900.00, 'CAD', 900.00, 'SCHEDULED');

insert into bids (bid_id, auction_id, bidder_id, bid_amount, currency, bid_time) values
('bid-001', 'auction-001', 'user-004', 370.00, 'CAD', '2026-04-03 10:00:00'),
('bid-002', 'auction-001', 'user-005', 410.00, 'CAD', '2026-04-03 11:15:00');

insert into invoices (invoice_id, auction_id, buyer_id, seller_id, issue_date, due_date, final_sale_amount, currency, status, method) values
('invoice-001', 'auction-001', 'user-005', 'user-001', '2026-03-12 09:00:00', '2026-04-25 09:00:00', 410.00, 'CAD', 'PENDING', 'CREDIT_CARD'),
('invoice-002', 'auction-002', 'user-007', 'user-002', '2026-03-12 09:15:00', '2026-04-25 09:15:00', 975.00, 'CAD', 'PAID', 'PAYPAL');
