alter table oauth2_client
add kasittelija_henkilo_id bigint,
add updated timestamp with time zone not null default now(),
add constraint kasittelija_henkilo_id_fkey foreign key (kasittelija_henkilo_id) references henkilo(id);