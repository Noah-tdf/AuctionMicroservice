insert into users (user_id, username, email, registration_date, verified, rating, total_reviews, street, city, zip_code, country) values
('user-001', 'noahseller1', 'noahseller1@example.com', '2026-03-01 09:00:00', true, 4.80, 24, '101 King St W', 'Toronto', 'M5H1J9', 'Canada'),
('user-002', 'noahseller2', 'noahseller2@example.com', '2026-03-01 09:15:00', true, 4.65, 18, '202 Queen St W', 'Toronto', 'M5V2A1', 'Canada'),
('user-003', 'noahseller3', 'noahseller3@example.com', '2026-03-01 09:30:00', false, 4.10, 7, '303 Front St W', 'Toronto', 'M5V2T6', 'Canada'),
('user-004', 'noahbuyer1', 'noahbuyer1@example.com', '2026-03-01 09:45:00', true, 4.92, 31, '404 Yonge St', 'Toronto', 'M5B1T1', 'Canada'),
('user-005', 'noahbuyer2', 'noahbuyer2@example.com', '2026-03-01 10:00:00', true, 4.55, 15, '505 Bloor St W', 'Toronto', 'M5S1X9', 'Canada'),
('user-006', 'noahbuyer3', 'noahbuyer3@example.com', '2026-03-01 10:15:00', false, 3.95, 5, '606 College St', 'Toronto', 'M6G1B4', 'Canada'),
('user-007', 'auctionfan1', 'auctionfan1@example.com', '2026-03-01 10:30:00', true, 4.73, 21, '707 Dundas St W', 'Toronto', 'M5T2W6', 'Canada'),
('user-008', 'auctionfan2', 'auctionfan2@example.com', '2026-03-01 10:45:00', true, 4.44, 12, '808 Spadina Ave', 'Toronto', 'M5S2J2', 'Canada'),
('user-009', 'auctionfan3', 'auctionfan3@example.com', '2026-03-01 11:00:00', true, 4.88, 27, '909 Bay St', 'Toronto', 'M5S3G5', 'Canada'),
('user-010', 'auctionfan4', 'auctionfan4@example.com', '2026-03-01 11:15:00', false, 4.02, 6, '1000 Danforth Ave', 'Toronto', 'M4J1L2', 'Canada');

insert into listings (listing_id, seller_id, title, description, category, listing_condition, published) values
('listing-001', 'user-001', 'PlayStation 5 Console', 'Lightly used PS5 with one controller and HDMI cable.', 'Electronics', 'GOOD', true),
('listing-002', 'user-002', 'MacBook Air M2', '13-inch MacBook Air with 16GB RAM and 512GB SSD.', 'Computers', 'LIKE_NEW', true),
('listing-003', 'user-003', 'Mountain Bike', 'Trail-ready bike with upgraded tires and brakes.', 'Sports', 'GOOD', true),
('listing-004', 'user-001', 'Canon EOS Camera', 'Mirrorless camera body with battery and charger.', 'Photography', 'LIKE_NEW', true),
('listing-005', 'user-002', 'Nintendo Switch OLED', 'Switch OLED model with dock and carrying case.', 'Gaming', 'GOOD', true),
('listing-006', 'user-003', 'Coffee Espresso Machine', 'Compact espresso machine with steam wand.', 'Home', 'FAIR', true),
('listing-007', 'user-001', 'iPad Pro 11', '11-inch iPad Pro with Apple Pencil.', 'Tablets', 'GOOD', true),
('listing-008', 'user-002', 'Gaming Chair', 'Ergonomic chair with adjustable armrests.', 'Furniture', 'LIKE_NEW', true),
('listing-009', 'user-003', 'Acoustic Guitar', 'Full-size acoustic guitar with gig bag.', 'Music', 'GOOD', true),
('listing-010', 'user-001', 'Samsung 4K Monitor', '27-inch 4K monitor with USB-C connectivity.', 'Electronics', 'NEW', true);

