UPDATE kayttajatiedot
SET username = CONCAT(username, CAST(id as varchar))
WHERE username IS NOT NULL
  AND id NOT IN (SELECT min(id) FROM kayttajatiedot GROUP BY LOWER(username));

CREATE COLLATION case_insensitive (provider = icu, locale = 'und-u-ks-level2', deterministic = false);

ALTER TABLE kayttajatiedot
ALTER COLUMN username SET DATA TYPE character varying(255) COLLATE "case_insensitive";
