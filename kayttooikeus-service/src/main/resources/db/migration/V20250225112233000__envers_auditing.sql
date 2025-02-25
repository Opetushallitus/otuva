create table revinfo (rev integer not null, revtstmp bigint, primary key (rev));

create table myonnetty_kayttooikeusryhma_tapahtuma_aud (
    rev integer not null,
    revtype smallint,
    voimassaalkupvm date,
    voimassaloppupvm date,
    aikaleima timestamp(6),
    id bigint not null,
    kasittelija_henkilo_id bigint,
    kayttooikeusryhma_id bigint,
    organisaatiohenkilo_id bigint,
    syy varchar(255),
    tila varchar(255),
    primary key (rev, id)
);

alter table if exists myonnetty_kayttooikeusryhma_tapahtuma_aud
    add constraint FK2w6t3oi4i35eskr8i3tqnt2p5 foreign key (rev) references revinfo;

create index if not exists mkt_aud_organisaatiohenkilo_id on myonnetty_kayttooikeusryhma_tapahtuma_aud (organisaatiohenkilo_id);
create index if not exists mkt_aud_kayttooikeusryhma_id on myonnetty_kayttooikeusryhma_tapahtuma_aud (kayttooikeusryhma_id);
create index if not exists mkt_aud_kasittelija_henkilo_id on myonnetty_kayttooikeusryhma_tapahtuma_aud (kasittelija_henkilo_id);
