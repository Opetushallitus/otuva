CREATE TABLE oauth2_client(
    id text not null primary key,
    secret text not null
);

alter table oauth2_client
    add constraint fk_oauth2_client_id
    foreign key (id) references kayttajatiedot(username);