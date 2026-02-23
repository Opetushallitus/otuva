create index cas_tickets_sessionindex_gin_idx
  on Cas_Tickets using gin (((attributes->'sessionindex')::jsonb));