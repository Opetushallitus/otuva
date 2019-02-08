create table if not exists henkilo_anomusilmoitus_kayttooikeusryhma (
  henkilo_id bigint not null references henkilo (id),
  kayttooikeusryhma_id bigint not null references kayttooikeusryhma (id)
);
