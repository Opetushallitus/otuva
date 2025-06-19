alter table oauth2_client
add created timestamp with time zone not null default now();