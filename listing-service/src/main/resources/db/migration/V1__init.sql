create table listings (
    listing_id varchar(64) primary key,
    seller_id varchar(64) not null,
    title varchar(255) not null,
    description text not null,
    category varchar(255) not null,
    listing_condition varchar(50) not null,
    published boolean not null
);
