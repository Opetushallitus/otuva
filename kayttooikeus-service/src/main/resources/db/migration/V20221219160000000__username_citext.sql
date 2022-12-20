CREATE EXTENSION IF NOT EXISTS citext;

ALTER TABLE kayttajatiedot ALTER COLUMN username TYPE citext;
