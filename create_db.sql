CREATE CREATE DATABASE household_budget;

CREATE TABLE ARTICLES (
id BIGSERIAL NOT NULL PRIMARY KEY,
name VARCHAR(50) NOT NULL);

CREATE TABLE BALANCE (
id BIGSERIAL NOT NULL PRIMARY KEY,
create_date TIMESTAMP(3) NOT NULL,
debit NUMERIC(18,2) NOT NULL,
credit NUMERIC(18,2) NOT NULL,
amount NUMERIC(18,2) NOT NULL);

CREATE TABLE OPERATIONS (
id BIGSERIAL NOT NULL PRIMARY KEY,
article_id BIGINT REFERENCES articles (id),
debit NUMERIC(18,2) NOT NULL,
credit NUMERIC(18,2) NOT NULL,
create_date TIMESTAMP(3) NOT NULL,
balance_id BIGINT REFERENCES balance (id));