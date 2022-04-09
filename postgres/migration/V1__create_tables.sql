CREATE SCHEMA IF NOT EXISTS simpleserver;

DROP TABLE IF EXISTS simpleserver.product_group CASCADE;
DROP TABLE IF EXISTS simpleserver.product_book CASCADE;
DROP TABLE IF EXISTS simpleserver.product_movie CASCADE;
DROP TABLE IF EXISTS simpleserver.session CASCADE;
DROP TABLE IF EXISTS simpleserver.ssuser CASCADE; -- NOTE: user is a reserved word in sql!

CREATE TABLE simpleserver.product_group
(
    id    TEXT PRIMARY KEY,
    name  TEXT
);

CREATE TABLE simpleserver.product_book
(
    id       TEXT PRIMARY KEY,
    pg_id    TEXT REFERENCES simpleserver.product_group (id) ON DELETE RESTRICT,
    title    TEXT,
    price    NUMERIC(6,2),
    author   TEXT,
    year     INT,
    country  TEXT,
    language TEXT
);

CREATE TABLE simpleserver.product_movie
(
    id       TEXT PRIMARY KEY,
    pg_id    TEXT REFERENCES simpleserver.product_group (id) ON DELETE RESTRICT,
    title    TEXT,
    price    NUMERIC(6,2),
    director TEXT,
    year     INT,
    country  TEXT,
    genre    TEXT
);

CREATE TABLE simpleserver.session
(
    token  TEXT PRIMARY KEY
);

CREATE TABLE simpleserver.ssuser
(
    id              TEXT PRIMARY KEY,
    email           TEXT,
    first_name      TEXT,
    last_name       TEXT,
    hashed_password TEXT
);
