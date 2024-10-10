create extension if not exists "uuid-ossp";

alter table oauth2_client
add column uuid uuid default uuid_generate_v4();