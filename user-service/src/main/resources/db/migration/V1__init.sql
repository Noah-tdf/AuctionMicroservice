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
