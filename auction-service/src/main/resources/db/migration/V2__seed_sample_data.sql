insert into auctions (auction_id, listing_id, seller_id, start_time, end_time, starting_price_amount, currency, current_price_amount, status) values
('auction-001', 'listing-001', 'user-001', '2026-04-03 09:00:00', '2026-04-10 21:00:00', 350.00, 'CAD', 410.00, 'ACTIVE'),
('auction-002', 'listing-002', 'user-002', '2026-04-03 10:00:00', '2026-04-11 21:00:00', 900.00, 'CAD', 900.00, 'SCHEDULED');

insert into bids (bid_id, auction_id, bidder_id, bid_amount, currency, bid_time) values
('bid-001', 'auction-001', 'user-004', 370.00, 'CAD', '2026-04-03 10:00:00'),
('bid-002', 'auction-001', 'user-005', 410.00, 'CAD', '2026-04-03 11:15:00');
