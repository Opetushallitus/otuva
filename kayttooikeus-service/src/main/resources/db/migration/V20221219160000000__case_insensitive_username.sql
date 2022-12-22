UPDATE kayttajatiedot
SET username = CONCAT(username, CAST(id as varchar))
WHERE username IS NOT NULL
  AND id NOT IN (SELECT min(id) FROM kayttajatiedot GROUP BY LOWER(username));

ALTER TABLE kayttajatiedot
ADD CONSTRAINT username_ci_unique UNIQUE (lower(username));