insert into auctions (auction_id, listing_id, seller_id, start_time, end_time, starting_price_amount, currency, current_price_amount, status) values
('auction-001', 'listing-001', 'user-001', '2026-03-10 09:00:00', '2026-03-20 21:00:00', 350.00, 'CAD', 410.00, 'ACTIVE'),
('auction-002', 'listing-002', 'user-002', '2026-03-10 09:30:00', '2026-03-21 21:00:00', 900.00, 'CAD', 975.00, 'ACTIVE'),
('auction-003', 'listing-003', 'user-003', '2026-03-10 10:00:00', '2026-03-22 21:00:00', 250.00, 'CAD', 250.00, 'SCHEDULED'),
('auction-004', 'listing-004', 'user-001', '2026-03-08 11:00:00', '2026-03-14 21:00:00', 700.00, 'CAD', 820.00, 'SOLD'),
('auction-005', 'listing-005', 'user-002', '2026-03-08 11:30:00', '2026-03-14 22:00:00', 280.00, 'CAD', 315.00, 'SOLD'),
('auction-006', 'listing-006', 'user-003', '2026-03-09 12:00:00', '2026-03-15 20:00:00', 120.00, 'CAD', 120.00, 'CLOSED'),
('auction-007', 'listing-007', 'user-001', '2026-03-11 12:30:00', '2026-03-23 20:00:00', 600.00, 'CAD', 640.00, 'ACTIVE'),
('auction-008', 'listing-008', 'user-002', '2026-03-12 13:00:00', '2026-03-24 20:00:00', 180.00, 'CAD', 180.00, 'SCHEDULED'),
('auction-009', 'listing-009', 'user-003', '2026-03-07 13:30:00', '2026-03-13 20:00:00', 150.00, 'CAD', 205.00, 'SOLD'),
('auction-010', 'listing-010', 'user-001', '2026-03-11 14:00:00', '2026-03-25 20:00:00', 300.00, 'CAD', 345.00, 'ACTIVE');

insert into bids (bid_id, auction_id, bidder_id, bid_amount, currency, bid_time) values
('bid-001', 'auction-001', 'user-004', 370.00, 'CAD', '2026-03-10 10:00:00'),
('bid-002', 'auction-001', 'user-005', 410.00, 'CAD', '2026-03-11 11:15:00'),
('bid-003', 'auction-002', 'user-006', 930.00, 'CAD', '2026-03-10 13:00:00'),
('bid-004', 'auction-002', 'user-007', 975.00, 'CAD', '2026-03-11 15:30:00'),
('bid-005', 'auction-004', 'user-008', 760.00, 'CAD', '2026-03-09 14:05:00'),
('bid-006', 'auction-004', 'user-009', 820.00, 'CAD', '2026-03-10 16:45:00'),
('bid-007', 'auction-005', 'user-010', 300.00, 'CAD', '2026-03-09 17:00:00'),
('bid-008', 'auction-005', 'user-004', 315.00, 'CAD', '2026-03-10 18:20:00'),
('bid-009', 'auction-007', 'user-005', 640.00, 'CAD', '2026-03-12 19:10:00'),
('bid-010', 'auction-010', 'user-007', 345.00, 'CAD', '2026-03-12 20:25:00');

insert into invoices (invoice_id, auction_id, buyer_id, seller_id, issue_date, due_date, final_sale_amount, currency, status, method) values
('invoice-001', 'auction-001', 'user-005', 'user-001', '2026-03-12 09:00:00', '2026-03-19 09:00:00', 410.00, 'CAD', 'PENDING', 'CREDIT_CARD'),
('invoice-002', 'auction-002', 'user-007', 'user-002', '2026-03-12 09:15:00', '2026-03-19 09:15:00', 975.00, 'CAD', 'PENDING', 'PAYPAL'),
('invoice-003', 'auction-003', 'user-004', 'user-003', '2026-03-12 09:30:00', '2026-03-20 09:30:00', 250.00, 'CAD', 'PENDING', 'DEBIT_CARD'),
('invoice-004', 'auction-004', 'user-009', 'user-001', '2026-03-10 10:00:00', '2026-03-17 10:00:00', 820.00, 'CAD', 'PAID', 'APPLE_PAY'),
('invoice-005', 'auction-005', 'user-004', 'user-002', '2026-03-10 10:15:00', '2026-03-17 10:15:00', 315.00, 'CAD', 'PAID', 'GOOGLE_PAY'),
('invoice-006', 'auction-006', 'user-006', 'user-003', '2026-03-10 10:30:00', '2026-03-17 10:30:00', 120.00, 'CAD', 'ERROR', 'CREDIT_CARD'),
('invoice-007', 'auction-007', 'user-005', 'user-001', '2026-03-12 10:45:00', '2026-03-21 10:45:00', 640.00, 'CAD', 'PENDING', 'DEBIT_CARD'),
('invoice-008', 'auction-008', 'user-008', 'user-002', '2026-03-12 11:00:00', '2026-03-22 11:00:00', 180.00, 'CAD', 'PENDING', 'PAYPAL'),
('invoice-009', 'auction-009', 'user-010', 'user-003', '2026-03-10 11:15:00', '2026-03-17 11:15:00', 205.00, 'CAD', 'REFUNDED', 'APPLE_PAY'),
('invoice-010', 'auction-010', 'user-007', 'user-001', '2026-03-12 11:30:00', '2026-03-23 11:30:00', 345.00, 'CAD', 'PENDING', 'CREDIT_CARD');
