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
