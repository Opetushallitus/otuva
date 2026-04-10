update organisaatiohenkilo
set passivoitu = true
where id in (
    select distinct o.id
    from organisaatiohenkilo o
    left join myonnetty_kayttooikeusryhma_tapahtuma m on m.organisaatiohenkilo_id = o.id
    where o.passivoitu = false and m.id is null
);
